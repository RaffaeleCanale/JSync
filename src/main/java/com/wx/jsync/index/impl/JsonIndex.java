package com.wx.jsync.index.impl;

import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.jsync.index.Index;
import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.index.RemoteConfig;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.SyncManager;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.wx.jsync.Constants.INDEX_FILE;
import static com.wx.jsync.util.JsonUtils.*;


public class JsonIndex implements Index {

    private JSONObject root;

    @Override
    public void create(FileSystem fileSystem, String author) throws IOException {
        if (fileSystem.exists(INDEX_FILE)) {
            throw new IOException("Index already exists");
        }

        root = new JSONObject();

        set(root, author, "local", "owner");
    }

    @Override
    public void load(FileSystem fileSystem) throws IOException {
        if (fileSystem.exists(INDEX_FILE)) {
            root = new JSONObject(new JSONTokener(fileSystem.read(INDEX_FILE)));

            // Remove ignored files
            getObjectOpt(root, "files")
                    .ifPresent(filesObj -> {
                        Predicate<String> fileFilter = getFileFilter();

                        Iterator<String> it = filesObj.keys();
                        while (it.hasNext()) {
                            if (!fileFilter.test(it.next())) {
                                it.remove();
                            }
                        }
                    });
        } else {
            throw new IOException("Index not found");
        }
    }

    @Override
    public void save(FileSystem fileSystem) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(root.toString(4).getBytes("UTF-8"));
        fileSystem.write(INDEX_FILE, in);
    }

    @Override
    public String getOwnerName() {
        return getStringOpt(root, "local", "owner").orElse(null);
    }

    @Override
    public boolean useBackup() {
        return SyncManagerInitializer.useBackup(root);
    }

    @Override
    public void initialize(SyncManager syncManager, SyncTasksExecutor baseExecutor) {
        SyncManagerInitializer.initialize(root, syncManager, baseExecutor);
    }

    @Override
    public void setRemote(RemoteConfig config) {
        RemoteConfigLoader.setRemote(root, config);
    }

    @Override
    public Optional<RemoteConfig> getRemote() {
        return RemoteConfigLoader.getRemote(root);
    }

    @Override
    public Collection<SyncFile> getAllFiles() {
        return getObjectOpt(root, "files")
                .map(files -> files.keySet().stream()
                        .map(file -> getSyncFile(file, files.getJSONObject(file)))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public void removeFile(String path) {
        remove(root, "files", path);
    }

    @Override
    public Optional<SyncFile> getFile(String path) {
        return getObjectOpt(root, "files", path)
                .map(object -> getSyncFile(path, object));
    }

    private SyncFile getSyncFile(String path, JSONObject obj) {
        FileStat stat = getStat(obj).orElse(FileStat.REMOVED);

        return new SyncFile(path, stat, getDouble(obj, "version"), getString(obj, "author"), getDoubleOpt(obj, "version_base"));
    }

    private Optional<FileStat> getStat(JSONObject fileObj) {
        return getObjectOpt(fileObj, "stat").map(stat -> FileStat.create(
                getLong(stat, "timestamp"),
                getLong(stat, "size"),
                getBytes(stat, "checksum")
        ));
    }

    @Override
    public void setFile(SyncFile file) {
        set(root, file.getVersion(), "files", file.getPath(), "version");
        set(root, file.getVersionAuthor(), "files", file.getPath(), "author");

        file.getBaseVersion().ifPresent(bv -> set(root, bv, "files", file.getPath(), "version_base"));

        FileStat stat = file.getStat();
        if (stat.isRemoved()) {
            remove(root, "files", file.getPath(), "stat");
        } else {
            set(root, stat.getTimestamp(), "files", file.getPath(), "stat", "timestamp");
            set(root, stat.getFileSize(), "files", file.getPath(), "stat", "size");
            set(root, stat.getChecksum(), "files", file.getPath(), "stat", "checksum");
        }
    }

    @Override
    public Set<String> getParticipants() {
        return new HashSet<>(getStringListOpt(root, "local", "participants").orElse(Collections.emptyList()));
    }

    @Override
    public void setParticipants(Set<String> participants) {
        set(root, participants, "local", "participants");
    }

    @Override
    public Optional<Crypter> getCrypter() {
        return CrypterLoader.getCrypter(root);
    }

    @Override
    public void generateKey(Crypter crypter) throws CryptoException {
        CrypterLoader.generateKey(root, crypter);
    }

    @Override
    public Predicate<String> getFileFilter() {
        return FilterLoader.getFileFilter(root);
    }
}
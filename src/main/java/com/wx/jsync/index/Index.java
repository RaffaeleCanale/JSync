package com.wx.jsync.index;

import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.SyncManager;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;
import com.wx.util.pair.Pair;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface Index {

    void create(FileSystem fileSystem, String author) throws IOException;

    void load(FileSystem fileSystem) throws IOException;

    void save(FileSystem fileSystem) throws IOException;

    Collection<SyncFile> getAllFiles();

    Optional<SyncFile> getFile(String path);

    String getOwnerName();

    void setRemote(RemoteConfig remote);

    Optional<RemoteConfig> getRemote();

    void generateKey(Crypter crypter) throws CryptoException;

    boolean useBackup();

    Optional<Crypter> getCrypter();

    Predicate<String> getFileFilter();

    void setParticipants(Set<String> participants);

    Set<String> getParticipants();

    void setFile(SyncFile file);

    default void removeFile(SyncFile file) {
        removeFile(file.getPath());
    }

    void removeFile(String path);

    void initialize(SyncManager syncManager, SyncTasksExecutor baseExecutor);

}
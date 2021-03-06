package com.wx.jsync.index;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.index.loader.ListLoader;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.util.JsonUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static com.wx.jsync.Constants.INDEX_FILE;
import static com.wx.jsync.index.IndexKey.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 28.09.17.
 */
public class Index {

    private JSONObject root;

    public void create(FileSystem fileSystem) throws IOException {
        if (fileSystem.exists(INDEX_FILE)) {
            throw new IOException("Index already exists");
        }

        root = new JSONObject();
    }

    public void load(FileSystem fileSystem) throws IOException {
        if (fileSystem.exists(INDEX_FILE)) {
            root = new JSONObject(new JSONTokener(fileSystem.read(INDEX_FILE)));

            // Remove ignored files
            Collection<SyncFile> files = get(FILES);
            Predicate<String> fileFilter = get(IGNORE);
            for (SyncFile file : files) {
                if (!fileFilter.test(file.getUserPath())) {
                    removeSingle(FILES, file);
                }
            }
        } else {
            throw new IOException("Index not found");
        }
    }

    public void save(FileSystem fileSystem) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(root.toString(4).getBytes("UTF-8"));
        fileSystem.write(INDEX_FILE, in);
    }

    public void userSet(IndexKey key, ArgumentsSupplier args) {
        key.getLoader().userSet(root, args, key.getPath());
    }

    public <E> E get(IndexKey key) {
        return get(key, (Loader<E>) key.getLoader());
    }

    public <E> E get(IndexKey key, Loader<E> loader) {
        return loader.load(root, key.getPath());
    }

    public <E> Optional<E> getOpt(IndexKey key) {
        return (Optional<E>) key.getLoader().loadOpt(root, key.getPath());
    }

    public <E> void set(IndexKey key, E value, Loader<E> loader) {
        loader.setValue(root, value, key.getPath());
    }

    public <E> void set(IndexKey key, E value) {
        set(key, value, (Loader<E>) key.getLoader());
    }

    public <E> Optional<E> getSingle(IndexKey key, String id) {
        return ((ListLoader<E>) key.getLoader()).getSingleValue(root, key.getPath(), id);

    }

    public <E> void setSingle(IndexKey key, E value) {
        setSingle(key, value, (ListLoader<E>) key.getLoader());
    }

    public <E> void setSingle(IndexKey key, E value, ListLoader<E> loader) {
        loader.setSingleValue(root, key.getPath(), value);
    }

    public <E> void remove(IndexKey key) {
        JsonUtils.remove(root, key.getPath());
    }

    public <E> void removeSingle(IndexKey key, E value) {
        ((ListLoader<E>) key.getLoader()).removeSingleValue(root, key.getPath(), value);
    }

//    public Optional<String> getStringOpt(IndexKey key) {
//        return JsonUtils.getStringOpt(root, key.getPrefix());
//    }
//
//    public Optional<List<String>> getStringListOpt(IndexKey key) {
//        return JsonUtils.getStringListOpt(root, key.getPrefix());
//    }
//
//    public Optional<JSONObject> getObjectOpt(IndexKey key) {
//        return JsonUtils.getObjectOpt(root, key.getPrefix());
//    }

//    public void setFile(SyncFile file) {
//        JsonUtils.set(root, file.getVersion(), "files", file.getPrefix(), "version");
//        JsonUtils.set(root, file.getVersionAuthor(), "files", file.getPrefix(), "author");
//
//        file.getBaseVersion().ifPresent(bv -> JsonUtils.set(root, bv, "files", file.getPrefix(), "version_base"));
//
//        FileStat stat = file.getStat();
//        if (stat.isRemoved()) {
//            remove(root, "files", file.getPrefix(), "stat");
//        } else {
//            JsonUtils.set(root, stat.getTimestamp(), "files", file.getPrefix(), "stat", "timestamp");
//            JsonUtils.set(root, stat.getFileSize(), "files", file.getPrefix(), "stat", "size");
//            JsonUtils.set(root, stat.getChecksum(), "files", file.getPrefix(), "stat", "checksum");
//        }
//    }


}

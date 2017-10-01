package com.wx.jsync.index;

import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.sync.SyncFile;
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

    public void create(FileSystem fileSystem, String author) throws IOException {
        if (fileSystem.exists(INDEX_FILE)) {
            throw new IOException("Index already exists");
        }

        root = new JSONObject();

        set(OWNER, author);
    }

    public void load(FileSystem fileSystem) throws IOException {
        if (fileSystem.exists(INDEX_FILE)) {
            root = new JSONObject(new JSONTokener(fileSystem.read(INDEX_FILE)));

            // Remove ignored files
            Collection<SyncFile> files = get(FILES);
            Predicate<String> fileFilter = get(FILE_FILTER);
            for (SyncFile file : files) {
                if (!fileFilter.test(file.getPath())) {
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


//    public void setValue(IndexKey key, NamedOptions<?> options) {
//        JSONObject value = NamedOptionsLoader.create(options);
//        JsonUtils.setValue(root, value, key.getPath());
//    }

//    public void setValue(IndexKey key, Collection<?> value) {
//        JsonUtils.setValue(root, value, key.getPath());
//    }
//
//    public void setValue(IndexKey key, Object value) {
//        JsonUtils.setValue(root, value, key.getPath());
//    }
//
//    public String getString(IndexKey key) {
//        return JsonUtils.getString(root, key.getPath());
//    }

    public <E> E get(IndexKey key) {
        return (E) key.getLoader().load(root, key.getPath());
    }

    public <E> Optional<E> getOpt(IndexKey key) {
        return (Optional<E>) key.getLoader().loadOpt(root, key.getPath());
    }

    public <E> void set(IndexKey key, E value) {
        ((Loader<E>) key.getLoader()).setValue(root, value, key.getPath());
    }

    public <E> Optional<E> getSingle(IndexKey key, String id) {
        return ((SetLoader<E>) key.getLoader()).getSingleValue(root, key.getPath(), id);

    }

    public <E> void setSingle(IndexKey key, E value) {
        ((SetLoader<E>) key.getLoader()).setSingleValue(root, key.getPath(), value);
    }

    public <E> void removeSingle(IndexKey key, E value) {
        ((SetLoader<E>) key.getLoader()).removeSingleValue(root, key.getPath(), value);
    }

//    public Optional<String> getStringOpt(IndexKey key) {
//        return JsonUtils.getStringOpt(root, key.getPath());
//    }
//
//    public Optional<List<String>> getStringListOpt(IndexKey key) {
//        return JsonUtils.getStringListOpt(root, key.getPath());
//    }
//
//    public Optional<JSONObject> getObjectOpt(IndexKey key) {
//        return JsonUtils.getObjectOpt(root, key.getPath());
//    }

//    public void setFile(SyncFile file) {
//        JsonUtils.set(root, file.getVersion(), "files", file.getPath(), "version");
//        JsonUtils.set(root, file.getVersionAuthor(), "files", file.getPath(), "author");
//
//        file.getBaseVersion().ifPresent(bv -> JsonUtils.set(root, bv, "files", file.getPath(), "version_base"));
//
//        FileStat stat = file.getStat();
//        if (stat.isRemoved()) {
//            remove(root, "files", file.getPath(), "stat");
//        } else {
//            JsonUtils.set(root, stat.getTimestamp(), "files", file.getPath(), "stat", "timestamp");
//            JsonUtils.set(root, stat.getFileSize(), "files", file.getPath(), "stat", "size");
//            JsonUtils.set(root, stat.getChecksum(), "files", file.getPath(), "stat", "checksum");
//        }
//    }


}

package com.wx.jsync.index.loader;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.sync.SyncFile;
import org.json.JSONObject;

import java.util.Optional;

import static com.wx.jsync.util.JsonUtils.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 29.09.17.
 */
public class FilesLoader extends SetLoader<SyncFile> {


    @Override
    protected String getId(SyncFile file) {
        return file.getPath();
    }

    @Override
    protected SyncFile load(Object entry) {
        JSONObject obj = (JSONObject) entry;
        FileStat stat = getStat(obj).orElse(FileStat.REMOVED);

        return new SyncFile(
                getString(obj, "path"),
                stat,
                getDouble(obj, "version"),
                getString(obj, "author"),
                getDoubleOpt(obj, "version_base")
        );
    }

    @Override
    protected Object getValue(SyncFile file) {
        JSONObject obj = new JSONObject();
        set(obj, file.getVersion(), "version");
        set(obj, file.getVersionAuthor(), "author");
        set(obj, file.getPath(), "path");

        file.getBaseVersion().ifPresent(bv -> set(obj, bv, "version_base"));

        FileStat stat = file.getStat();
        if (!stat.isRemoved()) {
            set(obj, stat.getTimestamp(), "stat", "timestamp");
            set(obj, stat.getFileSize(), "stat", "size");
            set(obj, stat.getChecksum(), "stat", "checksum");
        }

        return obj;
    }

    @Override
    protected Object getUserValue(ArgumentsSupplier args) {
        throw new IllegalArgumentException("Cannot set value for files");
    }

    private Optional<FileStat> getStat(JSONObject fileObj) {
        return getObjectOpt(fileObj, "stat").map(stat -> FileStat.create(
                getLong(stat, "timestamp"),
                getLong(stat, "size"),
                getBytes(stat, "checksum")
        ));
    }
}

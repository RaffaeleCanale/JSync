package com.wx.jsync.index.loader;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.SyncFileBuilder;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Optional;

import static com.wx.jsync.util.JsonUtils.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 29.09.17.
 */
public class FilesLoader extends SetLoader<SyncFile> {


    @Override
    protected String getId(SyncFile file) {
        return file.getRealPath();
    }

    @Override
    protected SyncFile load(Object entry) {
        JSONObject obj = (JSONObject) entry;
        FileStat stat = getStat(obj).orElse(FileStat.REMOVED);

        String realPath = getString(obj, "path");
        String userPath = getStringOpt(obj, "user_path").orElse(realPath);

        return new SyncFileBuilder(userPath, realPath)
                .setStat(stat)
                .setVersion(getDouble(obj, "version"))
                .setVersionAuthor(getString(obj, "author"))
                .setBaseVersion(getDoubleOpt(obj, "version_base"))
                .setCanViewUsers(getStringListOpt(obj, "can_view"))
                .create();
    }

    @Override
    protected Object getValue(SyncFile file) {
        JSONObject obj = new JSONObject();
        set(obj, file.getVersion(), "version");
        set(obj, file.getVersionAuthor(), "author");
        set(obj, file.getRealPath(), "path");

        Collection<String> canViewUsers = file.getCanViewUsers();
        if (canViewUsers != null && !canViewUsers.isEmpty()) {
            set(obj, canViewUsers, "can_view");
        }

        String userPath = file.getUserPath();
        if (!userPath.equals(file.getRealPath())) {
            set(obj, userPath, "user_path");
        }

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

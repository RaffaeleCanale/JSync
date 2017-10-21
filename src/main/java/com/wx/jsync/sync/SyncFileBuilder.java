package com.wx.jsync.sync;

import com.wx.jsync.filesystem.FileStat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SyncFileBuilder {


    private final String userPath;
    private String realPath;
    private FileStat stat;
    private double version = 0.0;
    private String versionAuthor;
    private Double baseVersion;
    private Collection<String> canViewUsers = new ArrayList<>();

    public SyncFileBuilder(String userPath) {
        this(userPath, userPath);
    }

    public SyncFileBuilder(String userPath, String realPath) {
        this.userPath = userPath;
        this.realPath = realPath;
    }

    public String getRealPath() {
        return realPath;
    }

    public SyncFileBuilder setRealPath(String realPath) {
        this.realPath = realPath;
        return this;
    }

    public SyncFileBuilder setRealPath(Optional<String> id) {
        if (id.isPresent()) {
            setRealPath(id);
        }

        return this;
    }

    public SyncFileBuilder setStat(FileStat stat) {
        this.stat = stat;
        return this;
    }

    public SyncFileBuilder setVersion(double version) {
        this.version = version;
        return this;
    }

    public SyncFileBuilder setVersionAuthor(String versionAuthor) {
        this.versionAuthor = versionAuthor;
        return this;
    }

    public SyncFileBuilder setBaseVersion(Optional<Double> baseVersion) {
        return setBaseVersion(baseVersion.orElse(null));
    }

    public SyncFileBuilder setBaseVersion(Double baseVersion) {
        this.baseVersion = baseVersion;
        return this;
    }

    public SyncFileBuilder setCanViewUsers(Optional<List<String>> canViewUsers) {
        return setCanViewUsers(canViewUsers.orElse(new ArrayList<>()));
    }

    public SyncFileBuilder setCanViewUsers(Collection<String> canViewUsers) {
        this.canViewUsers = new ArrayList<>(canViewUsers);
        return this;
    }

    public SyncFileBuilder addCanViewUser(String user) {
        this.canViewUsers.add(user);
        return this;
    }

    public SyncFileBuilder setVersionOf(SyncFile otherFile) {
        return setVersion(otherFile.getVersion())
                .setVersionAuthor(otherFile.getVersionAuthor());
    }

    public SyncFileBuilder withUpToDateBaseVersion() {
        return setBaseVersion(version);
    }

    public SyncFile create() {
        return new SyncFile(userPath, realPath, stat, version, versionAuthor, baseVersion, canViewUsers);
    }
}
package com.wx.jsync.sync;

import com.wx.jsync.filesystem.FileStat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static com.wx.util.collections.CollectionsUtil.safe;
import static java.util.Objects.requireNonNull;

public class SyncFile {

    private final String userPath;
    private final String realPath;
    private final FileStat stat;
    private final double version;
    private final String versionAuthor;
    private final Double baseVersion;
    private final Collection<String> canViewUsers;

    public SyncFile(String userPath, String realPath, FileStat stat, double version, String versionAuthor, Double baseVersion, Collection<String> canViewUsers) {
        this.userPath = requireNonNull(userPath);
        this.realPath = requireNonNull(realPath);
        this.stat = requireNonNull(stat);
        this.version = version;
        this.versionAuthor = requireNonNull(versionAuthor);
        this.baseVersion = baseVersion;
        this.canViewUsers = safe(new ArrayList<>(canViewUsers));
    }

    public String getRealPath() {
        return realPath;
    }

    public Optional<Double> getBaseVersion() {
        return Optional.ofNullable(baseVersion);
    }

    public String getUserPath() {
        return userPath;
    }

    public FileStat getStat() {
        return stat;
    }

    public double getVersion() {
        return version;
    }

    public String getVersionAuthor() {
        return versionAuthor;
    }

    public boolean isRemoved() {
        return getStat().isRemoved();
    }

    public boolean canView(String user) {
        return canViewUsers.isEmpty() || canViewUsers.contains(user);
    }

    public Collection<String> getCanViewUsers() {
        return canViewUsers;
    }

    public SyncFileBuilder builder() {
        return new SyncFileBuilder(userPath, realPath)
                .setStat(stat)
                .setVersion(version)
                .setVersionAuthor(versionAuthor)
                .setBaseVersion(baseVersion)
                .setCanViewUsers(canViewUsers);
    }


    public SyncFile bumpBaseVersion() {
        return builder().withUpToDateBaseVersion().create();
    }
}
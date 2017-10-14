package com.wx.jsync.sync;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.index.options.Options;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SyncFile {

    private final String path;
    private final FileStat stat;
    private final double version;
    private final String versionAuthor;
    private final Double baseVersion;

    public SyncFile(String path, FileStat stat, double version, String versionAuthor, Optional<Double> baseVersion) {
        this(path, stat, version, versionAuthor, baseVersion.orElse(null));
    }

    public SyncFile(String path, FileStat stat, double version, String versionAuthor, Double baseVersion) {
        this.path = requireNonNull(path);
        this.stat = requireNonNull(stat);
        this.version = version;
        this.versionAuthor = versionAuthor;
        this.baseVersion = baseVersion;
    }

    public Optional<Double> getBaseVersion() {
        return Optional.ofNullable(baseVersion);
    }

    public String getPath() {
        return path;
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

    public SyncFile withSameVersionAs(SyncFile reference) {
        return new SyncFile(path, stat, reference.version, reference.versionAuthor, baseVersion);
    }

    public SyncFile bumpBaseVersion() {
        return new SyncFile(path, stat, version, versionAuthor, version);
    }

    public boolean isRemoved() {
        return getStat().isRemoved();
    }
}
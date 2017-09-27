package com.wx.jsync.sync.diff;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.sync.SyncFile;

public interface FsDiffFile {

    enum Status {
        ADDED,
        REMOVED,
        CHANGED,
        UNCHANGED
    }

    Status getStatus();

    FileStat getFsStat();

    SyncFile getIndexFile();

}
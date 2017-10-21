package com.wx.jsync.sync.diff;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.sync.SyncFile;

import java.util.*;

import static com.wx.util.collections.CollectionsUtil.safe;

public class FsDiff {

    private final Map<String, FsDiffFile> diffFiles;

    public FsDiff(Map<String, FsDiffFile> diffFiles) {
        this.diffFiles = safe(diffFiles);
    }

    public FsDiffFile get(String file) {
        return diffFiles.get(file);
    }

    public Set<String> getFiles() {
        return diffFiles.keySet();
    }

    public static class Builder {

        private final Map<String, FsDiffFile> diffFiles = new HashMap<>();

        public Builder addUnchanged(SyncFile indexFile) {
            diffFiles.put(indexFile.getRealPath(), new UnchangedFile(indexFile));

            return this;
        }

        public Builder addChanged(SyncFile indexFile, FileStat fsStat) {
            diffFiles.put(indexFile.getRealPath(), new ChangedFile(indexFile, fsStat));

            return this;
        }

        public Builder addRemoved(SyncFile indexFile) {
            diffFiles.put(indexFile.getRealPath(), new RemovedFile(indexFile));

            return this;
        }

        public Builder addNew(String file, FileStat fsStat) {
            diffFiles.put(file, new AddedFile(fsStat));

            return this;
        }

        public FsDiff create() {
            return new FsDiff(diffFiles);
        }
    }

    private static abstract class IndexBaseDiffFile implements FsDiffFile {

        private final SyncFile indexFile;

        private IndexBaseDiffFile(SyncFile indexFile) {
            this.indexFile = indexFile;
        }

        @Override
        public SyncFile getIndexFile() {
            return indexFile;
        }
    }

    private static class UnchangedFile extends IndexBaseDiffFile {

        public UnchangedFile(SyncFile indexFile) {
            super(indexFile);
        }

        @Override
        public Status getStatus() {
            return Status.UNCHANGED;
        }

        @Override
        public FileStat getFsStat() {
            return getIndexFile().getStat();
        }
    }

    private static class ChangedFile extends IndexBaseDiffFile {

        private final FileStat fsStat;

        public ChangedFile(SyncFile indexFile, FileStat fsStat) {
            super(indexFile);
            this.fsStat = fsStat;
        }

        @Override
        public Status getStatus() {
            return Status.CHANGED;
        }

        @Override
        public FileStat getFsStat() {
            return fsStat;
        }
    }

    private static class RemovedFile extends IndexBaseDiffFile {

        public RemovedFile(SyncFile indexFile) {
            super(indexFile);
        }

        @Override
        public Status getStatus() {
            return Status.REMOVED;
        }

        @Override
        public FileStat getFsStat() {
            throw new NoSuchElementException();
        }
    }


    private static class AddedFile implements FsDiffFile {

        private final FileStat fsStat;

        public AddedFile(FileStat fsStat) {
            this.fsStat = fsStat;
        }

        @Override
        public Status getStatus() {
            return Status.ADDED;
        }

        @Override
        public FileStat getFsStat() {
            return fsStat;
        }

        @Override
        public SyncFile getIndexFile() {
            throw new NoSuchElementException();
        }
    }
}
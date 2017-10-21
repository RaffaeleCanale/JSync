package com.wx.jsync.dataset;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.BackupFileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.SyncFileBuilder;
import com.wx.jsync.sync.diff.FsDiff;
import com.wx.jsync.sync.diff.FsDiffFile;
import com.wx.jsync.util.helpers.CrypterFileSystemHelper;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.wx.jsync.Constants.CONFIG_DIR;
import static com.wx.jsync.Constants.VERSION_INCREMENT_DELTA;
import static com.wx.jsync.filesystem.FileStat.REMOVED;
import static com.wx.jsync.index.IndexKey.*;
import static com.wx.jsync.util.Common.bumpVersion;

public class DataSet {

    private static final Logger LOG = LogHelper.getLogger(DataSet.class);

    private final FileSystem fileSystem;
    private final Index index;

    public DataSet(FileSystem fileSystem, Index index) throws IOException {
        if (index.get(ENABLE_ENCRYPTION)) {
            String algorithm = index.get(ENCRYPTION_ALGORITHM);
            fileSystem = CrypterFileSystemHelper.initDecorator(algorithm, fileSystem);
        }

        if (index.get(ENABLE_BACKUP)) {
            String backupDirectory = index.get(BACKUP_DIRECTORY);
            fileSystem = new BackupFileSystem(fileSystem, backupDirectory);
        }

        this.fileSystem = fileSystem;
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }


    private Collection<String> getAllFileSystemFiles() throws IOException {
        return fileSystem.getAllFiles(index.get(IGNORE)).stream()
                .filter(file -> !file.startsWith(CONFIG_DIR))
                .collect(Collectors.toList());
    }

    public FsDiff computeDiff() throws IOException {
        FsDiff.Builder diffBuilder = new FsDiff.Builder();

        Collection<SyncFile> indexFiles = index.get(FILES);
        Set<String> fsFiles = new HashSet<>(getAllFileSystemFiles());


        for (SyncFile indexFile : indexFiles) {
            String path = indexFile.getRealPath();

            if (fsFiles.remove(path)) {
                // File exists in FS
                FileStat fsStat = indexFile.getStat();

                if (indexFile.getStat().matches(fsStat)) {
                    diffBuilder.addUnchanged(indexFile);
                } else {
                    diffBuilder.addChanged(indexFile, fsStat);
                }
            } else {
                // File disappeared from FS
                if (!indexFile.isRemoved()) {
                    diffBuilder.addRemoved(indexFile);
                }
            }
        }

        for (String fsFile : fsFiles) {
            FileStat fsStat = fileSystem.getFileStat(fsFile);
            diffBuilder.addNew(fsFile, fsStat);
        }

        return diffBuilder.create();
    }

    public boolean commit() throws IOException {
        FsDiff diff = computeDiff();

        int changed = 0, added = 0, removed = 0;

        for (String path : diff.getFiles()) {
            FsDiffFile diffFile = diff.get(path);

            switch (diffFile.getStatus()) {
                case UNCHANGED:
                    // Do nothing
                    break;

                case CHANGED:
                    // Bump version
                    index.setSingle(FILES, diffFile.getIndexFile().bumpBaseVersion());
                    changed++;
                    break;

                case ADDED:
                    //  Add fresh
                    index.setSingle(FILES, new SyncFileBuilder(path)
                            .setStat(diffFile.getFsStat())
                            .setVersion(VERSION_INCREMENT_DELTA)
                            .setVersionAuthor(index.get(USER))
                            .create()
                    );
                    added++;
                    break;

                case REMOVED:
                    // Set 'removed' + bumpVersion version
                    index.setSingle(FILES, diffFile.getIndexFile().builder()
                            .setStat(REMOVED)
                            .setVersionAuthor(index.get(USER))
                            .create()
                    );
                    removed++;
                    break;

            }
        }

        index.save(fileSystem);

        int sum = added + removed + changed;
        if (sum > 0) {
            LOG.log(Level.INFO, "Commit {0} change(s) to {1} ({2} changed, {3} added, {4} removed)",
                    new Object[]{sum, index.get(USER), changed, added, removed});
        }

        return sum > 0;
    }

    public void saveIndex() throws IOException {
        index.save(fileSystem);
    }
}
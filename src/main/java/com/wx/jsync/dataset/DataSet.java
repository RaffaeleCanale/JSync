package com.wx.jsync.dataset;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.MultiFileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.diff.FsDiff;
import com.wx.jsync.sync.diff.FsDiffFile;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.wx.jsync.Constants.CONFIG_DIR;
import static com.wx.jsync.Constants.VERSION_INCREMENT_DELTA;
import static com.wx.jsync.index.IndexKey.FILES;
import static com.wx.jsync.index.IndexKey.IGNORE;
import static com.wx.jsync.index.IndexKey.OWNER;
import static com.wx.jsync.util.Common.bumpVersion;

public class DataSet {

    private static final Logger LOG = LogHelper.getLogger(DataSet.class);

    private final MultiFileSystem fileSystem;
    private final Index index;

    public DataSet(FileSystem fileSystem, Index index) {
        this.fileSystem = new MultiFileSystem(fileSystem);
        this.index = index;
    }

    public Index getIndex() {
        return index;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public <E extends FileSystem> E getBaseFs() {
        return (E) this.fileSystem.getBaseFs();
    }

    public DataSet addDecorator(String path, Function<FileSystem, DecoratorFileSystem> factory) {
        fileSystem.addDecorator(path, factory);

        return this;
    }

    public Collection<String> getAllFileSystemFiles() throws IOException {
        return fileSystem.getAllFiles().stream()
                .filter(file -> !file.startsWith(CONFIG_DIR))
                .filter(index.get(IGNORE))
                .collect(Collectors.toList());
    }

    public FsDiff computeDiff() throws IOException {
        FsDiff.Builder diffBuilder = new FsDiff.Builder();

        Collection<SyncFile> indexFiles = index.get(FILES);
        Set<String> fsFiles = new HashSet<>(getAllFileSystemFiles());


        for (SyncFile indexFile : indexFiles) {
            if (fsFiles.remove(indexFile.getPath())) {
                // File exists in FS
                FileStat indexStat = indexFile.getStat();
                FileStat fsStat = fileSystem.getFileStat(indexFile.getPath());

                if (indexStat.matches(fsStat)) {
                    diffBuilder.addUnchanged(indexFile);
                } else {
                    diffBuilder.addChanged(indexFile, fsStat);
                }
            } else {
                // File disappeared from FS
                if (!indexFile.getStat().isRemoved()) {
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
                    index.setSingle(FILES, new SyncFile(
                            path,
                            diffFile.getFsStat(),
                            bumpVersion(diffFile.getIndexFile().getVersion()),
                            index.get(OWNER),
                            diffFile.getIndexFile().getBaseVersion()
                    ));
                    changed++;
                    break;

                case ADDED:
                    //  Add fresh
                    index.setSingle(FILES, new SyncFile(
                            path,
                            diffFile.getFsStat(),
                            VERSION_INCREMENT_DELTA,
                            index.get(OWNER),
                            Optional.empty()
                    ));
                    added++;
                    break;

                case REMOVED:
                    // Set 'removed' + bumpVersion version
                    index.setSingle(FILES, new SyncFile(
                            path,
                            FileStat.REMOVED,
                            bumpVersion(diffFile.getIndexFile().getVersion()),
                            index.get(OWNER),
                            diffFile.getIndexFile().getBaseVersion()
                    ));
                    removed++;
                    break;

            }
        }

        index.save(fileSystem);

        int sum = added + removed + changed;
        if (sum > 0) {
            LOG.log(Level.INFO, "Commit {0} change(s) to {1} ({2} changed, {3} added, {4} removed)",
                    new Object[]{sum, index.get(OWNER), changed, added, removed});
        }

        return sum > 0;
    }
}
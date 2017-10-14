package com.wx.jsync.sync.tasks;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.sync.SyncFile;
import com.wx.util.log.LogHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static com.wx.jsync.index.IndexKey.FILES;
import static com.wx.jsync.sync.tasks.SyncTask.Type.CONFLICT;

public class SyncTasksAnalyzer {

    private static final Logger LOG = LogHelper.getLogger(SyncTasksAnalyzer.class);

    private final boolean enableBump;
    private final Predicate<String> acceptFilter;

    public SyncTasksAnalyzer(boolean enableBump, Predicate<String> filter) {
        this.enableBump = enableBump;
        this.acceptFilter = filter;
    }

    public SyncTasks.Builder computeTasks(DataSet local, DataSet remote) {
        SyncTasks.Builder builder = new SyncTasks.Builder(enableBump);

        Set<String> visitedFiles = new HashSet<>();

        Collection<SyncFile> localFiles = local.getIndex().get(FILES);
        for (SyncFile localFile : localFiles) {
            if (acceptFilter.test(localFile.getPath())) {
                Optional<SyncFile> remoteFile = remote.getIndex().getSingle(FILES, localFile.getPath());

                visitedFiles.add(localFile.getPath());

                addTask(builder, localFile, remoteFile);
            }
        }


        Collection<SyncFile> remoteFiles = remote.getIndex().get(FILES);
        for (SyncFile remoteFile : remoteFiles) {
            if (!visitedFiles.contains(remoteFile.getPath()) && acceptFilter.test(remoteFile.getPath())) {
                builder.updateLocal(Optional.empty(), remoteFile);
            }
        }

        return builder;

    }

    private void addTask(SyncTasks.Builder builder, SyncFile localFile, Optional<SyncFile> remoteFileOpt) {
            /*
            new = NO bv
            changed = lv > bv
            unchanged = lv == bv


            LOCAL       REMOTE
            new         not exists    -> updateRemote
            new         exists        -> bumpVersion || conflict

            changed     not exists    -> updateRemote
            changed     bv == rv      -> bumpVersion || updateRemote
            changed     bv < rv       -> bumpVersion || conflict
            changed     bv > rv       -> bumpVersion || conflict

            unchanged   not exists    -> updateRemote
            unchanged   bv == rv      -> do nothing
            unchanged   bv < rv       -> bumpVersion || updateLocal
            unchanged   bv > rv       -> bumpVersion || conflict

            not exists  exists        -> updateLocal
             */

        if (!remoteFileOpt.isPresent()) {
            builder.updateRemote(localFile, remoteFileOpt);
            return;
        }
        SyncFile remoteFile = remoteFileOpt.get();

        Optional<Double> baseVersion = localFile.getBaseVersion();
        if (!baseVersion.isPresent()) {
            // new
            if (!builder.bump(localFile, remoteFile)) {
                builder.put(CONFLICT, localFile, remoteFile);
            }

        } else if (localFile.getVersion() > baseVersion.get()) {
            // changed
            if (!builder.bump(localFile, remoteFile)) {
                if (baseVersion.get() == remoteFile.getVersion()) {
                    builder.updateRemote(localFile, remoteFileOpt);
                } else {
                    builder.put(CONFLICT, localFile, remoteFile);
                }
            }

        } else if (localFile.getVersion() == baseVersion.get()) {
            // unchanged
            if (baseVersion.get() != remoteFile.getVersion()) {
                if (!builder.bump(localFile, remoteFile)) {
                    builder.updateLocal(Optional.of(localFile), remoteFile);
                }
            }
        }
    }
}
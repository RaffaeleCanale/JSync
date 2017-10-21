package com.wx.jsync.sync.tasks;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.sync.SyncFile;
import com.wx.util.log.LogHelper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.wx.jsync.index.IndexKey.FILES;
import static com.wx.jsync.index.IndexKey.USER;
import static com.wx.jsync.sync.tasks.SyncTask.Type.CONFLICT;
import static com.wx.jsync.util.Common.filter;

public class SyncTasksAnalyzer {

    private static final Logger LOG = LogHelper.getLogger(SyncTasksAnalyzer.class);

    private final boolean enableBump;
    private final Predicate<SyncFile> acceptFilter;

    public SyncTasksAnalyzer(boolean enableBump, Predicate<String> filter) {
        this.enableBump = enableBump;
        this.acceptFilter = file -> filter.test(file.getUserPath());
    }

    public SyncTasks.Builder computeTasks(DataSet local, DataSet remote) {
        SyncTasks.Builder builder = new SyncTasks.Builder(enableBump);

        Collection<SyncFile> localFiles = filter(
                local.getIndex().get(FILES),
                acceptFilter
        );
        Map<String, SyncFile> remoteFiles = filter(
                remote.getIndex().get(FILES),
                acceptFilter.and(canViewPredicate(local.getIndex().get(USER)))
        ).stream().collect(Collectors.toMap(
                SyncFile::getUserPath,
                Function.identity()
        ));

        Set<String> visitedFiles = new HashSet<>();


        for (SyncFile localFile : localFiles) {
            Optional<SyncFile> remoteFile = Optional.ofNullable(remoteFiles.get(localFile.getUserPath()));

            visitedFiles.add(localFile.getUserPath());

            addTask(builder, localFile, remoteFile);
        }


        for (SyncFile remoteFile : remoteFiles.values()) {
            if (!visitedFiles.contains(remoteFile.getUserPath())) {
                builder.updateLocal(Optional.empty(), remoteFile);
            }
        }

        return builder;

    }

    private Predicate<SyncFile> canViewPredicate(String user) {
        return file -> file.canView(user);
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
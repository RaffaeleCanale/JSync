package com.wx.jsync.sync;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.index.IndexKey;
import com.wx.jsync.sync.conflict.ConflictHandler;
import com.wx.jsync.sync.tasks.SyncTask;
import com.wx.jsync.sync.tasks.SyncTasks;
import com.wx.jsync.sync.tasks.SyncTasksAnalyzer;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;
import com.wx.jsync.sync.tasks.impl.DefaultExecutor;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.union;
import static com.wx.jsync.index.IndexKey.OWNER;
import static com.wx.jsync.index.IndexKey.PARTICIPANTS;

public class SyncManager {

    private static final Logger LOG = LogHelper.getLogger(SyncManager.class);

    private final DataSet local;
    private final DataSet remote;

    private boolean enableBump = true;
    private ConflictHandler conflictHandler;
    private SyncTasksExecutor tasksExecutor = new DefaultExecutor();

    public SyncManager(DataSet local, DataSet remote) throws IOException {
        this.local = local;
        this.remote = remote;

        updateRemote();
        purgeRemoved();

        local.commit();
        remote.commit();
    }

    public void setEnableBump(boolean enableBump) {
        this.enableBump = enableBump;
    }

    public void setTasksExecutor(SyncTasksExecutor tasksExecutor) {
        this.tasksExecutor = tasksExecutor;
    }

    public void setConflictHandler(ConflictHandler conflictHandler) {
        this.conflictHandler = conflictHandler;
    }

    public SyncTasks getStatus() throws IOException {
        return getStatus0().create();
    }

    public DataSet getLocal() {
        return local;
    }

    public DataSet getRemote() {
        return remote;
    }

    public void execute() throws IOException {
        SyncTasks.Builder tasksBuilder = getStatus0();

        for (SyncTask conflict : tasksBuilder.removeConflicts()) {
            if (!conflictHandler.handle(tasksBuilder, conflict)) {
                LOG.warning("All conflicts must be solved");
                return;
            }
        }

        SyncTasks tasks = tasksBuilder.create();
        if (tasks.isEmpty()) {
            LOG.info("Already up-to-date");
            return;
        }

        tasksExecutor.execute(local, remote, tasks);
    }

    private void updateRemote() {
        Set<String> participants = union(
                Collections.singleton(local.getIndex().get(OWNER)),
                remote.getIndex().get(PARTICIPANTS)
        );
        remote.getIndex().set(PARTICIPANTS, participants);

    }

    private void purgeRemoved() {
        // TODO: 21.09.17 Purge

    }

    private SyncTasks.Builder getStatus0() {
        return new SyncTasksAnalyzer(enableBump).computeTasks(local, remote);
    }

}
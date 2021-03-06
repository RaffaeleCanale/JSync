package com.wx.jsync;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.index.Index;
import com.wx.jsync.sync.SyncManager;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;
import com.wx.jsync.sync.tasks.impl.ConfirmExecutor;
import com.wx.jsync.sync.tasks.impl.DefaultExecutor;
import com.wx.jsync.util.helpers.DataSetsHelper;

import java.io.IOException;

import static com.wx.jsync.index.IndexKey.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class SyncHelper {

    public static SyncManager initSyncManager(DataSetsHelper dataSets) throws IOException {
        /*

        - loadOpt local index
        - loadOpt remote index

        - init base executor
        - int local decorators
        - int remote decorators

        - init sync manager

         */

        DataSet local = dataSets.getLocal();
        DataSet remote = dataSets.getRemote();

        SyncManager syncManager = new SyncManager(local, remote);
        syncManager.setEnableBump(local.getIndex().get(ENABLE_BUMP));
        syncManager.setConflictHandler(local.getIndex().get(CONFLICT_HANDLER));
        syncManager.setTasksExecutor(getExecutor(local.getIndex()));

        return syncManager;
    }

    private static SyncTasksExecutor getExecutor(Index localIndex) {
        SyncTasksExecutor executor = new DefaultExecutor();

        if (localIndex.get(ASK_CONFIRMATION)) {
            executor = new ConfirmExecutor(executor);
        }

        return executor;
    }

    private SyncHelper() {
    }
}

package com.wx.jsync;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.dataset.factory.impl.LocalDataSetFactory;
import com.wx.jsync.filesystem.decorator.factory.DecoratorType;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.sync.SyncManager;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;
import com.wx.jsync.sync.tasks.impl.ConfirmExecutor;
import com.wx.jsync.sync.tasks.impl.DefaultExecutor;

import java.io.IOException;
import java.util.Set;

import static com.wx.jsync.index.IndexKey.*;
import static com.wx.jsync.util.DesktopUtils.getCwd;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class SyncHelper {

    public static SyncManager initSyncManager() throws IOException {
        /*

        - loadOpt local index
        - loadOpt remote index

        - init base executor
        - int local decorators
        - int remote decorators

        - init sync manager

         */
        DataSet local = new LocalDataSetFactory().loadFrom(getCwd());
        DataSet remote = connectRemote(local);

        initDecorators(local, local.getIndex());
        initDecorators(remote, local.getIndex());

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

    private static DataSet connectRemote(DataSet local) throws IOException {
        NamedOptions<DataSetType> remoteConfig = local.getIndex().get(REMOTE);
        return remoteConfig.getType().getFactory().connect(local, remoteConfig.getOptions());
    }

    private static void initDecorators(DataSet target, Index localIndex) throws IOException {
        Set<NamedOptions<DecoratorType>> decorators = target.getIndex().get(DECORATORS);

        for (NamedOptions<DecoratorType> decorator : decorators) {
            Options options = decorator.getOptions();


            decorator.getType().getFactory().getFactory(localIndex, options)
                    .apply(target::addDecorator);
        }
    }


    private SyncHelper() {
    }
}

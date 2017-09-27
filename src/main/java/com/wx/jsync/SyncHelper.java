package com.wx.jsync;

import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.factory.impl.LocalDataSetFactory;
import com.wx.jsync.index.RemoteConfig;
import com.wx.jsync.sync.SyncManager;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;
import com.wx.jsync.sync.tasks.impl.BackupExecutor;
import com.wx.jsync.sync.tasks.impl.DefaultExecutor;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.wx.jsync.util.DesktopUtils.getCwd;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class SyncHelper {

    public static SyncManager initSyncManager() throws IOException {
        /*

        - load local index
        - load remote index

        - init base executor (crypter)
        - init remote backup executor
        - init sync manager

         */
        DataSet local = new LocalDataSetFactory().loadFrom(getCwd());
        DataSet remote = connectRemote(local);

        initCrypter(local, remote);

        SyncTasksExecutor baseExecutor = initRemoteBackup(remote, new DefaultExecutor());


        SyncManager syncManager = new SyncManager(local, remote);
        local.getIndex().initialize(syncManager, baseExecutor);

        return syncManager;
    }

    private static DataSet connectRemote(DataSet local) throws IOException {
        RemoteConfig remoteConfig = local.getIndex().getRemote().orElseThrow(() -> new NoSuchElementException("This data set has no remote configured"));
        return remoteConfig.getType().getFactory().connectOrInit(local, remoteConfig);
    }

    private static void initCrypter(DataSet local, DataSet remote) throws IOException {
        Optional<Crypter> crypter = remote.getIndex().getCrypter();

        if (crypter.isPresent()) {
            try {
                local.getIndex().generateKey(crypter.get());

                remote.setCrypter(crypter.get());
            } catch (CryptoException e) {
                throw new IOException(e);
            }
        }
    }

    private static SyncTasksExecutor initRemoteBackup(DataSet remote, SyncTasksExecutor baseExecutor) {
        if (remote.getIndex().useBackup()) {
            return new BackupExecutor(false, true, baseExecutor);
        }

        return baseExecutor;
    }

    private SyncHelper() {
    }
}

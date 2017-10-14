package com.wx.jsync.util.helpers;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.dataset.factory.impl.LocalDataSetFactory;
import com.wx.jsync.filesystem.base.LocalFileSystem;
import com.wx.jsync.index.options.MutableOptions;
import com.wx.jsync.index.options.NamedOptions;

import java.io.IOException;

import static com.wx.jsync.index.IndexKey.REMOTE;
import static com.wx.jsync.util.DesktopUtils.getCwd;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 02.10.17.
 */
public class DataSetsHelper {

    private DataSet local;
    private DataSet remote;

    public DataSet createOrInitLocal() throws IOException {
        local = new LocalDataSetFactory().createOrInit(getCwd());
        return local;
    }

    public DataSet getByName(String name) throws IOException {
        if (name.equals("local")) {
            return getLocal();
        } else if (name.equals("remote")) {
            return getRemote();
        } else {
            throw new IllegalArgumentException("Data set name must be local or remote");
        }
    }

    public DataSet getLocal() throws IOException {
        if (local == null) {
            local = new LocalDataSetFactory().loadFrom(getCwd());
        }

        return local;
    }

    public DataSet getRemote() throws IOException {
        if (remote == null) {
            remote = connectRemote(getLocal());
        }

        return remote;
    }

    private static DataSet connectRemote(DataSet local) throws IOException {
        NamedOptions<DataSetType> remoteConfig = local.getIndex().get(REMOTE);

        MutableOptions mutableOptions = remoteConfig.getOptions().toMutable();
        DataSet remote = remoteConfig.getType().getFactory().connect(mutableOptions);

        if (mutableOptions.hasChanged()) {
            NamedOptions<DataSetType> newRemoteConfig = new NamedOptions<>(remoteConfig.getType(), mutableOptions.toOptions());
            local.getIndex().set(REMOTE, newRemoteConfig);
            local.saveIndex();
        }

        return remote;
    }

    public String getCurrentPath() throws IOException {
        LocalFileSystem fs = (LocalFileSystem) getLocal().getFileSystem().getBase();
        return fs.relative(getCwd());
    }
}

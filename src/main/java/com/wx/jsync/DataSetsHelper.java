package com.wx.jsync;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.dataset.factory.impl.LocalDataSetFactory;
import com.wx.jsync.filesystem.base.LocalFileSystem;
import com.wx.jsync.filesystem.decorator.factory.DecoratorType;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;

import java.io.IOException;
import java.util.Collection;

import static com.wx.jsync.index.IndexKey.DECORATORS;
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
            initDecorators(local);
        }

        return local;
    }

    public DataSet getRemote() throws IOException {
        if (remote == null) {
            remote = connectRemote(getLocal());
            initDecorators(remote);
        }

        return remote;
    }

    private static DataSet connectRemote(DataSet local) throws IOException {
        NamedOptions<DataSetType> remoteConfig = local.getIndex().get(REMOTE);
        return remoteConfig.getType().getFactory().connect(remoteConfig.getOptions());
    }

    private static void initDecorators(DataSet target) throws IOException {
        Collection<NamedOptions<DecoratorType>> decorators = target.getIndex().get(DECORATORS);

        for (NamedOptions<DecoratorType> decorator : decorators) {
            Options options = decorator.getOptions();


            decorator.getType().getFactory().getFactory(options)
                    .apply(target::addDecorator);
        }
    }

    public String getCurrentPath() throws IOException {
        LocalFileSystem fs = getLocal().getBaseFs();
        return fs.relative(getCwd());
    }
}

package com.wx.jsync.dataset.factory;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.impl.LocalFileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.RemoteConfig;
import com.wx.jsync.index.impl.JsonIndex;

import java.io.IOException;
import java.util.Collections;

import static com.wx.jsync.Constants.INDEX_FILE;

public abstract class DataSetFactory {

    public final DataSet connectOrInit(DataSet local, RemoteConfig config) throws IOException {
        FileSystem remoteFs = initFileSystem(local, config);
        JsonIndex remoteIndex = new JsonIndex();

        if (remoteFs.exists(INDEX_FILE)) {
            remoteIndex.load(remoteFs);
        } else {
            // TODO: 24.09.17 Better name?
            remoteIndex.create(remoteFs, config.getType().name());
            remoteIndex.setParticipants(Collections.singleton(config.getType().name()));
            remoteIndex.save(remoteFs);
        }

        return new DataSet(remoteFs, remoteIndex);
    }

    public abstract RemoteConfig parseConfig(ArgumentsSupplier args);

    protected abstract FileSystem initFileSystem(DataSet local, RemoteConfig config) throws IOException;

}
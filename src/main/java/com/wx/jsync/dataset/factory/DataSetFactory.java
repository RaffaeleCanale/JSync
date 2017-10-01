package com.wx.jsync.dataset.factory;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.Options;

import java.io.IOException;
import java.util.Collections;

import static com.wx.jsync.Constants.INDEX_FILE;
import static com.wx.jsync.index.IndexKey.PARTICIPANTS;

public abstract class DataSetFactory {

    public final DataSet connect(DataSet local, Options options) throws IOException {
        FileSystem remoteFs = initFileSystem(local, options);
        Index remoteIndex = new Index();

        if (remoteFs.exists(INDEX_FILE)) {
            remoteIndex.load(remoteFs);
        } else {
            throw new IOException("Remote not found");
        }

        return new DataSet(remoteFs, remoteIndex);
    }

    public final DataSet init(DataSet local, Options options) throws IOException {
        FileSystem remoteFs = initFileSystem(local, options);
        Index remoteIndex = new Index();

        if (remoteFs.exists(INDEX_FILE)) {
            throw new IOException("Remote already exists");
        } else {
            // TODO: 24.09.17 Better name?
            String author = "remote";
            remoteIndex.create(remoteFs, author);
            remoteIndex.set(PARTICIPANTS, Collections.singleton(author));
            remoteIndex.save(remoteFs);
        }

        return new DataSet(remoteFs, remoteIndex);
    }

    public abstract Options parseConfig(ArgumentsSupplier args);

    protected abstract FileSystem initFileSystem(DataSet local, Options options) throws IOException;

}
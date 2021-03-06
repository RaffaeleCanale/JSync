package com.wx.jsync.dataset.factory;

import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.MutableOptions;
import com.wx.jsync.index.options.Options;

import java.io.IOException;
import java.util.Collections;

import static com.wx.jsync.Constants.INDEX_FILE;
import static com.wx.jsync.index.IndexKey.USER;
import static com.wx.jsync.index.IndexKey.PARTICIPANTS;

public abstract class DataSetFactory {

    public final DataSet connect(MutableOptions options) throws IOException {
        FileSystem remoteFs = initFileSystem(options, false);
        Index remoteIndex = new Index();

        if (remoteFs.exists(INDEX_FILE)) {
            remoteIndex.load(remoteFs);
        } else {
            throw new IOException("Remote not found");
        }

        return new DataSet(remoteFs, remoteIndex);
    }

    public final DataSet init(MutableOptions options) throws IOException {
        FileSystem remoteFs = initFileSystem(options, true);
        Index remoteIndex = new Index();

        if (remoteFs.exists(INDEX_FILE)) {
            throw new IOException("Remote already exists");
        } else {
            // TODO: 24.09.17 Better name?
            String author = "remote";
            remoteIndex.create(remoteFs);
            remoteIndex.set(USER, author);
            remoteIndex.set(PARTICIPANTS, new Options(ImmutableMap.of(
                    author, 1,
                    "last_id", 1
            )));
            remoteIndex.save(remoteFs);
        }

        return new DataSet(remoteFs, remoteIndex);
    }

    public abstract Options parseOptions(ArgumentsSupplier args);

    protected abstract FileSystem initFileSystem(MutableOptions options, boolean create) throws IOException;

}
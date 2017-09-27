package com.wx.jsync.dataset.factory.impl;

import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.dataset.factory.DataSetFactory;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.RemoteConfig;
import com.wx.jsync.index.impl.JsonIndex;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.impl.LocalFileSystem;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.util.DesktopUtils;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.wx.jsync.Constants.CONFIG_DIR;

public class LocalDataSetFactory extends DataSetFactory {

    public DataSet init(File directory, RemoteConfig remote) throws IOException {
        FileSystem fileSystem = new LocalFileSystem(directory);
        Index index = new JsonIndex();

        index.create(fileSystem, DesktopUtils.getHostName());
        index.setRemote(remote);
        index.save(fileSystem);

        return new DataSet(fileSystem, index);
    }

    @Override
    public RemoteConfig parseConfig(ArgumentsSupplier args) {
        String directory = args.supplyString();

        return new RemoteConfig(DataSetType.LOCAL, ImmutableMap.of("directory", directory));
    }

    @Override
    protected FileSystem initFileSystem(DataSet local, RemoteConfig config) {
        return new LocalFileSystem(getDirectory(config));
    }

    public DataSet loadFrom(File directory) throws IOException {
        while (directory != null && !new File(directory, CONFIG_DIR).isDirectory()) {
            directory = directory.getParentFile();
        }

        if (directory == null) {
            throw new NoSuchElementException("No data set found");
        }

        FileSystem fileSystem = new LocalFileSystem(directory);
        Index index = new JsonIndex();

        index.load(fileSystem);

        return new DataSet(fileSystem, index);
    }

    private static File getDirectory(RemoteConfig options) {
        return new File(options.getOption("directory"));
    }


}
package com.wx.jsync.dataset.factory.impl;

import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.io.file.FileUtil;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.factory.DataSetFactory;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.base.LocalFileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.util.DesktopUtils;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import static com.wx.jsync.Constants.CONFIG_DIR;
import static com.wx.jsync.Constants.INDEX_FILE;

public class LocalDataSetFactory extends DataSetFactory {

    public DataSet createOrInit(File directory) throws IOException {
        FileSystem fileSystem = new LocalFileSystem(directory);
        Index index = new Index();

        if (fileSystem.exists(INDEX_FILE)) {
            index.load(fileSystem);
        } else {
            index.create(fileSystem, DesktopUtils.getHostName());
            index.save(fileSystem);
        }

        return new DataSet(fileSystem, index);
    }

    @Override
    public Options parseConfig(ArgumentsSupplier args) {
        String directory = args.supplyString();

        return new Options(ImmutableMap.of("directory", directory));
    }

    @Override
    protected FileSystem initFileSystem(DataSet local, Options config, boolean create) throws IOException {
        File directory = getDirectory(config);
        if (create) {
            FileUtil.autoCreateDirectory(directory);
        }
        return new LocalFileSystem(directory);
    }

    public DataSet loadFrom(File directory) throws IOException {
        while (directory != null && !new File(directory, CONFIG_DIR).isDirectory()) {
            directory = directory.getParentFile();
        }

        if (directory == null) {
            throw new NoSuchElementException("No data setValue found");
        }

        FileSystem fileSystem = new LocalFileSystem(directory);
        Index index = new Index();

        index.load(fileSystem);

        return new DataSet(fileSystem, index);
    }

    private static File getDirectory(Options options) {
        return new File((String) options.get("directory"));
    }


}
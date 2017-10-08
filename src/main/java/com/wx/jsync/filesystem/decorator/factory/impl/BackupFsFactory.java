package com.wx.jsync.filesystem.decorator.factory.impl;

import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.Constants;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;
import com.wx.jsync.filesystem.decorator.factory.DecoratorFactory;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.impl.BackupFileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.Options;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.09.17.
 */
public class BackupFsFactory extends DecoratorFactory {

    @Override
    protected Function<FileSystem, DecoratorFileSystem> initDecorator(Options options, String path) throws IOException {
        String backupPath = getBackupPath(options);

        return fs -> new BackupFileSystem(path, fs, backupPath);
    }

    @Override
    public Options getOptions(ArgumentsSupplier args) {
        if (args.hasMore()) {
            return new Options(ImmutableMap.of(
                    "backupDirectory", args.supplyString()
            ));

        } else {
            return new Options(Collections.emptyMap());
        }
    }

    private String getBackupPath(Options options) {
        String backupPath = options.get("backupDirectory");

        if (backupPath == null) {
            backupPath = Constants.DEFAULT_BACKUP_DIR;
        }
        return backupPath;
    }
}

package com.wx.jsync.dataset.factory.impl;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.dataset.factory.DataSetFactory;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.base.GDriveFileSystem;
import com.wx.jsync.filesystem.base.LocalFileSystem;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.util.extensions.google.DriveServiceFactory;
import com.wx.jsync.util.extensions.google.DriveServiceHelper;
import com.wx.jsync.util.extensions.google.GDriveUtils;
import com.wx.util.log.LogHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.logging.Logger;

import static com.wx.jsync.Constants.CONFIG_DIR;
import static com.wx.jsync.Constants.GOOGLE_DIR;
import static com.wx.jsync.Constants.GOOGLE_DIR_GLOBAL;
import static com.wx.jsync.dataset.DataSetType.GDRIVE;
import static com.wx.jsync.index.IndexKey.REMOTE;

public class GDriveDataSetFactory extends DataSetFactory {

    private static final Logger LOG = LogHelper.getLogger(GDriveDataSetFactory.class);

    private static final String KEY_DIRECTORY = "directory";
    private static final String KEY_USER = "user";
    private static final String KEY_ROOT_ID = "rootId";


    @Override
    public Options parseConfig(ArgumentsSupplier args) {
        return new Options(ImmutableMap.of(
                KEY_DIRECTORY, args.supplyString()
        ));
    }

    @Override
    protected FileSystem initFileSystem(DataSet local, Options options, boolean create) throws IOException {
        initDriveService(local);

        String user = options.get(KEY_USER);
        String rootId = options.get(KEY_ROOT_ID);
        String directory = options.get(KEY_DIRECTORY);

        Drive driveService;
        boolean saveConfig = false;

        if (user == null) {
            Userinfoplus userInfo = DriveServiceFactory.getUserInfo(true);
            LOG.info("User authorized: " + userInfo.getName());
            user = userInfo.getName();
            saveConfig = true;
        }

        driveService = DriveServiceFactory.getDriveService(false);
        DriveServiceHelper drive = new DriveServiceHelper(driveService);


        if (rootId == null) {
            if (directory == null) {
                throw new IllegalArgumentException("Must specify a Drive directory");
            }

            Optional<String> id = GDriveUtils.findFileByPath(drive, directory)
                    .map(File::getId);
            if (id.isPresent()) {
                rootId = id.get();
            } else if (create) {
                rootId = GDriveUtils.mkdir(drive, directory).getId();
            } else {
                throw new FileNotFoundException(directory);
            }

            saveConfig = true;
        }

        if (saveConfig) {
            NamedOptions<DataSetType> remoteConfig = new NamedOptions<>(GDRIVE, new Options(ImmutableMap.of(
                    KEY_DIRECTORY, directory,
                    KEY_USER, user,
                    KEY_ROOT_ID, rootId
            )));

            local.getIndex().set(REMOTE, remoteConfig);
            local.getIndex().save(local.getFileSystem());
        }

        return new GDriveFileSystem(drive, rootId);
    }

    private static void initDriveService(DataSet local) throws IOException {
        if (DriveServiceFactory.isInit()) {
            return;
        }

        try {
            LocalFileSystem localFs = local.getBaseFs();
            java.io.File localDir = localFs.getFile(GOOGLE_DIR);
            java.io.File globalDir = new java.io.File(GOOGLE_DIR_GLOBAL);

            if (!localDir.isDirectory() && globalDir.isDirectory()) {
                DriveServiceFactory.init(globalDir);
            } else {
                DriveServiceFactory.init(localDir);
            }

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("GDrive data setValue can only be combined with a standard local filesystem");
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
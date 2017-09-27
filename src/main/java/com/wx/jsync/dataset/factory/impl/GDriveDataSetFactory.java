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
import com.wx.jsync.filesystem.impl.GDriveFileSystem;
import com.wx.jsync.filesystem.impl.LocalFileSystem;
import com.wx.jsync.index.RemoteConfig;
import com.wx.jsync.util.extensions.google.DriveServiceFactory;
import com.wx.jsync.util.extensions.google.DriveServiceHelper;
import com.wx.jsync.util.extensions.google.GDriveUtils;
import com.wx.util.log.LogHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import static com.wx.jsync.Constants.CONFIG_DIR;
import static com.wx.jsync.dataset.DataSetType.GDRIVE;

public class GDriveDataSetFactory extends DataSetFactory {

    private static final Logger LOG = LogHelper.getLogger(GDriveDataSetFactory.class);
    private static final String GOOGLE_DIR = CONFIG_DIR + "google/";

    @Override
    public RemoteConfig parseConfig(ArgumentsSupplier args) {
        return new RemoteConfig(GDRIVE, ImmutableMap.of(
                "directory", args.supplyString()
        ));
    }

    @Override
    protected FileSystem initFileSystem(DataSet local, RemoteConfig config) throws IOException {
        initDriveService(local);

        String user = config.getOption("user");
        String rootId = config.getOption("rootId");
        String directory = config.getOption("directory");

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

            rootId = GDriveUtils.findFileByPath(drive, directory)
                    .map(File::getId)
                    .orElseThrow(() -> new FileNotFoundException(directory));
            saveConfig = true;
        }

        if (saveConfig) {
            RemoteConfig remoteConfig = new RemoteConfig(GDRIVE, ImmutableMap.of(
                    "directory", directory,
                    "user", user,
                    "rootId", rootId
            ));
            local.getIndex().setRemote(remoteConfig);
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

            DriveServiceFactory.init(localFs.getFile(GOOGLE_DIR));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("GDrive data set can only be combined with a standard local filesystem");
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
package com.wx.jsync.sync.tasks.impl;

import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;
import com.wx.util.Format;

import java.io.IOException;
import java.util.Date;

import static com.wx.jsync.Constants.BACKUP_DIR;
import static com.wx.util.Format.formatDate;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class BackupExecutor extends SyncTasksExecutor {

    private final SyncTasksExecutor executor;
    private final boolean backupLocal;
    private final boolean backupRemote;


    public BackupExecutor(boolean backupLocal, boolean backupRemote, SyncTasksExecutor executor) {
        this.executor = executor;
        this.backupLocal = backupLocal;
        this.backupRemote = backupRemote;
    }

    @Override
    public void pull(FileSystem localFs, FileSystem remoteFs, String path) throws IOException {
        if (backupLocal && localFs.exists(path)) {
            String backupPath = getBackupPath(localFs, path);

            localFs.move(path, backupPath);
        }

        executor.pull(localFs, remoteFs, path);
    }

    @Override
    public void push(FileSystem localFs, FileSystem remoteFs, String path) throws IOException {
        if (backupRemote) {
            String backupPath = getBackupPath(remoteFs, path);

            remoteFs.move(path, backupPath);
        }

        executor.push(localFs, remoteFs, path);
    }

    @Override
    public void removeLocal(FileSystem localFs, String path) throws IOException {
        if (backupLocal && localFs.exists(path)) {
            String backupPath = getBackupPath(localFs, path);

            localFs.move(path, backupPath);

        } else {
            executor.removeLocal(localFs, path);
        }
    }

    @Override
    public void removeRemote(FileSystem remoteFs, String path) throws IOException {
        if (backupRemote) {
            String backupPath = getBackupPath(remoteFs, path);

            remoteFs.move(path, backupPath);

        } else {
            executor.removeRemote(remoteFs, path);
        }
    }

    private String getBackupPath(FileSystem fs, String path) {
        String parent = BACKUP_DIR + path.replace('/', '_') + "/";
        String name = path;

        int lastSep = name.lastIndexOf('/');
        if (lastSep >= 0) {
            name = name.substring(lastSep + 1);
        }

        return parent + formatDate(new Date().getTime(), "yyyy.MM.dd HH:mm:ss") + " " + name;
    }
}

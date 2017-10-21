package com.wx.jsync.sync.tasks.impl;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.sync.tasks.SyncTasks;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;

import java.io.IOException;

import static com.wx.jsync.Main.IN;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.09.17.
 */
public class ConfirmExecutor extends SyncTasksExecutor {

    private final SyncTasksExecutor executor;

    public ConfirmExecutor(SyncTasksExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(DataSet local, DataSet remote, SyncTasks tasks) throws IOException {
        IN.getConsole().println("\n\n" + tasks.toString() + "\n\nDo you want to apply these changes? ");
        if (!IN.inputYesNo()) {
            throw new RuntimeException("Cancelled by user");
        }

        super.execute(local, remote, tasks);
    }

    @Override
    public void pull(FileSystem localFs, FileSystem remoteFs, String localPath, String remotePath) throws IOException {
        executor.pull(localFs, remoteFs, localPath, remotePath);
    }

    @Override
    public void push(FileSystem localFs, FileSystem remoteFs, String localPath, String remotePath) throws IOException {
        executor.push(localFs, remoteFs, localPath, remotePath);
    }

    @Override
    public void removeLocal(FileSystem localFs, String path) throws IOException {
        executor.removeLocal(localFs, path);
    }

    @Override
    public void removeRemote(FileSystem remoteFs, String path) throws IOException {
        executor.removeRemote(remoteFs, path);
    }
}

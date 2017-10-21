package com.wx.jsync.sync.tasks.impl;

import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;

import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.09.17.
 */
public class DefaultExecutor extends SyncTasksExecutor {


    @Override
    public void pull(FileSystem localFs, FileSystem remoteFs, String localPath, String remotePath) throws IOException {
        localFs.write(localPath, remoteFs.read(remotePath));
    }

    @Override
    public void push(FileSystem localFs, FileSystem remoteFs, String localPath, String remotePath) throws IOException {
        remoteFs.write(remotePath, localFs.read(localPath));
    }

    @Override
    public void removeLocal(FileSystem localFs, String localPath) throws IOException {
        localFs.remove(localPath);
    }

    @Override
    public void removeRemote(FileSystem remoteFs, String remotePath) throws IOException {
        remoteFs.remove(remotePath);
    }

}

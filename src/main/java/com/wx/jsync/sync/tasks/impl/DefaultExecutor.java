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
    public void pull(FileSystem localFs, FileSystem remoteFs, String path) throws IOException {
        localFs.write(path, remoteFs.read(path));
    }

    @Override
    public void push(FileSystem localFs, FileSystem remoteFs, String path) throws IOException {
        remoteFs.write(path, localFs.read(path));
    }

    @Override
    public void removeLocal(FileSystem localFs, String path) throws IOException {
        localFs.remove(path);
    }

    @Override
    public void removeRemote(FileSystem remoteFs, String path) throws IOException {
        remoteFs.remove(path);
    }

}

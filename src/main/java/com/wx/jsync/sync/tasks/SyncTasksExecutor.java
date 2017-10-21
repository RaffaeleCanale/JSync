package com.wx.jsync.sync.tasks;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.SyncFileBuilder;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wx.jsync.index.IndexKey.FILES;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.09.17.
 */
public abstract class SyncTasksExecutor {

    private static final Logger LOG = LogHelper.getLogger(SyncTasksExecutor.class);

    public void execute(DataSet local, DataSet remote, SyncTasks tasks) throws IOException {
        if (tasks.hasConflicts()) {
            throw new IllegalArgumentException("Must solve conflicts before executing any task");
        }

        Index localIndex = local.getIndex();
        Index remoteIndex = remote.getIndex();
        FileSystem localFs = local.getFileSystem();
        FileSystem remoteFs = remote.getFileSystem();


        for (SyncTask task : tasks.getTasks()) {
            try {
                executeTask(localIndex, remoteIndex, localFs, remoteFs, task);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to synchronize " + task.getUserPath(), e);
            }
        }


        localIndex.save(localFs);
        remoteIndex.save(remoteFs);
    }

    private void executeTask(Index localIndex, Index remoteIndex, FileSystem localFs, FileSystem remoteFs, SyncTask task) throws IOException {
        String userPath = task.getUserPath();
        SyncFile localFile = task.getLocalFile();
        SyncFile remoteFile = task.getRemoteFile();


        SyncFileBuilder localFileBuilder = localFile != null ?
                localFile.builder() :
                new SyncFileBuilder(userPath);

        SyncFileBuilder remoteFileBuilder = remoteFile != null ?
                remoteFile.builder() :
                new SyncFileBuilder(userPath);

        String localPath = localFileBuilder.getRealPath();
        String remotePath = remoteFileBuilder.getRealPath();

        switch (task.getType()) {
            case PULL:
                LOG.finest("Pulling " + userPath + "...");
                pull(localFs, remoteFs, localPath, remotePath);

                FileStat localStat = localFs.getFileStat(localPath);
                localIndex.setSingle(FILES, localFileBuilder
                        .setStat(localStat)
                        .setVersionOf(remoteFile)
                        .withUpToDateBaseVersion()
                        .create()
                );
                break;

            case PUSH:
                LOG.finest("Pushing " + userPath + "...");
                push(localFs, remoteFs, localPath, remotePath);

                FileStat remoteStat = remoteFs.getFileStat(remotePath);
                remoteIndex.setSingle(FILES, remoteFileBuilder
                        .setStat(remoteStat)
                        .setVersionOf(localFile)
                        .create()
                );
                localIndex.setSingle(FILES, localFile.bumpBaseVersion());
                break;

            case REMOVE_LOCAL:
                LOG.finest("Removing " + userPath + "...");
                removeLocal(localFs, localPath);

                localIndex.setSingle(FILES, localFileBuilder
                        .setVersionOf(remoteFile)
                        .withUpToDateBaseVersion()
                        .setStat(FileStat.REMOVED)
                        .create()
                );
                break;

            case REMOVE_REMOTE:
                LOG.finest("Removing " + userPath + "...");
                removeRemote(remoteFs, remotePath);

                remoteIndex.setSingle(FILES, remoteFileBuilder
                        .setVersionOf(localFile)
                        .setStat(FileStat.REMOVED)
                        .create()
                );
                localIndex.setSingle(FILES, localFile.bumpBaseVersion());
                break;

            case SOFT_PULL:
                LOG.finest("Bumping " + userPath);
                localIndex.setSingle(FILES, localFileBuilder
                        .setVersionOf(remoteFile)
                        .withUpToDateBaseVersion()
                        .create()
                );
                break;

            case SOFT_PUSH:
                LOG.finest("Bumping " + userPath);
                remoteIndex.setSingle(FILES, remoteFileBuilder
                        .setVersionOf(localFile)
                        .create()
                );
                localIndex.setSingle(FILES, localFile.bumpBaseVersion());
                break;

            case DELETE_LOCAL_ENTRY:
                LOG.warning("Removing local index entry that failed to purge for " + userPath);
                localIndex.removeSingle(FILES, localFile);
                break;

            default:
                throw new AssertionError();
        }
    }

    public abstract void pull(FileSystem localFs, FileSystem remoteFs, String localPath, String remotePath) throws IOException;

    public abstract void push(FileSystem localFs, FileSystem remoteFs, String localPath, String remotePath) throws IOException;

    public abstract void removeLocal(FileSystem localFs, String localPath) throws IOException;

    public abstract void removeRemote(FileSystem remoteFs, String remotePath) throws IOException;
}

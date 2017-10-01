package com.wx.jsync.sync.tasks;

import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.sync.SyncFile;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.util.Optional;
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
                LOG.log(Level.SEVERE, "Failed to synchronize " + task.getPath(), e);
            }
        }


        localIndex.save(localFs);
        remoteIndex.save(remoteFs);
    }

    private void executeTask(Index localIndex, Index remoteIndex, FileSystem localFs, FileSystem remoteFs, SyncTask task) throws IOException {
        String path = task.getPath();
        SyncFile localFile = task.getLocalFile();
        SyncFile remoteFile = task.getRemoteFile();


        switch (task.getType()) {
            case PULL:
                LOG.finest("Pulling " + path + "...");
                pull(localFs, remoteFs, path);

                FileStat localStat = localFs.getFileStat(path);
                localIndex.setSingle(FILES, new SyncFile(
                        path,
                        localStat,
                        remoteFile.getVersion(),
                        remoteFile.getVersionAuthor(),
                        remoteFile.getVersion()
                ));
                break;

            case PUSH:
                LOG.finest("Pushing " + path + "...");
                push(localFs, remoteFs, path);

                FileStat remoteStat = remoteFs.getFileStat(path);
                remoteIndex.setSingle(FILES, new SyncFile(
                        path,
                        remoteStat,
                        localFile.getVersion(),
                        localFile.getVersionAuthor(),
                        Optional.empty()
                ));
                localIndex.setSingle(FILES, localFile.bumpBaseVersion());
                break;

            case REMOVE_LOCAL:
                LOG.finest("Removing " + path + "...");
                removeLocal(localFs, path);

                localIndex.setSingle(FILES, new SyncFile(
                        path,
                        FileStat.REMOVED,
                        remoteFile.getVersion(),
                        remoteFile.getVersionAuthor(),
                        remoteFile.getVersion()
                ));
                break;

            case REMOVE_REMOTE:
                LOG.finest("Removing " + path + "...");
                removeRemote(remoteFs, path);

                remoteIndex.setSingle(FILES, new SyncFile(
                        path,
                        FileStat.REMOVED,
                        localFile.getVersion(),
                        localFile.getVersionAuthor(),
                        Optional.empty()
                ));
                localIndex.setSingle(FILES, localFile.bumpBaseVersion());
                break;

            case SOFT_PULL:
                LOG.finest("Bumping " + localFile.getPath());
                localIndex.setSingle(FILES, localFile
                        .withSameVersionAs(remoteFile)
                        .bumpBaseVersion());
                break;

            case SOFT_PUSH:
                LOG.finest("Bumping " + localFile.getPath());
                remoteIndex.setSingle(FILES, remoteFile.withSameVersionAs(localFile));
                localIndex.setSingle(FILES, localFile.bumpBaseVersion());
                break;

            case DELETE_LOCAL_ENTRY:
                LOG.warning("Removing local index entry that failed to purge for " + path);
                localIndex.removeSingle(FILES, localFile);
                break;

            default:
                throw new AssertionError();
        }
    }

    public abstract void pull(FileSystem localFs, FileSystem remoteFs, String path) throws IOException;

    public abstract void push(FileSystem localFs, FileSystem remoteFs, String path) throws IOException;

    public abstract void removeLocal(FileSystem localFs, String path) throws IOException;

    public abstract void removeRemote(FileSystem remoteFs, String path) throws IOException;
}

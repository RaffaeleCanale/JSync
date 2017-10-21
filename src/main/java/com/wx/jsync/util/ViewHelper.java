package com.wx.jsync.util;

import com.wx.io.file.FileUtil;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.sync.SyncFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static com.wx.jsync.index.IndexKey.FILES;
import static com.wx.jsync.index.IndexKey.PARTICIPANTS;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 21.10.17.
 */
public class ViewHelper {

    private final DataSet remote;
    private final String user;

    private final Options participants;
    private final FileSystem remoteFs;

    public ViewHelper(DataSet remote, String user) {
        this.remote = remote;
        this.user = user;
        this.remoteFs = remote.getFileSystem();

        this.participants = remote.getIndex().get(PARTICIPANTS);
        if (!participants.has(user)) {
            // TODO: 21.10.17 Update participants
            throw new UnsupportedOperationException();
        }
    }

    public void addViewTo(String path) throws IOException {
        SyncFile file = getFile(path);
        Collection<String> newViewGroup = getNewViewGroup(file);

        String currentPath = file.getRealPath();
        String newPath = computePath(currentPath, newViewGroup);

        remoteFs.move(currentPath, newPath);

        remote.getIndex().setSingle(FILES, file.builder()
                .setRealPath(newPath)
                .setCanViewUsers(newViewGroup)
                .create()
        );
        remote.saveIndex();
    }

    private Collection<String> getNewViewGroup(SyncFile file) {
        Collection<String> users = new ArrayList<>(file.getCanViewUsers());
        users.add(user);

        return users;
    }

    private SyncFile getFile(String path) {
        Collection<SyncFile> files = remote.getIndex().get(FILES);

        for (SyncFile file : files) {
            if (file.getUserPath().equals(path)) {
                return file;
            }
        }

        throw new IllegalArgumentException("File not found");
    }

    private String computePath(String path, Collection<String> viewGroup) {
        if (viewGroup.stream().anyMatch(user -> !participants.has(user))) {
            throw new IllegalArgumentException();
        }

        int[] ids = viewGroup.stream()
                .mapToInt(participants::get)
                .toArray();

        long group = getGroup(ids);

        return FileUtil.nameWithoutExtension(path)
                + "_" + Long.toHexString(group)
                + FileUtil.fileExtension(path);
    }

    private long getGroup(int[] ids) {
        long group = 0;

        for (int id : ids) {
            if (id >= 64) {
                throw new RuntimeException();
            }

            group = group | (1 << id);
        }

        return group;
    }
}

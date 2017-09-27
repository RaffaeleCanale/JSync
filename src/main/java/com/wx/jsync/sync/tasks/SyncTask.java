package com.wx.jsync.sync.tasks;

import com.wx.jsync.sync.SyncFile;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.wx.jsync.sync.tasks.SyncTask.Type.*;
import static java.util.Objects.requireNonNull;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.09.17.
 */
public class SyncTask {

    public enum Type {
        PUSH,
        SOFT_PUSH,
        PULL,
        SOFT_PULL,
        REMOVE_LOCAL,
        REMOVE_REMOTE,
        CONFLICT,
        DELETE_LOCAL_ENTRY
    }

    private final Type type;
    private final SyncFile localFile;
    private final SyncFile remoteFile;

    public SyncTask(Type type, SyncFile localFile, SyncFile remoteFile) {
        this.type = type;
        this.localFile = validateLocal(type, localFile);
        this.remoteFile = validateRemote(type, remoteFile);
    }

    public Type getType() {
        return type;
    }

    public SyncFile getLocalFile() {
        return localFile;
    }

    public SyncFile getRemoteFile() {
        return remoteFile;
    }

    public String getPath() {
        return localFile == null ? remoteFile.getPath() : localFile.getPath();
    }

    private static SyncFile validateLocal(Type type, SyncFile localFile) {
        Set<Type> typesRequiringLocal = newHashSet(PUSH, SOFT_PUSH, SOFT_PULL, REMOVE_REMOTE, CONFLICT, DELETE_LOCAL_ENTRY);
        boolean requireNonNull = typesRequiringLocal.contains(type);

        if (requireNonNull && localFile == null) {
            throw new IllegalArgumentException(type + " requires a local file");
        } else if (!requireNonNull && localFile != null) {
            throw new IllegalArgumentException(type + " requires no local file");
        }

        if (type.equals(PUSH) && localFile.getStat().isRemoved()) {
            throw new IllegalArgumentException("Cannot push a removed file");
        }

        return localFile;
    }

    private static SyncFile validateRemote(Type type, SyncFile remoteFile) {
        Set<Type> typesRequiringRemote = newHashSet(PULL, SOFT_PULL, SOFT_PUSH, REMOVE_LOCAL, CONFLICT);
        boolean requireNonNull = typesRequiringRemote.contains(type);

        if (requireNonNull && remoteFile == null) {
            throw new IllegalArgumentException(type + " requires a local file");
        } else if (!requireNonNull && remoteFile != null) {
            throw new IllegalArgumentException(type + " requires no local file");
        }

        if (type.equals(PULL) && remoteFile.getStat().isRemoved()) {
            throw new IllegalArgumentException("Cannot pull a removed file");
        }

        return remoteFile;
    }
}

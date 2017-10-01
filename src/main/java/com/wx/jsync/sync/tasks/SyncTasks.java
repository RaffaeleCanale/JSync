package com.wx.jsync.sync.tasks;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.sync.SyncFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static com.wx.jsync.sync.tasks.SyncTask.Type.*;

public class SyncTasks {

    private final Map<String, SyncTask> tasks;

    public SyncTasks(Map<String, SyncTask> tasks) {
        this.tasks = tasks;
    }

    public Collection<SyncTask> getTasks() {
        return tasks.values();
    }

    public boolean hasConflicts() {
        return tasks.values().stream().anyMatch(task -> task.getType().equals(CONFLICT));
    }

    private Collection<SyncTask> getTasks(SyncTask.Type... types) {
        Set<SyncTask.Type> typesSet = newHashSet(types);

        return tasks.values().stream()
                .filter(task -> typesSet.contains(task.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder report = new StringBuilder();


        appendList(report, "Files to push:", PUSH);
        appendList(report, "Files to pull:", PULL);
        appendList(report, "Files to remove:", REMOVE_REMOTE, REMOVE_LOCAL);
        appendList(report, "Conflicts:", CONFLICT);
        appendList(report, "Version bumps:", SOFT_PULL, SOFT_PUSH);
        appendList(report, "Purges:", DELETE_LOCAL_ENTRY);

        if (report.length() == 0) {
            return "Already up-to-date";
        }

        return report.toString();
    }

    private void appendList(StringBuilder report, String title, SyncTask.Type... types) {
        Collection<SyncTask> tasks = getTasks(types);
        if (!tasks.isEmpty()) {
            report.append(title);
            for (SyncTask task : tasks) {
                report.append("\n - ").append(task.getPath());
            }
            report.append("\n\n");
        }
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    public static class Builder {

        private final Map<String, SyncTask> tasks = new HashMap<>();
        private final boolean enableBump;

        public Builder(boolean enableBump) {
            this.enableBump = enableBump;
        }

        public void put(SyncTask.Type type, SyncFile localFile, SyncFile remoteFile) {
            String path = localFile == null ? remoteFile.getPath() : localFile.getPath();
            SyncTask oldValue = tasks.put(path, new SyncTask(type, localFile, remoteFile));

            if (oldValue != null) {
                throw new IllegalArgumentException("A task was already setValue for " + path);
            }
        }

        public void updateRemote(SyncFile localFile, Optional<SyncFile> remoteFile) {
            /*
            LocalFile   RemoteIndex     RemoteFile
            exists      exists          exists      ->  push
            exists      exists          not exists  ->  push
            exists      not exists      -           ->  push
            not exists  exists          exists      ->  remove remote
            not exists  exists          not exists  ->  soft push
            not exists  not exists      -           ->  delete index
             */

            if (!localFile.getStat().isRemoved()) {
                put(PUSH, localFile, null);
            } else if (remoteFile.isPresent()) {
                if (!remoteFile.get().getStat().isRemoved()) {
                    put(REMOVE_REMOTE, localFile, null);
                } else {
                    put(SOFT_PUSH, localFile, remoteFile.get());
                }
            } else {
                put(DELETE_LOCAL_ENTRY, localFile, null);
            }
        }

        public void updateLocal(Optional<SyncFile> localFile, SyncFile remoteFile) {
            /*
            RemoteFile  LocalIndex      LocalFile
            exists      exists          exists      ->  pull
            exists      exists          not exists  ->  pull
            exists      not exists      -           ->  pull
            not exists  exists          exists      ->  remove local
            not exists  exists          not exists  ->  soft pull
            not exists  not exists      -           ->  create + soft pull
             */

            if (!remoteFile.getStat().isRemoved()) {
                put(PULL, null, remoteFile);
            } else if (localFile.isPresent()) {
                if (!localFile.get().getStat().isRemoved()) {
                    put(REMOVE_LOCAL, null, remoteFile);
                } else {
                    put(SOFT_PULL, localFile.get(), remoteFile);
                }
            } else {
                put(SOFT_PULL, new SyncFile(
                        remoteFile.getPath(),
                        FileStat.REMOVED,
                        0.0,
                        "",
                        Optional.empty()
                ), remoteFile);
            }
        }


        public boolean bump(SyncFile localFile, SyncFile remoteFile) {
            if (!enableBump) {
                return false;
            }

            if (localFile.getStat().matches(remoteFile.getStat())) {
                if (localFile.getVersion() > remoteFile.getVersion()) {
                    put(SOFT_PUSH, localFile, remoteFile);
                } else {
                    put(SOFT_PULL, localFile, remoteFile);
                }

                return true;
            }

            return false;
        }

        public SyncTasks create() {
            return new SyncTasks(tasks);
        }

        public Collection<SyncTask> removeConflicts() {
            List<SyncTask> result = new ArrayList<>(tasks.size());

            Iterator<SyncTask> it = tasks.values().iterator();
            while (it.hasNext()) {
                SyncTask task = it.next();
                if (task.getType().equals(CONFLICT)) {
                    result.add(task);
                    it.remove();
                }
            }

            return result;
        }
    }

}
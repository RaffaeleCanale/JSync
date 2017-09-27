package com.wx.jsync.index.impl;

import com.wx.jsync.sync.SyncManager;
import com.wx.jsync.sync.conflict.ConflictHandler;
import com.wx.jsync.sync.conflict.impl.UserInputHandler;
import com.wx.jsync.sync.tasks.SyncTasksExecutor;
import com.wx.jsync.sync.tasks.impl.BackupExecutor;
import com.wx.jsync.sync.tasks.impl.ConfirmExecutor;
import org.json.JSONObject;

import static com.wx.jsync.Constants.*;
import static com.wx.jsync.util.JsonUtils.getBooleanOpt;
import static com.wx.jsync.util.JsonUtils.getStringOpt;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
class SyncManagerInitializer {

    static void initialize(JSONObject root, SyncManager sync, SyncTasksExecutor baseExecutor) {
        sync.setEnableBump(getBooleanOpt(root, "local", "options", "enableBump").orElse(DEFAULT_ENABLE_BUMP));
        sync.setConflictHandler(initConflictHandler(root));
        sync.setTasksExecutor(initTasksExecutor(root, baseExecutor));
    }

    private static SyncTasksExecutor initTasksExecutor(JSONObject root, SyncTasksExecutor baseExecutor) {
        boolean useBackup = useBackup(root);
        boolean askConfirmation = getBooleanOpt(root, "local", "options", "executor", "askConfirmation")
                .orElse(DEFAULT_ASK_CONFIRMATION);

        if (useBackup) {
            baseExecutor = new BackupExecutor(true, false, baseExecutor);
        }

        if (askConfirmation) {
            baseExecutor = new ConfirmExecutor(baseExecutor);
        }

        return baseExecutor;
    }

    static boolean useBackup(JSONObject root) {
        return getBooleanOpt(root, "local", "options", "executor", "backup").orElse(DEFAULT_USE_BACKUP);
    }

    private static ConflictHandler initConflictHandler(JSONObject root) {
        String name = getStringOpt(root, "local", "options", "conflictHandler").orElse(DEFAULT_CONFLICT_HANDLER);

        switch (name) {
            case "user_input":
                return new UserInputHandler();
            default:
                throw new IllegalArgumentException("No conflict handler found for " + name);
        }
    }

    private SyncManagerInitializer() {
    }
}

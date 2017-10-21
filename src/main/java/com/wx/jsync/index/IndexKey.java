package com.wx.jsync.index;

import com.wx.jsync.GlobalConfig;

import static com.wx.jsync.Constants.*;
import static com.wx.jsync.index.Loader.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public enum IndexKey {

    REMOTE              (REMOTE_OPTIONS,                          "remote"),
    FILES               (FILE_SET,                                "files"),
    USER                (STRING.or(GlobalConfig::getUser),        "info", "user"),
    PARTICIPANTS        (OPTIONS,                                 "info", "participants"),
    ENABLE_BACKUP       (BOOLEAN.or(DEFAULT_ENABLE_BACKUP),       "info", "options", "backup", "enabled"),
    BACKUP_DIRECTORY    (STRING.or(DEFAULT_BACKUP_DIR),           "info", "options", "backup", "directory"),
    ENABLE_ENCRYPTION   (BOOLEAN.or(DEFAULT_ENABLE_ENCRYPTION),   "info", "options", "encryption", "enabled"),
    ENCRYPTION_ALGORITHM(STRING.or(DEFAULT_ENCRYPTION_ALGORITHM), "info", "options", "encryption", "algorithm"),
    IGNORE              (FILTER,                                  "info", "options", "ignore"),
    ENABLE_BUMP         (BOOLEAN.or(DEFAULT_ENABLE_BUMP),         "info", "options", "executor", "useBump"),
    ASK_CONFIRMATION    (BOOLEAN.or(DEFAULT_ASK_CONFIRMATION),    "info", "options", "executor", "askConfirmation"),
    CONFLICT_HANDLER    (HANDLER,                                 "info", "options", "executor", "conflictHandler"),



    ;

    private final Loader<?> loader;
    private final String path[];


    IndexKey(Loader<?> loader, String... path) {
        this.loader = loader;
        this.path = path;
    }

    public String[] getPath() {
        return path;
    }

    public Loader<?> getLoader() {
        return loader;
    }
}


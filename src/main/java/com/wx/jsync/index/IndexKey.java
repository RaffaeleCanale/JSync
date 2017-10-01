package com.wx.jsync.index;

import static com.wx.jsync.Constants.*;
import static com.wx.jsync.index.Loader.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public enum IndexKey {

    REMOTE          (DATA_SET_OPTIONS,                  "remote"),
    FILES           (FILE_SET,                          "files"),
    OWNER           (STRING,                            "info", "owner"),
    PARTICIPANTS    (STRING_SET,                        "info", "participants"),
    STORED_KEY      (STORED_KEYS,                       "info", "key"),
    ENABLE_STORE_KEY(BOOLEAN(DEFAULT_STORE_KEY),        "info", "options", "storeKey"),
    IGNORE          (FILTER,                            "info", "options", "ignore"),
    ENABLE_BUMP     (BOOLEAN(DEFAULT_ENABLE_BUMP),      "info", "options", "executor", "useBump"),
    ASK_CONFIRMATION(BOOLEAN(DEFAULT_ASK_CONFIRMATION), "info", "options", "executor", "askConfirmation"),
    CONFLICT_HANDLER(HANDLER,                           "info", "options", "executor", "conflictHandler"),
    DECORATORS      (DECORATOR_SET,                     "info", "options", "decorators"),


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


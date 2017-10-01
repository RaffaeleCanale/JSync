package com.wx.jsync.index.loader;

import com.wx.jsync.index.Loader;
import com.wx.jsync.sync.conflict.ConflictHandler;
import com.wx.jsync.sync.conflict.impl.UserInputHandler;
import org.json.JSONObject;

import java.util.Optional;

import static com.wx.jsync.util.JsonUtils.getStringOpt;
import static com.wx.jsync.util.JsonUtils.set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 29.09.17.
 */
public class ConflictHandlerLoader implements Loader<ConflictHandler> {

    @Override
    public Optional<ConflictHandler> loadOpt(JSONObject root, String[] key) {
        return getStringOpt(root, key)
                .map(this::resolve);
    }

    @Override
    public ConflictHandler load(JSONObject root, String[] key) {
        return loadOpt(root, key).orElseGet(UserInputHandler::new);
    }

    @Override
    public void setValue(JSONObject root, ConflictHandler value, String[] path) {
        set(root, resolve(value), path);
    }

    private ConflictHandler resolve(String name) {
        switch (name) {
            case "user":
                return new UserInputHandler();
            default:
                throw new IllegalArgumentException("No conflict handler found for name " + name);
        }
    }

    private String resolve(ConflictHandler handler) {
        if (handler instanceof UserInputHandler) {
            return "user";
        } else {
            throw new AssertionError();
        }
    }
}

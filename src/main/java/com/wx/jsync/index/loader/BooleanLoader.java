package com.wx.jsync.index.loader;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.index.Loader;
import com.wx.jsync.util.StringArgsSupplier;
import org.json.JSONObject;

import java.util.Optional;

import static com.wx.jsync.util.JsonUtils.getBooleanOpt;
import static com.wx.jsync.util.JsonUtils.set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 29.09.17.
 */
public class BooleanLoader implements Loader<Boolean> {
    @Override
    public Optional<Boolean> loadOpt(JSONObject root, String[] key) {
        return getBooleanOpt(root, key);
    }

    @Override
    public void setValue(JSONObject root, Boolean value, String[] path) {
        set(root, value, path);
    }

    @Override
    public void userSet(JSONObject root, ArgumentsSupplier args, String... path) {
        set(root, args.supplyBoolean(), path);
    }
}

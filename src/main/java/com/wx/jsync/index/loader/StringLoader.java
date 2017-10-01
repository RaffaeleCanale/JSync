package com.wx.jsync.index.loader;

import com.wx.jsync.index.Loader;
import org.json.JSONObject;

import java.util.Optional;

import static com.wx.jsync.util.JsonUtils.getStringOpt;
import static com.wx.jsync.util.JsonUtils.set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 28.09.17.
 */
public class StringLoader implements Loader<String> {

    @Override
    public Optional<String> loadOpt(JSONObject root, String[] path) {
        return getStringOpt(root, path);
    }

    @Override
    public void setValue(JSONObject root, String value, String[] path) {
        set(root, value, path);
    }
}

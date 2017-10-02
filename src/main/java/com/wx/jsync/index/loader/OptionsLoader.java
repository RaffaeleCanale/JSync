package com.wx.jsync.index.loader;

import com.wx.jsync.index.Loader;
import com.wx.jsync.index.SetLoader;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.util.JsonUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.wx.jsync.util.JsonUtils.getObjectOpt;
import static com.wx.jsync.util.JsonUtils.set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 29.09.17.
 */
public class OptionsLoader implements Loader<Options> {

    @Override
    public Options load(JSONObject root, String... key) {
        return loadOpt(root, key).orElse(Options.EMPTY);
    }

    @Override
    public Optional<Options> loadOpt(JSONObject root, String[] key) {
        return getObjectOpt(root, key).map(this::getOptions);
    }

    @Override
    public void setValue(JSONObject root, Options value, String[] path) {
        set(root, create(value), path);
    }

    private JSONObject create(Options options) {
        JSONObject obj = new JSONObject();

        for (String key : options.keySet()) {
            Object value = options.get(key);

            if (value instanceof Options) {
                value = create((Options) value);
            } else if (value instanceof Collection) {
                value = new JSONArray((Collection) value);
            }

            set(obj, value, key);
        }

        return obj;
    }

    private Options getOptions(JSONObject obj) {
        Map<String, Object> result = new HashMap<>();
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            if (value instanceof JSONObject) {
                value = getOptions((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = ((JSONArray) value).toList();
            }

            result.put(key, value);
        }

        return new Options(result);

    }
}

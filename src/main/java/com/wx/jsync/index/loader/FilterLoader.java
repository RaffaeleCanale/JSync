package com.wx.jsync.index.loader;

import com.wx.jsync.index.Loader;
import com.wx.jsync.util.Common;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.wx.jsync.util.JsonUtils.getStringListOpt;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class FilterLoader implements Loader<Predicate<String>> {

    @Override
    public Optional<Predicate<String>> loadOpt(JSONObject root, String[] key) {
        return getStringListOpt(root, key)
                .map(Common::ignoreFilter);
    }

    @Override
    public void setValue(JSONObject root, Predicate<String> value, String[] path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate<String> load(JSONObject root, String[] key) {
        return loadOpt(root, key).orElse(Common.alwaysTrue());
    }
}

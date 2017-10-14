package com.wx.jsync.index.loader;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.index.Loader;
import com.wx.jsync.util.Common;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.wx.jsync.util.JsonUtils.getObjectOpt;
import static com.wx.jsync.util.JsonUtils.getStringListOpt;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class FilterLoader implements Loader<Predicate<String>> {

    @Override
    public Optional<Predicate<String>> loadOpt(JSONObject root, String[] key) {
        return getStringListOpt(root, key)
                .map(FilterLoader::createFilter);

    }

    @Override
    public void setValue(JSONObject root, Predicate<String> value, String[] path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate<String> load(JSONObject root, String[] key) {
        return loadOpt(root, key).orElse(FilterLoader.alwaysTrue());
    }

    @Override
    public void userSet(JSONObject root, ArgumentsSupplier args, String... path) {
        Loader.STRING_LIST.userSet(root, args, path);
    }

    private static Predicate<String> createFilter(List<String> list) {
        return file -> {
            boolean matches = true;

            for (String filter : list) {
                if (filter.startsWith("!") && file.matches(filter.substring(1))) {
                    matches = true;

                } else if (file.matches(filter)) {
                    matches = false;
                }
            }

            return matches;
        };
    }

    private static <E> Predicate<E> alwaysTrue() {
        return file -> true;
    }
}

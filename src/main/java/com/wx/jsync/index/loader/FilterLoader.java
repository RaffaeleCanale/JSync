package com.wx.jsync.index.loader;

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

    private static final String WHITE_LIST_KEY = "whitelist";
    private static final String BLACK_LIST_KEY = "blacklist";

    @Override
    public Optional<Predicate<String>> loadOpt(JSONObject root, String[] key) {
//        return getStringListOpt(root, key)
//                .map(FilterLoader::ignoreFilter);
        return getObjectOpt(root, key)
                .map(obj -> createFilter(
                        getStringListOpt(obj, WHITE_LIST_KEY).orElse(Collections.emptyList()),
                        getStringListOpt(obj, BLACK_LIST_KEY).orElse(Collections.emptyList())
                ));

    }

    @Override
    public void setValue(JSONObject root, Predicate<String> value, String[] path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Predicate<String> load(JSONObject root, String[] key) {
        return loadOpt(root, key).orElse(FilterLoader.alwaysTrue());
    }

    private static Predicate<String> createFilter(List<String> whiteList, List<String> blacklist) {
//        return file -> ignoreList.stream().noneMatch(file::matches);
        return file -> whiteList.stream().anyMatch(file::matches) || blacklist.stream().noneMatch(file::matches);
    }

    private static <E> Predicate<E> alwaysTrue() {
        return file -> true;
    }
}

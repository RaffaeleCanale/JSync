package com.wx.jsync.index.impl;

import com.wx.jsync.util.Common;
import org.json.JSONObject;

import java.util.function.Predicate;

import static com.wx.jsync.util.JsonUtils.getStringListOpt;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
class FilterLoader {

    static Predicate<String> getFileFilter(JSONObject root) {
        return getStringListOpt(root, "local", "options", "ignore")
                .map(Common::ignoreFilter)
                .orElse(Common.alwaysTrue());
    }


    private FilterLoader() {}
}

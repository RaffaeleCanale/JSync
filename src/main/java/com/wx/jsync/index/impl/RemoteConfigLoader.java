package com.wx.jsync.index.impl;

import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.index.RemoteConfig;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.wx.jsync.util.JsonUtils.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
class RemoteConfigLoader {

    static void setRemote(JSONObject root, RemoteConfig config) {
        set(root, config.getType().name(), "remote", "type");
        for (String key : config.keySet()) {
            set(root, config.getOption(key), "remote", "options", key);
        }
    }

    static Optional<RemoteConfig> getRemote(JSONObject root) {
        return getObjectOpt(root, "remote")
                .map(obj -> new RemoteConfig(
                        resolveType(obj),
                        getOptions(obj)
                ));
    }

    private static Map<String, String> getOptions(JSONObject remoteObj) {
        return getObjectOpt(remoteObj, "options")
                .map(options -> {
                    Map<String, String> result = new HashMap<>();
                    for (String key : options.keySet()) {
                        result.put(key, options.getString(key));
                    }

                    return result;
                })
                .orElse(Collections.emptyMap());
    }

    private static DataSetType resolveType(JSONObject remoteObj) {
        String typeName = getString(remoteObj, "type");

        return DataSetType.valueOf(typeName);
    }


}

package com.wx.jsync.index;

import com.wx.jsync.dataset.DataSetType;

import java.util.Map;
import java.util.Set;

import static com.wx.util.collections.CollectionsUtil.safe;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class RemoteConfig {

    private final DataSetType type;
    private final Map<String, String> options;

    public RemoteConfig(DataSetType type, Map<String, String> options) {
        this.type = type;
        this.options = safe(options);
    }

    public DataSetType getType() {
        return type;
    }

    public String getOption(String key) {
        return options.get(key);
    }

    public Set<String> keySet() {
        return options.keySet();
    }
}

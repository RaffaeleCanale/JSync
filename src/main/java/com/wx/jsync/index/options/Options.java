package com.wx.jsync.index.options;

import java.util.*;

import static com.wx.util.collections.CollectionsUtil.safe;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public class Options {

    public static final Options EMPTY = new Options(Collections.emptyMap());

    private final Map<String, Object> options;

    public Options(Map<String, Object> options) {
        this.options = safe(options);
    }

    public <E> E get(String key) {
        return (E) options.get(key);
    }

    public <E> E get(String key, E def) {
        return (E) options.getOrDefault(key, def);
    }

    public Set<String> keySet() {
        return options.keySet();
    }

    public Options with(String key, Object value) {
        HashMap<String, Object> copy = new HashMap<>(options);
        copy.put(key, value);

        return new Options(copy);
    }

    public MutableOptions toMutable() {
        return new MutableOptions(options);
    }

}

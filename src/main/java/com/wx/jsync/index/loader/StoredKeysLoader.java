package com.wx.jsync.index.loader;

import com.google.common.collect.ImmutableMap;
import com.wx.jsync.index.Loader;
import com.wx.jsync.index.SetLoader;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.index.options.StoredKeys;
import com.wx.util.representables.string.EncodedBytesRepr;
import org.json.JSONObject;

import java.util.*;


/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 28.09.17.
 */
public class StoredKeysLoader implements Loader<StoredKeys> {

//    private static final String KEY_GLOBAL = "global";
//    private static final String KEY_PATH = "path";

    private final OptionsList list = new OptionsList();

    @Override
    public StoredKeys load(JSONObject root, String... key) {
        return loadOpt(root, key).orElse(new KeysStore(null, new HashMap<>()));
    }

    @Override
    public Optional<StoredKeys> loadOpt(JSONObject root, String[] p) {
        Optional<Set<Options>> optionsOpt = list.loadOpt(root, p);
        if (!optionsOpt.isPresent()) {
            return Optional.empty();
        }

        Set<Options> options = optionsOpt.get();
        EncodedBytesRepr caster = new EncodedBytesRepr();

        byte[] global = null;
        Map<String, byte[]> paths = new HashMap<>();

        for (Options keyOptions : options) {
            String path = keyOptions.get("path");
            byte[] key = caster.castOut(keyOptions.get("key"));

            if (path == null || path.isEmpty()) {
                global = key;
            } else {
                paths.put(path, key);
            }
        }

        return Optional.of(new KeysStore(global, paths));
    }

    @Override
    public void setValue(JSONObject root, StoredKeys value, String[] path) {
        Set<Options> result = new HashSet<>();
        KeysStore keys = (KeysStore) value;
        EncodedBytesRepr caster = new EncodedBytesRepr();

        keys.keys.forEach((p, k) -> result.add(new Options(ImmutableMap.of(
                "path", p,
                "key", caster.castIn(k)
        ))));
        if (keys.global != null) {
            result.add(new Options(ImmutableMap.of(
                    "key", caster.castIn(keys.global)
            )));
        }

        list.setValue(root, result, path);
    }


    private class OptionsList extends SetLoader<Options> {

        @Override
        protected String getId(Options options) {
            return Optional.of((String) options.get("path")).orElse("");
        }

        @Override
        protected Options load(Object entry) {
            return Loader.OPTIONS.load((JSONObject) entry);
        }

        @Override
        protected Object getValue(Options value) {
            JSONObject obj = new JSONObject();
            Loader.OPTIONS.setValue(obj, value, "");

            return obj.get("");
        }
    }

    private static class KeysStore implements StoredKeys {

        private final byte[] global;
        private final Map<String, byte[]> keys;

        public KeysStore(byte[] global, Map<String, byte[]> keys) {
            this.global = global;
            this.keys = keys;
        }

        @Override
        public byte[] getKey(String path) {
            return keys.getOrDefault(path, global);
        }

        @Override
        public boolean isGlobal() {
            return global != null && keys.isEmpty();
        }

        @Override
        public void put(String path, byte[] key) {
            keys.put(path, key);
        }
    }


}

package com.wx.jsync.index.loader;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.index.Loader;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.util.Common;
import org.json.JSONObject;

import java.util.Optional;

import static com.wx.jsync.util.JsonUtils.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class NamedOptionsLoader<E extends Enum<E>> implements Loader<NamedOptions<E>> {


    private final Class<E> cls;

    public NamedOptionsLoader(Class<E> cls) {
        this.cls = cls;
    }

    @Override
    public Optional<NamedOptions<E>> loadOpt(JSONObject root, String[] key) {
        return getObjectOpt(root, key)
                .map(obj -> new NamedOptions<>(
                        resolveType(obj),
                        Loader.OPTIONS.load(obj)
                ));
    }

    @Override
    public void setValue(JSONObject root, NamedOptions<E> value, String[] path) {
        set(root, create(value), path);
    }

    @Override
    public void userSet(JSONObject root, ArgumentsSupplier args, String... path) {
        throw new UnsupportedOperationException();
    }

    private E resolveType(JSONObject remoteObj) {
        String typeName = getString(remoteObj, "type");

        return Common.enumCaster(cls).castOut(typeName);
    }

    public JSONObject create(NamedOptions<?> config) {
        JSONObject obj = new JSONObject();
        Options options = config.getOptions();

        Loader.OPTIONS.setValue(obj, options, "");
        set(obj, config.getType().name(), "", "type");

        return obj.getJSONObject("");
    }

}

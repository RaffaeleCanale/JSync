package com.wx.jsync.index.loader;

import com.wx.jsync.filesystem.decorator.factory.DecoratorType;
import com.wx.jsync.index.Loader;
import com.wx.jsync.index.SetLoader;
import com.wx.jsync.index.options.NamedOptions;
import org.json.JSONObject;

import static com.wx.jsync.filesystem.decorator.factory.DecoratorFactory.KEY_PATH;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.09.17.
 */
public class DecoratorSetLoader extends SetLoader<NamedOptions<DecoratorType>> {

    private static final Loader<NamedOptions<DecoratorType>> DECORATOR_LOADER = new NamedOptionsLoader<>(DecoratorType.class);

    @Override
    protected String getId(NamedOptions<DecoratorType> decorator) {
        String path = decorator.getOptions().get(KEY_PATH);
        return path == null ? "" : path;
    }

    @Override
    protected NamedOptions<DecoratorType> load(Object entry) {
        return DECORATOR_LOADER.load((JSONObject) entry);
    }

    @Override
    protected Object getValue(NamedOptions<DecoratorType> value) {
        JSONObject obj = new JSONObject();
        DECORATOR_LOADER.setValue(obj, value, "");

        return obj.get("");
    }
}

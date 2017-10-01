package com.wx.jsync.filesystem.decorator.factory;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.Options;
import com.wx.util.pair.Pair;
import com.wx.util.representables.string.EnumCasterLC;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public abstract class DecoratorFactory {

    public static final String KEY_PATH = "mask";
    private static final String KEY_NESTED_DECORATOR = "nested";


    public Pair<String, Function<FileSystem, DecoratorFileSystem>> getFactory(Index localIndex, Options options) throws IOException {
        String path = options.get(KEY_PATH);
        Options nested = options.get(KEY_NESTED_DECORATOR);

        if (path == null) {
            path = "";
        }

        Function<FileSystem, DecoratorFileSystem> fn = initDecorator(localIndex, path, options);

        if (nested != null) {
            Function<FileSystem, DecoratorFileSystem> nestedFn = getNested(localIndex, nested);
            fn = nestedFn.andThen(fn);
        }

        return Pair.of(path, fn);
    }

    private Function<FileSystem, DecoratorFileSystem> getNested(Index localIndex, Options nestedOptions) throws IOException {
        DecoratorFactory factory = new EnumCasterLC<>(DecoratorType.class).castOut(nestedOptions.get("type")).getFactory();
        return factory.getFactory(localIndex, nestedOptions).get2();
    }

    protected abstract Function<FileSystem, DecoratorFileSystem> initDecorator(Index localIndex, String path, Options options) throws IOException;

    public abstract Options getOptions(ArgumentsSupplier args);
}

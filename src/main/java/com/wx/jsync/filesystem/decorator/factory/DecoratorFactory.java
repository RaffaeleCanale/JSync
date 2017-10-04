package com.wx.jsync.filesystem.decorator.factory;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;
import com.wx.jsync.index.options.Options;
import com.wx.util.pair.Pair;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public abstract class DecoratorFactory {

    public static final String KEY_PATH = "path";

    public Pair<Function<FileSystem, DecoratorFileSystem>, String> getFactory(Options options) throws IOException {
        String path = options.get(KEY_PATH);

        if (path == null || path.isEmpty()) {
            path = "";
        }

        return Pair.of(initDecorator(options, path), path);
    }

    protected abstract Function<FileSystem, DecoratorFileSystem> initDecorator(Options options, String path) throws IOException;

    public Options getOptions(String selector, ArgumentsSupplier args) {
        if (selector != null && !selector.isEmpty()) {
            return getOptions(args).with(KEY_PATH, selector);
        }

        return getOptions(args);
    }

    protected abstract Options getOptions(ArgumentsSupplier args);
}

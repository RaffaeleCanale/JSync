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

    public static final String KEY_PREFIX = "prefix";

    public Pair<Function<FileSystem, DecoratorFileSystem>, String> getFactory(Options options) throws IOException {
        String prefix = options.get(KEY_PREFIX);

        if (prefix == null || prefix.isEmpty()) {
            prefix = "";
        }

        return Pair.of(initDecorator(options, prefix), prefix);
    }

    protected abstract Function<FileSystem, DecoratorFileSystem> initDecorator(Options options, String prefix) throws IOException;

    public Options getOptions(String prefix, ArgumentsSupplier args) {
        if (prefix != null && !prefix.isEmpty()) {
            return getOptions(args).with(KEY_PREFIX, prefix);
        }

        return getOptions(args);
    }

    protected abstract Options getOptions(ArgumentsSupplier args);
}

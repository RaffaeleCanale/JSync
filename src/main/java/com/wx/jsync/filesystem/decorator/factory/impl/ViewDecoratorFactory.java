package com.wx.jsync.filesystem.decorator.factory.impl;

import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;
import com.wx.jsync.filesystem.decorator.factory.DecoratorFactory;
import com.wx.jsync.filesystem.decorator.impl.ViewDecorator;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.Options;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
public class ViewDecoratorFactory extends DecoratorFactory {

    @Override
    protected Function<FileSystem, DecoratorFileSystem> initDecorator(Index localIndex, String path, Options options) throws IOException {
        String view = options.get("view");

        return fs -> new ViewDecorator(fs, view);
    }

    @Override
    public Options getOptions(ArgumentsSupplier args) {
        return new Options(ImmutableMap.of(
                "view", args.supplyString()
        ));
    }

}

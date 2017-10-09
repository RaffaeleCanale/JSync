//package com.wx.jsync.filesystem.decorator.factory.impl;
//
//import com.wx.action.arg.ArgumentsSupplier;
//import com.wx.jsync.Main;
//import com.wx.jsync.filesystem.FileSystem;
//import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;
//import com.wx.jsync.filesystem.decorator.factory.DecoratorFactory;
//import com.wx.jsync.filesystem.decorator.impl.ViewDecorator;
//import com.wx.jsync.index.options.Options;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.function.Function;
//
//import static com.wx.jsync.index.IndexKey.OWNER;
//
///**
// * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
// * @version 0.1 - created on 01.10.17.
// */
//public class ViewDecoratorFactory extends DecoratorFactory {
//
//    @Override
//    protected Function<FileSystem, DecoratorFileSystem> initDecorator(Options options, String prefix) throws IOException {
//        String owner = Main.getDataSets().getLocal().getIndex().get(OWNER);
//
//        return fs -> new ViewDecorator(prefix, fs, owner);
//    }
//
//    @Override
//    protected Options getOptions(ArgumentsSupplier args) {
//        return new Options(Collections.emptyMap());
//    }
//
//}

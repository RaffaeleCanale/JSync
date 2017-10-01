package com.wx.jsync.filesystem.decorator;

import com.wx.jsync.filesystem.FileSystem;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
public interface DecoratorFileSystem extends FileSystem {

    <E extends FileSystem> E getBaseFs();

}

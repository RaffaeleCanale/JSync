package com.wx.jsync.filesystem.decorator.factory;

import com.wx.jsync.filesystem.decorator.factory.impl.BackupFsFactory;
import com.wx.jsync.filesystem.decorator.factory.impl.CrypterFsFactory;
import com.wx.jsync.filesystem.decorator.factory.impl.ViewDecoratorFactory;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public enum DecoratorType {
    CRYPTER(new CrypterFsFactory()),
    BACKUP(new BackupFsFactory()),
    VIEW(new ViewDecoratorFactory());


    private final DecoratorFactory factory;

    DecoratorType(DecoratorFactory factory) {
        this.factory = factory;
    }

    public DecoratorFactory getFactory() {
        return factory;
    }
}

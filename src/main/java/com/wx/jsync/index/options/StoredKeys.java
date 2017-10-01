package com.wx.jsync.index.options;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 28.09.17.
 */
public interface StoredKeys {

    byte[] getKey(String path);

    boolean isGlobal();

    void put(String path, byte[] key);
}

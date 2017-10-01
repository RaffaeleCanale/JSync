package com.wx.jsync.index.loader;

import com.wx.jsync.index.SetLoader;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 28.09.17.
 */
public class StringSetLoader extends SetLoader<String> {

    @Override
    protected String getId(String s) {
        return s;
    }

    @Override
    protected String load(Object entry) {
        return (String) entry;
    }

    @Override
    protected Object getValue(String value) {
        return value;
    }
}

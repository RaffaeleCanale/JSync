package com.wx.jsync.index.loader;

import com.wx.action.arg.ArgumentsSupplier;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 28.09.17.
 */
public class StringListLoader extends ListLoader<String> {

    @Override
    protected Object getUserValue(ArgumentsSupplier args) {
        return args.supplyString();
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

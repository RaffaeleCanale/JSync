package com.wx.jsync.index.options;

import static com.wx.util.collections.CollectionsUtil.safe;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class NamedOptions<E extends Enum<E>> {

    private final E type;
    private final Options options;

    public NamedOptions(E type, Options options) {
        this.type = type;
        this.options = options;
    }

    public E getType() {
        return type;
    }

    public Options getOptions() {
        return options;
    }
}

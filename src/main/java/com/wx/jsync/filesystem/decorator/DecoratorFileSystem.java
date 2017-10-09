package com.wx.jsync.filesystem.decorator;

import com.wx.jsync.filesystem.FileSystem;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
public abstract class DecoratorFileSystem implements FileSystem {

    private final FileSystem baseFs;
    private final String prefix;

    public DecoratorFileSystem(FileSystem baseFs, String prefix) {
        this.baseFs = baseFs;
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public <E extends FileSystem> E getBaseFs() {
        return (E) baseFs;
    }

    protected abstract String getUserPath(String realPath);

    public final String resolveUserPath(String realPath) {
        checkPath(realPath);

        FileSystem baseFs = getBaseFs();

        if (baseFs instanceof DecoratorFileSystem) {
            realPath = ((DecoratorFileSystem) baseFs).resolveUserPath(realPath);
        }

        return getUserPath(realPath);
    }

    protected final String checkPath(String path) {
        if (!path.startsWith(prefix)) {
            throw new AssertionError();
        }

        return path;
    }
}

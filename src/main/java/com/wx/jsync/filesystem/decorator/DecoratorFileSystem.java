package com.wx.jsync.filesystem.decorator;

import com.wx.jsync.filesystem.FileSystem;

import java.util.Optional;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
public abstract class DecoratorFileSystem implements FileSystem {

    private final String path;

    public DecoratorFileSystem(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public abstract <E extends FileSystem> E getBaseFs();

    protected abstract Optional<String> getUserPath(String realPath);

    private Optional<String> resolveUserPath(String realPath) {
        FileSystem baseFs = getBaseFs();

        if (baseFs instanceof DecoratorFileSystem) {
            return ((DecoratorFileSystem) baseFs).resolveUserPath(realPath)
                    .flatMap(this::getUserPath);
        }

        return this.getUserPath(realPath);
    }

    public final Optional<String> resolvePath(String realPath) {
        Optional<String> userPath = resolveUserPath(realPath);

        if (userPath.isPresent() && userPath.get().startsWith(getPath())) {
            return userPath;
        }

        return Optional.empty();
    }

}

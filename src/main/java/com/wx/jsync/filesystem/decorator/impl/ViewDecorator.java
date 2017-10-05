package com.wx.jsync.filesystem.decorator.impl;

import com.wx.jsync.filesystem.FileSystem;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
public class ViewDecorator extends AbstractRenameDecorator {

    private final String viewDirectory;


    public ViewDecorator(String path, FileSystem fs, String viewDirectory) {
        super(path, fs);
        this.viewDirectory = viewDirectory + "/";
    }

    @Override
    public String toString() {
        return "View(" + viewDirectory + ")[" + getBaseFs() + "]";
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        return getBaseFs().getAllFiles().stream()
                .filter(p -> p.startsWith(viewDirectory))
                .map(this::userPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    protected String userPath(String filename) {
        if (!filename.startsWith(viewDirectory)) {
            return null;
        }

        return filename.substring(viewDirectory.length());
    }

    @Override
    protected String realPath(String filename) {
        return viewDirectory + filename;
    }
}

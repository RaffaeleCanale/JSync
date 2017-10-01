package com.wx.jsync.filesystem.decorator.impl;

import com.wx.jsync.filesystem.FileSystem;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
public class ViewDecorator extends AbstractRenameDecorator {

    private final String viewDirectory;


    public ViewDecorator(FileSystem fs, String viewDirectory) {
        super(fs);
        this.viewDirectory = viewDirectory;
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        return super.getAllFiles().stream()
                .filter(p -> p.startsWith(viewDirectory))
                .collect(Collectors.toList());
    }

    @Override
    protected String userPath(String filename) {
        if (!filename.startsWith(viewDirectory)) {
            throw new RuntimeException("Expected " + filename + " to start with " + filename);
        }

        return filename.substring(viewDirectory.length());
    }

    @Override
    protected String realPath(String filename) {
        return viewDirectory + filename;
    }
}

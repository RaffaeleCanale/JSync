package com.wx.jsync.filesystem.decorator.impl;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wx.jsync.Constants.INDEX_FILE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
public abstract class AbstractRenameDecorator extends DecoratorFileSystem {


    public AbstractRenameDecorator(FileSystem baseFs, String prefix) {
        super(baseFs, prefix);
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        return getBaseFs().getFileStat(realPath0(filename));
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        return getBaseFs().getAllFiles().stream()
                .map(this::userPath0)
                .collect(Collectors.toList());
    }

    @Override
    public InputStream read(String filename) throws IOException {
        return getBaseFs().read(realPath0(filename));
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        getBaseFs().write(realPath0(filename), input);
    }

    @Override
    public void remove(String filename) throws IOException {
        getBaseFs().remove(realPath0(filename));
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        getBaseFs().move(realPath0(filename), realPath0(destination));
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return getBaseFs().exists(realPath0(filename));
    }

    @Override
    protected String getUserPath(String realPath) {
        return userPath0(realPath);
    }

    private String userPath0(String filename) {
        checkPath(filename);
        if (filename.equals(INDEX_FILE)) {
            return filename;
        }

        return checkPath(userPath(filename));
    }

    private String realPath0(String filename) {
        checkPath(filename);
        if (filename.equals(INDEX_FILE)) {
            return filename;
        }

        return checkPath(realPath(filename));
    }

    protected abstract String userPath(String filename);

    protected abstract String realPath(String filename);

}

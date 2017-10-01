package com.wx.jsync.filesystem.decorator.impl;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.wx.jsync.Constants.INDEX_FILE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
public abstract class AbstractRenameDecorator implements DecoratorFileSystem {

    private final FileSystem fs;

    public AbstractRenameDecorator(FileSystem fs) {
        this.fs = fs;
    }

    @Override
    public <E extends FileSystem> E getBaseFs() {
        return (E) fs;
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        return fs.getFileStat(realPath0(filename));
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        return fs.getAllFiles().stream()
                .map(this::userPath0)
                .collect(Collectors.toList());
    }

    @Override
    public InputStream read(String filename) throws IOException {
        return fs.read(realPath0(filename));
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        fs.write(realPath0(filename), input);
    }

    @Override
    public void remove(String filename) throws IOException {
        fs.remove(realPath0(filename));
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        fs.move(realPath0(filename), realPath0(destination));
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return fs.exists(realPath0(filename));
    }

    private String userPath0(String filename) {
        if (filename.equals(INDEX_FILE)) {
            return filename;
        }

        return userPath(filename);
    }

    private String realPath0(String filename) {
        if (filename.equals(INDEX_FILE)) {
            return filename;
        }

        return realPath(filename);
    }

    protected abstract String userPath(String filename);

    protected abstract String realPath(String filename);
}

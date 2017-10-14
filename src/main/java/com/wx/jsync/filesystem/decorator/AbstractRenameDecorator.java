package com.wx.jsync.filesystem.decorator;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.wx.jsync.Constants.INDEX_FILE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.10.17.
 */
abstract class AbstractRenameDecorator implements FileSystem {

    private final FileSystem baseFs;

    AbstractRenameDecorator(FileSystem baseFs) {
        this.baseFs = baseFs;
    }

    public FileSystem getBaseFs() {
        return baseFs.getBase();
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        return baseFs.getFileStat(realPath0(filename));
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        return baseFs.getAllFiles().stream()
                .map(this::userPath0)
                .collect(Collectors.toList());
    }

    @Override
    public InputStream read(String filename) throws IOException {
        return baseFs.read(realPath0(filename));
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        baseFs.write(realPath0(filename), input);
    }

    @Override
    public void remove(String filename) throws IOException {
        baseFs.remove(realPath0(filename));
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        baseFs.move(realPath0(filename), realPath0(destination));
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return baseFs.exists(realPath0(filename));
    }

    @Override
    public String toString() {
        return baseFs.toString();
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

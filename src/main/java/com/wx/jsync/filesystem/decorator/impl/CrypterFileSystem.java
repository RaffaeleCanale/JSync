package com.wx.jsync.filesystem.decorator.impl;

import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.wx.jsync.Constants.ENCRYPTED_EXTENSION;
import static com.wx.jsync.Constants.INDEX_FILE;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class CrypterFileSystem implements DecoratorFileSystem {

    private final FileSystem fs;
    private final Crypter crypter;

    public CrypterFileSystem(FileSystem fs, Crypter crypter) {
        this.fs = fs;
        this.crypter = crypter;
    }

    @Override
    public <E extends FileSystem> E getBaseFs() {
        return (E) fs;
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        return fs.getFileStat(realPath(filename));
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        return fs.getAllFiles().stream()
                .map(this::userPath)
                .collect(Collectors.toList());
    }

    @Override
    public InputStream read(String filename) throws IOException {
        InputStream in = fs.read(realPath(filename));

        if (useEncryption(filename)) {
            try {
                return crypter.getInputStream(in, false);
            } catch (CryptoException e) {
                throw new IOException(e);
            }

        } else {
            return in;
        }
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        if (useEncryption(filename)) {
            try {
                input = crypter.getInputStream(input, true);
            } catch (CryptoException e) {
                throw new IOException(e);
            }
        }

        fs.write(realPath(filename), input);
    }

    @Override
    public void remove(String filename) throws IOException {
        fs.remove(realPath(filename));
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        fs.move(realPath(filename), realPath(destination));
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return fs.exists(realPath(filename));
    }

    private String realPath(String userPath) {
        if (useEncryption(userPath)) {
            return userPath + ENCRYPTED_EXTENSION;
        }

        return userPath;
    }

    private String userPath(String realPath) {
        if (useEncryption(realPath)) {
            return realPath.substring(0, realPath.length() - ENCRYPTED_EXTENSION.length());
        }

        return realPath;
    }

    private boolean useEncryption(String path) {
        return !path.equals(INDEX_FILE);
    }
}

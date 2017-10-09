package com.wx.jsync.filesystem.decorator.impl;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static com.wx.jsync.Constants.INDEX_FILE;
import static com.wx.util.Format.formatDate;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.09.17.
 */
public class BackupFileSystem extends DecoratorFileSystem {

    private final String backupPath;

    public BackupFileSystem(FileSystem baseFs, String prefix, String backupPath) {
        super(baseFs, prefix);
        this.backupPath = backupPath;
    }

    @Override
    public String toString() {
        return "Backup[" + getBaseFs() + "]";
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        return getBaseFs().getFileStat(filename);
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        return getBaseFs().getAllFiles();
    }

    @Override
    public InputStream read(String filename) throws IOException {
        return getBaseFs().read(filename);
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        backup(filename);
        getBaseFs().write(filename, input);
    }

    private void backup(String filename) throws IOException {
        if (!filename.equals(INDEX_FILE) && getBaseFs().exists(filename)) {
            getBaseFs().move(filename, getBackupPath(filename));
        }
    }

    @Override
    public void remove(String filename) throws IOException {
        backup(filename);
        getBaseFs().remove(filename);
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        getBaseFs().move(filename, destination);
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return getBaseFs().exists(filename);
    }

    @Override
    protected String getUserPath(String realPath) {
        return realPath;
    }

    private String getBackupPath(String path) {
        String parent = backupPath + path.replace('/', '_') + "/";
        String name = path;

        int lastSep = name.lastIndexOf('/');
        if (lastSep >= 0) {
            name = name.substring(lastSep + 1);
        }

        return parent + formatDate(new Date().getTime(), "yyyy.MM.dd HH:mm:ss") + " " + name;
    }
}

package com.wx.jsync.filesystem.decorator;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import static com.wx.jsync.Constants.INDEX_FILE;
import static com.wx.util.Format.formatDate;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 30.09.17.
 */
public class BackupFileSystem implements FileSystem {

    private final FileSystem baseFs;
    private final String backupPath;

    public BackupFileSystem(FileSystem baseFs, String backupPath) {
        this.baseFs = baseFs;
        this.backupPath = backupPath;
    }

    @Override
    public FileSystem getBase() {
        return baseFs.getBase();
    }

    @Override
    public String toString() {
        return "Backup[" + baseFs + "]";
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        return baseFs.getFileStat(filename);
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        return baseFs.getAllFiles();
    }

    @Override
    public InputStream read(String filename) throws IOException {
        return baseFs.read(filename);
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        backup(filename);
        baseFs.write(filename, input);
    }

    private void backup(String filename) throws IOException {
        if (!filename.equals(INDEX_FILE) && baseFs.exists(filename)) {
            baseFs.move(filename, getBackupPath(filename));
        }
    }

    @Override
    public void remove(String filename) throws IOException {
        backup(filename);
        baseFs.remove(filename);
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        baseFs.move(filename, destination);
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return baseFs.exists(filename);
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

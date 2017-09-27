package com.wx.jsync.filesystem.impl;

import com.wx.io.Accessor;
import com.wx.io.file.FileUtil;
import com.wx.jsync.filesystem.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.wx.jsync.Constants.TRANSFER_BUFFER_SIZE;


/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class LocalFileSystem implements FileSystem {

    private final File directory;

    public LocalFileSystem(File directory) {
        this.directory = directory;
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        return FileStat.compute(getFile(filename));
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        List<String> result = new ArrayList<>();

        getDataSetFiles(directory, result);

        return result;
    }

    private void getDataSetFiles(File directory, List<String> files) {
        File[] children = directory.listFiles();

        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    getDataSetFiles(child, files);
                } else {
                    files.add(relative(child));
                }
            }
        }
    }

    @Override
    public InputStream read(String filename) throws IOException {
        return new BufferedInputStream(new FileInputStream(getFile(filename)));
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        File file = getFile(filename);

        createParent(file);

        try (Accessor accessor = new Accessor()
                .setIn(input)
                .setOut(file)) {
            accessor.pourInOut(TRANSFER_BUFFER_SIZE);
        }
    }

    @Override
    public void remove(String filename) throws IOException {
        if (!getFile(filename).delete()) {
            throw new IOException("Failed to delete " + filename);
        }
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        File destinationFile = getFile(destination);
        createParent(destinationFile);

        if (!getFile(filename).renameTo(destinationFile)) {
            throw new IOException("Cannot rename " + filename + " to " + destination);
        }
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return getFile(filename).exists();
    }


    public File getFile(String filename) {
        return new File(directory, filename);
    }

    private String relative(File file) {
        return directory.toURI().relativize(file.toURI()).getPath();
    }

    private void createParent(File file) throws IOException {
        FileUtil.autoCreateDirectories(file.getParentFile());
    }
}

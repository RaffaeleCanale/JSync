package com.wx.jsync.filesystem.base;

import com.google.api.services.drive.model.File;
import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.util.extensions.google.DriveServiceHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.wx.jsync.util.extensions.google.GDriveUtils.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
public class GDriveFileSystem implements FileSystem {

    private final DriveServiceHelper drive;
    private final String rootId;

    private final Map<String, FileStat> statBuffer = new HashMap<>();
    private final Map<String, String> idBuffer = new HashMap<>();
    private boolean filesListLoaded = false;

    public GDriveFileSystem(DriveServiceHelper drive, String rootId) {
        this.drive = drive;
        this.rootId = rootId;
    }

    @Override
    public String toString() {
        return "GDrive(" + rootId + ")";
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        ensureHasList();

        FileStat stat = statBuffer.get(filename);
        if (stat == null) {
            throw new FileNotFoundException(filename);
        }

        return stat;
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        ensureHasList();

        return statBuffer.keySet();
    }

    @Override
    public InputStream read(String filename) throws IOException {
        ensureHasList();

        String id = idBuffer.get(filename);
        if (id == null) {
            throw new FileNotFoundException(filename);
        }

        return drive.downloadFile(id);
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        ensureHasList();

        String id = idBuffer.get(filename);
        File file;
        if (id == null) {
            Path path = new Path(filename);
            String parentId = mkdirs(path.getParent());

            file = drive.createFile(parentId, path.getName(), input);
        } else {
            file = drive.updateFile(id, input);
        }

        idBuffer.put(filename, file.getId());
        statBuffer.put(filename, getStat(file));
    }

    @Override
    public void remove(String filename) throws IOException {
        ensureHasList();

        String id = idBuffer.get(filename);
        if (id == null) {
            throw new FileNotFoundException(filename);
        }

        drive.removeFile(id);

        statBuffer.remove(filename);
        idBuffer.remove(filename);
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        ensureHasList();

        String id = idBuffer.get(filename);
        if (id == null) {
            throw new FileNotFoundException(filename);
        }

        Path path = new Path(destination);
        String parentId = mkdirs(path.getParent());

        idBuffer.put(path.getParent(), parentId);

        drive.moveFile(id, parentId, path.getName());
    }

    @Override
    public boolean exists(String filename) throws IOException {
        ensureHasList();

        return statBuffer.containsKey(filename);
    }

    private void ensureHasList() throws IOException {
        if (filesListLoaded) {
            return;
        }
        statBuffer.clear();

        List<File> files = new ArrayList<>();
        listFilesRec(rootId, files);

        PathConstructor pathConstructor = new PathConstructor(rootId, files);

        for (File file : files) {
            String path = pathConstructor.getPath(file);

            idBuffer.put(path, file.getId());
            if (!isDirectory(file)) {
                statBuffer.put(path, getStat(file));
            }
        }

        filesListLoaded = true;
    }

    private void listFilesRec(String parentId, Collection<File> result) throws IOException {
        List<File> files = drive.listFiles(parentId);

        result.addAll(files);

        for (File file : files) {
            if (isDirectory(file)) {
                listFilesRec(file.getId(), result);
            }
        }
    }


    private String mkdirs(String filename) throws IOException {
        if (filename == null) {
            return rootId;
        }

        String id = idBuffer.get(filename);
        if (id != null) {
            return id;
        }

        Path path = new Path(filename);
        String parentId = mkdirs(path.getParent());

        File dir = drive.createDirectory(parentId, path.getName());
        idBuffer.put(filename, dir.getId());

        return dir.getId();
    }

}

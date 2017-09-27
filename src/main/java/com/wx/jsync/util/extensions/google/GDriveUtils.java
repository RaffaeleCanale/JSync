package com.wx.jsync.util.extensions.google;

import com.google.api.services.drive.model.File;
import com.wx.jsync.filesystem.FileStat;
import com.wx.util.representables.string.EncodedBytesRepr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 25.09.17.
 */
public class GDriveUtils {

    private static String getParent(File file) throws IOException {
        List<String> parentsId = file.getParents();

        if (parentsId.size() != 1) {
            throw new IOException("Files with multiple parents are not supported: " + file);
        }

        return parentsId.get(0);
    }

    public static boolean isDirectory(File file) {
        return file.getSize() == null;
    }

    public static FileStat getStat(File file) {
        return FileStat.create(
                file.getModifiedTime().getValue(),
                file.getSize(),
                new EncodedBytesRepr().castOut(file.getMd5Checksum())
        );
    }

    public static Optional<File> findFileByPath(DriveServiceHelper drive, String filename) throws IOException {
        String[] path = filename.split("/");

        if (path.length == 0) {
            throw new IllegalArgumentException();
        }

        File parent = new File().setId("root");
        for (String dir : path) {
            Optional<File> child = drive.getFile(parent.getId(), dir);
            if (!child.isPresent()) {
                return Optional.empty();
            }

            parent = child.get();
        }

        return Optional.of(parent);
    }

    public static class PathConstructor {

        private final String rootId;
        private final Map<String, File> idToFile;
        private final Map<String, String> idToFullPath = new HashMap<>();

        public PathConstructor(String rootId, Collection<File> files) {
            idToFile = files.stream().collect(
                    Collectors.toMap(File::getId, Function.identity())
            );
            this.rootId = rootId;
        }

        public String getPath(File file) throws IOException {
            String path = idToFullPath.get(file.getId());
            if (path == null) {
                path = compute(file);
                idToFullPath.put(file.getId(), path);
            }

            return path;
        }

        private String compute(File file) throws IOException {
            String parentId = getParent(file);
            if (parentId.equals(rootId)) {
                return file.getName();
            }

            File parent = idToFile.get(parentId);
            if (parent == null) {
                throw new FileNotFoundException(parentId);
            }

            String parentPath = getPath(parent);

            return parentPath + "/" + file.getName();
        }
    }

    public static class Path {

        private final String parent;
        private final String name;

        public Path(String path) {
            int i = path.lastIndexOf('/');

            if (i < 0) {
                name = path;
                parent = null;
            } else {
                if (i == 0) throw new IllegalArgumentException();

                name = path.substring(i + 1);
                parent = path.substring(0, i);
            }
        }

        public String getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return parent + " / " + name;
        }
    }

    private GDriveUtils() {}

}

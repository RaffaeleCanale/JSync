package com.wx.jsync.filesystem;

import java.io.*;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public interface FileSystem {

    FileStat getFileStat(String filename) throws IOException;

    default Collection<String> getAllFiles(Predicate<String> acceptPredicate) throws IOException {
        return getAllFiles().stream()
                .filter(acceptPredicate)
                .collect(Collectors.toList());
    };

    Collection<String> getAllFiles() throws IOException;

    InputStream read(String filename) throws IOException;

    void write(String filename, InputStream input) throws IOException;

    void remove(String filename) throws IOException;

    void move(String filename, String destination) throws IOException;

    boolean exists(String filename) throws IOException;

    default FileSystem getBase() {
        return this;
    }

}

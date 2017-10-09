package com.wx.jsync.filesystem;

import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public class MultiFileSystem implements FileSystem {

    private final BaseFsWithView baseFs;
    private final SortedMap<String, DecoratorFileSystem> decorators = new TreeMap<>(Comparator.comparingInt(String::length).reversed());

    public MultiFileSystem(FileSystem baseFs) {
        this.baseFs = new BaseFsWithView(baseFs);
    }

    public void addDecorator(Function<FileSystem, DecoratorFileSystem> factory, String prefix) {
        FileSystem parentFs = resolveFs(prefix);

        decorators.put(prefix, factory.apply(parentFs));
    }

    @Override
    public FileStat getFileStat(String filename) throws IOException {
        return resolveFs(filename).getFileStat(filename);
    }

    @Override
    public Collection<String> getAllFiles() throws IOException {
        Collection<String> allFiles = getBaseFs().getAllFiles();

        Map<String, Collection<String>> decoratorFiles = new HashMap<>();
        Collection<String> baseFiles = new ArrayList<>();

        for (String file : allFiles) {
            FileSystem fs = resolveFs(file);

            if (fs == baseFs) {
                baseFiles.add(file);
            } else {
                decoratorFiles
                        .computeIfAbsent(((DecoratorFileSystem) fs).getPrefix(), k -> new ArrayList<>())
                        .add(file);
            }
        }


        for (String decoratorPath : decoratorFiles.keySet()) {
            Collection<String> files = decoratorFiles.get(decoratorPath);
            DecoratorFileSystem decorator = decorators.get(decoratorPath);

            baseFs.setView(files);
            decoratorFiles.put(decoratorPath, decorator.getAllFiles());
            baseFs.setView(null);
        }

        decoratorFiles.values().forEach(baseFiles::addAll);

        return baseFiles;
    }

    @Override
    public InputStream read(String filename) throws IOException {
        return resolveFs(filename).read(filename);
    }

    @Override
    public void write(String filename, InputStream input) throws IOException {
        resolveFs(filename).write(filename, input);
    }

    @Override
    public void remove(String filename) throws IOException {
        resolveFs(filename).remove(filename);
    }

    @Override
    public void move(String filename, String destination) throws IOException {
        FileSystem sourceFs = resolveFs(filename);
        FileSystem destinationFs = resolveFs(destination);

        if (sourceFs == destinationFs) {
            sourceFs.move(filename, destination);
        } else {
            destinationFs.write(destination, sourceFs.read(filename));
            sourceFs.remove(filename);
        }
    }

    @Override
    public boolean exists(String filename) throws IOException {
        return resolveFs(filename).exists(filename);
    }

    public FileSystem resolveFs(String filename) {
        for (String prefix : decorators.keySet()) {
            if (filename.startsWith(prefix)) {
                return decorators.get(prefix);
            }
        }

        return baseFs;
    }

    public FileSystem getBaseFs() {
        return baseFs.fs;
    }

    private static class BaseFsWithView implements FileSystem {

        private final FileSystem fs;
        private Collection<String> view;

        public BaseFsWithView(FileSystem fs) {
            this.fs = fs;
        }

        @Override
        public String toString() {
            return fs.toString();
        }

        public void setView(Collection<String> view) {
            this.view = view;
        }

        @Override
        public Collection<String> getAllFiles() throws IOException {
            return Objects.requireNonNull(view);
        }

        @Override
        public FileStat getFileStat(String filename) throws IOException {
            return fs.getFileStat(filename);
        }

        @Override
        public InputStream read(String filename) throws IOException {
            return fs.read(filename);
        }

        @Override
        public void write(String filename, InputStream input) throws IOException {
            fs.write(filename, input);
        }

        @Override
        public void remove(String filename) throws IOException {
            fs.remove(filename);
        }

        @Override
        public void move(String filename, String destination) throws IOException {
            fs.move(filename, destination);
        }

        @Override
        public boolean exists(String filename) throws IOException {
            return fs.exists(filename);
        }
    }
}

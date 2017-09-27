package com.wx.jsync.filesystem;


import com.wx.io.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class FileStat {

    public static final FileStat REMOVED = new FileStat();

    public static FileStat compute(File file) throws IOException {
        return new FileStat(
                file.lastModified(),
                file.length(),
                FileUtil.checkSum(file)
        );
    }

    public static FileStat create(long timestamp, long fileSize, byte[] checksum) {
        return new FileStat(timestamp, fileSize, checksum);
    }

    private final long timestamp;
    private final long fileSize;
    private final byte[] checksum;

    private FileStat() {
        this.timestamp = 0L;
        this.fileSize = 0L;
        this.checksum = null;
    }

    private FileStat(long timestamp, long fileSize, byte[] checksum) {
        this.timestamp = timestamp;
        this.fileSize = fileSize;
        this.checksum = requireNonNull(checksum);
    }

    public boolean isRemoved() {
        return this == REMOVED;
    }

    public long getTimestamp() {
        checkNotRemoved();

        return timestamp;
    }

    public long getFileSize() {
        checkNotRemoved();

        return fileSize;
    }

    public byte[] getChecksum() {
        checkNotRemoved();

        return checksum;
    }


    public boolean matches(FileStat stat) {
        if (isRemoved() && stat.isRemoved()) {
            return true;
        }

        if (this.fileSize != stat.fileSize) {
            return false;
        }

        if (this.checksum != null && stat.checksum != null) {
            return Arrays.equals(this.checksum, stat.checksum);
        }

        return this.timestamp != stat.timestamp;
    }

    private void checkNotRemoved() {
        if (isRemoved()) {
            throw new NoSuchElementException("This property is not available for removed files");
        }
    }
}

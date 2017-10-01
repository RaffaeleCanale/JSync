package com.wx.jsync;

public class Constants {
    public static final String CONFIG_DIR = ".jsync/";
    public static final String DEFAULT_BACKUP_DIR = CONFIG_DIR + "backup/";
    public static final String INDEX_FILE = CONFIG_DIR + "index.json";
    public static final double VERSION_INCREMENT_DELTA = 0.1;
    public static final String APPLICATION_NAME = "JSync";


    public static final boolean DEFAULT_ENABLE_BUMP = true;
    public static final boolean DEFAULT_USE_BACKUP = false;
    public static final boolean DEFAULT_ASK_CONFIRMATION = true;
    public static final boolean DEFAULT_STORE_KEY = true;

    public static final byte[] SALT = "b^7i+FH{;J`F%rz^VYcfX}~N+w|P^=3;oDc7=o>d0-3|p|g8enW/``MDG/ R.}U{".getBytes();
    public static final String ENCRYPTED_EXTENSION = ".wxc";
    public static final int TRANSFER_BUFFER_SIZE = 10 << 20;

}
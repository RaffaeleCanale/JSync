package com.wx.jsync;

import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.crypto.cipher.AESCrypter;
import com.wx.io.Accessor;
import com.wx.io.file.FileUtil;
import com.wx.jsync.index.Loader;
import com.wx.jsync.index.options.StoredKeys;
import com.wx.jsync.util.DesktopUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Optional;

import static com.wx.jsync.Constants.GLOBAL_CONFIG_DIR;
import static com.wx.jsync.Constants.SALT;
import static com.wx.jsync.util.JsonUtils.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 05.10.17.
 */
public class GlobalConfig {

    private static final File GLOBAL_DIR = new File(GLOBAL_CONFIG_DIR);
    private static JSONObject config;

    private static File syncDirectory;

    public static void setSyncDirectory(File syncDirectory) {
        GlobalConfig.syncDirectory = syncDirectory;
    }

    public static File getSyncDirectory() {
        return syncDirectory;
    }

    public static File getGoogleDir() {
        return new File(GLOBAL_DIR, "google");
    }

    public static Optional<byte[]> getStoredKey() throws IOException {
        String path = getKeyPath();

        StoredKeys storedKeys = Loader.STORED_KEYS.load(getConfig(), "stored_keys");
        return Optional.ofNullable(storedKeys.getKey(path));
    }

    private static String getKeyPath() throws IOException {
        return syncDirectory.getAbsolutePath();
    }

    public static void storeKey(byte[] key) throws IOException {
        Boolean enabledStoredKeys = getBooleanOpt(config, "enabled_stored_keys").orElse(false);
        if (!enabledStoredKeys) {
            return;
        }

        String keyPath = getKeyPath();
        StoredKeys storedKeys = Loader.STORED_KEYS.load(getConfig(), "stored_keys");

        storedKeys.put(keyPath, key);
        Loader.STORED_KEYS.setValue(config, storedKeys, "stored_keys");
        saveConfig();
    }

    public static InputStream getClientSecret() throws IOException {
        InputStream plainSecret = GlobalConfig.class.getResourceAsStream("/google/client_secret.json");
        if (plainSecret != null) {
            return plainSecret;
        }

        InputStream encryptedSecret = GlobalConfig.class.getResourceAsStream("/google/client_secret.json.wxc");
        try {
            return loadSystemCrypter().getInputStream(encryptedSecret, false);
        } catch (CryptoException e) {
            throw new IOException(e);
        }
    }

    private static Crypter loadSystemCrypter() throws IOException, CryptoException {
        JSONObject config = getConfig();
        Optional<byte[]> key = getBytesOpt(config, "system_key");
        Crypter crypter = new AESCrypter();

        if (key.isPresent()) {
            crypter.initKey(key.get());
        } else {
            char[] password = Main.IN.readPassword("Input system password: ");
            crypter.generateKey(password, SALT);

            set(config, crypter.getKey(), "system_key");
            saveConfig();
        }

        return crypter;
    }

    private static JSONObject getConfig() throws IOException {
        if (config == null) {
            File configFile = new File(GLOBAL_DIR, "config.json");
            if (configFile.isFile()) {
                config = new JSONObject(new JSONTokener(new FileInputStream(configFile)));
            } else {
                config = new JSONObject();
            }
        }

        return config;
    }

    private static void saveConfig() throws IOException {
        JSONObject config = getConfig();

        File configFile = new File(GLOBAL_DIR, "config.json");
        if (!configFile.isFile()) {
            FileUtil.autoCreateDirectories(configFile.getParentFile());
        }

        ByteArrayInputStream in = new ByteArrayInputStream(config.toString(4).getBytes("UTF-8"));
        try (Accessor accessor = new Accessor()
                .setIn(in)
                .setOut(configFile)) {
            accessor.pourInOut();
        }
    }

    public static String getUser() {
        try {
            return getStringOpt(getConfig(), "user").orElse(DesktopUtils.getHostName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.wx.jsync.index.impl;

import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.crypto.CryptoUtil;
import org.json.JSONObject;

import java.util.Optional;

import static com.wx.jsync.Constants.DEFAULT_STORE_KEY;
import static com.wx.jsync.Constants.SALT;
import static com.wx.jsync.Main.IN;
import static com.wx.jsync.util.JsonUtils.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 24.09.17.
 */
class CrypterLoader {

    static void generateKey(JSONObject root, Crypter crypter) throws CryptoException {
        Optional<byte[]> storedKey = getBytesOpt(root, "remote", "key");

        if (storedKey.isPresent()) {
            crypter.initKey(storedKey.get());
        } else {
            char[] password = IN.readPassword("Input the remote password: ");
            crypter.generateKey(password, SALT);

            if (storeKeyEnabled(root)) {
                set(root, crypter.getKey(), "remote", "key");
            }
        }

    }

    private static boolean storeKeyEnabled(JSONObject root) {
        return getBooleanOpt(root, "options", "executor", "storeKeyEnabled").orElse(DEFAULT_STORE_KEY);
    }



    static Optional<Crypter> getCrypter(JSONObject root) {
        return getStringOpt(root, "local", "encryption", "algorithm")
                .map(CrypterLoader::getCrypterFromNameSafe);
    }

    private static Crypter getCrypterFromNameSafe(String algorithm) {
        Crypter crypter = CryptoUtil.getCrypterFromName(algorithm);
        if (crypter == null) {
            throw new IllegalArgumentException("No crypter found for algorithm: " + algorithm);
        }

        return crypter;
    }

    private CrypterLoader() {}
}

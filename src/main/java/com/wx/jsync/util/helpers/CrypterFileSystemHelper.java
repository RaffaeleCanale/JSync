package com.wx.jsync.util.helpers;

import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.crypto.CryptoUtil;
import com.wx.jsync.GlobalConfig;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.CrypterFileSystem;

import java.io.IOException;
import java.util.Optional;

import static com.wx.jsync.Constants.SALT;
import static com.wx.jsync.Main.IN;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public class CrypterFileSystemHelper {


    public static FileSystem initDecorator(String algorithm, FileSystem baseFs) throws IOException {
        try {
            Crypter crypter = getCrypter(algorithm);
            Optional<byte[]> key = GlobalConfig.getStoredKey();

            if (key.isPresent()) {
                crypter.initKey(key.get());
            } else {
                char[] password = IN.readPassword("Input the remote password: ");
                crypter.generateKey(password, SALT);
                GlobalConfig.storeKey(crypter.getKey());
            }

            return new CrypterFileSystem(baseFs, crypter);
        } catch (CryptoException e) {
            throw new IOException(e);
        }
    }


    private static Crypter getCrypter(String algorithm) throws IOException {
        Crypter crypter = CryptoUtil.getCrypterFromName(algorithm);

        if (crypter == null) {
            throw new IOException("Invalid algorithm: " + algorithm);
        }
        return crypter;
    }


    private CrypterFileSystemHelper() {}

}

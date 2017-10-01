package com.wx.jsync.filesystem.decorator.factory.impl;

import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.crypto.CryptoUtil;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;
import com.wx.jsync.filesystem.decorator.factory.DecoratorFactory;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.impl.CrypterFileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.index.options.StoredKeys;

import java.io.IOException;
import java.util.function.Function;

import static com.wx.jsync.Constants.SALT;
import static com.wx.jsync.Main.IN;
import static com.wx.jsync.index.IndexKey.ENABLE_STORE_KEY;
import static com.wx.jsync.index.IndexKey.STORED_KEY;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.09.17.
 */
public class CrypterFsFactory extends DecoratorFactory {

    private static final String KEY_ALGORITHM = "algorithm";

    @Override
    public Function<FileSystem, DecoratorFileSystem> initDecorator(Index localIndex, String path, Options options) throws IOException {
        try {
            Crypter crypter = getCrypter(options);
            StoredKeys keys = localIndex.get(STORED_KEY);

            byte[] key = keys.getKey(path);

            if (key != null) {
                crypter.initKey(key);
            } else {
                char[] password = IN.readPassword("Input the remote password for " + path + ": ");
                crypter.generateKey(password, SALT);

                if (localIndex.get(ENABLE_STORE_KEY)) {
                    keys.put(path, crypter.getKey());
                    localIndex.set(STORED_KEY, keys);
                }
            }

            return fs -> new CrypterFileSystem(fs, crypter);
        } catch (CryptoException e) {
            throw new IOException(e);
        }
    }

    private Crypter getCrypter(Options options) throws IOException {
        String algorithmName = options.get(KEY_ALGORITHM);
        Crypter crypter = CryptoUtil.getCrypterFromName(algorithmName);

        if (crypter == null) {
            throw new IOException("Missing option for the algorithm name");
        }
        return crypter;
    }


}

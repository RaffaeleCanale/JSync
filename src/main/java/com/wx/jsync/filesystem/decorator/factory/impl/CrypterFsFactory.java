package com.wx.jsync.filesystem.decorator.factory.impl;

import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.crypto.Crypter;
import com.wx.crypto.CryptoException;
import com.wx.crypto.CryptoUtil;
import com.wx.crypto.cipher.AESCrypter;
import com.wx.jsync.Main;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.DecoratorFileSystem;
import com.wx.jsync.filesystem.decorator.factory.DecoratorFactory;
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
    protected Function<FileSystem, DecoratorFileSystem> initDecorator(Options options, String prefix) throws IOException {
        try {
            Index localIndex = Main.getDataSets().getLocal().getIndex();
            Crypter crypter = getCrypter(options);
            StoredKeys keys = localIndex.get(STORED_KEY);

            byte[] key = keys.getKey(prefix);

            if (key != null) {
                crypter.initKey(key);
            } else {
                char[] password = IN.readPassword("Input the remote password for " + prefix + ": ");
                crypter.generateKey(password, SALT);

                if (localIndex.get(ENABLE_STORE_KEY)) {
                    keys.put(prefix, crypter.getKey());
                    localIndex.set(STORED_KEY, keys);
                }
            }

            return fs -> new CrypterFileSystem(fs, prefix, crypter);
        } catch (CryptoException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected Options getOptions(ArgumentsSupplier args) {
        return new Options(ImmutableMap.of(
                KEY_ALGORITHM, args.hasMore() ? args.supplyString() : new AESCrypter().getAlgorithmName()
        ));
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

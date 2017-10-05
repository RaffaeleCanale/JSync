package com.wx.jsync;

import com.wx.crypto.CryptoException;
import com.wx.crypto.cipher.AESCrypter;
import com.wx.io.Accessor;

import java.io.File;
import java.io.IOException;

import static com.wx.jsync.Constants.SALT;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 05.10.17.
 */
public class EncryptSecretMain {

    public static void main(String[] args) throws CryptoException, IOException {
        File secretFile = new File("src/main/resources/google/client_secret.json");
        File encryptedFile = new File("src/main/resources/google/client_secret.json.wxc");

        char[] password = Main.IN.readPassword("Enter password: ");
        AESCrypter aesCrypter = new AESCrypter(password, SALT);
        try (Accessor accessor = new Accessor()
                .setIn(secretFile)
                .setOutCrypter(encryptedFile, false, aesCrypter, true)) {
            accessor.pourInOut();
        }
    }

}

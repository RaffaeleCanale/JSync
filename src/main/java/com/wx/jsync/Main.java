package com.wx.jsync;

import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.console.UserConsoleInterface;
import com.wx.console.system.UnixSystemConsole;
import com.wx.crypto.cipher.AESCrypter;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.filesystem.decorator.factory.DecoratorType;
import com.wx.jsync.filesystem.base.LocalFileSystem;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.dataset.factory.DataSetFactory;
import com.wx.jsync.dataset.factory.impl.LocalDataSetFactory;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.sync.SyncManager;
import com.wx.jsync.util.StringArgsSupplier;
import com.wx.util.log.LogHelper;
import com.wx.util.representables.string.EnumCasterLC;

import java.io.IOException;
import java.util.logging.Level;

import static com.wx.jsync.SyncHelper.initSyncManager;
import static com.wx.jsync.filesystem.decorator.factory.DecoratorFactory.KEY_PATH;
import static com.wx.jsync.index.IndexKey.DECORATORS;
import static com.wx.jsync.util.DesktopUtils.getCwd;

public class Main {


    public static final UserConsoleInterface IN = new UserConsoleInterface(new UnixSystemConsole(), "", "> ");

    public static void main(String[] argv) throws IOException {
        LogHelper.setupLogger(LogHelper.consoleHandler(Level.ALL));

        ArgumentsSupplier args = new StringArgsSupplier(argv);

        String mode = args.supplyString();

        switch (mode) {
            case "init":
                init(args);
                break;
            case "remote":
                setRemote(args);
                break;
            case "sync":
                sync();
                break;
            case "decorate":
                addDecorator(args);
                break;
            case "encrypt":
                encrypt(args);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static void addDecorator(ArgumentsSupplier args) throws IOException {
        DataSet target = getTarget(args.supplyString());
        DecoratorType type = new EnumCasterLC<>(DecoratorType.class).castOut(args.supplyString());
        Options options = type.getFactory().getOptions(args);


        NamedOptions<DecoratorType> decorator = new NamedOptions<>(type, options);

        Index index = target.getIndex();
        index.setSingle(DECORATORS, decorator);
        index.save(target.getFileSystem());
    }

    private static DataSet getTarget(String targetName) throws IOException {
        if (targetName.equals("local")) {
            return new LocalDataSetFactory().loadFrom(getCwd());
        } else if (targetName.equals("remote")) {
            return SyncHelper.initSyncManager().getRemote();
        } else {
            throw new IllegalArgumentException("Invalid target: " + targetName);
        }
    }

    public static void encrypt(ArgumentsSupplier args) throws IOException {
        String algorithm = args.hasMore() ? args.supplyString() : new AESCrypter().getAlgorithmName();

        SyncManager sync = initSyncManager();
        LocalFileSystem fs = sync.getLocal().getBaseFs();

        String currentPath = fs.relative(getCwd());
        NamedOptions<DecoratorType> decoratorOptions = new NamedOptions<>(
                DecoratorType.CRYPTER,
                new Options(ImmutableMap.of(
                        "algorithm", algorithm,
                        KEY_PATH, currentPath
                ))
        );

        Index remoteIndex = sync.getRemote().getIndex();
        remoteIndex.setSingle(DECORATORS, decoratorOptions);
        remoteIndex.save(sync.getRemote().getFileSystem());
    }

    public static void sync() throws IOException {
        initSyncManager().execute();
    }

    public static void init(ArgumentsSupplier args) throws IOException {
        DataSet local = new LocalDataSetFactory().createOrInit(getCwd());

        local.commit();
        local.getIndex().save(local.getFileSystem());
    }

    public static void setRemote(ArgumentsSupplier args) throws IOException {
        String remoteType = args.supplyString().toUpperCase();
        DataSetFactory factory = DataSetType.valueOf(remoteType).getFactory();

        Options options = factory.parseConfig(args);

        DataSet local = new LocalDataSetFactory().loadFrom(getCwd());

        factory.connectOrInit(local, options);

    }


}
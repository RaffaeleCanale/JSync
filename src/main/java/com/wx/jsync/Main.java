package com.wx.jsync;

import com.google.common.collect.ImmutableMap;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.console.UserConsoleInterface;
import com.wx.console.system.UnixSystemConsole;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.dataset.factory.DataSetFactory;
import com.wx.jsync.dataset.factory.impl.LocalDataSetFactory;
import com.wx.jsync.filesystem.base.LocalFileSystem;
import com.wx.jsync.filesystem.decorator.factory.DecoratorType;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.Loader;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.util.StringArgsSupplier;
import com.wx.jsync.util.extensions.google.DriveServiceFactory;
import com.wx.util.log.LogHelper;
import com.wx.util.representables.string.EnumCasterLC;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;

import static com.sun.org.apache.bcel.internal.generic.InstructionConstants.bla;
import static com.wx.jsync.Constants.GOOGLE_DIR_GLOBAL;
import static com.wx.jsync.SyncHelper.initSyncManager;
import static com.wx.jsync.index.IndexKey.DECORATORS;
import static com.wx.jsync.index.IndexKey.FILE_FILTER;
import static com.wx.jsync.index.loader.FilterLoader.BLACK_LIST_KEY;
import static com.wx.jsync.index.loader.FilterLoader.WHITE_LIST_KEY;
import static com.wx.jsync.util.DesktopUtils.getCwd;
import static java.util.Collections.emptyList;

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
                setRemote(args, false);
                break;
            case "remote-init":
                setRemote(args, true);
                break;
            case "sync":
                sync();
                break;
            case "decorate":
                addDecorator(args);
                break;
            case "whitelist":
                filter(args, true);
                break;
            case "blacklist":
                filter(args, false);
                break;
            case "status":
                System.out.println(initSyncManager().getStatus());
                break;
            case "set-global-gdrive":
                setGlobalDrive(args);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static void filter(ArgumentsSupplier args, boolean addToWhitelist) throws IOException {
        DataSet target = getTarget(args.supplyString());
        String filter = args.supplyString();

        Index index = target.getIndex();
        Options filters = index.get(FILE_FILTER, Loader.OPTIONS);

        Set<Object> whitelist = new HashSet<>(filters.get(WHITE_LIST_KEY, emptyList()));
        Set<Object> blacklist = new HashSet<>(filters.get(BLACK_LIST_KEY, emptyList()));

        if (addToWhitelist) {
            whitelist.add(filter);
        } else {
            blacklist.add(filter);
        }

        index.set(FILE_FILTER, new Options(ImmutableMap.of(
                WHITE_LIST_KEY, whitelist,
                BLACK_LIST_KEY, blacklist
        )), Loader.OPTIONS);
        index.save(target.getFileSystem());
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

//    public static void encrypt(ArgumentsSupplier args) throws IOException {
//        String algorithm = args.hasMore() ? args.supplyString() : new AESCrypter().getAlgorithmName();
//
//        SyncManager sync = initSyncManager();
//        LocalFileSystem fs = sync.getLocal().getBaseFs();
//
//        String currentPath = fs.relative(getCwd());
//        NamedOptions<DecoratorType> decoratorOptions = new NamedOptions<>(
//                DecoratorType.CRYPTER,
//                new Options(ImmutableMap.of(
//                        "algorithm", algorithm,
//                        KEY_PATH, currentPath
//                ))
//        );
//
//        Index remoteIndex = sync.getRemote().getIndex();
//        remoteIndex.setSingle(DECORATORS, decoratorOptions);
//        remoteIndex.save(sync.getRemote().getFileSystem());
//    }

    public static void sync() throws IOException {
        initSyncManager().execute();
    }

    public static void init(ArgumentsSupplier args) throws IOException {
        DataSet local = new LocalDataSetFactory().createOrInit(getCwd());

        local.commit();
        local.getIndex().save(local.getFileSystem());
    }

    public static void setRemote(ArgumentsSupplier args, boolean init) throws IOException {
        String remoteType = args.supplyString().toUpperCase();
        DataSetFactory factory = DataSetType.valueOf(remoteType).getFactory();

        Options options = factory.parseConfig(args);

        DataSet local = new LocalDataSetFactory().loadFrom(getCwd());

        if (init) {
            factory.init(local, options);
        } else {
            factory.connect(local, options);
        }
    }


    public static void setGlobalDrive(ArgumentsSupplier args) throws IOException {
        File dir = new File(GOOGLE_DIR_GLOBAL);

        try {
            DriveServiceFactory.init(dir);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
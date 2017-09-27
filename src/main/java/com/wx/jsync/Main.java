package com.wx.jsync;

import com.google.common.collect.ImmutableMap;
import com.sun.prism.PixelFormat;
import com.wx.action.arg.ArgumentsSupplier;
import com.wx.console.UserConsoleInterface;
import com.wx.console.system.UnixSystemConsole;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.index.RemoteConfig;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.dataset.factory.DataSetFactory;
import com.wx.jsync.dataset.factory.impl.LocalDataSetFactory;
import com.wx.jsync.util.StringArgsSupplier;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;

import static com.wx.jsync.SyncHelper.initSyncManager;
import static com.wx.jsync.dataset.DataSetType.GDRIVE;
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
            case "sync":
                sync();
                break;
        }
    }

    public static void sync() throws IOException {
        initSyncManager().execute();
    }

    public static void init(ArgumentsSupplier args) throws IOException {
        String remoteType = args.supplyString().toUpperCase();
        DataSetFactory factory = DataSetType.valueOf(remoteType).getFactory();

        RemoteConfig remoteConfig = factory.parseConfig(args);

        DataSet local = new LocalDataSetFactory().init(getCwd(), remoteConfig);
        factory.connectOrInit(local, remoteConfig);
    }




}
package com.wx.jsync;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.console.UserConsoleInterface;
import com.wx.console.system.UnixSystemConsole;
import com.wx.jsync.util.Common;
import com.wx.jsync.util.StringArgsSupplier;
import com.wx.util.log.LogHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wx.jsync.util.Common.EMPTY_ARGS;
import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

public class Main {


    public static final UserConsoleInterface IN = new UserConsoleInterface(new UnixSystemConsole(), "", "> ");

    static final DataSetsHelper dataSets = new DataSetsHelper();

    public static DataSetsHelper getDataSets() {
        return dataSets;
    }

    public static void main(String[] argv) {
        LogHelper.setupLogger(LogHelper.consoleHandler(Level.ALL));

        ArgumentsSupplier args = new StringArgsSupplier(argv);

        Commands cmd;
        try {
            cmd = Common.enumCaster(Commands.class).castOut(args.supplyString());
        } catch (Exception e) {
            cmd = Commands.HELP;
            args = EMPTY_ARGS;
        }
        try {
            cmd.execute(args);
        } catch (Exception e) {
            System.err.println("[" + e.getClass().getSimpleName() + "] " + e.getMessage() + "\n\n" +
                    cmd.usage(EMPTY_ARGS));
            e.printStackTrace();
        }
    }
}
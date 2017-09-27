package com.wx.jsync.sync.conflict.impl;

import com.wx.jsync.filesystem.FileStat;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.conflict.ConflictHandler;
import com.wx.jsync.sync.tasks.SyncTask;
import com.wx.jsync.sync.tasks.SyncTasks;
import com.wx.util.Format;

import java.util.Optional;

import static com.wx.jsync.Main.IN;
import static com.wx.jsync.util.PrintUtils.formatTable;
import static com.wx.jsync.util.PrintUtils.printWithColors;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.09.17.
 */
public class UserInputHandler implements ConflictHandler {
    @Override
    public boolean handle(SyncTasks.Builder tasks, SyncTask conflict) {
        SyncFile local = conflict.getLocalFile();
        SyncFile remote = conflict.getRemoteFile();

        String[][] table = {
                {"", "Version", "Base Version", "Author", "Date", "File size"},
                getRow("Local", local),
                getRow("Remote", remote)
        };

        printWithColors(IN.getConsole(), "Conflict detected for {Red}" + local.getPath() + "{}\n\n" +
                formatTable(table) + "\n\n" +
                "Choose which version you want to {b}keep{}:\n" +
                "({b}l{}ocal/{b}r{}emote/{b}i{}gnore/{b}c{}ancel)");
        int choice = IN.inputMultipleChar(true, 'l', 'r', 'i', 'c');

        switch (choice) {
            case 0:
                tasks.updateRemote(local, Optional.empty());
                return true;
            case 1:
                tasks.updateLocal(Optional.empty(), remote);
                return true;
            case 2:
                printWithColors(IN.getConsole(), "{Yellow}{b}" + local.getPath() + "{}{Yellow} ignored{}");
                return true;
            default:
                return false;
        }

    }

    private String[] getRow(String label, SyncFile file) {
        FileStat stat = file.getStat();

        return new String[]{label,
                "{Blue}" + file.getVersion() + "{}",
                "{Blue}" + file.getBaseVersion().map(String::valueOf).orElse("-") + "{}",
                file.getVersionAuthor(),
                stat.isRemoved() ?  "{Red}(deleted){}" : Format.formatDate(stat.getTimestamp()),
                stat.isRemoved() ?  "{Red}(deleted){}" : Format.formatSize(stat.getFileSize()),
        };
    }


}

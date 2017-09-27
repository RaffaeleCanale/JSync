package com.wx.jsync.sync.conflict;

import com.wx.jsync.sync.tasks.SyncTask;
import com.wx.jsync.sync.tasks.SyncTasks;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.09.17.
 */
public interface ConflictHandler {

    boolean handle(SyncTasks.Builder tasks, SyncTask conflict);

}

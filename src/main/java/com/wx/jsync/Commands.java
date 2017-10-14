package com.wx.jsync;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.index.IndexKey;
import com.wx.jsync.index.options.MutableOptions;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.util.Common;
import com.wx.jsync.util.extensions.google.DriveServiceFactory;
import com.wx.util.representables.TypeCaster;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wx.jsync.Main.dataSets;
import static com.wx.jsync.SyncHelper.initSyncManager;
import static com.wx.jsync.util.Common.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 02.10.17.
 */
public enum Commands {
    HELP {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "</command>";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            if (args.hasMore()) {
                // TODO: 03.10.17
            }

            TypeCaster<String, Commands> caster = enumCaster(Commands.class);
            System.out.println(Stream.of(Commands.values())
                    .map(c -> "jsync " + caster.castIn(c) + " " + c.usage(EMPTY_ARGS))
                    .collect(Collectors.joining("\n")));
        }
    },
    INIT {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            DataSet local = dataSets.createOrInitLocal();

            local.commit();
            local.getIndex().save(local.getFileSystem());
        }
    },
    REMOTE {
        @Override
        public String usage(ArgumentsSupplier args) {
            if (args.hasMore()) {
                // TODO: 02.10.17 Print type args
            }
            TypeCaster<String, DataSetType> caster = Common.enumCaster(DataSetType.class);
            return "[" + Stream.of(DataSetType.values())
                    .map(caster::castIn)
                    .collect(Collectors.joining("|")) + "] <remote_args...>";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            DataSetType type = getDataSetType(args.supplyString());
            Options options = type.getFactory().parseOptions(args);

            DataSet local = dataSets.getLocal();
            local.getIndex().set(IndexKey.REMOTE, new NamedOptions<>(
                    type,
                    options
            ));

            MutableOptions mutableOptions = options.toMutable();
            type.getFactory().connect(mutableOptions);

            if (mutableOptions.hasChanged()) {
                local.getIndex().set(IndexKey.REMOTE, new NamedOptions<>(type, mutableOptions.toOptions()));
            }

            local.saveIndex();
        }
    },
    REMOTE_INIT {
        @Override
        public String usage(ArgumentsSupplier args) {
            TypeCaster<String, DataSetType> caster = Common.enumCaster(DataSetType.class);
            return "[" + Stream.of(DataSetType.values())
                    .map(caster::castIn)
                    .collect(Collectors.joining("|")) + "] <remote_args...>";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            DataSetType type = getDataSetType(args.supplyString());
            Options options = type.getFactory().parseOptions(args);

            DataSet local = dataSets.getLocal();
            local.getIndex().set(IndexKey.REMOTE, new NamedOptions<>(
                    type,
                    options
            ));

            MutableOptions mutableOptions = options.toMutable();
            type.getFactory().init(mutableOptions);

            if (mutableOptions.hasChanged()) {
                local.getIndex().set(IndexKey.REMOTE, new NamedOptions<>(type, mutableOptions.toOptions()));
            }

            local.saveIndex();
        }
    },
    SYNC {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            initSyncManager(dataSets).execute();
        }
    },
    STATUS {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            System.out.println(initSyncManager(dataSets).getStatus());
        }
    },
    SET_GLOBAL_DRIVE {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            File dir = GlobalConfig.getGoogleDir();

            try {
                DriveServiceFactory.init(dir);
                DriveServiceFactory.getUserInfo(true);
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
    },
    SET {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "[local|remote] <key> <value>";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            DataSet target = dataSets.getByName(args.supplyString());
            IndexKey key = Common.enumCaster(IndexKey.class).castOut(args.supplyString());

            if (args.hasMore()) {
                target.getIndex().userSet(key, args);
            } else {
                target.getIndex().remove(key);
            }

            target.saveIndex();
        }
    },
    ;

    public abstract void execute(ArgumentsSupplier args) throws IOException;

    public abstract String usage(ArgumentsSupplier args);
}

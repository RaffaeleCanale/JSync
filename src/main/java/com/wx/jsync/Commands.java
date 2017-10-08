package com.wx.jsync;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.dataset.DataSet;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.filesystem.FileSystem;
import com.wx.jsync.filesystem.decorator.factory.DecoratorType;
import com.wx.jsync.index.Index;
import com.wx.jsync.index.IndexKey;
import com.wx.jsync.index.Loader;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.util.Common;
import com.wx.jsync.util.extensions.google.DriveServiceFactory;
import com.wx.util.representables.TypeCaster;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wx.jsync.Main.dataSets;
import static com.wx.jsync.SyncHelper.initSyncManager;
import static com.wx.jsync.index.IndexKey.*;
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
            Options options = type.getFactory().parseConfig(args);

            DataSet local = dataSets.getLocal();
            local.getIndex().set(IndexKey.REMOTE, new NamedOptions<>(
                    type,
                    options
            ));

            type.getFactory().connect(options);
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
            Options options = type.getFactory().parseConfig(args);

            DataSet local = dataSets.getLocal();
            local.getIndex().set(IndexKey.REMOTE, new NamedOptions<>(
                    type,
                    options
            ));

            type.getFactory().init(options);
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
    DECORATE {
        @Override
        public String usage(ArgumentsSupplier args) {
            // TODO: 03.10.17 Print decorator args help
            return "[local|remote] <decorator_type> <decorator_args...>";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            DataSet target = dataSets.getByName(args.supplyString());

            DecoratorType type = Common.getDecoratorTyoe(args.supplyString());
            String path = dataSets.getCurrentPath();

            Options options = type.getFactory().getOptions(path, args);

            NamedOptions<DecoratorType> decorator = new NamedOptions<>(type, options);

            Index index = target.getIndex();
            index.setSingle(DECORATORS, decorator);
            target.saveIndex();
        }
    },
    IGNORE {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "[local|remote] <expression_to_ignore>";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            DataSet target = dataSets.getByName(args.supplyString());
            String filter = args.supplyString();

            Index index = target.getIndex();
            index.setSingle(FILE_FILTER, filter, Loader.STRING_LIST);
            target.saveIndex();
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
    RESET_OPTION {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "[local|remote] <key_name>";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            DataSet target = dataSets.getByName(args.supplyString());
            IndexKey key = Common.enumCaster(IndexKey.class).castOut(args.supplyString());

            target.getIndex().remove(key);
            target.saveIndex();
        }
    },
    DECORATOR_DEBUG {
        @Override
        public String usage(ArgumentsSupplier args) {
            return "[local|remote] [fs|index]";
        }

        @Override
        public void execute(ArgumentsSupplier args) throws IOException {
            DataSet target = dataSets.getByName(args.supplyString());
            String source = "index";
            if (args.hasMore()) {
                source = args.supplyString();
            }

            Map<FileSystem, Set<String>> fsToFiles = new HashMap<>();

            if (source.equals("index")) {
                Collection<SyncFile> files = target.getIndex().get(FILES);

                for (SyncFile file : files) {
                    String path = file.getPath();
                    FileSystem fs = target.getFileSystem().resolveFs(path);
                    fsToFiles.computeIfAbsent(fs, k -> new HashSet<>()).add(path);
                }

            } else if (source.equals("fs")) {
                Collection<String> files = target.getFileSystem().getAllFiles(
                        target.getIndex().get(FILE_FILTER)
                );

                for (String file : files) {
                    FileSystem fs = target.getFileSystem().resolveFs(file);
                    fsToFiles.computeIfAbsent(fs, k -> new HashSet<>()).add(file);
                }
            } else {
                throw new IllegalArgumentException();
            }

            for (FileSystem fs : fsToFiles.keySet()) {
                System.out.println(fs);
                for (String file : fsToFiles.get(fs)) {
                    System.out.println("  - " + file);
                }
            }
        }
    };

    public abstract void execute(ArgumentsSupplier args) throws IOException;

    public abstract String usage(ArgumentsSupplier args);
}

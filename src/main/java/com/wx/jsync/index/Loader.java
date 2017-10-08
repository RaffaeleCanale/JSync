package com.wx.jsync.index;

import com.wx.jsync.ListLoader;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.filesystem.decorator.factory.DecoratorType;
import com.wx.jsync.index.loader.*;
import com.wx.jsync.index.loader.StoredKeysLoader;
import com.wx.jsync.index.loader.StringSetLoader;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.index.options.StoredKeys;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.conflict.ConflictHandler;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 28.09.17.
 */
public interface Loader<E> {

    Loader<String> STRING = new StringLoader();
    Loader<Boolean> BOOLEAN = new BooleanLoader();
    ListLoader<String> STRING_SET = new StringSetLoader();
    ListLoader<String> STRING_LIST = new StringListLoader();

    Loader<NamedOptions<DataSetType>> DATA_SET_OPTIONS = new NamedOptionsLoader<>(DataSetType.class);
    Loader<Collection<NamedOptions<DecoratorType>>> DECORATOR_SET = new DecoratorSetLoader();
    Loader<StoredKeys> STORED_KEYS = new StoredKeysLoader();
    Loader<Predicate<String>> FILTER = new FilterLoader();
    Loader<ConflictHandler> HANDLER = new ConflictHandlerLoader();
    Loader<Collection<SyncFile>> FILE_SET = new FilesLoader();
    Loader<Options> OPTIONS = new OptionsLoader();

    static Loader<Boolean> BOOLEAN(boolean def) {
        return new BooleanLoader() {
            @Override
            public Boolean load(JSONObject root, String... key) {
                return loadOpt(root, key).orElse(def);
            }
        };
    }


    Optional<E> loadOpt(JSONObject root, String[] key);

    default E load(JSONObject root, String... key) {
        return loadOpt(root, key)
                .orElseThrow(() -> new IllegalArgumentException("Missing value for " + String.join(".", (CharSequence[]) key)));
    }

    void setValue(JSONObject root, E value, String... path);
}

package com.wx.jsync.index;

import com.wx.action.arg.ArgumentsSupplier;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.index.loader.*;
import com.wx.jsync.index.options.NamedOptions;
import com.wx.jsync.index.options.Options;
import com.wx.jsync.index.options.StoredKeys;
import com.wx.jsync.sync.SyncFile;
import com.wx.jsync.sync.conflict.ConflictHandler;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 28.09.17.
 */
public interface Loader<E> {

    Loader<String> STRING = new StringLoader();
    Loader<Boolean> BOOLEAN = new BooleanLoader();
    ListLoader<String> STRING_SET = new StringSetLoader();
    ListLoader<String> STRING_LIST = new StringListLoader();

    Loader<NamedOptions<DataSetType>> REMOTE_OPTIONS = new NamedOptionsLoader<>(DataSetType.class);
    Loader<StoredKeys> STORED_KEYS = new StoredKeysLoader();
    Loader<Predicate<String>> FILTER = new FilterLoader();
    Loader<ConflictHandler> HANDLER = new ConflictHandlerLoader();
    Loader<Collection<SyncFile>> FILE_SET = new FilesLoader();
    Loader<Options> OPTIONS = new OptionsLoader();

    Optional<E> loadOpt(JSONObject root, String[] key);

    default E load(JSONObject root, String... key) {
        return loadOpt(root, key)
                .orElseThrow(() -> new IllegalArgumentException("Missing value for " + String.join(".", (CharSequence[]) key)));
    }

    void setValue(JSONObject root, E value, String... path);

    void userSet(JSONObject root, ArgumentsSupplier args, String... path);

    default Loader<E> or(Supplier<E> defaultValue) {
        return new Loader<E>() {

            @Override
            public E load(JSONObject root, String... key) {
                return loadOpt(root, key).orElseGet(defaultValue);
            }

            @Override
            public Optional<E> loadOpt(JSONObject root, String[] key) {
                return Loader.this.loadOpt(root, key);
            }

            @Override
            public void setValue(JSONObject root, E value, String... path) {
                Loader.this.setValue(root, value, path);
            }

            @Override
            public void userSet(JSONObject root, ArgumentsSupplier args, String... path) {
                Loader.this.userSet(root, args, path);
            }
        };
    }

    default Loader<E> or(E defaultValue) {
        return new Loader<E>() {

            @Override
            public E load(JSONObject root, String... key) {
                return loadOpt(root, key).orElse(defaultValue);
            }

            @Override
            public Optional<E> loadOpt(JSONObject root, String[] key) {
                return Loader.this.loadOpt(root, key);
            }

            @Override
            public void setValue(JSONObject root, E value, String... path) {
                Loader.this.setValue(root, value, path);
            }

            @Override
            public void userSet(JSONObject root, ArgumentsSupplier args, String... path) {
                Loader.this.userSet(root, args, path);
            }
        };
    }
}

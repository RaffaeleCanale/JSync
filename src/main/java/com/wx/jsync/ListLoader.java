package com.wx.jsync;

import com.wx.jsync.index.Loader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

import static com.wx.jsync.util.JsonUtils.getArrayOpt;
import static com.wx.jsync.util.JsonUtils.set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 08.10.17.
 */
public abstract class ListLoader<E> implements Loader<Collection<E>> {

    @Override
    public Optional<Collection<E>> loadOpt(JSONObject root, String[] key) {
        return getArrayOpt(root, key)
                .map(array -> {
                    List<E> result = new ArrayList<>();

                    for (Object obj : array) {
                        result.add(load(obj));
                    }

                    return result;
                });
    }


    @Override
    public Collection<E> load(JSONObject root, String[] key) {
        return loadOpt(root, key).orElseGet(Collections::emptyList);
    }

    @Override
    public void setValue(JSONObject root, Collection<E> value, String[] path) {
        JSONArray array = new JSONArray(value.stream()
                .map(this::getValue)
                .collect(Collectors.toSet())
        );

        set(root, array, path);
    }

    public void setSingleValue(JSONObject root, String[] path, E value) {
        JSONArray array = getArrayOpt(root, path).orElse(null);

        if (array == null) {
            array = new JSONArray();
        }

        array.put(getValue(value));

        set(root, array, path);
    }

    public void removeSingleValue(JSONObject root, String[] path, E value) {
        throw new UnsupportedOperationException();
    }

    public Optional<E> getSingleValue(JSONObject root, String[] path, String id) {
        throw new UnsupportedOperationException();
    }

    protected abstract E load(Object entry);

    protected abstract Object getValue(E value);
}

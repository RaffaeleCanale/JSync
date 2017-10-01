package com.wx.jsync.index;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

import static com.wx.jsync.util.JsonUtils.getArrayOpt;
import static com.wx.jsync.util.JsonUtils.set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 29.09.17.
 */
public abstract class SetLoader<E> implements Loader<Set<E>> {

    @Override
    public Optional<Set<E>> loadOpt(JSONObject root, String[] key) {
        return getArrayOpt(root, key)
                .map(array -> {
                    Set<E> result = new TreeSet<>(Comparator.comparing(this::getId));

                    for (Object obj : array) {
                        result.add(load(obj));
                    }

                    return result;
                });
    }


    @Override
    public Set<E> load(JSONObject root, String[] key) {
        return loadOpt(root, key).orElseGet(Collections::emptySet);
    }

    @Override
    public void setValue(JSONObject root, Set<E> value, String[] path) {
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

        int i = findEntry(array, value);
        if (i < 0) {
            array.put(getValue(value));
        } else {
            array.put(i, getValue(value));
        }

        set(root, array, path);
    }

    public void removeSingleValue(JSONObject root, String[] path, E value) {
        JSONArray array = getArrayOpt(root, path).orElse(null);

        if (array != null) {
            int i = findEntry(array, value);
            if (i >= 0) {
                array.remove(i);
                set(root, array, path);
            }
        }
    }

    public Optional<E> getSingleValue(JSONObject root, String[] path, String id) {
        JSONArray array = getArrayOpt(root, path).orElse(null);

        if (array != null) {
            int i = findEntry(array, id);
            if (i >= 0) {
                return Optional.of(load(array.get(i)));
            }
        }

        return Optional.empty();
    }

    private int findEntry(JSONArray array, E value) {
        return findEntry(array, getId(value));
    }

    private int findEntry(JSONArray array, String id) {
        for (int i = 0; i < array.length(); i++) {
            E entry = load(array.get(i));

            if (getId(entry).equals(id)) {
                return i;
            }
        }

        return -1;
    }


    protected abstract String getId(E e);

    protected abstract E load(Object entry);

    protected abstract Object getValue(E value);


}

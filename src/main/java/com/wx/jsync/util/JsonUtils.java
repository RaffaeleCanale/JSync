package com.wx.jsync.util;

import com.wx.jsync.index.InvalidIndexException;
import com.wx.util.representables.string.EncodedBytesRepr;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class JsonUtils {

    public static String getString(JSONObject object, String... path) {
        return getStringOpt(object, path).orElseThrow(() -> new InvalidIndexException("Missing value for: " + Arrays.toString(path)));
    }

    public static long getLong(JSONObject object, String... path) {
        return getLongOpt(object, path).orElseThrow(() -> new InvalidIndexException("Missing value for: " + Arrays.toString(path)));
    }

    public static double getDouble(JSONObject object, String... path) {
        return getDoubleOpt(object, path).orElseThrow(() -> new InvalidIndexException("Missing value for: " + Arrays.toString(path)));
    }

    public static byte[] getBytes(JSONObject object, String... path) {
        return getBytesOpt(object, path).orElseThrow(() -> new InvalidIndexException("Missing value for: " + Arrays.toString(path)));
    }

    public static Optional<String> getStringOpt(JSONObject object, String... path) {
        return get(object, JSONObject::getString, path);
    }

    public static Optional<JSONObject> getObjectOpt(JSONObject object, String... path) {
        return get(object, JSONObject::getJSONObject, path);
    }

    public static Optional<Long> getLongOpt(JSONObject object, String... path) {
        return get(object, JSONObject::getLong, path);
    }

    public static Optional<Double> getDoubleOpt(JSONObject object, String... path) {
        return get(object, JSONObject::getDouble, path);
    }

    public static Optional<Boolean> getBooleanOpt(JSONObject object, String... path) {
        return get(object, JSONObject::getBoolean, path);
    }

    public static Optional<byte[]> getBytesOpt(JSONObject object, String... path) {
        return get(object, (obj, key) -> new EncodedBytesRepr().castOut(obj.getString(key)), path);
    }

    public static Optional<List<String>> getStringListOpt(JSONObject object, String... path) {
        return getArray(object, path).map(array -> populate(array::getString, array.length()));
    }

    public static void remove(JSONObject object, String... path) {
        getParent(object, path).ifPresent(parent -> parent.remove(field(path)));
    }

    private static Optional<JSONArray> getArray(JSONObject object, String... path) {
        return get(object, JSONObject::getJSONArray, path);
    }

    public static void  set(JSONObject object, Collection<?> value, String... path) {
        set(object, new JSONArray(value), path);
    }


    public static void set(JSONObject object, byte[] value, String... path) {
        set(object, new EncodedBytesRepr().castIn(value), path);
    }


    public static void set(JSONObject object, Object value, String... path) {
        String field = field(path);
        JSONObject parent = getOrCreateParent(object, path);

        parent.put(field, value);
    }


    private static <E> List<E> populate(Function<Integer, E> getValue, int length) {
        List<E> result = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            result.add(getValue.apply(i));
        }

        return result;
    }

    private static <E> Optional<E> get(JSONObject object, BiFunction<JSONObject, String, E> getter, String... path) {
        return getParent(object, path).flatMap((parent) -> {
            String field = field(path);

            if (parent.has(field)) {
                return Optional.of(getter.apply(parent, field));
            } else {
                return Optional.empty();
            }
        });
    }

    private static JSONObject getOrCreateParent(JSONObject object, String... path) {
        for (int i = 0; i < path.length - 1; i++) {
            String field = path[i];

            if (object.has(field)) {
                object = object.getJSONObject(field);
            } else {
                JSONObject newObject = new JSONObject();
                object.put(field, newObject);

                object = newObject;
            }
        }

        return object;
    }

    private static Optional<JSONObject> getParent(JSONObject object, String... path) {
        for (int i = 0; i < path.length - 1; i++) {
            String field = path[i];

            if (object.has(field)) {
                object = object.getJSONObject(field);
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(object);
    }


    private static String field(String... path) {
        return path[path.length - 1];
    }

    private JsonUtils() {
    }

}

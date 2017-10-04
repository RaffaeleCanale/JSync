package com.wx.jsync.util;


import com.wx.action.arg.ArgumentsSupplier;
import com.wx.action.arg.ObjectsSupplier;
import com.wx.jsync.dataset.DataSetType;
import com.wx.jsync.filesystem.decorator.factory.DecoratorType;
import com.wx.util.representables.TypeCaster;
import com.wx.util.representables.string.EnumCasterLC;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.wx.jsync.Constants.VERSION_INCREMENT_DELTA;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.06.17.
 */
public class Common {

    public static final ArgumentsSupplier EMPTY_ARGS = new ObjectsSupplier();

    public static DataSetType getDataSetType(String name) {
        return enumCaster(DataSetType.class).castOut(name);
    }

    public static DecoratorType getDecoratorTyoe(String name) {
        return enumCaster(DecoratorType.class).castOut(name);
    }

    public static <E extends Enum<E>> TypeCaster<String, E> enumCaster(Class<E> cls) {
        return new EnumCasterLC<E>(cls) {
            @Override
            public String castIn(E value) throws ClassCastException {
                return super.castIn(value).replace('_', '-');
            }

            @Override
            public E castOut(String value) throws ClassCastException {
                return super.castOut(value.replace('-', '_'));
            }
        };
    }

    public static double bumpVersion(double version) {
        return roundToTwoPlaces(version + VERSION_INCREMENT_DELTA);
    }

    public static double roundToTwoPlaces(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static List<String> toList(ArgumentsSupplier args) {
        List<String> result = new LinkedList<>();
        while (args.hasMore()) {
            result.add(args.supplyString());
        }

        return result;
    }

    public static <E> Optional<E> last(List<E> list) {
        return list.isEmpty() ?
                Optional.empty() :
                Optional.of(list.get(list.size() - 1));
    }

    public static <E> Optional<E> first(List<E> list) {
        return list.isEmpty() ?
                Optional.empty() :
                Optional.of(list.get(0));
    }



    private Common() {
    }
}

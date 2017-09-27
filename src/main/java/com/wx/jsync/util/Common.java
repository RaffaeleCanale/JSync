package com.wx.jsync.util;


import com.wx.action.arg.ArgumentsSupplier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.wx.jsync.Constants.VERSION_INCREMENT_DELTA;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.06.17.
 */
public class Common {

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


    public static Predicate<String> ignoreFilter(List<String> ignoreList) {
        return file -> ignoreList.stream().noneMatch(file::matches);
    }

    public static <E> Predicate<E> alwaysTrue() {
        return file -> true;
    }


    private Common() {
    }
}

package com.wx.jsync.util;

import com.wx.console.Console;
import com.wx.console.color.Color;
import jdk.nashorn.internal.runtime.ConsString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 23.09.17.
 */
public class PrintUtils {


    public static String formatTable(String[][] table) {
        StringBuilder result = new StringBuilder();

        int[] colWidths = IntStream.range(0, maxColCount(table))
                                .map(col -> maxWidth(table, col))
                                .toArray();


        for (String[] row : table) {
            for (int col = 0; col < row.length; col++) {
                int colWidth = colWidths[col];
                appendWithPadding(result, row[col], colWidth + 2);
            }
            result.append("\n");
        }

        return result.toString();
    }

    private static void appendWithPadding(StringBuilder builder, String s, int width) {
        int padding = width - s.replaceAll("\\{[^\\}]*\\}", "").length();

        for (int i = 0; i < Math.floorDiv(padding, 2); i++) {
            builder.append(" ");
        }
        builder.append(s);
        for (int i = 0; i < Math.floorDiv(padding+1, 2); i++) {
            builder.append(" ");
        }
    }

    private static int maxColCount(String[][] table) {
        return Stream.of(table)
                .mapToInt(row -> row.length)
                .max().orElse(0);
    }

    private static int maxWidth(String[][] table, int col) {
        return Stream.of(table)
                .mapToInt(row -> col < row.length ? row[col].replaceAll("\\{[^\\}]*\\}", "").length() : 0)
                .max().orElse(0);
    }

    public static void printWithColors(Console c, String s) {
        int start = s.indexOf('{');

        while (start >= 0) {
            s = printAndMove(c, s, start);

            int end = s.indexOf('}');
            if (end < 0) throw new AssertionError();

            String code = s.substring(1, end);
            printColorCode(c, code);

            s = s.substring(end + 1);
            start = s.indexOf('{');
        }

        c.println(s);
    }

    private static void printColorCode(Console c, String code) {
        if (code.isEmpty()) {
            c.resetStyle();
        } else if (code.equals("b")) {
            c.setBold();
        } else if (code.equals("i")) {
            c.setItalic();
        } else if (code.equals("u")) {
            c.setUnderlined();
        } else if (code.startsWith("*")) {
            c.setBackgroundColor(Color.valueOf(code.substring(1)));
        } else {
            c.setColor(Color.valueOf(code));
        }
    }

    private static String printAndMove(Console c, String s, int index) {
        c.print(s.substring(0, index));

        return s.substring(index);
    }
}

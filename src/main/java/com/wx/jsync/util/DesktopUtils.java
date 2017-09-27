package com.wx.jsync.util;

import com.wx.io.file.FileUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;

import static java.awt.SystemColor.desktop;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=JSync">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 12.05.17.
 */
public class DesktopUtils {

    private static String storedHostname;


    /**
     * Open an web URL in a browser. Precise behaviour depend on the current machine preferences.
     *
     * @param url URL to show
     */
    public static void openUrl(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String getHostName() {
        if (storedHostname == null) {
            storedHostname = getHostname0();
        }

        return storedHostname;
    }

    private static String getHostname0() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            Map<String, String> env = System.getenv();
            if (env.containsKey("COMPUTERNAME"))
                return env.get("COMPUTERNAME");
            else if (env.containsKey("HOSTNAME"))
                return env.get("HOSTNAME");
            else
                return "Unknown Computer";
        }
    }

    public static void deleteDirContent(File dir) {
        if (!dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    FileUtil.deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }
    }

    public static File getCwd() {
        return new File("").getAbsoluteFile();
    }

    private DesktopUtils() {
    }

}

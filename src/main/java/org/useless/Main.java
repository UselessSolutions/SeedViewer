package org.useless;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.jetbrains.annotations.NotNull;
import org.useless.seedviewer.Global;
import org.useless.seedviewer.gui.SeedViewer;

import javax.swing.*;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("net.minecraft.core.world.biome.provider.BiomeProviderOverworld");
        } catch (ClassNotFoundException e) {
            Global.LOGGER.error("Could not locate BTA jar! Canceling Startup!", e);
            return;
        }
        try {
            UIManager.setLookAndFeel( new FlatDarculaLaf() );
        } catch( Exception ex ) {
            Global.LOGGER.error("Failed to initialize LaF theme!", ex);
        }
        new SeedViewer(argsToProperties(args));
    }

    /**
     * Reads all provided strings for key value pairs, for strings which fail to follow the
     * key value format will be ignored
     * @param args Array of strings formated like { -key1, value1, -key2, value2}
     * @return Properties map representation of provided key value pairs
     */
    public static @NotNull Properties argsToProperties(String[] args) {
        Properties properties = new Properties();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                String key = arg.substring(1);
                if (key.isEmpty()) continue;
                if (i >= args.length - 1) continue; // Ensure that can't index out of bound
                String value = args[++i]; // Get next argument and shift pointer
                properties.setProperty(key, value);
            } else {
                Global.LOGGER.error("Unrecognized argument '{}'!", arg);
            }

        }
        return properties;
    }
}
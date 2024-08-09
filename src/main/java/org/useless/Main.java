package org.useless;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
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
                System.err.println("Unrecognized argument '" + arg + "'!");
            }

        }
        return properties;
    }
}
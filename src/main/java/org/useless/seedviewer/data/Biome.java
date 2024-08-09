package org.useless.seedviewer.data;

import org.useless.seedviewer.collections.NamespaceID;

public interface Biome {
    String getName();
    NamespaceID getID();
    int getColor();
}

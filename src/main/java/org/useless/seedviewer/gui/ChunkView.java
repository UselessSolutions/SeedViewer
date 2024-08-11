package org.useless.seedviewer.gui;

import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkView {
    public static final int MAX_IN_PROGRESS_CHUNKS = 10;
    public static AtomicInteger inProgressChunks = new AtomicInteger(0);

    private final int RESOLUTION_SCALE = 1;
    private final BufferedImage biomeMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X * RESOLUTION_SCALE, Chunk.CHUNK_SIZE_Z * RESOLUTION_SCALE, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage terrainMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X * RESOLUTION_SCALE, Chunk.CHUNK_SIZE_Z * RESOLUTION_SCALE, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage heightMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X * RESOLUTION_SCALE, Chunk.CHUNK_SIZE_Z * RESOLUTION_SCALE, BufferedImage.TYPE_INT_ARGB);
    private final ChunkLocation location;
    private final ChunkProvider provider;

    public long lastSeenTime = System.currentTimeMillis();

    public ChunkView(ChunkLocation location, ChunkProvider provider) {
        this.location = location;
        this.provider = provider;

        new Thread(
            () -> {

                while (inProgressChunks.get() > MAX_IN_PROGRESS_CHUNKS) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Global.LOGGER.error("Expected Interrupt", e);
                        break;
                    }
                }
                inProgressChunks.incrementAndGet();
                try {
                    Chunk chunk = provider.getChunk(location);
                    Graphics g = biomeMapImage.getGraphics();
                    g.setColor(new Color(1, 1, 1, 0));
                    g.fillRect(0, 0, Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z);
                    for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                            Biome b = chunk.getBiome(new ChunkPos3D(x, 128, z));
                            Color c = new Color(b.getColor());
                            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
                            g.setColor(c);
                            g.fillRect(x * RESOLUTION_SCALE, z * RESOLUTION_SCALE, RESOLUTION_SCALE, RESOLUTION_SCALE);
                        }
                    }
                    g.dispose();
                    Graphics g2 = terrainMapImage.getGraphics();
                    g2.setColor(new Color(1, 1, 1, 0));
                    g2.fillRect(0, 0, Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z);
                    for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                            Color c = chunk.getBlockColor(new ChunkPos3D(x, chunk.getHeight(new ChunkPos2D(x, z)) - 1, z));
                            g2.setColor(c);
                            g2.fillRect(x * RESOLUTION_SCALE, z * RESOLUTION_SCALE, RESOLUTION_SCALE, RESOLUTION_SCALE);
                        }
                    }
                    g2.dispose();
                } finally {
                    inProgressChunks.decrementAndGet();
                }
            }
        ).start();
    }
    private void drawDebugImage() {
        Graphics g = biomeMapImage.getGraphics();
        try {
            //Make it fancy
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }catch (Exception ignored) {}

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, biomeMapImage.getWidth(), biomeMapImage.getHeight());
        g.setColor(Color.BLACK);
        g.drawString(String.format("(%s,%s)", location.x, location.z), 1, 10);
        g.dispose();
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public BufferedImage getBiomeMapImage() {
        return biomeMapImage;
    }

    public BufferedImage getTerrainMapImage() {
        return terrainMapImage;
    }

    public BufferedImage getHeightMapImage() {
        return heightMapImage;
    }

    public Rectangle getWorldBounds() {
        return new Rectangle(location.x * Chunk.CHUNK_SIZE_X, location.z * Chunk.CHUNK_SIZE_Z, Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z);
    }
}

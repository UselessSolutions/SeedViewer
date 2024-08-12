package org.useless.seedviewer.gui;

import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ChunkView {
    private static final ExecutorService InitializerService = Executors.newFixedThreadPool(10);
    private static final ExecutorService PostProcessorService = Executors.newFixedThreadPool(10);

    private final int RESOLUTION_SCALE = 1;
    private final BufferedImage biomeMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X * RESOLUTION_SCALE, Chunk.CHUNK_SIZE_Z * RESOLUTION_SCALE, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage terrainMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X * RESOLUTION_SCALE, Chunk.CHUNK_SIZE_Z * RESOLUTION_SCALE, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage heightMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X * RESOLUTION_SCALE, Chunk.CHUNK_SIZE_Z * RESOLUTION_SCALE, BufferedImage.TYPE_BYTE_GRAY);
    private final BufferedImage waterDepthMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X * RESOLUTION_SCALE, Chunk.CHUNK_SIZE_Z * RESOLUTION_SCALE, BufferedImage.TYPE_BYTE_GRAY);
    private final ChunkLocation location;
    private final ChunkProvider provider;

    public long lastSeenTime = System.currentTimeMillis();

    private Future<?> loadTask;
    private Future<?> processTask = null;

    private boolean hasInitialized = false;
    private volatile boolean hasPostProcessed = false;

    public ChunkView(ChunkLocation location, ChunkProvider provider) {
        this.location = location;
        this.provider = provider;

        loadTask = InitializerService.submit(
            () -> {
                try {
                    Chunk chunk = provider.getChunk(location);

                    Graphics terrain = terrainMapImage.getGraphics();
                    Graphics biome = biomeMapImage.getGraphics();
                    Graphics height = heightMapImage.getGraphics();
                    Graphics depth = waterDepthMapImage.getGraphics();
                    terrain.setColor(new Color(1, 1, 1, 0));
                    terrain.fillRect(0, 0, Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z);
                    biome.setColor(new Color(1, 1, 1, 0));
                    biome.fillRect(0, 0, Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z);
                    for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                            ChunkPos2D pos2D = new ChunkPos2D(x, z);
                            int h = chunk.getHeight(pos2D) - 1;
                            if (h < 0) h = 0;
                            height.setColor(new Color(h, h, h));
                            height.fillRect(x * RESOLUTION_SCALE, z * RESOLUTION_SCALE, RESOLUTION_SCALE, RESOLUTION_SCALE);

                            int wd = chunk.getWaterDepth(pos2D);
                            depth.setColor(new Color(wd, wd, wd, 255));
                            depth.fillRect(x * RESOLUTION_SCALE, z * RESOLUTION_SCALE, RESOLUTION_SCALE, RESOLUTION_SCALE);

                            Biome b = chunk.getBiome(new ChunkPos3D(x, h, z));
                            Color biomeColor = new Color(b.getColor());
                            biomeColor = new Color(biomeColor.getRed(), biomeColor.getGreen(), biomeColor.getBlue(), 128);
                            biome.setColor(biomeColor);
                            biome.fillRect(x * RESOLUTION_SCALE, z * RESOLUTION_SCALE, RESOLUTION_SCALE, RESOLUTION_SCALE);

                            Color c = chunk.getBlockColor(new ChunkPos3D(x, h, z));
                            terrain.setColor(c);
                            terrain.fillRect(x * RESOLUTION_SCALE, z * RESOLUTION_SCALE, RESOLUTION_SCALE, RESOLUTION_SCALE);
                        }
                    }
                    terrain.dispose();
                    biome.dispose();
                    height.dispose();
                    depth.dispose();
                } catch (Exception e) {
                    Global.LOGGER.error("Error when initializing chunk {}, {}", location.x, location.z, e);
                }
                finally {
                    hasInitialized = true;
                }
            }
        );
    }

    public boolean hasInit() {
        return hasInitialized;
    }

    public boolean hasProcessed() {
        return hasPostProcessed;
    }

    public void process(ChunkView posZ) {
        if (hasProcessed()) return;
        synchronized (this) {
            processTask = PostProcessorService.submit(() -> {
                try {
                    for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                            int depth = waterDepthMapImage.getRGB(x, z) >> 8 & 0xFF;

                            int shade = 220;
                            if (depth == 0) {
                                int lastHeight;
                                if (z == 0) {
                                    lastHeight = posZ.getHeightMapImage().getRGB(x, z + 15) & 0xFF;
                                } else {
                                    lastHeight = heightMapImage.getRGB(x, z - 1) & 0xFF;
                                }
                                int height = heightMapImage.getRGB(x, z) & 0xFF;

                                if (height == lastHeight) {
                                    shade = 220;
                                } else if (height < lastHeight) {
                                    shade = 180;
                                } else {
                                    shade = 255;
                                }
                            } else {
                                double d3 = (double) depth * 0.02D + (double) ((x + z) & 0b1) * 0.2D;
                                if(d3 < 0.5D) {
                                    shade = 255;
                                } else if (d3 > 0.9D) {
                                    shade = 180;
                                } else {
                                    shade = 220;
                                }
                            }


                            if (shade != 255) {
                                final int current = terrainMapImage.getRGB(x, z) ;
                                final int a = (((current >> 24) & 0xFF));
                                final int r = (((current >> 16) & 0xFF) * shade) / 0xFF;
                                final int g = (((current >>  8) & 0xFF) * shade) / 0xFF;
                                final int b = (((current      ) & 0xFF) * shade) / 0xFF;
                                terrainMapImage.setRGB(x, z, (a << 24 | r << 16 | g << 8 | b));
                            }
                        }
                    }
                } finally {
                    hasPostProcessed = true;
                }
            });
        }
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

    public void kill() {
        if (processTask != null) {
            processTask.cancel(true);
        }
        if (loadTask != null) {
            loadTask.cancel(true);
        }
    }
}

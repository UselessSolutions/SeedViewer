package org.useless.seedviewer.gui;

import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ChunkPos2D;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;
import org.useless.seedviewer.gui.components.Viewport;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ChunkView {
    private static final ExecutorService InitializerService = Executors.newFixedThreadPool(10);
    private static final ExecutorService PostProcessorService = Executors.newFixedThreadPool(10);

    private final BufferedImage biomeMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage terrainMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage heightMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z, BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage waterDepthMapImage = new BufferedImage(Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z, BufferedImage.TYPE_INT_ARGB);

    private final Rectangle bounds;
    private final ChunkLocation location;
    private final ChunkProvider provider;
    private final Viewport viewport;

    public long lastSeenTime = System.currentTimeMillis();

    private Future<?> loadTask;
    private Future<?> processTask = null;

    private boolean hasInitialized = false;
    private volatile boolean hasPostProcessed = false;

    public ChunkView(Viewport viewport, ChunkLocation location, ChunkProvider provider) {
        this.viewport = viewport;
        this.location = location;
        this.provider = provider;
        this.bounds = new Rectangle(location.x * Chunk.CHUNK_SIZE_X, location.z * Chunk.CHUNK_SIZE_Z, Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Z);

        loadTask = InitializerService.submit(
            () -> {
                try {
                    Chunk chunk = provider.getChunk(location);

                    for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                            ChunkPos2D pos2D = new ChunkPos2D(x, z);
                            int h = chunk.getHeight(pos2D) - 1;
                            if (h < 0) h = 0;
                            heightMapImage.setRGB(x, z, h & 0xFF);

                            int wd = chunk.getWaterDepth(pos2D) & 0xFF;
                            waterDepthMapImage.setRGB(x, z, 0xFF_00_00_00 | wd << 16 | wd << 8 | wd);

                            Biome b = chunk.getBiome(new ChunkPos3D(x, h, z));
                            biomeMapImage.setRGB(x, z, b.getColor() | 0x80_00_00_00);

                            terrainMapImage.setRGB(x, z, chunk.getBlockColor(new ChunkPos3D(x, h, z)) | 0xFF_00_00_00);
                        }
                    }
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
                            int depth = waterDepthMapImage.getRGB(x, z) & 0xFF;

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
                                double d3 = (double) depth * 0.1D + (double) ((x + z) & 0b1) * 0.2D;
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
                    viewport.repaint();
                }
            });
        }
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
        return bounds;
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

package org.useless.seedviewer.gui.components;

import org.jetbrains.annotations.NotNull;
import org.useless.seedviewer.Global;
import org.useless.seedviewer.bta.BTAChunkProvider;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.collections.ObjectWrapper;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;
import org.useless.seedviewer.gui.ChunkProvider;
import org.useless.seedviewer.gui.ChunkView;
import org.useless.seedviewer.gui.SeedViewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Viewport extends JLabel {
    public static final long MS_UNTIL_CHUNK_UNLOAD = 5000;
    public static final int VIEWPORT_CHUNKS_OVERSCAN = 4;

    public static final float ZOOM_SENSITIVITY = 0.125f;
    public static final float ZOOM_MIN = 1f;
    public static final float ZOOM_MAX = 16f;

    public final Map<ChunkLocation, ChunkView> chunkViewMap = new HashMap<>();
    private final BufferedImage slimeVignette;

    public ChunkProvider chunkProvider;
    public ObjectWrapper<@NotNull Long> seed = new ObjectWrapper<>(100L);

    public ObjectWrapper<@NotNull Float> zoom = new ObjectWrapper<>(1F);
    public ObjectWrapper<@NotNull Float> viewX = new ObjectWrapper<>(0F);
    public ObjectWrapper<@NotNull Float> viewZ = new ObjectWrapper<>(0F);

    public ObjectWrapper<@NotNull Boolean> showSlimeChunks = new ObjectWrapper<>(true);
    public ObjectWrapper<@NotNull Boolean> showBiomeBorders = new ObjectWrapper<>(true);
    public ObjectWrapper<@NotNull Boolean> showCrosshair = new ObjectWrapper<>(true);

    private final SeedViewer seedViewer;

    private Point lastLeftClickPoint = null;

    public Viewport(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;

        BufferedImage _vignette = null;
        try {
            InputStream stream = Viewport.class.getResourceAsStream("/slime_vignette.png");
            if (stream != null) {
                _vignette = ImageIO.read(stream);
            } else {
                Global.LOGGER.error("Stream for 'slime_vignette.png' is null!");
            }
        } catch (IOException e) {
            Global.LOGGER.error("Failed to load slime vignette!");
        }
        slimeVignette = _vignette;

        seed.addChangeListener(newValue -> chunkProvider = new BTAChunkProvider(newValue));
        try {
            seed.set(Long.parseLong(seedViewer.launchProperties.getProperty("seed", "100")));
        } catch (NumberFormatException ignored){
            seed.set((long) seedViewer.launchProperties.getProperty("seed", "100").hashCode());
        }
        chunkProvider = new BTAChunkProvider(seed.get());
    }

    public void setup() {
        addMouseWheelListener(e -> {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                offsetZoom(-e.getUnitsToScroll() * ZOOM_SENSITIVITY);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastLeftClickPoint = e.getPoint();
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastLeftClickPoint != null) {
                    int dx = e.getX() - lastLeftClickPoint.x;
                    int dy = e.getY() - lastLeftClickPoint.y;
                    lastLeftClickPoint = e.getPoint();

                    offsetView(-dx, dy);
                }
            }
        });
        this.setBorder(new LineBorder(Color.BLACK, 1, false));
        this.setFocusable(true);
    }

    public void onResize(Rectangle newShape) {
        this.setBounds(newShape.x, newShape.y, newShape.width, newShape.height);
    }

    public void tick() {
        Rectangle viewportBounds = getViewportBounds();
        Set<ChunkLocation> removalQueue = new HashSet<>();
        for (ChunkView view : chunkViewMap.values()) {
            if (viewportBounds.intersects(view.getWorldBounds())) {
                view.lastSeenTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - view.lastSeenTime > MS_UNTIL_CHUNK_UNLOAD) {
                removalQueue.add(view.getLocation());
            }
        }
        for (ChunkLocation location : removalQueue) {
            removeChunkView(location);
        }

        ChunkLocation topLeftLocation =
            new ChunkLocation(viewportBounds.x/Chunk.CHUNK_SIZE_X,
                viewportBounds.y/Chunk.CHUNK_SIZE_Z);
        int chunksX = viewportBounds.width/Chunk.CHUNK_SIZE_X;
        int chunksZ = viewportBounds.height/Chunk.CHUNK_SIZE_Z;

        for (int _x = topLeftLocation.x; _x < topLeftLocation.x + chunksX; _x++) {
            for (int _z = topLeftLocation.z; _z < topLeftLocation.z + chunksZ; _z++) {
                ChunkLocation location = new ChunkLocation(_x, _z);
                if (chunkViewMap.containsKey(location)) continue;
                addChunkView(location);
            }
        }
        repaint();
    }

    public void setSeed(long seed) {
        this.seed.set(seed);
        chunkViewMap.clear();
        viewX.set(0F);
        viewZ.set(0F);
        seedViewer.needsResize = true;
    }

    public Rectangle getViewportBounds() {
        final int blockX = (int) Math.floor((viewX.get() - (getWidth()/(zoom.get() * 2))));
        final int blockZ = (int) Math.floor((-viewZ.get() - (getHeight()/(zoom.get() * 2))));
        final int widthBlocks = (int) Math.ceil((getWidth()/zoom.get()));
        final int heightBlocks = (int) Math.ceil((getHeight()/zoom.get()));
        return new Rectangle(blockX - (Chunk.CHUNK_SIZE_X * VIEWPORT_CHUNKS_OVERSCAN), blockZ - (Chunk.CHUNK_SIZE_Z * VIEWPORT_CHUNKS_OVERSCAN), widthBlocks + Chunk.CHUNK_SIZE_X * (VIEWPORT_CHUNKS_OVERSCAN * 2 + 1), heightBlocks + Chunk.CHUNK_SIZE_Z * (VIEWPORT_CHUNKS_OVERSCAN * 2 + 1));
    }

    public synchronized void offsetZoom(float delta) {
        setZoom(zoom.get() + delta);
    }

    public synchronized void setZoom(float newZoom) {
        if (newZoom < Viewport.ZOOM_MIN) newZoom = Viewport.ZOOM_MIN;
        if (newZoom > Viewport.ZOOM_MAX) newZoom = Viewport.ZOOM_MAX;
        zoom.set(newZoom);
        repaint();
    }

    public synchronized void offsetView(float deltaX, float deltaZ) {
        setOffsetView(viewX.get() + (deltaX / zoom.get()), viewZ.get() + (deltaZ / zoom.get()));
    }

    public synchronized void setOffsetView(float newX, float newZ) {
        viewX.set(newX);
        viewZ.set(newZ);
        repaint();
    }

    public synchronized void addChunkView(ChunkLocation location) {
        chunkViewMap.put(location, new ChunkView(location, chunkProvider));
    }

    public synchronized void removeChunkView(ChunkLocation location) {
        chunkViewMap.remove(location);
    }

    public Biome getHoveredBiome() {
        ChunkLocation chunkLocation = new ChunkLocation((int) Math.floor(viewX.get()/ Chunk.CHUNK_SIZE_X), (int) Math.floor(-viewZ.get()/ Chunk.CHUNK_SIZE_Z));
        return chunkProvider.getChunk(chunkLocation).getBiome(new ChunkPos3D(((int) Math.floor(viewX.get())) - chunkLocation.x * Chunk.CHUNK_SIZE_X, 128, ((int) Math.floor(-viewZ.get())) - chunkLocation.z * Chunk.CHUNK_SIZE_Z));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintToGraphics(g);
    }
    public void paintToGraphics(Graphics g) {
        synchronized (this) {
            Rectangle viewportBounds = getViewportBounds();
            for (ChunkView view : chunkViewMap.values()) {
                if (!viewportBounds.intersects(view.getWorldBounds())) continue;
                int blockX = view.getLocation().x * Chunk.CHUNK_SIZE_X;
                int blockZ = view.getLocation().z * Chunk.CHUNK_SIZE_Z;

                int subImgX = (int) Math.floor((blockX - viewX.get()) * zoom.get() + getWidth()/2d);
                int subImgZ = (int) Math.floor((blockZ + viewZ.get()) * zoom.get() + getHeight()/2d);
                int subImgWidth = (int) Math.floor(Chunk.CHUNK_SIZE_X * zoom.get());
                int subImgHeight = (int) Math.floor(Chunk.CHUNK_SIZE_Z * zoom.get());
                g.drawImage(view.getBiomeMapImage(),
                    subImgX,
                    subImgZ,
                    subImgWidth,
                    subImgHeight,
                    null,
                    null);
                if (showSlimeChunks.get() && SeedViewer.isSlimeChunk(seed.get(), view.getLocation())) {
                    Graphics gSlime = g.create();
                    if (slimeVignette == null) {
                        gSlime.setColor(new Color(64, 255, 120, 128));
                        gSlime.fillRect(
                            subImgX,
                            subImgZ,
                            subImgWidth,
                            subImgHeight);
                    } else {
                        gSlime.drawImage(slimeVignette,
                            subImgX,
                            subImgZ,
                            subImgWidth,
                            subImgHeight,
                            null, null);
                    }
                    gSlime.dispose();
                }
            }
            if (showBiomeBorders.get()) {
                int leftChunk = viewportBounds.x / Chunk.CHUNK_SIZE_X;
                int widthChunks = viewportBounds.width / Chunk.CHUNK_SIZE_X;
                int topChunk = viewportBounds.y / Chunk.CHUNK_SIZE_Z;
                int heightChunks = viewportBounds.height / Chunk.CHUNK_SIZE_Z;

                Graphics gBorders = g.create();
                gBorders.setColor(Color.BLACK);
                for (int _x = leftChunk; _x <= leftChunk + widthChunks; _x++) {
                    float blockX = (_x * Chunk.CHUNK_SIZE_X);
                    int subImgX = (int) Math.floor((blockX - viewX.get()) * zoom.get() + getWidth()/2d);

                    gBorders.drawLine(subImgX, 0, subImgX, getHeight());
                }
                for (int _z = topChunk; _z < topChunk + heightChunks; _z++) {
                    float blockZ = (_z * Chunk.CHUNK_SIZE_Z);
                    int subImgZ = (int) Math.floor((blockZ + viewZ.get()) * zoom.get() + getHeight()/2d);

                    gBorders.drawLine(0, subImgZ, getWidth(), subImgZ);
                }
                gBorders.dispose();
            }
            if (showCrosshair.get()) {
                Graphics gCrosshair = g.create();
                int centX = getWidth()/2;
                int centZ = getHeight()/2;
                int lineReach = 10;
                int lineWidth = 2;
                gCrosshair.setColor(Color.BLACK);
                gCrosshair.setXORMode(Color.WHITE);
                gCrosshair.fillRect(centX - lineReach, centZ - lineWidth/2, lineReach * 2, lineWidth);
                gCrosshair.fillRect(centX - lineWidth/2, centZ - lineReach, lineWidth, lineReach * 2);
                gCrosshair.dispose();
            }
        }
    }
}

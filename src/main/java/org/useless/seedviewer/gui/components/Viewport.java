package org.useless.seedviewer.gui.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.collections.ObjectWrapper;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;
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

public class Viewport extends JPanel {
    private static final BufferedImage SLIME_VIGNETTE;
    static {
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
        SLIME_VIGNETTE = _vignette;
    }
    public static final long MS_UNTIL_CHUNK_UNLOAD = 5000;
    public static final int VIEWPORT_CHUNKS_OVERSCAN = 4;

    public static final float ZOOM_SENSITIVITY = 0.125f;
    public static final float ZOOM_MIN = 1f;
    public static final float ZOOM_MAX = 16f;


    public final ObjectWrapper<@NotNull Float> zoom = new ObjectWrapper<>(1F);
    public final ObjectWrapper<@NotNull Float> viewX = new ObjectWrapper<>(0F);
    public final ObjectWrapper<@NotNull Float> viewZ = new ObjectWrapper<>(0F);

    public final ObjectWrapper<@NotNull Boolean> showSlimeChunks = new ObjectWrapper<>(true);
    public final ObjectWrapper<@NotNull Boolean> showBiomeBorders = new ObjectWrapper<>(true);
    public final ObjectWrapper<@NotNull Boolean> showCrosshair = new ObjectWrapper<>(true);

    public final Map<Point, SubFrame> chunkFrames = new HashMap<>();
    private final SubFrame subFrame;

    private final SeedViewer seedViewer;

    private Point lastLeftClickPoint = null;

    private boolean didConstruct = false;
    public Viewport(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;
        this.subFrame = new SubFrame(new Point(0, 0), this);
        chunkFrames.put(subFrame.framePos, subFrame);
        didConstruct = true;
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

        this.add(subFrame);
    }

    public void onResize(Rectangle newShape) {
        this.setBounds(newShape.x, newShape.y, newShape.width, newShape.height);
        repositionFrames();
    }

    private void repositionFrames() {
        if (!didConstruct) return;
        for (SubFrame frame : chunkFrames.values()) {
            int pX = (int) Math.floor(-viewX.get() + frame.framePos.x * SubFrame.CHUNKS_X);
            int pZ = (int) Math.floor(viewZ.get() + frame.framePos.y * SubFrame.CHUNKS_Z);
            frame.setBounds(pX, pZ, SubFrame.CHUNKS_X * Chunk.CHUNK_SIZE_X, SubFrame.CHUNKS_Z * Chunk.CHUNK_SIZE_Z);
        }
    }

    public void tick() {
        for (SubFrame frame : chunkFrames.values()) {
            frame.tick();
        }
        repaint();
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
//        repaint();
    }

    public synchronized void offsetView(float deltaX, float deltaZ) {
        setOffsetView(viewX.get() + (deltaX / zoom.get()), viewZ.get() + (deltaZ / zoom.get()));
    }

    public synchronized void setOffsetView(float newX, float newZ) {
        viewX.set(newX);
        viewZ.set(newZ);
//        repaint();
    }

    public Biome getHoveredBiome() {
        ChunkLocation chunkLocation = new ChunkLocation((int) Math.floor(viewX.get()/ Chunk.CHUNK_SIZE_X), (int) Math.floor(-viewZ.get()/ Chunk.CHUNK_SIZE_Z));
        return seedViewer.chunkProvider.getChunk(chunkLocation).getBiome(new ChunkPos3D(((int) Math.floor(viewX.get())) - chunkLocation.x * Chunk.CHUNK_SIZE_X, 128, ((int) Math.floor(-viewZ.get())) - chunkLocation.z * Chunk.CHUNK_SIZE_Z));
    }

    @Override
    public void repaint() {
//        repositionFrames();
//        if (chunkFrames != null) {
//            for (SubFrame frame : chunkFrames.values()) {
//                frame.repaint();
//            }
//        }
        super.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintToGraphics(g);
    }
    public void paintToGraphics(Graphics g) {
        synchronized (this) {
            Rectangle viewportBounds = getViewportBounds();
//            for (ChunkView view : chunkViewMap.values()) {
//                if (!viewportBounds.intersects(view.getWorldBounds())) continue;
//                int blockX = view.getLocation().x * Chunk.CHUNK_SIZE_X;
//                int blockZ = view.getLocation().z * Chunk.CHUNK_SIZE_Z;
//
//                int subImgX = (int) Math.floor((blockX - viewX.get()) * zoom.get() + getWidth()/2d);
//                int subImgZ = (int) Math.floor((blockZ + viewZ.get()) * zoom.get() + getHeight()/2d);
//                int subImgWidth = (int) Math.floor(Chunk.CHUNK_SIZE_X * zoom.get());
//                int subImgHeight = (int) Math.floor(Chunk.CHUNK_SIZE_Z * zoom.get());
//                g.drawImage(view.getBiomeMapImage(),
//                    subImgX,
//                    subImgZ,
//                    subImgWidth,
//                    subImgHeight,
//                    null,
//                    null);
//                if (showSlimeChunks.get() && SeedViewer.isSlimeChunk(seedViewer.seed.get(), view.getLocation())) {
//                    Graphics gSlime = g.create();
//                    if (slimeVignette == null) {
//                        gSlime.setColor(new Color(64, 255, 120, 128));
//                        gSlime.fillRect(
//                            subImgX,
//                            subImgZ,
//                            subImgWidth,
//                            subImgHeight);
//                    } else {
//                        gSlime.drawImage(slimeVignette,
//                            subImgX,
//                            subImgZ,
//                            subImgWidth,
//                            subImgHeight,
//                            null, null);
//                    }
//                    gSlime.dispose();
//                }
//                if (showBiomeBorders.get()) {
//                    Graphics gBorders = g.create();
//                    gBorders.setColor(Color.BLACK);
//                    gBorders.drawRect(
//                        subImgX,
//                        subImgZ,
//                        subImgWidth,
//                        subImgHeight
//                    );
//                    gBorders.dispose();
//                }
//            }
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

    static class SubFrame extends JLabel {
        public static final int CHUNKS_X = 16;
        public static final int CHUNKS_Z = 16;
        private final @Nullable ChunkView[] chunkViews = new ChunkView[CHUNKS_X * CHUNKS_Z];

        private final Point framePos;
        private final Viewport viewport;

        public SubFrame(Point framePos, Viewport viewport) {
            this.framePos = framePos;
            this.viewport = viewport;
        }

        public void setChunkView(int x, int z, ChunkView view) {
            if (x < 0 || x >= 16) {
                Global.LOGGER.error("X out of range! Skipping!");
            }
            if (z < 0 || z >= 16) {
                Global.LOGGER.error("X out of range! Skipping!");
            }
            chunkViews[getArrayIndex(x, z)] = view;
        }

        public ChunkView getChunkView(int x, int z) {
            if (x < 0 || x >= 16) {
                Global.LOGGER.error("X out of range! Skipping!");
            }
            if (z < 0 || z >= 16) {
                Global.LOGGER.error("X out of range! Skipping!");
            }
            return chunkViews[getArrayIndex(x, z)];
        }

        private int getArrayIndex(int x, int z) {
            return z * CHUNKS_X + x;
        }

        public void tick() {
            Rectangle viewportBounds = viewport.getViewportBounds();
            for (int i = 0; i < chunkViews.length; i++) {
                ChunkView view = chunkViews[i];
                if (view == null) continue;
                if (viewportBounds.intersects(view.getWorldBounds())) {
                    view.lastSeenTime = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - view.lastSeenTime > MS_UNTIL_CHUNK_UNLOAD) {
                    chunkViews[i] = null;
                }
            }

            Rectangle subFrameBounds = getFrameBounds();
            ChunkLocation topLeftLocation =
                new ChunkLocation(subFrameBounds.x/Chunk.CHUNK_SIZE_X,
                    subFrameBounds.y/Chunk.CHUNK_SIZE_Z);
            int chunksX = subFrameBounds.width/Chunk.CHUNK_SIZE_X;
            int chunksZ = subFrameBounds.height/Chunk.CHUNK_SIZE_Z;

            for (int _x = topLeftLocation.x; _x < topLeftLocation.x + chunksX; _x++) {
                for (int _z = topLeftLocation.z; _z < topLeftLocation.z + chunksZ; _z++) {
                    int index = getArrayIndex(_x, _z);
                    if (chunkViews[index] != null) continue;
                    chunkViews[index] = new ChunkView(new ChunkLocation(_x, _z), viewport.seedViewer.chunkProvider);
                }
            }
        }

        public Rectangle getFrameBounds() {
            final int blockX = framePos.x * CHUNKS_X * Chunk.CHUNK_SIZE_X;
            final int blockZ = framePos.y * CHUNKS_Z * Chunk.CHUNK_SIZE_Z;
            final int width = CHUNKS_X * Chunk.CHUNK_SIZE_X;
            final int height = CHUNKS_Z * Chunk.CHUNK_SIZE_Z;
            return new Rectangle(blockX, blockZ, width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle viewportBounds = viewport.getViewportBounds();
            for (ChunkView view : chunkViews) {
                if (view == null ||!viewportBounds.intersects(view.getWorldBounds())) continue;
                int blockX = view.getLocation().x * Chunk.CHUNK_SIZE_X;
                int blockZ = view.getLocation().z * Chunk.CHUNK_SIZE_Z;

                int subImgX = (int) Math.floor((blockX) + getWidth() / 2d);
                int subImgZ = (int) Math.floor((blockZ) + getHeight() / 2d);
                int subImgWidth = Chunk.CHUNK_SIZE_X;
                int subImgHeight = Chunk.CHUNK_SIZE_Z;
                g.drawImage(view.getBiomeMapImage(),
                    subImgX,
                    subImgZ,
                    subImgWidth,
                    subImgHeight,
                    null,
                    null);
            }
        }
    }
}

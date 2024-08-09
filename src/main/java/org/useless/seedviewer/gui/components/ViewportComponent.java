package org.useless.seedviewer.gui.components;

import org.jetbrains.annotations.NotNull;
import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ObjectWrapper;
import org.useless.seedviewer.data.Chunk;
import org.useless.seedviewer.gui.ChunkView;
import org.useless.seedviewer.gui.SeedViewer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class ViewportComponent extends JLabel {
    public static final float ZOOM_SENSITIVITY = 0.125f;
    public static final float ZOOM_MIN = 1f;
    public static final float ZOOM_MAX = 16f;

    private BufferedImage biomeImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    public ObjectWrapper<@NotNull Float> zoom = new ObjectWrapper<>(1F);
    public ObjectWrapper<@NotNull Float> viewX = new ObjectWrapper<>(0F);
    public ObjectWrapper<@NotNull Float> viewZ = new ObjectWrapper<>(0F);

    private final SeedViewer seedViewer;

    private Point lastLeftClickPoint = null;

    public ViewportComponent(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;
    }

    public void setup() {
        addMouseWheelListener(e -> {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                seedViewer.offsetZoom(-e.getUnitsToScroll() * ZOOM_SENSITIVITY);
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

                    seedViewer.offsetView(dx, dy);
                }
            }
        });
        this.setBorder(new LineBorder(Color.BLACK, 1, false));
        this.setFocusable(true);
    }

    public void onResize(Rectangle newShape) {
        biomeImage = new BufferedImage(newShape.width, newShape.height, BufferedImage.TYPE_INT_ARGB);

        this.setBounds(newShape.x, newShape.y, newShape.width, newShape.height);
        this.setIcon(new ImageIcon(biomeImage));
    }

    public void tick() {
        final byte OVER_SCAN = 4;
        ChunkLocation topLeftLocation =
            new ChunkLocation(
                (int) ((-viewX.get() - (biomeImage.getWidth()/(zoom.get() * 2)))/ Chunk.CHUNK_SIZE_X) - OVER_SCAN,
                (int) ((-viewZ.get() - (biomeImage.getHeight()/(zoom.get() * 2)))/Chunk.CHUNK_SIZE_Z) - OVER_SCAN);
        int chunksX = (int) Math.ceil(biomeImage.getWidth()/(Chunk.CHUNK_SIZE_X * zoom.get())) + (OVER_SCAN * 2);
        int chunksZ = (int) Math.ceil(biomeImage.getHeight()/(Chunk.CHUNK_SIZE_Z * zoom.get())) + (OVER_SCAN * 2);

        Set<ChunkLocation> offScreenLocations = new HashSet<>(seedViewer.chunkViewMap.keySet());
        for (int _x = topLeftLocation.x; _x < topLeftLocation.x + chunksX; _x++) {
            for (int _z = topLeftLocation.z; _z < topLeftLocation.z + chunksZ; _z++) {
                ChunkLocation location = new ChunkLocation(_x, _z);
                offScreenLocations.remove(location);
                if (seedViewer.chunkViewMap.containsKey(location)) continue;
                seedViewer.addChunkView(location);
            }
        }
        for (ChunkLocation location : offScreenLocations) {
            seedViewer.removeChunkView(location);
        }
        updateImage();
    }
    public synchronized void updateImage() {
        Global.LOGGER.debug("Start update image");
        Graphics g = biomeImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, biomeImage.getWidth(), biomeImage.getHeight());
        for (ChunkView view : seedViewer.chunkViewMap.values()) {
            int blockX = view.getLocation().x * Chunk.CHUNK_SIZE_X;
            int blockZ = view.getLocation().z * Chunk.CHUNK_SIZE_Z;

            int subImgX = (int) Math.floor((blockX + viewX.get()) * zoom.get() + biomeImage.getWidth()/2d);
            int subImgZ = (int) Math.floor((blockZ + viewZ.get()) * zoom.get() + biomeImage.getHeight()/2d);
            int subImgWidth = (int) Math.floor(Chunk.CHUNK_SIZE_X * zoom.get());
            int subImgHeight = (int) Math.floor(Chunk.CHUNK_SIZE_Z * zoom.get());
            g.drawImage(view.getBiomeMapImage(),
                subImgX,
                subImgZ,
                subImgWidth,
                subImgHeight,
                Color.BLACK,
                null);
            if (seedViewer.showSlimeChunks && SeedViewer.isSlimeChunk(seedViewer.seed.get(), view.getLocation())) {
                g.setColor(new Color(64, 255, 120, 128));
                g.fillRect(
                    subImgX,
                    subImgZ,
                    subImgWidth,
                    subImgHeight);
            }
            if (seedViewer.showBiomeBorders) {
                g.setColor(Color.BLACK);
                g.drawRect(
                    subImgX,
                    subImgZ,
                    subImgWidth,
                    subImgHeight
                );
            }
        }
        if (seedViewer.showCrosshair) {
            int centX = biomeImage.getWidth()/2;
            int centZ = biomeImage.getHeight()/2;
            int lineReach = 10;
            int lineWidth = 2;
            g.setColor(Color.BLACK);
            g.setXORMode(Color.WHITE);
            g.fillRect(centX - lineReach, centZ - lineWidth/2, lineReach * 2, lineWidth);
            g.fillRect(centX - lineWidth/2, centZ - lineReach, lineWidth, lineReach * 2);
        }
        g.dispose();
        repaint();
        Global.LOGGER.debug("Finished Image Update");
    }
}

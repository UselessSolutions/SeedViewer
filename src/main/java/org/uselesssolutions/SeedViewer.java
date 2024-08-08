package org.uselesssolutions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.uselesssolutions.collections.ChunkLocation;
import org.uselesssolutions.components.ViewportComponent;
import org.uselesssolutions.data.Chunk;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class SeedViewer {
    public static final int TICKS_PER_SECOND = 10;
    public static final float ZOOM_SENSITIVITY = 0.25f;
    public final Properties launchProperties;

    private final Map<ChunkLocation, ChunkView> chunkViewMap = new HashMap<>();
    private BufferedImage biomeImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private static final float ZOOM_MIN = 1f;
    private static final float ZOOM_MAX = 8f;
    private float zoom = 1f;

    private float viewX = 0;
    private float viewZ = 0;

    private final JFrame mainFrame;
    private JLabel imageFrame;
    private JButton button;

    public SeedViewer(Properties properties) {
        this.launchProperties = properties;
        mainFrame = createFrame();
        initComponents(null);

        new Thread(
            () -> {
                while (true) {
                    try {
                        tick();
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1000/TICKS_PER_SECOND);
                    } catch (InterruptedException e) {
                    }
                }
            }
        ).start();
    }

    public @NotNull JFrame createFrame() {
        // Creating instance of JFrame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("BTA Seed Viewer!");

        // Creating instance of JButton
        button = new JButton("Random Seed");
        button.addActionListener(e -> {
            launchProperties.setProperty("seed", String.valueOf(new Random().nextLong()));
            initComponents(null);
        });

        imageFrame = new ViewportComponent(this);
        imageFrame.setBorder(new LineBorder(Color.DARK_GRAY, 3, true));
        imageFrame.setFocusable(true);

        // adding button in JFrame
        frame.add(button);
        frame.add(imageFrame);
        frame.setSize(960, 720);

        frame.setResizable(true);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                initComponents(e);
            }
        }); // Register `initComponents` to run when frame is resized
        frame.setLayout(null); // using no layout managers
        frame.setVisible(true); // making the frame visible

        return frame;
    }

    public void initComponents(@Nullable ComponentEvent event) {
        int screenWidth = mainFrame.getContentPane().getWidth();
        int screenHeight = mainFrame.getContentPane().getHeight();

        int bWidth = (int) (screenWidth * 0.5f);
        int bHeight = (int) (screenHeight * 0.05f);
        button.setBounds((screenWidth - bWidth)/2, screenHeight - bHeight - 15, bWidth, bHeight);

        int imgWidth = screenWidth - (30 * 2);
        int imgHeight = (screenHeight - bHeight - (30 * 2));

        biomeImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);

        imageFrame.setBounds(30, 30, imgWidth, imgHeight);
        imageFrame.setIcon(new ImageIcon(biomeImage));
    }

    public void updateImage() {
        Graphics g = biomeImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, biomeImage.getWidth(), biomeImage.getHeight());
        for (ChunkView view : chunkViewMap.values()) {
            int blockX = view.getLocation().x * Chunk.CHUNK_SIZE_X;
            int blockZ = view.getLocation().z * Chunk.CHUNK_SIZE_Z;

            int subImgX = (int) Math.floor((blockX + viewX) * zoom + biomeImage.getWidth()/2d);
            int subImgZ = (int) Math.floor((blockZ + viewZ) * zoom + biomeImage.getHeight()/2d);
            int subImgWidth = (int) Math.floor(Chunk.CHUNK_SIZE_X * zoom);
            int subImgHeight = (int) Math.floor(Chunk.CHUNK_SIZE_Z * zoom);
            g.drawImage(view.getBiomeMapImage(),
                subImgX,
                subImgZ,
                subImgWidth,
                subImgHeight,
                Color.BLACK,
                null);
            if (isSlimeChunk(Long.parseLong(launchProperties.getProperty("seed", "100")), view.getLocation())) {
                g.setColor(new Color(64, 255, 120, 128));
                g.fillRect(
                    subImgX,
                    subImgZ,
                    subImgWidth,
                    subImgHeight);
            }
        }
        g.dispose();
        imageFrame.setIcon(new ImageIcon(biomeImage));
    }

    public void tick() {
        ChunkLocation topLeftLocation = new ChunkLocation((int) ((viewX - biomeImage.getWidth()/2d)/Chunk.CHUNK_SIZE_X), (int) ((viewZ - biomeImage.getHeight()/2d)/Chunk.CHUNK_SIZE_Z));

        int chunksX = (int) Math.ceil((double) biomeImage.getWidth()/Chunk.CHUNK_SIZE_X);
        int chunksZ = (int) Math.ceil((double) biomeImage.getHeight()/Chunk.CHUNK_SIZE_Z);
        for (int _x = topLeftLocation.x; _x < topLeftLocation.x + chunksX; _x++) {
            for (int _z = topLeftLocation.z; _z < topLeftLocation.z + chunksZ; _z++) {
                ChunkLocation location = new ChunkLocation(_x, _z);
                if (chunkViewMap.containsKey(location)) continue;
                addChunkView(location);
            }
        }
        updateImage();
    }

    public void offsetZoom(float delta) {
        zoom += delta;
        if (zoom < ZOOM_MIN) zoom = ZOOM_MIN;
        if (zoom > ZOOM_MAX) zoom = ZOOM_MAX;
        updateImage();
    }

    public void offsetView(float deltaX, float deltaZ) {
        viewX += (deltaX / zoom);
        viewZ += (deltaZ / zoom);
        updateImage();
    }

    public void addChunkView(ChunkLocation location) {
        chunkViewMap.put(location, new ChunkView(location, location1 -> null));
    }

    public void removeChunkView(ChunkLocation location) {
        chunkViewMap.remove(location);
    }

    public static boolean isSlimeChunk(long worldSeed, @NotNull ChunkLocation location) {
        long slimeXOR = 0x3AD8025FL;
        Random slimerandom = new Random(worldSeed + (location.x * location.x * 0x4c1906L) + (location.x * 0x5ac0dbL) + (location.z * location.z * 0x4307a7L) + (location.z * 0x5f24fL) ^ slimeXOR);
        return slimerandom.nextInt(10) == 0;
    }
}

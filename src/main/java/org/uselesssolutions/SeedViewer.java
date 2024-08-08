package org.uselesssolutions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.uselesssolutions.bta.BTAChunkProvider;
import org.uselesssolutions.collections.ChunkLocation;
import org.uselesssolutions.components.ViewportComponent;
import org.uselesssolutions.data.Chunk;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class SeedViewer {
    // Static Configuration
    public static final int TICKS_PER_SECOND = 10;
    public static final float ZOOM_SENSITIVITY = 0.25f;
    private static final float ZOOM_MIN = 1f;
    private static final float ZOOM_MAX = 8f;

    // Storage
    public final Properties launchProperties;
    private final Map<ChunkLocation, ChunkView> chunkViewMap = new HashMap<>();
    private BufferedImage biomeImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    // Configuration
    protected ChunkProvider chunkProvider;
    protected ObjectWrapper<@NotNull Long> seed = new ObjectWrapper<>(100L);

    // User State
    protected ObjectWrapper<@NotNull Float> zoom = new ObjectWrapper<>(1F);

    protected ObjectWrapper<@NotNull Float> viewX = new ObjectWrapper<>(0F);
    protected ObjectWrapper<@NotNull Float> viewZ = new ObjectWrapper<>(0F);

    private boolean showSlimeChunks = true;
    private boolean showBiomeBorders = true;
    private boolean showCrosshair = true;
    // Components

    private final JFrame mainFrame;
    private JLabel imageFrame;
    private JLabel seedLabel;
    private JLabel viewLabel;
    private JLabel zoomLabel;
    private JCheckBox slimeChunksBox;
    private JCheckBox showBordersBox;
    private JCheckBox showCrosshairBox;
    private JButton button;

    public SeedViewer(Properties properties) {
        this.launchProperties = properties;
        mainFrame = createFrame();
        try {
            seed.set(Long.parseLong(properties.getProperty("seed", "10000")));
        } catch (NumberFormatException ignored){}

        chunkProvider = new BTAChunkProvider(seed.get());
        seed.addChangeListener(newValue -> chunkProvider = new BTAChunkProvider(newValue));

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
        frame.setSize(960, 720);
        frame.setResizable(true);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                initComponents(e);
                updateImage();
            }
        }); // Register `initComponents` to run when frame is resized
        frame.setLayout(null); // using no layout managers
        frame.setVisible(true); // making the frame visible

        // State labels
        seedLabel = new JLabel("Seed: " + seed);
        seed.addChangeListener(newValue -> seedLabel.setText("Seed: " + seed));

        viewLabel = new JLabel(String.format("View: X:%s, Z:%s", viewX, viewZ));
        viewX.addChangeListener(newValue -> viewLabel.setText(String.format("View: X:%.2f, Z:%.2f", viewX.get(), viewZ.get())));
        viewZ.addChangeListener(newValue -> viewLabel.setText(String.format("View: X:%.2f, Z:%.2f", viewX.get(), viewZ.get())));

        zoomLabel = new JLabel("Zoom: " + zoom);
        zoom.addChangeListener(newValue -> zoomLabel.setText("Zoom: " + zoom));

        frame.add(seedLabel);
        frame.add(viewLabel);
        frame.add(zoomLabel);

        // Checkboxes
        slimeChunksBox = new JCheckBox("Slime Chunks");
        slimeChunksBox.setSelected(showSlimeChunks);
        slimeChunksBox.addChangeListener(e -> showSlimeChunks = slimeChunksBox.isSelected());
        showBordersBox = new JCheckBox("Chunk Borders");
        showBordersBox.setSelected(showBiomeBorders);
        showBordersBox.addChangeListener(e -> showBiomeBorders = showBordersBox.isSelected());
        showCrosshairBox = new JCheckBox("Enable Cross-hair");
        showCrosshairBox.setSelected(showCrosshair);
        showCrosshairBox.addChangeListener(e -> showCrosshair = showCrosshairBox.isSelected());

        frame.add(slimeChunksBox);
        frame.add(showBordersBox);
        frame.add(showCrosshairBox);

        button = new JButton("Random Seed");
        button.addActionListener(e -> {
            seed.set(new Random().nextLong());
            chunkViewMap.clear();
            initComponents(null);
        });
        frame.add(button);

        imageFrame = new ViewportComponent(this);
        imageFrame.setBorder(new LineBorder(Color.DARK_GRAY, 3, true));
        imageFrame.setFocusable(true);
        frame.add(imageFrame);

        return frame;
    }

    public void initComponents(@Nullable ComponentEvent event) {
        int screenWidth = mainFrame.getContentPane().getWidth();
        int screenHeight = mainFrame.getContentPane().getHeight();

        int bezel = 30;

        int bWidth = (int) (screenWidth * 0.5f);
        int bHeight = (int) (screenHeight * 0.05f);
        button.setBounds((screenWidth - bWidth)/2, screenHeight - bHeight - 15, bWidth, bHeight);

        int labels = 3;
        int currLabel = 0;
        int lX = (screenWidth + bWidth)/2 + 20;
        int lY = screenHeight - bHeight - bezel;
        int lHeight = (bHeight + bezel)/labels;
        seedLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);
        viewLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);
        zoomLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);

        int currBox = 0;
        slimeChunksBox.setBounds(bezel, lY + 15 * currBox++, 200, 15);
        showBordersBox.setBounds(bezel, lY + 15 * currBox++, 200, 15);
        showCrosshairBox.setBounds(bezel, lY + 15 * currBox++, 200, 15);

        int imgWidth = screenWidth - (bezel * 2);
        int imgHeight = (screenHeight - bHeight - (bezel * 2));

        biomeImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);

        imageFrame.setBounds(bezel, bezel, imgWidth, imgHeight);
        imageFrame.setIcon(new ImageIcon(biomeImage));
    }

    public synchronized void updateImage() {
        Graphics g = biomeImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, biomeImage.getWidth(), biomeImage.getHeight());
        for (ChunkView view : chunkViewMap.values()) {
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
            if (showSlimeChunks && isSlimeChunk(seed.get(), view.getLocation())) {
                g.setColor(new Color(64, 255, 120, 128));
                g.fillRect(
                    subImgX,
                    subImgZ,
                    subImgWidth,
                    subImgHeight);
            }
            if (showBiomeBorders) {
                g.setColor(Color.BLACK);
                g.drawRect(
                    subImgX,
                    subImgZ,
                    subImgWidth,
                    subImgHeight
                );
            }
        }
        if (showCrosshair) {
            int centX = biomeImage.getWidth()/2;
            int centZ = biomeImage.getHeight()/2;
            int lineReach = 10;
            int lineWidth = 2;
            g.setXORMode(Color.WHITE);
            g.fillRect(centX - lineReach, centZ - lineWidth/2, lineReach * 2, lineWidth);
            g.fillRect(centX - lineWidth/2, centZ - lineReach, lineWidth, lineReach * 2);
        }
        g.dispose();
        imageFrame.repaint();
    }

    public synchronized void tick() {
        final byte OVER_SCAN = 2;
        ChunkLocation topLeftLocation =
            new ChunkLocation(
                (int) ((-viewX.get() - (biomeImage.getWidth()/(zoom.get() * 2)))/Chunk.CHUNK_SIZE_X) - OVER_SCAN,
                (int) ((-viewZ.get() - (biomeImage.getHeight()/(zoom.get() * 2)))/Chunk.CHUNK_SIZE_Z) - OVER_SCAN);
        int chunksX = (int) Math.ceil(biomeImage.getWidth()/(Chunk.CHUNK_SIZE_X * zoom.get())) + (OVER_SCAN * 2);
        int chunksZ = (int) Math.ceil(biomeImage.getHeight()/(Chunk.CHUNK_SIZE_Z * zoom.get())) + (OVER_SCAN * 2);

        Set<ChunkLocation> offScreenLocations = new HashSet<>(chunkViewMap.keySet());
        for (int _x = topLeftLocation.x; _x < topLeftLocation.x + chunksX; _x++) {
            for (int _z = topLeftLocation.z; _z < topLeftLocation.z + chunksZ; _z++) {
                ChunkLocation location = new ChunkLocation(_x, _z);
                offScreenLocations.remove(location);
                if (chunkViewMap.containsKey(location)) continue;
                addChunkView(location);
            }
        }
        for (ChunkLocation location : offScreenLocations) {
            removeChunkView(location);
        }
        updateImage();
    }

    public synchronized void offsetZoom(float delta) {
        setZoom(zoom.get() + delta);
    }

    public synchronized void setZoom(float newZoom) {
        if (newZoom < ZOOM_MIN) newZoom = ZOOM_MIN;
        if (newZoom > ZOOM_MAX) newZoom = ZOOM_MAX;
        zoom.set(newZoom);
        updateImage();
    }

    public synchronized void offsetView(float deltaX, float deltaZ) {
        setOffsetView(viewX.get() + (deltaX / zoom.get()), viewZ.get() + (deltaZ / zoom.get()));
    }

    public synchronized void setOffsetView(float newX, float newZ) {
        viewX.set(newX);
        viewZ.set(newZ);
        updateImage();
    }

    public synchronized void addChunkView(ChunkLocation location) {
        chunkViewMap.put(location, new ChunkView(location, chunkProvider));
    }

    public synchronized void removeChunkView(ChunkLocation location) {
        chunkViewMap.remove(location);
    }

    public static boolean isSlimeChunk(long worldSeed, @NotNull ChunkLocation location) {
        long slimeXOR = 0x3AD8025FL;
        Random slimerandom = new Random(worldSeed + (location.x * location.x * 0x4c1906L) + (location.x * 0x5ac0dbL) + (location.z * location.z * 0x4307a7L) + (location.z * 0x5f24fL) ^ slimeXOR);
        return slimerandom.nextInt(10) == 0;
    }
}

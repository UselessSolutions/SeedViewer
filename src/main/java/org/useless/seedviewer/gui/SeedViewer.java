package org.useless.seedviewer.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ObjectWrapper;
import org.useless.seedviewer.bta.BTAChunkProvider;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.collections.ChunkPos3D;
import org.useless.seedviewer.gui.components.ViewportComponent;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.data.Chunk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class SeedViewer extends JFrame {
    // Static Configuration
    public static final int TICKS_PER_SECOND = 10;

    // Storage
    public final Properties launchProperties;
    private final Map<ChunkLocation, ChunkView> chunkViewMap = new HashMap<>();
    private BufferedImage biomeImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    // Configuration
    protected ChunkProvider chunkProvider;
    protected ObjectWrapper<@NotNull Long> seed = new ObjectWrapper<>(100L);

    // User State
    private boolean showSlimeChunks = true;
    private boolean showBiomeBorders = true;
    private boolean showCrosshair = true;

    // Components
    private final ViewportComponent viewport;

    private JLabel seedLabel;
    private JLabel viewLabel;
    private JLabel zoomLabel;
    private JLabel biomeLabel;
    private JCheckBox slimeChunksBox;
    private JCheckBox showBordersBox;
    private JCheckBox showCrosshairBox;
    private JTextField seedInputBox;


    public SeedViewer(Properties properties) {
        Global.LOGGER.info("Starting Seed Viewer!");

        viewport = new ViewportComponent(this);

        this.launchProperties = properties;
        seed.addChangeListener(newValue -> chunkProvider = new BTAChunkProvider(newValue));
        try {
            seed.set(Long.parseLong(properties.getProperty("seed", "100")));
        } catch (NumberFormatException ignored){
            seed.set((long) properties.getProperty("seed", "100").hashCode());
        }

        initFrame();
        addComponents();

        initComponents(null);

        chunkProvider = new BTAChunkProvider(seed.get());


        new Thread(
            () -> {
                while (true) {
                    try {
                        tick();
                    } catch (Exception e){
                        Global.LOGGER.error("Exception when running tick!", e);
                    }

                    try {
                        Thread.sleep(1000/TICKS_PER_SECOND);
                    } catch (InterruptedException e) {
                        Global.LOGGER.error("Unexpected interrupt in tick thread!", e);
                    }
                }
            }
        ).start();
    }

    public void initFrame() {
        // Creating instance of JFrame
        Global.LOGGER.info("Initializing Frame");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("BTA Seed Viewer!");
        this.setSize(960, 720);
        this.setMinimumSize(new Dimension(480, 360));
        this.setResizable(true);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                initComponents(e);
                updateImage();
            }
        }); // Register `initComponents` to run when frame is resized
        this.setLayout(null); // using no layout managers
        this.setVisible(true); // making the frame visible

        Global.LOGGER.info("Loading Icons");
        try {
            List<Image> l = new ArrayList<>();
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/1024.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/512.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/256.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/128.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/64.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/32.png")));
            l.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/16.png")));
            this.setIconImages(l);
        } catch (Exception e) {
            Global.LOGGER.error("Failed to load icons!", e);
        }
    }

    public void addComponents() {
        // State labels
        Global.LOGGER.info("Creating Labels");
        seedLabel = new JLabel("Seed: " + seed);
        seed.addChangeListener(newValue -> seedLabel.setText("Seed: " + seed));

        viewLabel = new JLabel(String.format("View: X:%s, Z:%s", viewport.viewX, viewport.viewZ));
        viewport.viewX.addChangeListener(newValue -> viewLabel.setText(String.format("View: X:%.2f, Z:%.2f", viewport.viewX.get(), viewport.viewZ.get())));
        viewport.viewX.addChangeListener(newValue -> {
            Biome b = getHoveredBiome();
            if (b == null) {
                biomeLabel.setText(String.format("Biome: %s", b));
            } else {
                biomeLabel.setText(String.format("Biome: %s", b.getName()));
            }
        });
        viewport.viewZ.addChangeListener(newValue -> viewLabel.setText(String.format("View: X:%.2f, Z:%.2f", viewport.viewX.get(), viewport.viewZ.get())));
        viewport.viewZ.addChangeListener(newValue -> {
            Biome b = getHoveredBiome();
            if (b == null) {
                biomeLabel.setText("Biome: null");
            } else {
                biomeLabel.setText(String.format("Biome: %s", b.getName()));
            }
        });

        zoomLabel = new JLabel("Zoom: " + viewport.zoom);
        viewport.zoom.addChangeListener(newValue -> zoomLabel.setText("Zoom: " + viewport.zoom));

        biomeLabel = new JLabel("Biome: None");

        this.add(seedLabel);
        this.add(viewLabel);
        this.add(zoomLabel);
        this.add(biomeLabel);

        // Checkboxes
        Global.LOGGER.info("Creating Check boxes");
        slimeChunksBox = new JCheckBox("Slime Chunks");
        slimeChunksBox.setSelected(showSlimeChunks);
        slimeChunksBox.addChangeListener(e -> showSlimeChunks = slimeChunksBox.isSelected());
        showBordersBox = new JCheckBox("Chunk Borders");
        showBordersBox.setSelected(showBiomeBorders);
        showBordersBox.addChangeListener(e -> showBiomeBorders = showBordersBox.isSelected());
        showCrosshairBox = new JCheckBox("Enable Cross-hair");
        showCrosshairBox.setSelected(showCrosshair);
        showCrosshairBox.addChangeListener(e -> showCrosshair = showCrosshairBox.isSelected());

        this.add(slimeChunksBox);
        this.add(showBordersBox);
        this.add(showCrosshairBox);

        seedInputBox = new JTextField(seed.toString());
        seedInputBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long boxSeed;
                try {
                    boxSeed = Long.parseLong(seedInputBox.getText());
                } catch (NumberFormatException exception) {
                    boxSeed = seedInputBox.getText().hashCode();
                }
                if (boxSeed != seed.get()) {
                    seed.set(boxSeed);
                    chunkViewMap.clear();
                    viewport.viewX.set(0F);
                    viewport.viewZ.set(0F);
                    initComponents(null);
                }
            }
        });

        this.add(seedInputBox);

        Global.LOGGER.info("Creating Image frame");
        viewport.init();
        this.add(viewport);
    }

    public void initComponents(@Nullable ComponentEvent event) {
        Global.LOGGER.info("Initializing Components");
        int screenWidth = this.getContentPane().getWidth();
        int screenHeight = this.getContentPane().getHeight();

        int bezel = 30;

        int bWidth = (int) (screenWidth * 0.5f);
        int bHeight = 40;
        seedInputBox.setBounds((screenWidth - bWidth)/2, screenHeight - bHeight - 15, bWidth, bHeight);

        int labels = 3;
        int currLabel = 0;
        int lX = (screenWidth + bWidth)/2 + 20;
        int lY = screenHeight - bHeight - bezel;
//        int lHeight = (bHeight + bezel)/labels;
        int lHeight = 15;
        seedLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);
        viewLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);
        zoomLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);
        biomeLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);

        int currBox = 0;
        slimeChunksBox.setBounds(bezel, lY + 15 * currBox++, 200, 15);
        showBordersBox.setBounds(bezel, lY + 15 * currBox++, 200, 15);
        showCrosshairBox.setBounds(bezel, lY + 15 * currBox++, 200, 15);

        int imgWidth = screenWidth - (bezel * 2);
        int imgHeight = (screenHeight - bHeight - (bezel * 2));

        biomeImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);

        viewport.setBounds(bezel, bezel, imgWidth, imgHeight);
        viewport.setIcon(new ImageIcon(biomeImage));
    }

    public synchronized void updateImage() {
        Global.LOGGER.debug("Start update image");
        Graphics g = biomeImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, biomeImage.getWidth(), biomeImage.getHeight());
        for (ChunkView view : chunkViewMap.values()) {
            int blockX = view.getLocation().x * Chunk.CHUNK_SIZE_X;
            int blockZ = view.getLocation().z * Chunk.CHUNK_SIZE_Z;

            int subImgX = (int) Math.floor((blockX + viewport.viewX.get()) * viewport.zoom.get() + biomeImage.getWidth()/2d);
            int subImgZ = (int) Math.floor((blockZ + viewport.viewZ.get()) * viewport.zoom.get() + biomeImage.getHeight()/2d);
            int subImgWidth = (int) Math.floor(Chunk.CHUNK_SIZE_X * viewport.zoom.get());
            int subImgHeight = (int) Math.floor(Chunk.CHUNK_SIZE_Z * viewport.zoom.get());
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
            g.setColor(Color.BLACK);
            g.setXORMode(Color.WHITE);
            g.fillRect(centX - lineReach, centZ - lineWidth/2, lineReach * 2, lineWidth);
            g.fillRect(centX - lineWidth/2, centZ - lineReach, lineWidth, lineReach * 2);
        }
        g.dispose();
        viewport.repaint();
        Global.LOGGER.debug("Finished Image Update");
    }

    public synchronized void tick() {
        final byte OVER_SCAN = 4;
        ChunkLocation topLeftLocation =
            new ChunkLocation(
                (int) ((-viewport.viewX.get() - (biomeImage.getWidth()/(viewport.zoom.get() * 2)))/Chunk.CHUNK_SIZE_X) - OVER_SCAN,
                (int) ((-viewport.viewZ.get() - (biomeImage.getHeight()/(viewport.zoom.get() * 2)))/Chunk.CHUNK_SIZE_Z) - OVER_SCAN);
        int chunksX = (int) Math.ceil(biomeImage.getWidth()/(Chunk.CHUNK_SIZE_X * viewport.zoom.get())) + (OVER_SCAN * 2);
        int chunksZ = (int) Math.ceil(biomeImage.getHeight()/(Chunk.CHUNK_SIZE_Z * viewport.zoom.get())) + (OVER_SCAN * 2);

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
        setZoom(viewport.zoom.get() + delta);
    }

    public synchronized void setZoom(float newZoom) {
        if (newZoom < ViewportComponent.ZOOM_MIN) newZoom = ViewportComponent.ZOOM_MIN;
        if (newZoom > ViewportComponent.ZOOM_MAX) newZoom = ViewportComponent.ZOOM_MAX;
        viewport.zoom.set(newZoom);
        updateImage();
    }

    public synchronized void offsetView(float deltaX, float deltaZ) {
        setOffsetView(viewport.viewX.get() + (deltaX / viewport.zoom.get()), viewport.viewZ.get() + (deltaZ / viewport.zoom.get()));
    }

    public synchronized void setOffsetView(float newX, float newZ) {
        viewport.viewX.set(newX);
        viewport.viewZ.set(newZ);
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

    public Biome getHoveredBiome() {
        ChunkLocation chunkLocation = new ChunkLocation((int) Math.floor(-viewport.viewX.get()/ Chunk.CHUNK_SIZE_X), (int) Math.floor(-viewport.viewZ.get()/ Chunk.CHUNK_SIZE_Z));
        return chunkProvider.getChunk(chunkLocation).getBiome(new ChunkPos3D(((int) Math.floor(-viewport.viewX.get())) - chunkLocation.x * Chunk.CHUNK_SIZE_X, 128, ((int) Math.floor(-viewport.viewZ.get())) - chunkLocation.z * Chunk.CHUNK_SIZE_Z));
    }
}

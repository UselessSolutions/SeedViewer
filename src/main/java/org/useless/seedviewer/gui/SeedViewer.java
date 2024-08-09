package org.useless.seedviewer.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.useless.seedviewer.Global;
import org.useless.seedviewer.collections.ObjectWrapper;
import org.useless.seedviewer.bta.BTAChunkProvider;
import org.useless.seedviewer.collections.ChunkLocation;
import org.useless.seedviewer.gui.components.ViewportComponent;
import org.useless.seedviewer.data.Biome;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class SeedViewer extends JFrame {
    // Static Configuration
    public static final int TICKS_PER_SECOND = 10;
    public static final int BEZEL = 30;

    // Storage
    public final Properties launchProperties;

    // Configuration
    public ChunkProvider chunkProvider;
    public ObjectWrapper<@NotNull Long> seed = new ObjectWrapper<>(100L);

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
                long tickCount = 0;
                while (true) {
                    try {
                        tick();
                        tickCount++;
                    } catch (Exception e){
                        Global.LOGGER.error("Exception when running tick!", e);
                    }

                    if (tickCount % (5 * TICKS_PER_SECOND) == 0) {
                        System.gc();
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
                viewport.updateImage();
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
            Biome b = viewport.getHoveredBiome();
            if (b == null) {
                biomeLabel.setText("Biome: null");
            } else {
                biomeLabel.setText(String.format("Biome: %s", b.getName()));
            }
        });
        viewport.viewZ.addChangeListener(newValue -> viewLabel.setText(String.format("View: X:%.2f, Z:%.2f", viewport.viewX.get(), viewport.viewZ.get())));
        viewport.viewZ.addChangeListener(newValue -> {
            Biome b = viewport.getHoveredBiome();
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
        slimeChunksBox.setSelected(viewport.showSlimeChunks.get());
        slimeChunksBox.addChangeListener(e -> viewport.showSlimeChunks.set(slimeChunksBox.isSelected()));
        showBordersBox = new JCheckBox("Chunk Borders");
        showBordersBox.setSelected(viewport.showBiomeBorders.get());
        showBordersBox.addChangeListener(e -> viewport.showBiomeBorders.set(showBordersBox.isSelected()));
        showCrosshairBox = new JCheckBox("Enable Cross-hair");
        showCrosshairBox.setSelected(viewport.showCrosshair.get());
        showCrosshairBox.addChangeListener(e -> viewport.showCrosshair.set(showCrosshairBox.isSelected()));

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
                    viewport.chunkViewMap.clear();
                    viewport.viewX.set(0F);
                    viewport.viewZ.set(0F);
                    initComponents(null);
                }
            }
        });

        this.add(seedInputBox);

        Global.LOGGER.info("Creating Image frame");
        viewport.setup();
        this.add(viewport);
    }

    public void initComponents(@Nullable ComponentEvent event) {
        Global.LOGGER.info("Initializing Components");
        int screenWidth = this.getContentPane().getWidth();
        int screenHeight = this.getContentPane().getHeight();

        int bWidth = (int) (screenWidth * 0.5f);
        int bHeight = 40;
        seedInputBox.setBounds((screenWidth - bWidth)/2, screenHeight - bHeight - 15, bWidth, bHeight);

        int currLabel = 0;
        int lX = (screenWidth + bWidth)/2 + 20;
        int lY = screenHeight - bHeight - BEZEL;

        int lHeight = 15;
        seedLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);
        viewLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);
        zoomLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);
        biomeLabel.setBounds(lX, lY + lHeight * currLabel++, bWidth, lHeight);

        int currBox = 0;
        slimeChunksBox.setBounds(BEZEL, lY + 15 * currBox++, 200, 15);
        showBordersBox.setBounds(BEZEL, lY + 15 * currBox++, 200, 15);
        showCrosshairBox.setBounds(BEZEL, lY + 15 * currBox++, 200, 15);

        int imgWidth = screenWidth - (BEZEL * 2);
        int imgHeight = (screenHeight - bHeight - (BEZEL * 2));

        viewport.onResize(new Rectangle(BEZEL, BEZEL, imgWidth, imgHeight));
    }

    public synchronized void tick() {
        viewport.tick();
    }

    public static boolean isSlimeChunk(long worldSeed, @NotNull ChunkLocation location) {
        long slimeXOR = 0x3AD8025FL;
        Random slimerandom = new Random(worldSeed + (location.x * location.x * 0x4c1906L) + (location.x * 0x5ac0dbL) + (location.z * location.z * 0x4307a7L) + (location.z * 0x5f24fL) ^ slimeXOR);
        return slimerandom.nextInt(10) == 0;
    }
}

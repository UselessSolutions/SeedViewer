package org.useless.seedviewer.gui.components;

import net.minecraft.core.Global;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.gui.SeedViewer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InfoPanel extends JPanel {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private final SeedViewer seedViewer;
    public JLabel titleLabel;

    public JLabel btaLabel;
    public JLabel seedLabel;
    public JLabel viewLabel;
    public JLabel zoomLabel;
    public JLabel biomeLabel;

    public JLabel worldNameLabel;
    public JLabel worldSeedLabel;
    public JLabel worldPlayedLabel;

    public List<Component> resizeList = new ArrayList<>();

    public InfoPanel(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;
        this.setLayout(null);
        this.setBorder(new LineBorder(Color.BLACK, 1));
    }

    public void setup() {
        final int textHeight = 15;

        titleLabel = new JLabel("Information: ");
        titleLabel.setSize(0, textHeight);
        this.add(titleLabel);

        try {
            Class.forName("net.minecraft.core.Global");
            btaLabel = new JLabel("BTA Version: " + Global.VERSION);
        } catch (ClassNotFoundException e) {
            btaLabel = new JLabel("BTA Version: Missing!");
        }
        add(btaLabel);
        resizeList.add(btaLabel);

        seedLabel = new JLabel("Seed: " + seedViewer.viewport.seed);
        seedLabel.setSize(0, textHeight);
        seedViewer.viewport.seed.addChangeListener(newValue -> seedLabel.setText("Seed: " + seedViewer.viewport.seed));

        viewLabel = new JLabel(String.format("View: X:%s, Z:%s", seedViewer.viewport.viewX, seedViewer.viewport.viewZ));
        viewLabel.setSize(0, textHeight);
        seedViewer.viewport.viewX.addChangeListener(newValue -> viewLabel.setText(String.format("View: X:%.2f, Z:%.2f", seedViewer.viewport.viewX.get(), seedViewer.viewport.viewZ.get())));
        seedViewer.viewport.viewX.addChangeListener(newValue -> {
            Biome b = seedViewer.viewport.getHoveredBiome();
            if (b == null) {
                biomeLabel.setText("Biome: null");
            } else {
                biomeLabel.setText(String.format("Biome: %s", b.getName()));
            }
        });
        seedViewer.viewport.viewZ.addChangeListener(newValue -> viewLabel.setText(String.format("View: X:%.2f, Z:%.2f", seedViewer.viewport.viewX.get(), seedViewer.viewport.viewZ.get())));
        seedViewer.viewport.viewZ.addChangeListener(newValue -> {
            Biome b = seedViewer.viewport.getHoveredBiome();
            if (b == null) {
                biomeLabel.setText("Biome: null");
            } else {
                biomeLabel.setText(String.format("Biome: %s", b.getName()));
            }
        });

        zoomLabel = new JLabel("Zoom: " + seedViewer.viewport.zoom);
        zoomLabel.setSize(0, textHeight);
        seedViewer.viewport.zoom.addChangeListener(newValue -> zoomLabel.setText("Zoom: " + seedViewer.viewport.zoom));

        biomeLabel = new JLabel("Biome: None");
        biomeLabel.setSize(0, textHeight);

        worldNameLabel = new JLabel();
        worldNameLabel.setSize(0, textHeight);
        seedViewer.viewport.world.addChangeListener(newValue -> worldNameLabel.setText("World: " + (seedViewer.viewport.world.get() == null ? "null" : seedViewer.viewport.world.get().getName())));
        seedViewer.viewport.world.addChangeListener(newValue -> {
            if (newValue != null) {
                onWorldOpen();
                seedViewer.queueResize();
            } else {
                onWorldClose();
                seedViewer.queueResize();
            }
        });

        worldSeedLabel = new JLabel();
        worldSeedLabel.setSize(0, textHeight);
        seedViewer.viewport.world.addChangeListener(newValue -> worldSeedLabel.setText("Seed: " + (seedViewer.viewport.world.get() == null ? "null" : seedViewer.viewport.world.get().getSeed())));

        worldPlayedLabel = new JLabel();
        worldPlayedLabel.setSize(0, textHeight);
        seedViewer.viewport.world.addChangeListener(newValue -> worldPlayedLabel.setText("Last Played: " + (seedViewer.viewport.world.get() == null ? "?" : DateFormat.getDateTimeInstance().format(new Date(seedViewer.viewport.world.get().getLastPlayed())))));

        addManaged(seedLabel);
        addManaged(viewLabel);
        addManaged(zoomLabel);
        addManaged(biomeLabel);
        addManaged(worldNameLabel);
        addManaged(worldSeedLabel);
        addManaged(worldPlayedLabel);

        if (seedViewer.viewport.world.get() == null) {
            onWorldClose();
        } else {
            onWorldOpen();
        }
    }

    public void addManaged(Component c) {
        add(c);
        resizeList.add(c);
    }

    public void onWorldOpen() {
        worldNameLabel.setEnabled(true);
        worldNameLabel.setVisible(true);
        worldSeedLabel.setEnabled(true);
        worldSeedLabel.setVisible(true);
        worldPlayedLabel.setEnabled(true);
        worldPlayedLabel.setVisible(true);

        seedLabel.setEnabled(false);
        seedLabel.setVisible(false);
    }

    public void onWorldClose() {
        worldNameLabel.setEnabled(false);
        worldNameLabel.setVisible(false);
        worldSeedLabel.setEnabled(false);
        worldSeedLabel.setVisible(false);
        worldPlayedLabel.setEnabled(false);
        worldPlayedLabel.setVisible(false);

        seedLabel.setEnabled(true);
        seedLabel.setVisible(true);
    }

    public void onResize(Rectangle newDimensions) {
        setBounds(newDimensions.x, newDimensions.y, newDimensions.width, newDimensions.height);

        final int edgePad = 5;
        final int elementPad = 2;
        titleLabel.setBounds(edgePad, edgePad, newDimensions.width - edgePad * 2, titleLabel.getHeight());

        int lastY = titleLabel.getY() + titleLabel.getHeight() + edgePad * 2;
        for (Component c : resizeList) {
            if (!c.isVisible()) continue;
            c.setBounds(edgePad, lastY, newDimensions.width - edgePad * 2, c.getHeight());
            lastY += c.getHeight() + elementPad;
        }
    }
}

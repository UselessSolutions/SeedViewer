package org.useless.seedviewer.gui.components;

import net.minecraft.core.Global;
import org.useless.seedviewer.data.Biome;
import org.useless.seedviewer.gui.SeedViewer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InfoPanel extends JPanel {
    private final SeedViewer seedViewer;
    public JLabel titleLabel;

    public JLabel btaLabel;
    public JLabel seedLabel;
    public JLabel worldLabel;
    public JLabel viewLabel;
    public JLabel zoomLabel;
    public JLabel biomeLabel;

    public List<Component> resizeList = new ArrayList<>();

    public InfoPanel(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;
        this.setLayout(null);
        this.setBorder(new LineBorder(Color.BLACK, 1));
    }

    public void setup() {
        titleLabel = new JLabel("Information: ");
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
        seedViewer.viewport.seed.addChangeListener(newValue -> seedLabel.setText("Seed: " + seedViewer.viewport.seed));

        worldLabel = new JLabel("World: " + seedViewer.viewport.world);
        seedViewer.viewport.world.addChangeListener(newValue -> worldLabel.setText("World: " + seedViewer.viewport.world));

        viewLabel = new JLabel(String.format("View: X:%s, Z:%s", seedViewer.viewport.viewX, seedViewer.viewport.viewZ));
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
        seedViewer.viewport.zoom.addChangeListener(newValue -> zoomLabel.setText("Zoom: " + seedViewer.viewport.zoom));

        biomeLabel = new JLabel("Biome: None");

        this.add(seedLabel);
        resizeList.add(seedLabel);
        this.add(worldLabel);
        resizeList.add(worldLabel);
        this.add(viewLabel);
        resizeList.add(viewLabel);
        this.add(zoomLabel);
        resizeList.add(zoomLabel);
        this.add(biomeLabel);
        resizeList.add(biomeLabel);
    }

    public void onResize(Rectangle newDimensions) {
        setBounds(newDimensions.x, newDimensions.y, newDimensions.width, newDimensions.height);

        final int boxHeight = 15;
        final int padding = 5;
        final int textHeight = 15;
        titleLabel.setBounds(padding, padding, newDimensions.width - padding * 2, 15);

        int lastY = titleLabel.getY() + titleLabel.getHeight() + padding * 2;
        for (Component c : resizeList) {
            if (!c.isVisible()) continue;
            if (c instanceof JCheckBox) {
                c.setBounds(padding, lastY, newDimensions.width - padding * 2, boxHeight);
                lastY += boxHeight;
            } else if (c instanceof JTextField || c instanceof JLabel) {
                c.setBounds(padding, lastY, newDimensions.width - padding * 2, textHeight);
                lastY += textHeight;
            }

        }
    }
}

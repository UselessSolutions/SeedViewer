package org.useless.seedviewer.gui.components;

import org.useless.seedviewer.gui.SeedViewer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class InputPanel extends JPanel {
    private final SeedViewer seedViewer;
    public JLabel titleLabel;

    public JCheckBox slimeChunksBox;
    public JCheckBox showBordersBox;
    public JCheckBox showCrosshairBox;
    public JTextField seedInputBox;

    private final List<Component> resizeList = new ArrayList<>();

    public InputPanel(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;
        this.setLayout(null); // using no layout managers
        this.setBorder(new LineBorder(Color.BLACK, 1));
    }

    public void setup() {
        titleLabel = new JLabel("User Controls: ");
        this.add(titleLabel);

        slimeChunksBox = new JCheckBox("Slime Chunks");
        slimeChunksBox.setSelected(seedViewer.viewport.showSlimeChunks.get());
        slimeChunksBox.addChangeListener(e -> seedViewer.viewport.showSlimeChunks.set(slimeChunksBox.isSelected()));
        showBordersBox = new JCheckBox("Chunk Borders");
        showBordersBox.setSelected(seedViewer.viewport.showBiomeBorders.get());
        showBordersBox.addChangeListener(e -> seedViewer.viewport.showBiomeBorders.set(showBordersBox.isSelected()));
        showCrosshairBox = new JCheckBox("Enable Cross-hair");
        showCrosshairBox.setSelected(seedViewer.viewport.showCrosshair.get());
        showCrosshairBox.addChangeListener(e -> seedViewer.viewport.showCrosshair.set(showCrosshairBox.isSelected()));

        this.add(slimeChunksBox);
        resizeList.add(slimeChunksBox);
        this.add(showBordersBox);
        resizeList.add(showBordersBox);
        this.add(showCrosshairBox);
        resizeList.add(showCrosshairBox);

        seedInputBox = new JTextField(seedViewer.seed.toString());
        seedInputBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long boxSeed;
                try {
                    boxSeed = Long.parseLong(seedInputBox.getText());
                } catch (NumberFormatException exception) {
                    boxSeed = seedInputBox.getText().hashCode();
                }
                if (boxSeed != seedViewer.seed.get()) {
                    seedViewer.seed.set(boxSeed);
                    seedViewer.viewport.chunkViewMap.clear();
                    seedViewer.viewport.viewX.set(0F);
                    seedViewer.viewport.viewZ.set(0F);
                    seedViewer.needsResize = true;
                }
            }
        });

        this.add(seedInputBox);
        resizeList.add(seedInputBox);
    }

    public void onResize(Rectangle newDimensions) {
        setBounds(newDimensions.x, newDimensions.y, newDimensions.width, newDimensions.height);


        final int boxHeight = 15;
        final int padding = 5;
        final int textHeight = 30;

        titleLabel.setBounds(padding, padding, newDimensions.width - padding * 2, 15);

        int lastY = titleLabel.getY() + titleLabel.getHeight() + padding * 2;
        for (Component c : resizeList) {
            if (c instanceof JCheckBox) {
                c.setBounds(padding, lastY, newDimensions.width - padding * 2, boxHeight);
                lastY += boxHeight;
            } else if (c instanceof JTextField) {
                c.setBounds(padding, lastY, newDimensions.width - padding * 2, textHeight);
                lastY += textHeight;
            }

        }
    }
}

package org.useless.seedviewer.gui.components;

import org.jetbrains.annotations.NotNull;
import org.useless.seedviewer.collections.ObjectWrapper;
import org.useless.seedviewer.gui.SeedViewer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;

public class ViewportComponent extends JLabel {
    public static final float ZOOM_SENSITIVITY = 0.125f;
    public static final float ZOOM_MIN = 1f;
    public static final float ZOOM_MAX = 16f;

    public ObjectWrapper<@NotNull Float> zoom = new ObjectWrapper<>(1F);
    public ObjectWrapper<@NotNull Float> viewX = new ObjectWrapper<>(0F);
    public ObjectWrapper<@NotNull Float> viewZ = new ObjectWrapper<>(0F);

    private final SeedViewer seedViewer;

    private Point lastLeftClickPoint = null;

    public ViewportComponent(SeedViewer seedViewer) {
        this.seedViewer = seedViewer;
    }

    public void init() {
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
}

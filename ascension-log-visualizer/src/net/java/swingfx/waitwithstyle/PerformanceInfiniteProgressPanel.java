/*
 * Copyright (c) 2005, romain guy (romain.guy@jext.org) and craig wickesser (craig@codecraig.com) and henry story
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.java.swingfx.waitwithstyle;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * A InfiniteProgressPanel-like component, but more efficient. This is the
 * preferred class to use unless you need the total control over the appearance
 * that InfiniteProgressPanel gives you.<br />
 * <br />
 * An infinite progress panel displays a rotating figure and a message to notice
 * the user of a long, duration unknown task. The shape and the text are drawn
 * upon a white veil which alpha level (or shield value) lets the underlying
 * component shine through. This panel is meant to be used as a <i>glass
 * pane</i> in the window performing the long operation. <br />
 * <br />
 * Calling setVisible(true) makes the component visible and starts the
 * animation. Calling setVisible(false) halts the animation and makes the
 * component invisible. Once you've started the animation all the mouse events
 * are intercepted by this panel, preventing them from being forwared to the
 * underlying components. <br />
 * <br />
 * The panel can be controlled by the <code>setVisible()</code>, method. <br />
 * <br />
 * This version of the infinite progress panel does not display any fade in/out
 * when the animation is started/stopped.<br />
 * <br />
 * Example: <br />
 * <br />
 *
 * <pre>
 * PerformanceInfiniteProgressPanel pane = new PerformanceInfiniteProgressPanel();
 * frame.setGlassPane(pane);
 * pane.setVisible(true);
 * // Do something here, presumably launch a new thread
 * // ...
 * // When the thread terminates:
 * pane.setVisible(false);
 * </pre>
 *
 * @see InfiniteProgressPanel <br />
 * <br />
 *      $Revision: 1.5 $
 * @author Romain Guy
 * @author Henry Story
 * @version 1.0
 */
public class PerformanceInfiniteProgressPanel extends JComponent implements
        ActionListener, CancelableAdaptee {
    /**
     *
     */
    private static final long serialVersionUID = 7564909929056647161L;
    private static final int DEFAULT_NUMBER_OF_BARS = 12;
    private final int numBars;
    protected InfiniteProgressAdapter infiniteProgressAdapter;
    private final double dScale = 1.2d;
    private final MouseAdapter mouseAdapter = new MouseAdapter() {
    };
    private final MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
    };
    private final KeyAdapter keyAdapter = new KeyAdapter() {
    };
    private final ComponentAdapter componentAdapter = new ComponentAdapter() {
        @Override
        public void componentResized(final ComponentEvent e) {
            if (PerformanceInfiniteProgressPanel.this.useBackBuffer == true) {
                PerformanceInfiniteProgressPanel.this.setOpaque(false);
                PerformanceInfiniteProgressPanel.this.imageBuf = null;
                PerformanceInfiniteProgressPanel.this.iterate = 3;
            }
        }
    };
    private BufferedImage imageBuf = null;
    private final Area[] bars;
    private Rectangle barsBounds = null;
    private Rectangle barsScreenBounds = null;
    private AffineTransform centerAndScaleTransform = null;
    private final Timer timer = new Timer(1000 / 4, this);
    private Color[] colors = null;
    private int colorOffset = 0;
    private final boolean useBackBuffer;
    private final boolean tempHide = false;
    private String text;

    /**
     * @param i_bUseBackBuffer
     *            When true a screen capture of the underlying window is taken.
     *            Therefore no update in the background can be visible through
     *            this glass pane. Increases performances.
     */
    public PerformanceInfiniteProgressPanel() {
        this(true);
    }

    public PerformanceInfiniteProgressPanel(final boolean i_bUseBackBuffer) {
        this(i_bUseBackBuffer,
                PerformanceInfiniteProgressPanel.DEFAULT_NUMBER_OF_BARS);
    }

    public PerformanceInfiniteProgressPanel(final int numBars) {
        this(true, numBars, null);
    }

    public PerformanceInfiniteProgressPanel(
            final InfiniteProgressAdapter infiniteProgressAdapter) {
        this(true, PerformanceInfiniteProgressPanel.DEFAULT_NUMBER_OF_BARS,
                infiniteProgressAdapter);
    }

    public PerformanceInfiniteProgressPanel(final boolean i_bUseBackBuffer,
            final int numBars) {
        this(i_bUseBackBuffer, numBars, null);
    }

    public PerformanceInfiniteProgressPanel(final boolean i_bUseBackBuffer,
            final InfiniteProgressAdapter infiniteProgressAdapter) {
        this(i_bUseBackBuffer,
                PerformanceInfiniteProgressPanel.DEFAULT_NUMBER_OF_BARS,
                infiniteProgressAdapter);
    }

    public PerformanceInfiniteProgressPanel(final int numBars,
            final InfiniteProgressAdapter infiniteProgressAdapter) {
        this(true, numBars, infiniteProgressAdapter);
    }

    public PerformanceInfiniteProgressPanel(final boolean i_bUseBackBuffer,
            final int numBars,
            final InfiniteProgressAdapter infiniteProgressAdapter) {
        this.useBackBuffer = i_bUseBackBuffer;
        this.numBars = numBars;
        this.setInfiniteProgressAdapter(infiniteProgressAdapter);
        this.colors = new Color[numBars * 2];
        // build bars
        this.bars = PerformanceInfiniteProgressPanel.buildTicker(numBars);
        // calculate bars bounding rectangle
        this.barsBounds = new Rectangle();
        for (final Area bar : this.bars) {
            this.barsBounds = this.barsBounds.union(bar.getBounds());
        }
        // create colors
        for (int i = 0; i < this.bars.length; i++) {
            final int channel = 224 - (128 / (i + 1));
            this.colors[i] = new Color(channel, channel, channel);
            this.colors[numBars + i] = this.colors[i];
        }
        // set cursor
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // set opaque
        this.setOpaque(this.useBackBuffer);
    }

    protected void setInfiniteProgressAdapter(
            final InfiniteProgressAdapter infiniteProgressAdapter) {
        this.infiniteProgressAdapter = infiniteProgressAdapter;
    }

    int iterate; // we draw use transparency to draw a number of iterations

    // before making a snapshot
    /**
     * Called to animate the rotation of the bar's colors
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        // rotate colors
        if (this.colorOffset == (this.numBars - 1)) {
            this.colorOffset = 0;
        } else {
            this.colorOffset++;
        }
        // repaint
        if (this.barsScreenBounds != null) {
            this.repaint(this.barsScreenBounds);
        } else {
            this.repaint();
        }
        if (this.useBackBuffer && (this.imageBuf == null)) {
            if (this.iterate < 0) {
                try {
                    this.makeSnapshot();
                    this.setOpaque(true);
                } catch (final AWTException e1) {
                    e1.printStackTrace(); // todo: decide what exception to
                    // throw
                }
            } else {
                this.iterate--;
            }
        }
    }

    /**
     * Show/Hide the pane, starting and stopping the animation as you go
     */
    @Override
    public void setVisible(final boolean i_bIsVisible) {
        this.setOpaque(false);
        // capture
        if (i_bIsVisible) {
            if (this.useBackBuffer) {
                // add window resize listener
                final Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) {
                    w.addComponentListener(this.componentAdapter);
                } else {
                    this.addAncestorListener(new AncestorListener() {
                        @Override
                        public void ancestorAdded(final AncestorEvent event) {
                            final Window w = SwingUtilities
                                    .getWindowAncestor(PerformanceInfiniteProgressPanel.this);
                            if (w != null) {
                                w.addComponentListener(PerformanceInfiniteProgressPanel.this.componentAdapter);
                            }
                        }

                        @Override
                        public void ancestorRemoved(final AncestorEvent event) {
                        }

                        @Override
                        public void ancestorMoved(final AncestorEvent event) {
                        }
                    });
                }
                this.iterate = 3;
            }
            // capture events
            this.addMouseListener(this.mouseAdapter);
            this.addMouseMotionListener(this.mouseMotionAdapter);
            this.addKeyListener(this.keyAdapter);
            // start anim
            if (this.infiniteProgressAdapter != null) {
                this.infiniteProgressAdapter.animationStarting();
                this.infiniteProgressAdapter.rampUpEnded();
            }
            this.timer.start();
        } else {
            // stop anim
            this.timer.stop();
            if (this.infiniteProgressAdapter != null) {
                this.infiniteProgressAdapter.animationStopping();
            }
            // / free back buffer
            this.imageBuf = null;
            // stop capturing events
            this.removeMouseListener(this.mouseAdapter);
            this.removeMouseMotionListener(this.mouseMotionAdapter);
            this.removeKeyListener(this.keyAdapter);
            // remove window resize listener
            final Window oWindow = SwingUtilities.getWindowAncestor(this);
            if (oWindow != null) {
                oWindow.removeComponentListener(this.componentAdapter);
            }
        }
        super.setVisible(i_bIsVisible);
    }

    private void makeSnapshot() throws AWTException {
        final Window oWindow = SwingUtilities.getWindowAncestor(this);
        final Insets oInsets = oWindow.getInsets();
        final Rectangle oRectangle = new Rectangle(oWindow.getBounds());
        oRectangle.x += oInsets.left;
        oRectangle.y += oInsets.top;
        oRectangle.width -= oInsets.left + oInsets.right;
        oRectangle.height -= oInsets.top + oInsets.bottom;
        // capture window contents
        this.imageBuf = new Robot().createScreenCapture(oRectangle);
        // no need to fade because we are allready using an image that is
        // showing through
    }

    /**
     * Recalc bars based on changes in size
     */
    @Override
    public void setBounds(final int x, final int y, final int width,
            final int height) {
        super.setBounds(x, y, width, height);
        // update centering transform
        this.centerAndScaleTransform = new AffineTransform();
        this.centerAndScaleTransform.translate(this.getWidth() / 2d,
                this.getHeight() / 2d);
        this.centerAndScaleTransform.scale(this.dScale, this.dScale);
        // calc new bars bounds
        if (this.barsBounds != null) {
            final Area oBounds = new Area(this.barsBounds);
            oBounds.transform(this.centerAndScaleTransform);
            this.barsScreenBounds = oBounds.getBounds();
        }
    }

    /**
     * paint background dimed and bars over top
     */
    @Override
    protected void paintComponent(final Graphics g) {
        if (!this.tempHide) {
            final Rectangle oClip = g.getClipBounds();
            if (this.imageBuf != null) {
                // draw background image
                // g.drawImage(imageBuf, 0, 0,
                // null);
            } else {
                g.setColor(new Color(255, 255, 255, 180));
                g.fillRect(oClip.x, oClip.y, oClip.width, oClip.height);
            }
            // move to center
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.transform(this.centerAndScaleTransform);
            // draw ticker
            for (int i = 0; i < this.bars.length; i++) {
                g2.setColor(this.colors[i + this.colorOffset]);
                g2.fill(this.bars[i]);
            }
            final double maxY = InfiniteProgressPanel.drawTextAt(this.text,
                    this.getFont(), g2, this.getWidth(),
                    this.barsScreenBounds.getMaxY(), this.getForeground());
            if (this.infiniteProgressAdapter != null) {
                this.infiniteProgressAdapter.paintSubComponents(maxY);
            }
        }
    }

    /**
     * Builds the circular shape and returns the result as an array of
     * <code>Area</code>. Each <code>Area</code> is one of the bars composing
     * the shape.
     */
    private static Area[] buildTicker(final int i_iBarCount) {
        final Area[] ticker = new Area[i_iBarCount];
        final Point2D.Double center = new Point2D.Double(0, 0);
        final double fixedAngle = (2.0 * Math.PI) / i_iBarCount;
        for (double i = 0.0; i < i_iBarCount; i++) {
            final Area primitive = PerformanceInfiniteProgressPanel
                    .buildPrimitive();
            final AffineTransform toCenter = AffineTransform
                    .getTranslateInstance(center.getX(), center.getY());
            final AffineTransform toBorder = AffineTransform
                    .getTranslateInstance(45.0, -6.0);
            final AffineTransform toCircle = AffineTransform.getRotateInstance(
                    -i * fixedAngle, center.getX(), center.getY());
            final AffineTransform toWheel = new AffineTransform();
            toWheel.concatenate(toCenter);
            toWheel.concatenate(toBorder);
            primitive.transform(toWheel);
            primitive.transform(toCircle);
            ticker[(int) i] = primitive;
        }
        return ticker;
    }

    /**
     * Builds a bar.
     */
    private static Area buildPrimitive() {
        final Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
        final Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
        final Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);
        final Area tick = new Area(body);
        tick.add(new Area(head));
        tick.add(new Area(tail));
        return tick;
    }

    @Override
    public void start() {
        this.setVisible(true);
    }

    @Override
    public void stop() {
        this.setVisible(false);
    }

    @Override
    public void setText(final String text) {
        this.text = text;
        this.repaint();
    }

    public String getText() {
        return this.text;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     * Adds a listener to the cancel button in this progress panel.
     *
     * @throws RuntimeException
     *             if the infiniteProgressAdapter is null or is not a
     *             CancelableProgessAdapter
     * @param listener
     */
    @Override
    public void addCancelListener(final ActionListener listener) {
        if (this.infiniteProgressAdapter instanceof CancelableProgessAdapter) {
            ((CancelableProgessAdapter) this.infiniteProgressAdapter)
                    .addCancelListener(listener);
        } else {
            throw new RuntimeException(
                    "Expected CancelableProgessAdapter for cancel listener.  Adapter is "
                            + this.infiniteProgressAdapter);
        }
    }

    /**
     * Removes a listener to the cancel button in this progress panel.
     *
     * @throws RuntimeException
     *             if the infiniteProgressAdapter is null or is not a
     *             CancelableProgessAdapter
     * @param listener
     */
    @Override
    public void removeCancelListener(final ActionListener listener) {
        if (this.infiniteProgressAdapter instanceof CancelableProgessAdapter) {
            ((CancelableProgessAdapter) this.infiniteProgressAdapter)
                    .removeCancelListener(listener);
        } else {
            throw new RuntimeException(
                    "Expected CancelableProgessAdapter for cancel listener.  Adapter is "
                            + this.infiniteProgressAdapter);
        }
    }
}

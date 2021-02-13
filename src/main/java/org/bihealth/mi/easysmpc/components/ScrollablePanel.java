/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bihealth.mi.easysmpc.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * This class allows for a panel, which is only scrollable vertically within a
 * JScrollPane. It is based on code introduced here:
 * 
 * https://tips4java.wordpress.com/2009/12/20/scrollable-panel/
 * 
 * which has been released under the following license:
 * 
 * "We assume no responsibility for the code. You are free to use and/or modify
 * and/or distribute any or all code posted on the Java Tips Weblog without
 * restriction. A credit in the code comments would be nice, but not in any way
 * mandatory."
 * 
 * Original documentation:
 * 
 * A panel that implements the Scrollable interface. This class allows you to
 * customize the scrollable features by using newly provided setter methods so
 * you don't have to extend this class every time.
 *
 * Scrollable amounts can be specifed as a percentage of the viewport size or as
 * an actual pixel value. The amount can be changed for both unit and block
 * scrolling for both horizontal and vertical scrollbars.
 *
 * The Scrollable interface only provides a boolean value for determining
 * whether or not the viewport size (width or height) should be used by the
 * scrollpane when determining if scrollbars should be made visible. This class
 * supports the concept of dynamically changing this value based on the size of
 * the viewport. In this case the viewport size will only be used when it is
 * larger than the panels size. This has the effect of ensuring the viewport is
 * always full as components added to the panel will be size to fill the area
 * available, based on the rules of the applicable layout manager of course.
 * 
 * @author Rob Camick
 * @author Felix Wirth
 */
public class ScrollablePanel extends JPanel implements Scrollable, SwingConstants {

    /** SVUID. */
    private static final long serialVersionUID = -725092065588584538L;

    /**
     * Defines the scrolling behavior as described below: the ScrollableSizeHint
     * enum for the height. The enum is used to determine the boolean value that
     * is returned by the getScrollableTracksViewportHeight() method. The valid
     * values are:
     *
     * ScrollableSizeHint.NONE - return "false", which causes the height of the
     * panel to be used when laying out the children ScrollableSizeHint.FIT -
     * return "true", which causes the height of the viewport to be used when
     * laying out the children ScrollableSizeHint.STRETCH - return "true" when
     * the viewport height is greater than the height of the panel, "false"
     * otherwise.
     * 
     * The description applies respectively to width
     */
    public enum ScrollableSizeHint {/** The none. */
                                    NONE,
                                    /** The fit. */
                                    FIT
    }

    /**
     * Increment in pixel or percentages.
     */
    public enum IncrementType {/** The percent. */
                               PERCENT,
                               /** The pixels. */
                               PIXELS
    }

    /** Scrolling in height. */
    private ScrollableSizeHint scrollableHeight;

    /** Scrolling in width. */
    private ScrollableSizeHint scrollableWidth;

    /** HorizontalBlock Scrolling. */
    private IncrementInfo      horizontalBlock;

    /** HorizontalUnit Scrolling. */
    private IncrementInfo      horizontalUnit;

    /** VerticalBlock Scrolling. */
    private IncrementInfo      verticalBlock;

    /** VerticalUnit Scrolling. */
    private IncrementInfo      verticalUnit;

    /**
     * Default constructor that uses a FlowLayout.
     */
    public ScrollablePanel() {
        this(new FlowLayout());
    }

    /**
     * Constuctor for specifying the LayoutManager of the panel.
     *
     * @param layout the LayountManger for the panel
     */
    private ScrollablePanel(LayoutManager layout) {
        
        // Super
        super(layout);

        // Initialize
        IncrementInfo block = new IncrementInfo(IncrementType.PERCENT, 100);
        IncrementInfo unit = new IncrementInfo(IncrementType.PERCENT, 10);

        // Configure
        setScrollableBlockIncrement(HORIZONTAL, block);
        setScrollableBlockIncrement(VERTICAL, block);
        setScrollableUnitIncrement(HORIZONTAL, unit);
        setScrollableUnitIncrement(VERTICAL, unit);

        // Setup
        this.scrollableHeight = ScrollableSizeHint.NONE;
        this.scrollableWidth = ScrollableSizeHint.FIT;
    }

    /**
     * Specify the information needed to do block scrolling.
     *
     * @param orientation
     *            specify the scrolling orientation. Must be either:
     *            SwingContants.HORIZONTAL or SwingContants.VERTICAL.
     * @param info
     *            An IncrementInfo object containing information of how to
     *            calculate the scrollable amount.
     */
    private void setScrollableBlockIncrement(int orientation, IncrementInfo info) {
        switch (orientation) {
        case SwingConstants.HORIZONTAL:
            horizontalBlock = info;
            break;
        case SwingConstants.VERTICAL:
            verticalBlock = info;
            break;
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    /**
     * Specify the information needed to do unit scrolling.
     *
     * @param orientation
     *            specify the scrolling orientation. Must be either:
     *            SwingContants.HORIZONTAL or SwingContants.VERTICAL.
     * @param info
     *            An IncrementInfo object containing information of how to
     *            calculate the scrollable amount.
     */
    private void setScrollableUnitIncrement(int orientation, IncrementInfo info) {
        switch (orientation) {
        case SwingConstants.HORIZONTAL:
            horizontalUnit = info;
            break;
        case SwingConstants.VERTICAL:
            verticalUnit = info;
            break;
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    /**
     * Gets the preferred scrollable viewport size.
     *
     * @return the preferred scrollable viewport size
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * Gets the scrollable unit increment.
     *
     * @param visible
     *            the visible
     * @param orientation
     *            the orientation
     * @param direction
     *            the direction
     * @return the scrollable unit increment
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        switch (orientation) {
        case SwingConstants.HORIZONTAL:
            return getScrollableIncrement(horizontalUnit, visible.width);
        case SwingConstants.VERTICAL:
            return getScrollableIncrement(verticalUnit, visible.height);
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    /**
     * Gets the scrollable block increment.
     *
     * @param visible
     *            the visible
     * @param orientation
     *            the orientation
     * @param direction
     *            the direction
     * @return the scrollable block increment
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        switch (orientation) {
        case SwingConstants.HORIZONTAL:
            return getScrollableIncrement(horizontalBlock, visible.width);
        case SwingConstants.VERTICAL:
            return getScrollableIncrement(verticalBlock, visible.height);
        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    /**
     * Returns the scrollable increment.
     *
     * @param info
     *            the info
     * @param distance
     *            the distance
     * @return the scrollable increment
     */
    protected int getScrollableIncrement(IncrementInfo info, int distance) {
        if (info.getIncrement() == IncrementType.PIXELS) return info.getAmount();
        else return distance * info.getAmount() / 100;
    }

    /**
     * Gets the scrollable tracks viewport width.
     *
     * @return the scrollable tracks viewport width
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        if (scrollableWidth == ScrollableSizeHint.NONE) return false;
        else return true;
    }

    /**
     * Gets the scrollable tracks viewport height.
     *
     * @return the scrollable tracks viewport height
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (scrollableHeight == ScrollableSizeHint.NONE) return false;
        else return true;
    }

    /**
     * Helper class to hold the information required to calculate the scroll
     * amount.
     */
    static class IncrementInfo {

        /** The type. */
        private IncrementType type;

        /** The amount. */
        private int           amount;

        /**
         * Instantiates a new increment info.
         *
         * @param type
         *            the type
         * @param amount
         *            the amount
         */
        public IncrementInfo(IncrementType type, int amount) {
            this.type = type;
            this.amount = amount;
        }

        /**
         * Gets the increment.
         *
         * @return the increment
         */
        public IncrementType getIncrement() {
            return type;
        }

        /**
         * Gets the amount.
         *
         * @return the amount
         */
        public int getAmount() {
            return amount;
        }

        /**
         * To string.
         *
         * @return the string
         */
        public String toString() {
            return "ScrollablePanel[" + type + ", " + amount + "]";
        }
    }
}

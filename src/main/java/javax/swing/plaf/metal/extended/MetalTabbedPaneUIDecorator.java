package javax.swing.plaf.metal.extended;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import static javax.swing.SwingConstants.BOTTOM;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.TOP;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.extended.ClosableTabComponent;
import javax.swing.extended.JTabbedPaneExtended;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public class MetalTabbedPaneUIDecorator extends MetalTabbedPaneUI {

    private JViewport tabViewport;
    private JPanel tabContainer;
    private JButton tabListButton;
    private JButton scrollForwardButton;
    private JButton scrollBackwardButton;
    private ChangeListener delegateStateChangeListener;
    private ChangeListener originalStateChangeListener;
    private MouseListener delegateMouseListener;
    private MouseListener originalMouseListener;
    private boolean tabsOverlapBorder;
    private int leadingTabIndex;
    private final Point tabViewPosition = new Point(0, 0);

    private void setLeadingTabIndex(final int tabPlacement, final int index) {
        leadingTabIndex = index;
        final Dimension viewSize = tabViewport.getViewSize();
        final Rectangle viewRect = tabViewport.getViewRect();

        switch (tabPlacement) {
            case TOP:
            case BOTTOM:
                tabViewPosition.x = leadingTabIndex == 0 ? 0 : rects[leadingTabIndex].x;

                if ((viewSize.width - tabViewPosition.x) < viewRect.width) {
                    // We've scrolled to the end, so adjust the viewport size
                    // to ensure the view position remains aligned on a tab boundary
                    final Dimension extentSize = new Dimension(viewSize.width - tabViewPosition.x,
                            viewRect.height);
                    tabViewport.setExtentSize(extentSize);
                }
                break;
            case LEFT:
            case RIGHT:
                tabViewPosition.y = leadingTabIndex == 0 ? 0 : rects[leadingTabIndex].y;

                if ((viewSize.height - tabViewPosition.y) < viewRect.height) {
                    // We've scrolled to the end, so adjust the viewport size
                    // to ensure the view position remains aligned on a tab boundary
                    final Dimension extentSize = new Dimension(viewRect.width,
                            viewSize.height - tabViewPosition.y);
                    tabViewport.setExtentSize(extentSize);
                }
        }
        tabViewport.setViewPosition(tabViewPosition);
    }

    /**
     * Scrolls tab with a specified to the visible area. If the tabbed pane has
     * the JTabbedPane.WRAP_TAB_LAYOUT layout policy then this method does
     * nothing.
     *
     * Adapted from GrepCode / com.jetbrains / intellij-idea
     *
     * @param index Index of the tab to which we want to scroll.
     */
    public final void scrollTabToVisible(final int index) {

        // Do nothing, if tab scrolling isn't supported by the currently selected UI.
        if (!isScrollTabLayout()) {
            return;
        }
        if (tabPane.getWidth() == 0) {
            return;
        }

        Rectangle tabBounds = getTabBounds(tabPane, index);
        final int tabPlacement = tabPane.getTabPlacement();

        if (tabPlacement == TOP || tabPlacement == BOTTOM) { // Tabs are on top or bottom side.
            int tabbedPaneWidth = tabPane.getWidth();
            // No scrolling, if the tabbedPane has not been layout yet.
            if (tabbedPaneWidth == 0) {
                return;
            }
            if (tabBounds.x < 50) { // If tab is to the left of visible area.
                int lTIndex = this.leadingTabIndex;
                while (lTIndex != index && lTIndex > 0 && tabBounds.x < 50) {
                    setLeadingTabIndex(tabPlacement, lTIndex - 1);
                    lTIndex = this.leadingTabIndex;
                    tabBounds = getTabBounds(tabPane, index);
                }
            } // If tab's right side is out of visible range.
            else if (tabBounds.x + tabBounds.width > tabbedPaneWidth - 50) {
                int lTIndex = this.leadingTabIndex;
                while (lTIndex != index && lTIndex < tabPane.getTabCount() - 1
                        && tabBounds.x + tabBounds.width > tabbedPaneWidth - 50) {
                    setLeadingTabIndex(tabPlacement, lTIndex + 1);
                    lTIndex = this.leadingTabIndex;
                    tabBounds = getTabBounds(tabPane, index);
                }
            }
        } else { // Tabs are on left or right side.
            final int tabbedPaneHeight = tabPane.getHeight();
            // No scrolling, if the tabbedPane has not been layout yet.
            if (tabbedPaneHeight == 0) {
                return;
            }
            if (tabBounds.y < 30) { // If tab is above visible area.
                int lTIndex = this.leadingTabIndex;
                while (lTIndex != index && lTIndex > 0 && tabBounds.y < 30) {
                    setLeadingTabIndex(tabPlacement, lTIndex - 1);
                    lTIndex = this.leadingTabIndex;
                    tabBounds = getTabBounds(tabPane, index);
                }
            } else if (tabBounds.y + tabBounds.height > tabPane.getHeight() - 30) { // Tab below visible area.
                int lTIndex = this.leadingTabIndex;
                while (lTIndex != index && lTIndex < tabPane.getTabCount() - 1
                        && tabBounds.y + tabBounds.height > tabPane.getHeight() - 30) {
                    setLeadingTabIndex(tabPlacement, lTIndex + 1);
                    lTIndex = this.leadingTabIndex;
                    tabBounds = getTabBounds(tabPane, index);
                }
            }
        }
    }

    @Override
    protected void installComponents() {
        super.installComponents();

        tabViewport = null;
        tabContainer = null;

        for (Component c : tabPane.getComponents()) {
            // find scrollable tab viewport
            if (isScrollTabLayout()) {
                if (c instanceof JViewport && (c.getClass().getName().equals("javax.swing.plaf.basic.BasicTabbedPaneUI$ScrollableTabViewport"))) {
                    tabViewport = (JViewport) c;
                    break;
                }
            } else {
                // find tabContainer    
                if (c instanceof JPanel && (c.getClass().getName().equals("javax.swing.plaf.basic.BasicTabbedPaneUI$TabContainer"))) {
                    tabContainer = (JPanel) c;
                    break;
                }
            }
        }

        if (isScrollTabLayout()) {
            installHiddenTabsNavigation();
        } else {
            installRightAllignedTabComponents();
        }
    }

    @Override
    protected void uninstallComponents() {
        // uninstall hidden tabs navigation before invoking super.uninstallComponents() for
        // correct uninstallation of BasicTabbedPaneUI tab scroller support
        uninstallHiddenTabsNavigation();

        super.uninstallComponents();

        tabViewport = null;
    }

    @Override
    protected void installListeners() {
        this.delegateStateChangeListener = this::stateChanged;
        this.delegateMouseListener = new DelegationMouseListener();
        super.installListeners();
    }

    @Override
    protected ChangeListener createChangeListener() {
        this.originalStateChangeListener = super.createChangeListener();
        return this.delegateStateChangeListener;
    }

    @Override
    protected MouseListener createMouseListener() {
        this.originalMouseListener = super.createMouseListener();
        return this.delegateMouseListener;
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabsOverlapBorder = UIManager.getBoolean("TabbedPane.tabsOverlapBorder");
    }

    @Override
    protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
        dest.width = rects[tabIndex].width;
        dest.height = rects[tabIndex].height;

        if (isScrollTabLayout()) { // SCROLL_TAB_LAYOUT
            // Need to translate coordinates based on viewport location &
            // view position
            Point vpp = tabViewport.getLocation();
            Point viewp = tabViewport.getViewPosition();
            dest.x = rects[tabIndex].x + vpp.x - viewp.x;
            dest.y = rects[tabIndex].y + vpp.y - viewp.y;

        } else { // WRAP_TAB_LAYOUT
            dest.x = rects[tabIndex].x;
            dest.y = rects[tabIndex].y;
        }
        return dest;
    }

    @Override
    protected JButton createScrollButton(int direction) {
        if (direction != SOUTH && direction != NORTH && direction != EAST
                && direction != WEST) {
            throw new IllegalArgumentException("Direction must be one of: "
                    + "SOUTH, NORTH, EAST or WEST");
        }

        if (direction == WEST || direction == NORTH) {
            scrollBackwardButton = new ScrollableTabButton(direction);
            return scrollBackwardButton;
        } else { // direction == EAST || direction == SOUTH) {
            scrollForwardButton = new ScrollableTabButton(direction);
            return scrollForwardButton;
        }
    }

    @Override
    protected void paintTab(Graphics g, int tabPlacement,
            Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect) {
        runWithOriginalLayoutManager(() -> {
            super.paintTab(g, tabPlacement,
                    rects, tabIndex,
                    iconRect, textRect);
        });
    }

    // Overwrites MetalTabbedPaneUI:paint()
    @Override
    public void paint(Graphics g, JComponent c) {
        int tabPlacement = tabPane.getTabPlacement();

        Insets insets = c.getInsets();
        Dimension size = c.getSize();

        // Paint the background for the tab area
        if (tabPane.isOpaque()) {
            Color background = c.getBackground();
            if (background instanceof UIResource && tabAreaBackground != null) {
                g.setColor(tabAreaBackground);
            } else {
                g.setColor(background);
            }
            switch (tabPlacement) {
                case LEFT:
                    g.fillRect(insets.left, insets.top,
                            calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth),
                            size.height - insets.bottom - insets.top);
                    break;
                case BOTTOM:
                    int totalTabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                    g.fillRect(insets.left, size.height - insets.bottom - totalTabHeight,
                            size.width - insets.left - insets.right,
                            totalTabHeight);
                    break;
                case RIGHT:
                    int totalTabWidth = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                    g.fillRect(size.width - insets.right - totalTabWidth,
                            insets.top, totalTabWidth,
                            size.height - insets.top - insets.bottom);
                    break;
                case TOP:
                default:
                    g.fillRect(insets.left, insets.top,
                            size.width - insets.right - insets.left,
                            calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight));
                    paintHighlightBelowTab();
            }
        }

        basicTabbedPaneUIPaint(g, c);
    }

    protected void basicTabbedPaneUIPaint(Graphics graphics, JComponent c) {
        int selectedIndex = tabPane.getSelectedIndex();
        int tabPlacement = tabPane.getTabPlacement();

        ensureCurrentLayout();

        // Paint content border and tab area.
        if (tabsOverlapBorder) {
            paintContentBorder(graphics, tabPlacement, selectedIndex);
        }
        // If scrollable tabs are enabled, the tab area will be
        // painted by the scrollable tab panel instead.
        //
        if (!isScrollTabLayout()) { // WRAP_TAB_LAYOUT
            paintTabArea(graphics, tabPlacement, selectedIndex);
        }
        if (!tabsOverlapBorder) {
            paintContentBorder(graphics, tabPlacement, selectedIndex);
        }
    }

    /**
     * Creates and returns a subclass of JButton that will enables the used to
     * display ta tab list, that means a selectable list of all available tabs.
     *
     * @param direction int : One of the SwingConstants constants: SOUTH, NORTH,
     * EAST or WEST
     * @return The created {@code TabListButton} with the specified direction.
     * @see javax.swing.JTabbedPane#setTabPlacement
     * @see javax.swing.SwingConstants
     * @throws IllegalArgumentException if direction is not one of NORTH, SOUTH,
     * EAST or WEST
     */
    protected JButton createTabListButton(final int direction) {
        if (direction != SOUTH && direction != NORTH && direction != EAST && direction != WEST) {
            throw new IllegalArgumentException("Direction must be one of: SOUTH, NORTH, EAST or WEST");
        }
        return new TabListButton(direction);
    }

    protected void installHiddenTabsNavigation() {
        if (!isScrollTabLayout() || tabViewport == null) {
            return;
        }

        // At this point, BasicTabbedPaneUI already has installed
        // TabbedPaneScrollLayout (in super.createLayoutManager()) and
        // ScrollableTabSupport, ScrollableTabViewport, ScrollableTabPanel, etc
        // (in super.installComponents()).
        // install own layout manager that delegates to original layout manager
        tabPane.setLayout(createScrollLayoutManager((BasicTabbedPaneUI.TabbedPaneLayout) tabPane.getLayout()));

        // create and add tabListButton
        tabListButton = createTabListButton(SOUTH);
        tabPane.add(tabListButton);
    }

    protected void uninstallHiddenTabsNavigation() {
        // restore layout manager before invoking super.uninstallComponents() for
        // correct uninstallation of MyBasicTabbedPaneUI tab scroller support
        if (tabPane.getLayout() instanceof TabbedPaneScrollLayoutDecorator) {
            tabPane.setLayout(((TabbedPaneScrollLayoutDecorator) tabPane.getLayout()).delegate);
        }

        if (tabListButton != null) {
            tabPane.remove(tabListButton);
            tabListButton = null;
        }
    }

    protected void installRightAllignedTabComponents() {
        if (isScrollTabLayout() || tabContainer == null) {
            return;
        }
        tabPane.setLayout(createWrapLayoutManager());
    }

    private JTabbedPaneExtended getExtendedTabbedPane() {
        if (super.tabPane instanceof JTabbedPaneExtended) {
            return (JTabbedPaneExtended) super.tabPane;
        }
        throw new Error("Wrong type, JTabbedPaneExtended expected.");
    }

    private void runWithOriginalLayoutManager(Runnable runnable) {
        this.runWithOriginalLayoutManager(runnable, false);
    }

    private void runWithOriginalLayoutManager(Runnable runnable, boolean skipInvalidate) {
        LayoutManager layout = tabPane.getLayout();
        if (layout instanceof TabbedPaneScrollLayoutDecorator) {
            // temporary change layout manager because the runnable may use
            // BasicTabbedPaneUI.scrollableTabLayoutEnabled()
            final JTabbedPaneExtended extendedPaneExtended = getExtendedTabbedPane();
            extendedPaneExtended.setSkipNextInvalidate(true);
            tabPane.setLayout(((TabbedPaneScrollLayoutDecorator) layout).delegate);
            runnable.run();
//            extendedPaneExtended.setSkipNextInvalidate(skipInvalidate);
            tabPane.setLayout(layout);
        } else {
            runnable.run();
        }
    }

    private boolean isScrollTabLayout() {
        return tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT;
    }

    private LayoutManager createScrollLayoutManager(BasicTabbedPaneUI.TabbedPaneLayout delegate) {
        return new TabbedPaneScrollLayoutDecorator(delegate);
    }

    private LayoutManager createWrapLayoutManager() {
        return new TabbedPaneWrapLayout();
    }

    private void stateChanged(ChangeEvent e) {
        runWithOriginalLayoutManager(() -> {
            this.originalStateChangeListener.stateChanged(e);
        }, true);
    }

    private void ensureCurrentLayout() {
        if (!tabPane.isValid()) {
            tabPane.validate();
        }
        /* If tabPane doesn't have a peer yet, the validate() call will
         * silently fail.  We handle that by forcing a layout if tabPane
         * is still invalid.  See bug 4237677.
         */
        if (!tabPane.isValid()) {
            BasicTabbedPaneUI.TabbedPaneLayout layout = (BasicTabbedPaneUI.TabbedPaneLayout) tabPane.getLayout();
            layout.calculateLayoutInfo();
        }
    }

    private final class DelegationMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mousePressed(MouseEvent e) {
            runWithOriginalLayoutManager(() -> {
                originalMouseListener.mousePressed(e);
            }, true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            originalMouseListener.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            originalMouseListener.mouseExited(e);
        }

    }

    private class TabbedPaneWrapLayout extends TabbedPaneLayout {

        @Override
        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            if (!isScrollTabLayout()) {
                layoutTabComponents();
            }
        }

        private void layoutTabComponents() {
            if (tabContainer == null) {
                return;
            }
            Rectangle rect = new Rectangle();
            Point delta = new Point(-tabContainer.getX(), -tabContainer.getY());
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                Component c = tabPane.getTabComponentAt(i);
                if (c == null || !(c instanceof ClosableTabComponent)) {
                    continue;
                }

                getTabBounds(i, rect);
                Insets insets = getTabInsets(tabPane.getTabPlacement(), i);
                int outerX = rect.x + insets.left + delta.x;
                int outerWidth = rect.width - insets.left - insets.right;
                rect = c.getBounds();

                // Overwrite X and widht value from the original layout.
                c.setBounds(outerX, rect.y, outerWidth, rect.height);
            }
        }
    }

    private final class TabbedPaneScrollLayoutDecorator extends TabbedPaneLayout implements LayoutManager {

        private final BasicTabbedPaneUI.TabbedPaneLayout delegate;

        protected TabbedPaneScrollLayoutDecorator(BasicTabbedPaneUI.TabbedPaneLayout delegate) {
            this.delegate = delegate;
        }
        //---- interface LayoutManager ----

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return delegate.preferredLayoutSize(parent);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return delegate.minimumLayoutSize(parent);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            delegate.addLayoutComponent(name, comp);
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            delegate.removeLayoutComponent(comp);
        }

        @Override
        public void layoutContainer(Container parent) {
            // Original layout manager calculates layout for 2 ScrollableTabButtons.
            // To add space for a 3'rd button, we temporarly increase the width of the buttons
            // find backward/forward scroll buttons
            final Dimension buttonSize = scrollForwardButton.getPreferredSize();
            final int originalWidth = buttonSize.width;
            final int gap = 2; // gab between ForwardBackwardButtons and TabListButton
            buttonSize.width = (3 * buttonSize.width + gap) / 2;
            scrollForwardButton.setPreferredSize(buttonSize);

            // delegate to original layout manager and let it layout tabs and buttons
            // runWithOriginalLayoutManager() is necessary for correct locations
            // of tab components layed out in TabbedPaneLayout.layoutTabComponents()
            runWithOriginalLayoutManager(() -> {
                delegate.layoutContainer(parent);
            }, true);

            scrollForwardButton.setPreferredSize(null);
            Rectangle bounds = scrollForwardButton.getBounds();
            bounds.x += (bounds.width - originalWidth);
            bounds.width = originalWidth;
            tabListButton.setBounds(bounds);

            bounds.x -= (bounds.width + gap);        
            scrollForwardButton.setBounds(bounds);
            bounds.x -= bounds.width;        
            scrollBackwardButton.setBounds(bounds);
        }
    }

    /**
     * Extends a scrollable tab button to enable a tab list pop-up menu.
     */
    @SuppressWarnings("serial")
    private final class TabListButton extends ScrollableTabButton {

        private static final int VISIBLE_ENTRIES = 30;
        private final JPopupMenu tabListPopup;
        private JPanel listPanel;

        public TabListButton(final int direction) {
            super(direction);
            this.tabListPopup = new JPopupMenu();
            this.tabListPopup.setLayout(new BorderLayout());
            this.addActionListener((final ActionEvent evt) -> {
                initAndShowPopup();
            });
        }

        private void initAndShowPopup() {
            // Clean up.
            this.tabListPopup.removeAll();

            // Generate list panel.
            this.listPanel = new JPanel();
            this.listPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
//            this.listPanel.setBackground(UIManager.getColor("Panel.background"));
            this.listPanel.setOpaque(true);

            final DefaultListModel<String> tabListModel = new DefaultListModel<>();
            String prototype = "";
            for (int idx = 0; idx < tabPane.getTabCount(); idx++) {
                final String title = tabPane.getTitleAt(idx);
                tabListModel.addElement(title);
                if (title.length() > prototype.length()) {
                    prototype = title;
                }
            }
            // Create list with custom layout.
            final JList<String> tabList = new JList<>(tabListModel);
            tabList.setPrototypeCellValue(prototype);
            tabList.setVisibleRowCount(Math.min(VISIBLE_ENTRIES, tabPane.getTabCount()));
            // Set cell renderer.
            tabList.setCellRenderer(new AveTabListCellRenderer<>());
            // Set selected index.
            final int selectedIndex = tabPane.getSelectedIndex();
            tabList.setSelectedIndex(selectedIndex);
            tabList.setFixedCellHeight(18);
            // Put list into a scroll panel.
            final JScrollPane tabListScroll = new JScrollPane(tabList);
            tabListScroll.setBorder(BorderFactory.createEmptyBorder());
            tabListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            this.listPanel.add(tabListScroll); //,gbc);

            final MouseListener mouseListener = new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent evt) {
                    final JList theList = (JList) evt.getSource();
                    if (evt.getClickCount() == 1) {
                        final int index = theList.locationToIndex(evt.getPoint());
                        if (index >= 0) {
                            tabPane.setSelectedIndex(index);
                        }
                        // TabListPopup.this.menuSelectionChanged(false);
                        tabListPopup.setVisible(false);
                    }
                }
            };
            tabList.addMouseListener(mouseListener);

            this.tabListPopup.add(this.listPanel, BorderLayout.CENTER);
            this.tabListPopup.show(this, 0, this.getHeight());

            // Scroll such that selected index is visible.
            tabList.ensureIndexIsVisible(selectedIndex);
        }

        /**
         * Provides a listCellRenderer for the tab list.
         */
        private final class AveTabListCellRenderer<E> extends JLabel implements ListCellRenderer<E> {

            /**
             * Creates a new instance of {@code AveTabListCellRenderer}.
             */
            private AveTabListCellRenderer() {
                super();
                // JLabel needs to be opaque to enable background setting.
                this.setOpaque(true);
            }

            /**
             * Customized ListCellRenderer Method, to change the spaces around
             * an entry and sets the appropriate font, foreground and
             * background.
             */
            @Override
            public Component getListCellRendererComponent(final JList list, final Object value,
                    final int index, final boolean isSelected, final boolean cellHasFocus) {
                // Add some horizontal space around text
                this.setText("  " + value.toString() + "  ");
                // Use tab foreground color
                this.setForeground(tabPane.getForegroundAt(index));
                // If tab background color has been explicitly set (not instance of ColorUIResource), use it
                if (tabPane.getBackgroundAt(index) instanceof ColorUIResource) {
                    this.setBackground(listPanel.getBackground());
                } else {
                    this.setBackground(tabPane.getBackgroundAt(index));
                }
                final Font font = this.getFont();
                this.setFont(isSelected ? font.deriveFont(Font.BOLD) : font.deriveFont(Font.PLAIN));
                return this;
            }
        }
    }

    @SuppressWarnings("serial")
    private class ScrollableTabButton extends BasicArrowButton implements UIResource,
            SwingConstants {

        private ScrollableTabButton(final int direction) {
            super(direction,
                    UIManager.getColor("Button.background"),
                    UIManager.getColor("Button.shadow"),
                    UIManager.getColor("Button.foreground"),
                    UIManager.getColor("Button.highlight"));
        }

        @Override
        protected void fireActionPerformed(ActionEvent event) {
            runWithOriginalLayoutManager(() -> {
                super.fireActionPerformed(event);
            }, true);
        }
    }

    @SuppressWarnings("serial")
    private static class BasicArrowButton extends javax.swing.plaf.basic.BasicArrowButton {

        Dimension fakeSize;

        public BasicArrowButton(int i, Color color, Color color1, Color color2, Color color3) {
            super(i, color, color1, color2, color3);
        }

        @Override
        public void setPreferredSize(Dimension preferredSize) {
            fakeSize = preferredSize;
        }

        @Override
        public Dimension getPreferredSize() {
            if (fakeSize != null) {
                return fakeSize;
            }
            return super.getPreferredSize();
        }
    }
}

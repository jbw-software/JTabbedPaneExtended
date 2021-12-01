package javax.swing.extended;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.plaf.metal.extended.MetalTabbedPaneUIDecorator;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.TabbedPaneUI;

@SuppressWarnings("serial")
public class JTabbedPaneExtended extends JTabbedPane {

    private MetalTabbedPaneUIDecorator metalTabbedPaneUIDecorator;
    private PropertyChangeListener tabLayoutPolicyListener;
    private volatile boolean skipInvalidate = false;

    public JTabbedPaneExtended() {
        // Narrow the right gap added around tab component of L&F;
        // MetalLookAndFeel default insets are: (0, 9, 1, 9).
        UIManager.put("TabbedPane.tabInsets", new InsetsUIResource(0, 9, 1, 1));

        // Increase the gap between label and icon or button in SCROLL_TAB_LAYOUT
        UIManager.put("TabbedPane.textIconGap", 4);
    }

    private void installListeners() {
        if ((tabLayoutPolicyListener = this::tabLayoutPolicyChange) != null) {
            addPropertyChangeListener("tabLayoutPolicy", this::tabLayoutPolicyChange);
        }
    }

    private void uninstallListeners() {
        if (tabLayoutPolicyListener != null) {
            removePropertyChangeListener(tabLayoutPolicyListener);
            tabLayoutPolicyListener = null;
        }
    }

    private void tabLayoutPolicyChange(PropertyChangeEvent evt) {
        if ("tabLayoutPolicy".equals(evt.getPropertyName())) {
            if ((int) evt.getNewValue() == JTabbedPane.SCROLL_TAB_LAYOUT) {
                // Ensure that selected index is within visible scroll area.
                final int selectedIndex = this.getSelectedIndex();
                if (selectedIndex >= 0) {
                    this.setSelectedIndex(selectedIndex);
                }
            }
        }
    }

    @Override
    public void addNotify() {
        this.installListeners();
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        this.uninstallListeners();
    }

    @Override
    public void setUI(TabbedPaneUI ui) {
        if ("javax.swing.plaf.metal.MetalTabbedPaneUI".equals(ui.getClass().getName())) {
            if (this.metalTabbedPaneUIDecorator == null) {
                this.metalTabbedPaneUIDecorator = new MetalTabbedPaneUIDecorator();
                ui = this.metalTabbedPaneUIDecorator;
            }
        }
        super.setUI(ui);
    }

    /**
     * Overrides setSelectedIndex in particular for the usage with scroll tab
     * layout to ensure that the selected index is scrolled into the visible
     * scroll range.
     *
     * @param index Index to be selected.
     */
    @Override
    public void setSelectedIndex(final int index) {
        if (index >= this.getTabCount() || index < 0) {
            return;
        }

        try {
            super.setSelectedIndex(index);
        } catch (final ArrayIndexOutOfBoundsException exception) {
            return;
        }

        // For JTabbedPane.SCROLL_TAB_LAYOUT, ensure that selected index is visible.
        if (this.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            // Use non-native method scrollTabToVisible from AveBasicTabbedPaneUI.
            ((MetalTabbedPaneUIDecorator) this.getUI()).scrollTabToVisible(index);

            // Redo Layout to adapt to changes due to scrolling.
            this.doLayout();
        }
    }
    
    /**
     * There are lots of additional calls to invallidate because of the change of layouts in {@code MetalTabbedPaneUIDecorator}'s runWithOriginalLayoutManager method.
     * To avoid uncecessary validation of TabbedPane's children these calls are skiped.
     */
    @Override
    public void invalidate() {
        if (!this.skipInvalidate) {
            super.invalidate();
        }
    }

    public void setSkipNextInvalidate(boolean skipNextInvalidate) {
        if (this.skipInvalidate != skipNextInvalidate) {
            this.skipInvalidate = skipNextInvalidate;
        }
    }

    /**
     * Toggles the tab layout policy between {@code JTabbedPane.WRAP_TAB_LAYOUT}
     * and {@code JTabbedPane.SCROLL_TAB_LAYOUT}.
     */
    public void toggleTabLayoutPolicy() {
        if (this.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            this.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        } else {
            this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }
    }
}

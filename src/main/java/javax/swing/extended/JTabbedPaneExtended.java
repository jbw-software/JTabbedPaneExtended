package javax.swing.extended;

import javax.swing.plaf.metal.extended.MetalTabbedPaneUIDecorator;
import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;

@SuppressWarnings("serial")
public class JTabbedPaneExtended extends JTabbedPane {

    private MetalTabbedPaneUIDecorator metalTabbedPaneUIDecorator;
    private volatile boolean skipInvalidate = false;

    @Override
    public void setUI(TabbedPaneUI ui) {
        System.out.println("setUI: " + ui);
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

//    @Override
//    public void invalidate() {
//        if (!this.skipInvalidate) {
//            System.err.println("JTabbedPaneExtended invalidate");
//            super.invalidate();
//        } else {
//            this.skipInvalidate = false;
//        }
//    }

    public void setSkipNextInvalidate(boolean skipNextInvalidate) {
        if (this.skipInvalidate != skipNextInvalidate) {
            this.skipInvalidate = skipNextInvalidate;
        }
    }
}

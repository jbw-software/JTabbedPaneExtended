/*
 * ClosableTabComponent.java
 */
package javax.swing.extended;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * A closable tabComponent component with a close button inserted for a
 * {@link JTabbedPane}.
 *
 * @author Jörg Wille
 */
@SuppressWarnings("serial")
public class ClosableTabComponent extends JPanel {

    public static final Color DEFAULT_BORDER_COLOR = Color.GRAY;
    public static final Color DEFAULT_CROSS_COLOR = Color.GRAY;
    public static final Color DEFAULT_CROSS_ROLL_OVER_COLOR = Color.RED;
    public static final int DEFAULT_TEXT_BUTTON_GAP = (int) UIManager.get("TabbedPane.textIconGap");
    private static final int TAB_BUTTON_SIZE = 15;
    private PropertyChangeListener tabLayoutPolicyListener;
    private final JTabbedPane tabbedPane;
    private final JLabel label;
    private final JButton button;
    private final JPanel labelPanel;
    private final JPanel buttonPanel;
    private final Color borderColor;
    private final Color crossColor;
    private final Color crossRolloverColor;
    private int textButtonGap;

    /**
     * Creates a new instance of {@link AveClosableTabComponent}.
     *
     * @param tabbedPane the {@code JTabbedPane} in which the
     * {@link AveClosableTabComponent} is being used.
     * @param action the {@code Action} to call when the close button is
     * clicked.
     */
    public ClosableTabComponent(final JTabbedPane tabbedPane, final Action action) {
        this(tabbedPane, action, DEFAULT_BORDER_COLOR, DEFAULT_CROSS_COLOR, DEFAULT_CROSS_ROLL_OVER_COLOR, DEFAULT_TEXT_BUTTON_GAP);
    }

    /**
     * Creates a new instance of {@link AveClosableTabComponent}.
     *
     * @param tabbedPane the {@code JTabbedPane} in which the
     * {@link AveClosableTabComponent} is being used.
     * @param action the {@code Action} to call when the close button is
     * clicked.
     * @param borderColor
     * @param crossColor
     * @param crossRolloverColor
     * @param textButtonGap
     */
    public ClosableTabComponent(final JTabbedPane tabbedPane, final Action action, final Color borderColor, final Color crossColor, final Color crossRolloverColor, int textButtonGap) {
        super(new BorderLayout());
        super.setOpaque(false);

        if (tabbedPane == null) {
            throw new NullPointerException("Argument \'TabbedPane\' must not be null.");
        }
        this.tabbedPane = tabbedPane;
        ClosableTabComponent.this.setName(tabbedPane.getTitleAt(tabbedPane.getTabCount() - 1));

        this.borderColor = borderColor;
        this.crossColor = crossColor;
        this.crossRolloverColor = crossRolloverColor;
        this.textButtonGap = textButtonGap;

        // Make JLabel read titles from JTabbedPane.
        label = new JLabel() {
            @Override
            public String getText() {
                int i = tabbedPane.indexOfTabComponent(ClosableTabComponent.this);
                if (i != -1) {
                    return tabbedPane.getTitleAt(i);
                }
                return null;
            }
        };

        // Unset default FlowLayout' gaps.
        labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        labelPanel.setOpaque(false);
        labelPanel.add(label);
        super.add(labelPanel, BorderLayout.CENTER);

        // The tabComponent button.
        button = new TabButton(ClosableTabComponent.this, action);
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(button);
        super.add(buttonPanel, BorderLayout.LINE_END);

        installListeners();
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

    private void installListeners() {
        if ((tabLayoutPolicyListener = this::tabLayoutPolicyChange) != null) {
            tabbedPane.addPropertyChangeListener("tabLayoutPolicy", this::tabLayoutPolicyChange);
        }
    }

    private void uninstallListeners() {
        if (tabLayoutPolicyListener != null) {
            tabbedPane.removePropertyChangeListener(tabLayoutPolicyListener);
            tabLayoutPolicyListener = null;
        }
    }

    private final static MouseListener BUTTON_MOUSE_LISTENER = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    private void tabLayoutPolicyChange(PropertyChangeEvent evt) {
        if ("tabLayoutPolicy".equals(evt.getPropertyName())) {
            if ((int) evt.getNewValue() == JTabbedPane.SCROLL_TAB_LAYOUT) {
                // Add a gap between the label and the button.
                labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, this.textButtonGap));
            } else {
                labelPanel.setBorder(null);
            }
        }
    }

    /**
     * Returns the {@code JTabbedPane} in which this
     * {@link AveClosableTabComponent} is being used.
     *
     * @return the corresponding {@code JTabbedPane}.
     */
    public JTabbedPane getTabbedPane() {
        return this.tabbedPane;
    }

    /**
     * The TabButton class should not be instantiated externally, but since the
     * button's action is accessable externaly, the class is made "puplic" to
     * allow casting of the event's source to this class.
     */
    @SuppressWarnings("serial")
    public final class TabButton extends JButton {

        private final Action action;
        private final ClosableTabComponent tabComponent;

        public TabButton(final ClosableTabComponent tabComponent, final Action action) {
            this.tabComponent = tabComponent;

            if (action == null) {
                throw new NullPointerException("Argument \'Action\' must not be null.");
            }

            this.action = action;
            super.setPreferredSize(new Dimension(TAB_BUTTON_SIZE, TAB_BUTTON_SIZE));

            // Make the button looks the same for all Laf's
            super.setUI(new BasicButtonUI());
            // Make it transparent
            super.setContentAreaFilled(false);
            // No need to be focusable.
            super.setFocusable(false);
            // Make a simple border
            super.setBorder(BorderFactory.createLineBorder(borderColor));
            super.setBorderPainted(false);
            // Making nice rollover effect
            super.setRolloverEnabled(true);
        }

        private void installListeners() {
            // Use the same listener for all buttons.
            this.addMouseListener(BUTTON_MOUSE_LISTENER);
            super.setAction(action);
            // Ovewrite Mnemonic after setting the action.
            super.setMnemonic(0);
            super.setText("");
        }

        private void removeListener() {
            this.removeMouseListener(BUTTON_MOUSE_LISTENER);
            super.setAction(null);
        }

        /**
         * Notifies this component that it no longer has a parent component.
         * This method is called by the toolkit internally and should not be
         * called directly by programs.
         */
        @Override
        public void removeNotify() {
            this.removeListener();
            super.removeNotify();
        }

        /**
         * Notifies this component that it now has a parent component. This
         * method is called by the toolkit internally and should not be called
         * directly by programs.
         */
        @Override
        public void addNotify() {
            this.installListeners();
            super.addNotify();
        }

        // Do not nothing for this button if the UI wants to update.
        @Override
        public void updateUI() {
        }

        // Paint the "closing cross"
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // Shift the image for pressed buttons.
            if (getModel().isPressed()) {
                g2.translate(0.5f, 0.5f);
            }
            // Define the stroke to draw the cross
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g2.setColor(crossColor);
            if (getModel().isRollover()) {
                g2.setColor(crossRolloverColor);
            }
            int gap = 4; // The gap to all edges.
            g2.drawLine(gap, gap, getWidth() - gap - 1, getHeight() - gap - 1);
            g2.drawLine(getWidth() - gap - 1, gap, gap, getHeight() - gap - 1);
            g2.dispose();
        }

        /**
         * Returns the {@link AveClosableTabComponent} to which the
         * {@link TabButton} belongs.
         *
         * @return the corresponding {@link AveClosableTabComponent}.
         */
        public ClosableTabComponent getTabComponent() {
            return tabComponent;
        }
    }
}

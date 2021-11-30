package de.joergwille.playground.jtabbedpaneextended;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.extended.ClosableTabComponent;
import javax.swing.extended.JTabbedPaneExtended;
import javax.swing.extended.RemoveTabAction;
import javax.swing.plaf.metal.MetalLookAndFeel;

@SuppressWarnings("serial")
public class Main extends JFrame {

    private final static boolean USE_EXTENDED_TABBED_PANE = true;
    private final static int NUM_TABS = 10;

    final JTabbedPane testTabbedPane;
    final JMenuItem tabComponentsItem;
    final JMenuItem scrollLayoutItem;

    public Main(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (USE_EXTENDED_TABBED_PANE) {
            this.testTabbedPane = new JTabbedPaneExtended();
        } else {
            this.testTabbedPane = new JTabbedPane();
        }
        this.testTabbedPane.setName("TestTabbedName");
        JMenuBar menuBar = new JMenuBar();
        tabComponentsItem = new JCheckBoxMenuItem("Use TabComponents", true);
        tabComponentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                InputEvent.ALT_MASK));
        tabComponentsItem.addActionListener(e -> {
            for (int i = 0; i < testTabbedPane.getTabCount(); i++) {
                if (tabComponentsItem.isSelected()) {
                } else {
                    testTabbedPane.setTabComponentAt(i, null);
                }
            }
        });
        scrollLayoutItem = new JCheckBoxMenuItem("Set WrapLayout");
        scrollLayoutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.ALT_MASK));
        scrollLayoutItem.addActionListener(e -> {
            if (testTabbedPane.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT) {
                testTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            } else {
                testTabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
            }
        });
        JMenuItem resetItem = new JMenuItem("Reset JTabbedPane");
        resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                InputEvent.ALT_MASK));
        resetItem.addActionListener(e -> runTest());
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.add(tabComponentsItem);
        optionsMenu.add(scrollLayoutItem);
        optionsMenu.add(resetItem);
        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);
        add(testTabbedPane);
    }

    private void runTest() {
        testTabbedPane.removeAll();
        final Action deleteAction = new RemoveTabAction();
        for (int i = 0; i < NUM_TABS; i++) {
            final String title = "Test " + i;
            final JPanel panel = createPanel(title);
//            if (USE_EXTENDED_TABBED_PANE) {
//                ((JTabbedPaneExtended) testTabbedPane).add(title, panel, deleteAction);
//            } else {
            testTabbedPane.add(title, panel);
            testTabbedPane.setTabComponentAt(i, new ClosableTabComponent(testTabbedPane, deleteAction));
//            }
        }
        tabComponentsItem.setSelected(true);
        testTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        scrollLayoutItem.setSelected(false);
        setSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createPanel(final String panelName) {

        final JPanel panel = new JPanel();
        final JLabel label = new JLabel(panelName);
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
            final String className = lookAndFeel.getClassName();
            if (className.equals(MetalLookAndFeel.class.getName())) {
                try {
                    UIManager.setLookAndFeel(className);
                } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    System.err.println("Could not set LaF " + lookAndFeel.getName() + ".");
                }
            }
        }
        new Main("JTabbedPaneExtended").runTest();
    }
}

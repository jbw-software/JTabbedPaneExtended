package javax.swing.extended;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class RemoveTabAction extends AbstractAction {

    private final static String CLOSE = "Close";
    private final static String SHORT_DESCRIPTION_CLOSE = "Close selected tab";

    public RemoveTabAction() {
        super(CLOSE);
        super.putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        removeTab(e);
    }

    protected boolean removeTab(final ActionEvent evt) {
        if (evt.getSource() instanceof ClosableTabComponent.TabButton) {
            ClosableTabComponent.TabButton tabButton = (ClosableTabComponent.TabButton) evt.getSource();
            ClosableTabComponent closableTabComponent = tabButton.getTabComponent();
            JTabbedPane tappedPane = closableTabComponent.getTabbedPane();
            int index = indexOfTabComponent(tappedPane, closableTabComponent);
            tappedPane.removeTabAt(index);
            tappedPane.revalidate();
            tappedPane.repaint();
            return true;
        }
        return false;
    }

    public int indexOfTabComponent(final JTabbedPane tabbedPane, final Component tabComponent) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component c = tabbedPane.getTabComponentAt(i);
            if (c == tabComponent) {
                return i;
            }
        }
        return -1;
    }
}

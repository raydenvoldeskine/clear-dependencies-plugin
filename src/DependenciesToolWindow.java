
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.*;

public class DependenciesToolWindow implements Observer {

    private JPanel myToolWindowContent;
    private JList<Dependency> list;
    private JScrollPane panel;

    private DependencyListViewModel viewModel = new DependencyListViewModel();

    public DependenciesToolWindow(ToolWindow toolWindow) {
        list.setBorder(new EmptyBorder(5, 5, 5, 5));
        list.setCellRenderer(new DependencyRenderer());
        list.setFixedCellHeight(22);
        list.setModel(viewModel.getModel());
        viewModel.addObserver(this);
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    @Override
    public void update(Observable o, Object arg) {
        list.setModel(viewModel.getModel());
    }
}

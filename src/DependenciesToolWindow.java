
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if(!e.getValueIsAdjusting()) {
                    final List<Dependency> selectedValuesList = list.getSelectedValuesList();
                    if (!selectedValuesList.isEmpty()){
                        viewModel.open(selectedValuesList.get(0).getFile());
                    }
                }
            }
        });
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

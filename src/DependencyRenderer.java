import com.intellij.ui.JBColor;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class DependencyRenderer extends JLabel implements ListCellRenderer<Dependency> {

    public DependencyRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Dependency> list,
                                                  Dependency entry,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {


        setText("  " + entry.getName());
        if (cellHasFocus){
            setBackground(JBColor.BLUE);
            setForeground(JBColor.WHITE);
        } else {

            switch (entry.getType()){
                case SEPARATOR: setBackground(Color.decode("#ECECEC"));
                                setForeground(JBColor.BLACK); break;

                case MESSAGE:   setBackground(JBColor.WHITE);
                                setForeground(JBColor.LIGHT_GRAY); break;
                default:
                                setBackground(JBColor.WHITE);
                                setForeground(JBColor.BLACK);
            }

        }

        return this;
    }
}

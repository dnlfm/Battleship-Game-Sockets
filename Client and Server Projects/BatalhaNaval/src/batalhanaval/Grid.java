package batalhanaval;

import batalhanaval.utils.Utilitarios;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author freitas
 */
public class Grid extends DefaultTableCellRenderer{
    public static int max_grid_height = 30;
    public static int max_grid_width = 30;

//    ArrayList<JLayeredPane> paineis = new ArrayList<>();
    
    public Grid(){
        this.setHorizontalAlignment(0); // 0 == centro
        this.setSize(max_grid_width, max_grid_height);
//        JLayeredPane jLayeredPane = new JLayeredPane();
//        jLayeredPane.setPreferredSize(new Dimension(max_grid_width, max_grid_height));        
//        paineis.add(jLayeredPane);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
//        if(value instanceof JLabel){
//            
//            jLayeredPane.add((JLabel) value, new Integer(1));
//        }
//        return jLayeredPane;
//        if(isSelected)
//            System.out.println("Linha: " + row + "\nColuna: " + column);
        JLabel label = (JLabel) value;
        
        return label;
    }
}

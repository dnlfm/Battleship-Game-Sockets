package batalhanaval.utils;

import batalhanaval.Grid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author  Daniel Freitas Martins - 2304
 *          Naiara Cristiane dos Reis Diniz - 3005
 */
public class UtilitariosTabela {
    
    public static final int QTD_MAX_COLUNAS_TABELA = 2;
    
    /**
     * Função responsável por alimentar uma tabela.
     * @param tabela Tabela a ser alimentada.
     * @param dados Dados a serem inseridos na tabela.
     */
    public static void inicializarTabela(JTable tabela, ArrayList<ArrayList<String>> dados){
        DefaultTableModel dfm_tabela = (DefaultTableModel) tabela.getModel();
        dfm_tabela.setRowCount(0);
        dfm_tabela.setColumnCount(QTD_MAX_COLUNAS_TABELA);
        
        // https://stackoverflow.com/questions/7433602/how-to-center-in-jtable-cell-a-value
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        for(int i = 0; i < QTD_MAX_COLUNAS_TABELA; i++){
            tabela.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }        
        /////
        
        for(ArrayList<String> linha : dados){
            dfm_tabela.addRow(linha.toArray());
        }
    }
    
    /**
     * Função responsável por alimentar uma tabela.
     * @param cenario Tabela a ser alimentada.
     * @param dados Dados a serem inseridos na tabela.
     * @param qtd_colunas Quantidade de colunas na tabela menos 1.
     */
    public static void inicializarTabelaGrid(JTable cenario, JLabel[][] dados, int qtd_colunas){
        DefaultTableModel dfm_tabela = (DefaultTableModel) cenario.getModel();
        dfm_tabela.setRowCount(0);
        dfm_tabela.setColumnCount(qtd_colunas + 1);
        
        // https://stackoverflow.com/questions/7433602/how-to-center-in-jtable-cell-a-value
//        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
//        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);                
//        cenario.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        for(int i = 0; i <= qtd_colunas; i++){            
            Grid grid = new Grid();
            cenario.getColumnModel().getColumn(i).setCellRenderer(grid);
            cenario.getColumnModel().getColumn(i).setPreferredWidth(Grid.max_grid_width);
            cenario.getColumnModel().getColumn(i).setMaxWidth(Grid.max_grid_width);
        }
        /////
        
        for(JLabel[] o : dados){
            dfm_tabela.addRow(o);
        }        
    }
    
//    
//    /**
//     * Função responsável por marcar números na tabela (visualmente, apenas).
//     * @param tabela Tabela a ser modificada.
//     * @param cartela Cartela a ser considerada.
//     */
//    public static void marcarNumerosEmTabela(JTable tabela, Cartela cartela){        
//        TableModel tm_tabela;
//        HashMap<Integer, Boolean> numeros;
//        try{
//            tm_tabela = tabela.getModel();
//            numeros = cartela.getNumeros();
//        } catch(NullPointerException ex){
//            return;
//        }
//        int qtd_linhas = tm_tabela.getRowCount();
//        int qtd_colunas = tm_tabela.getColumnCount();
//        int i, j;
//        Boolean valor = null;
//        String valor_celula;
//        UtilitariosHTML utils = new UtilitariosHTML(true);
//        for(i = 0; i < qtd_linhas; i++){
//            for(j = 0; j < qtd_colunas; j++){
//                valor_celula = tm_tabela.getValueAt(i, j).toString();
//                if(isStringInteger(valor_celula)){
//                    if((valor = numeros.get(Integer.parseInt(valor_celula))) != null){
//                        if(valor){
//                            utils.incorporarMensagem(utils.toH2(utils.toFontColor(valor_celula, "red")));
//                            tm_tabela.setValueAt(utils.getMensagemHTML(), i, j);
//                        }
//                    }
//                }
//            }
//        }
//    }
    
    /**
     * Verifica se uma String é um valor inteiro válido.
     * @param str String a ser avaliada
     * @return true se esta string pode ser representada como um inteiro; false caso contrário.
     */
    public static boolean isStringInteger(String str){
        if(str == null)
            return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) 
                return false;
        }
        return true;
    }    
}

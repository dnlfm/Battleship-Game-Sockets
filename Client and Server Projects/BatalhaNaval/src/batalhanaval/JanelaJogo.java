package batalhanaval;

import static batalhanaval.JanelaInicial.ID_JOGADOR_1;
import static batalhanaval.JanelaInicial.JOGADORES_CONECTADOS;
import batalhanaval.utils.Utilitarios;
import static batalhanaval.JanelaInicial.receberMensagemServidor;
import static batalhanaval.JanelaInicial.enviarMensagemServidor;
import static batalhanaval.JanelaInicial.MARCADOR_FIM_MENSAGEM;
import static batalhanaval.JanelaInicial.receberMensagemServidor;
import batalhanaval.utils.UtilitariosTabela;
import java.awt.AWTError;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author freitas
 */
public class JanelaJogo extends javax.swing.JDialog {

    private static final String LETRAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    public static final int QTD_JOGADORES = 2;
    public static final int JOGADOR_1 = 0;
    public static final int JOGADOR_2 = 1;
    
    public static final String ID_JOGADOR_1 = "9";
    public static final String ID_JOGADOR_2 = "8";
    
    public static final int MARCADOR_MEIO_EMBARCACAO_VERTICAL = 1;
    public static final int MARCADOR_INICIO_EMBARCACAO_VERTICAL = 2;
    public static final int MARCADOR_FIM_EMBARCACAO_VERTICAL = 3;
    public static final int MARCADOR_MEIO_EMBARCACAO_HORIZONTAL = 4;
    public static final int MARCADOR_INICIO_EMBARCACAO_HORIZONTAL = 5;
    public static final int MARCADOR_FIM_EMBARCACAO_HORIZONTAL = 6;
    public static final int MARCADOR_DESTRUIDO = 7;
    public static final int MARCADOR_TIRO_AGUA = 8;
    public static final int MARCADOR_DESTRUIDO_MEIO_EMBARCACAO_VERTICAL = 9;
    public static final int MARCADOR_DESTRUIDO_INICIO_EMBARCACAO_VERTICAL = 10;
    public static final int MARCADOR_DESTRUIDO_FIM_EMBARCACAO_VERTICAL = 11;
    public static final int MARCADOR_DESTRUIDO_MEIO_EMBARCACAO_HORIZONTAL = 12;
    public static final int MARCADOR_DESTRUIDO_INICIO_EMBARCACAO_HORIZONTAL = 13;
    public static final int MARCADOR_DESTRUIDO_FIM_EMBARCACAO_HORIZONTAL = 14;
    
    private boolean jogada_liberada = false;
    private int dimensao_matriz_N;
    private int tam_embarcacao_X;
    private int qtd_embarcacoes_Y;
    private int qtd_tentativas_Z;  
    private int[][] matriz;
    private int[][] matriz_inimiga;
    private boolean matriz_inimiga_inicializada = false;
    private String id_jogador;
    
    /**
     * Creates new form JanelaJogo
     */
    public JanelaJogo(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        // teste();
        try{
            this.id_jogador = receberMensagemServidor(); // Escutar o servidor para saber se é Jogador 1 ou Jogador 2            
            String parametrosCenario = receberMensagemServidor(); // mensagem para construir o cenário...            
            if(parametrosCenario != null){
                parseParametros(parametrosCenario, true);
                inicializarMatrizInimiga();
                construirCenario(jTable1, jPanel3, false);
                construirCenario(jTable2, jPanel4, true);
                jSplitPane1.setDividerLocation(-1);                
            } else{
                JOptionPane.showMessageDialog(null, "Parâmetros criados null");
            }            
        } catch(IOException ex){
            ex.printStackTrace();
            System.out.println("..");
        }        
    }
    
    /**
     * Creates new form JanelaJogo
     */
//    public JanelaJogo(java.awt.Frame parent, boolean modal, String parametrosCenario) {
//        super(parent, modal);
//        initComponents();
//    }
    
    private void teste(){
        // valores default para testes...
        dimensao_matriz_N = 10;
        tam_embarcacao_X = 1;
        qtd_embarcacoes_Y = 5;
        qtd_tentativas_Z = 40;
        ////
        
        matriz = new int[dimensao_matriz_N][dimensao_matriz_N];
        for(int i = 0; i < dimensao_matriz_N; i++){
            for(int j = 0; j < dimensao_matriz_N; j++){
                matriz[i][j] = 0;
            }
        }
        
        inicializarMatrizInimiga();
        construirCenario(jTable1, jPanel3, false);        
        construirCenario(jTable2, jPanel4, true);   
        jSplitPane1.setDividerLocation(-1);
        inicializarPartidaJogador2();
    }
    
    private void inicializarMatrizInimiga(){
        matriz_inimiga = new int[dimensao_matriz_N][dimensao_matriz_N];
        for(int i = 0; i < dimensao_matriz_N; i++){
            for(int j = 0; j < dimensao_matriz_N; j++){
                matriz_inimiga[i][j] = 0;
            }
        }
    }
    
    private void parseParametros(String msg_servidor, boolean parseMatriz){
        StringTokenizer st = new StringTokenizer(msg_servidor, "\n");
        
        this.jogada_liberada = st.nextToken().equals("1");
        this.dimensao_matriz_N = Integer.parseInt(st.nextToken());
        this.tam_embarcacao_X = Integer.parseInt(st.nextToken());
        this.qtd_embarcacoes_Y = Integer.parseInt(st.nextToken());
        this.qtd_tentativas_Z = Integer.parseInt(st.nextToken());
        Grid.max_grid_width = Integer.parseInt(st.nextToken());
        Grid.max_grid_height = Integer.parseInt(st.nextToken());
        
        if(parseMatriz){
            matriz = new int[dimensao_matriz_N][dimensao_matriz_N];
            for(int i = 0; i < dimensao_matriz_N; i++){
                String s = st.nextToken();
                matriz[i] = Utilitarios.stringToVetor(s);
                System.out.println(s);
            }            
        }
        return;
    }
    
    private void construirCenario(JTable tabela, JPanel componente_pai, boolean telaInimiga){
        JLabel[][] objetos = new JLabel[dimensao_matriz_N + 1][dimensao_matriz_N+1];
        tabela.setRowHeight(Grid.max_grid_height);
        tabela.setDragEnabled(false);        
//        tabela.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        DefaultTableModel dtm = (DefaultTableModel) tabela.getModel();
        dtm.setRowCount(0); // excluindo todas as linhas
        dtm.setColumnCount(0); // excluindo todas as colunas
        
        JLabel jLabel = new JLabel();
        jLabel.setText("");
        objetos[0][0] = jLabel;        
        for(int j = 1; j <= dimensao_matriz_N; j++){
            jLabel = new JLabel();
            jLabel.setText(j + "");
            jLabel.setHorizontalAlignment(JLabel.CENTER);
            objetos[0][j] = jLabel;
        }
        for(int i = 1; i <= dimensao_matriz_N; i++){
            jLabel = new JLabel();
            jLabel.setText(LETRAS.charAt(i-1) + "");
            jLabel.setHorizontalAlignment(JLabel.CENTER);
            objetos[i][0] = jLabel;
            for(int j = 1; j <= dimensao_matriz_N; j++){
                jLabel = new JLabel();
                String nome_arquivo;
                if(telaInimiga)
                    nome_arquivo = getNomeArquivoImagemAdequada(matriz_inimiga, i-1, j-1);
                else                    
                    nome_arquivo = getNomeArquivoImagemAdequada(matriz, i-1, j-1);
                try{                    
                    byte[] bytes = Utilitarios.svgToPng(nome_arquivo);
                    if(bytes != null)
                        jLabel.setIcon(new javax.swing.ImageIcon(bytes));        
                } catch(AWTError | NullPointerException ex){
                    System.out.println("Não foi possível carregar a imagem do programa...");
                }
                objetos[i][j] = jLabel;
            }
            
        }
//        JOptionPane.showMessageDialog(null, jLabel);
        UtilitariosTabela.inicializarTabelaGrid(tabela, objetos, dimensao_matriz_N);
//        componente_pai.setPreferredSize(jSplitPane1.getPreferredSize());
        if(telaInimiga && matriz_inimiga_inicializada == false){
            matriz_inimiga_inicializada = true;
            tabela.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    iniciarPartida(e);
                }
            });
        }
        return;
    }
    
    public String getNomeArquivoImagemAdequada(int[][] matriz, int i, int j){
        String nome_arquivo = "/img/Cenario/grid.svg";
        if(tam_embarcacao_X == 1 && matriz[i][j] != 0 && matriz[i][j] < MARCADOR_DESTRUIDO){
            nome_arquivo = "/img/Canoa/canoa.svg";            
        } else{
            switch(matriz[i][j]){
                case MARCADOR_INICIO_EMBARCACAO_VERTICAL: 
                    if(tam_embarcacao_X == 2){
                        nome_arquivo = "/img/Submarino/s_vertical_1.svg";
                    } else{
                        nome_arquivo = "/img/Navio/n_vertical_1.svg";
                    }
                    break;
                case MARCADOR_MEIO_EMBARCACAO_VERTICAL:
                    nome_arquivo = "/img/Navio/n_vertical_2.svg";
                    break;
                case MARCADOR_FIM_EMBARCACAO_VERTICAL:
                    if(tam_embarcacao_X == 2){
                        nome_arquivo = "/img/Submarino/s_vertical_2.svg";
                    } else{
                        nome_arquivo = "/img/Navio/n_vertical_3.svg";
                    }
                    break;
                case MARCADOR_INICIO_EMBARCACAO_HORIZONTAL:
                    if(tam_embarcacao_X == 2){
                        nome_arquivo = "/img/Submarino/s_horizontal_2.svg";
                    } else{
                        nome_arquivo = "/img/Navio/n_horizontal_3.svg";
                    }
                    break;
                case MARCADOR_MEIO_EMBARCACAO_HORIZONTAL:
                    nome_arquivo = "/img/Navio/n_horizontal_2.svg";
                    break;
                case MARCADOR_FIM_EMBARCACAO_HORIZONTAL:
                    if(tam_embarcacao_X == 2){
                        nome_arquivo = "/img/Submarino/s_horizontal_1.svg";
                    } else{
                        nome_arquivo = "/img/Navio/n_horizontal_1.svg";
                    }
                    break;
                case MARCADOR_DESTRUIDO:
                    nome_arquivo = "/img/embarcacoes_fogo/canoa_f.svg";
                    break;
                case MARCADOR_TIRO_AGUA:
                    nome_arquivo = "/img/Ataque/errou.svg";
                    break;
                case MARCADOR_DESTRUIDO_INICIO_EMBARCACAO_VERTICAL:
                    if(tam_embarcacao_X == 2){
                        nome_arquivo = "/img/embarcacoes_fogo/s_vertical_1_f.svg";
                    } else{
                        nome_arquivo = "/img/embarcacoes_fogo/n_vertical_1_f.svg";
                    }
                    break;
                case MARCADOR_DESTRUIDO_MEIO_EMBARCACAO_VERTICAL:
                    nome_arquivo = "/img/embarcacoes_fogo/n_vertical_2_f.svg";
                    break;
                case MARCADOR_DESTRUIDO_FIM_EMBARCACAO_VERTICAL:
                    if(tam_embarcacao_X == 2){
                        nome_arquivo = "/img/embarcacoes_fogo/s_vertical_2_f.svg";
                    } else{
                        nome_arquivo = "/img/embarcacoes_fogo/n_vertical_3_f.svg";
                    }
                    break;
                case MARCADOR_DESTRUIDO_INICIO_EMBARCACAO_HORIZONTAL:
                    if(tam_embarcacao_X == 2){
                        nome_arquivo = "/img/embarcacoes_fogo/s_horizontal_2_f.svg";
                    } else{
                        nome_arquivo = "/img/embarcacoes_fogo/n_horizontal_3_f.svg";
                    }
                    break;
                case MARCADOR_DESTRUIDO_MEIO_EMBARCACAO_HORIZONTAL:
                    nome_arquivo = "/img/embarcacoes_fogo/n_horizontal_2_f.svg";
                    break;
                case MARCADOR_DESTRUIDO_FIM_EMBARCACAO_HORIZONTAL:
                    if(tam_embarcacao_X == 2){
                        nome_arquivo = "/img/embarcacoes_fogo/s_horizontal_1_f.svg";
                    } else{
                        nome_arquivo = "/img/embarcacoes_fogo/n_horizontal_1_f.svg";
                    }
                    break;
            }
        }
        return nome_arquivo;
    }
    
    private void iniciarPartida(MouseEvent e){
        JTable elementoClicado = (JTable) e.getSource();
        int linha = elementoClicado.getSelectedRow();
        int coluna = elementoClicado.getSelectedColumn();
        System.out.println("BBBBBBBB");
        boolean encerrar = false;
        String info_jogada; // guarda se jogada está liberada ou não, ou se ocorreu empate.
        String pontuacao_jogador_1 = "", pontuacao_jogador_2 = "";
        int[] qtd_embarcacoes_restantes;
        int qtd_max_embarcacoes;        
        if(jogada_liberada && linha != 0 && coluna != 0){
            System.out.println("Linha: " + linha + "\nColuna: " + coluna);
            if(matriz_inimiga[linha-1][coluna-1] >= MARCADOR_DESTRUIDO){
                JOptionPane.showMessageDialog(null, "Um torpedo já atingiu essa área.\nPor favor, escolha outras coordenadas...");
                return;
            }
            System.out.println("*** MANDANDO BALA!!! ***");
            enviarJogada(linha - 1, coluna - 1);
            String msg_servidor = "";                        
            try {
                System.out.println("Escutando...");
                msg_servidor = receberMensagemServidor();
                System.out.println("Escutei!");
            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(JanelaJogo.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(msg_servidor.isEmpty()){
                System.out.println("AAAAzzz");
                return;
            }

            // reconstruindo cenário do inimigo após a jogada
            System.out.println("Reconstruindo cenário do adversário...");
//                        System.out.println(msg_servidor);
            StringTokenizer st = new StringTokenizer(msg_servidor, "\n");

            info_jogada = st.nextToken();
            System.out.println("Info_jogada: " + info_jogada);
            jogada_liberada = info_jogada.equals("1"); 
//            encerrar = verificarSeHaVencedor(info_jogada, pontuacao_jogador_1, pontuacao_jogador_2, false);
            pontuacao_jogador_1 = st.nextToken();
            pontuacao_jogador_2 = st.nextToken();
            qtd_tentativas_Z = Integer.parseInt(st.nextToken());
            qtd_embarcacoes_restantes = new int[QTD_JOGADORES];
            qtd_embarcacoes_restantes[JOGADOR_1] = Integer.parseInt(st.nextToken());
            qtd_embarcacoes_restantes[JOGADOR_2] = Integer.parseInt(st.nextToken());
            qtd_max_embarcacoes = Integer.parseInt(st.nextToken());
            String[] s = st.nextToken().split(" ");
            linha = Integer.parseInt(s[0]); coluna = Integer.parseInt(s[1]);
            int novo_dado = Integer.parseInt(st.nextToken());
//            Utilitarios.imprimirMatriz(matriz_inimiga);
            System.out.println("--------");
            matriz_inimiga[linha][coluna] = novo_dado;
//            Utilitarios.imprimirMatriz(matriz_inimiga);

            construirCenario(jTable2, jPanel4, true); 
            atualizarInformacoesTela(Integer.parseInt(pontuacao_jogador_1), Integer.parseInt(pontuacao_jogador_2),
                    getJogador(id_jogador), getAdversario(id_jogador), qtd_embarcacoes_restantes, qtd_max_embarcacoes);             
            jogada_liberada = info_jogada.equals("1");
            encerrar = verificarSeHaVencedor(info_jogada, pontuacao_jogador_1, pontuacao_jogador_2);
            if(encerrar)
                return;

            JOptionPane.showMessageDialog(null, "Torpedo a caminho...\nCoordenadas: " + LETRAS.charAt(linha) + (coluna + 1));

            // reconstruindo o próprio cenário após a jogada do inimigo
            System.out.println("Reconstruindo o próprio cenário...");
            try {
                System.out.println("Escutando...");
                msg_servidor = receberMensagemServidor();
                System.out.println("Escutei!");
            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(JanelaJogo.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(msg_servidor.isEmpty()){
                System.out.println("AAAAzzz");
                return;
            }

            System.out.println(msg_servidor);
            st = new StringTokenizer(msg_servidor, "\n");

            info_jogada = st.nextToken();
            System.out.println("Info_jogada: " + info_jogada);
            jogada_liberada = info_jogada.equals("1");             
//            encerrar = verificarSeHaVencedor(info_jogada, pontuacao_jogador_1, pontuacao_jogador_2, false);
             
            pontuacao_jogador_1 = st.nextToken();
            pontuacao_jogador_2 = st.nextToken();
            qtd_tentativas_Z = Integer.parseInt(st.nextToken());
            qtd_embarcacoes_restantes = new int[QTD_JOGADORES];
            qtd_embarcacoes_restantes[JOGADOR_1] = Integer.parseInt(st.nextToken());
            qtd_embarcacoes_restantes[JOGADOR_2] = Integer.parseInt(st.nextToken());
            qtd_max_embarcacoes = Integer.parseInt(st.nextToken());
            s = st.nextToken().split(" ");
            linha = Integer.parseInt(s[0]); coluna = Integer.parseInt(s[1]);
            novo_dado = Integer.parseInt(st.nextToken());
//            Utilitarios.imprimirMatriz(matriz);
            System.out.println("--------");
            matriz[linha][coluna] = novo_dado;
//            Utilitarios.imprimirMatriz(matriz);

            construirCenario(jTable1, jPanel3, false);
            atualizarInformacoesTela(Integer.parseInt(pontuacao_jogador_1), Integer.parseInt(pontuacao_jogador_2),
                    getJogador(id_jogador), getAdversario(id_jogador), qtd_embarcacoes_restantes, qtd_max_embarcacoes);             
            encerrar = verificarSeHaVencedor(info_jogada, pontuacao_jogador_1, pontuacao_jogador_2);
            System.out.println("Encerrar? " + encerrar);
            if(encerrar)
                return;
            JOptionPane.showMessageDialog(null, "Cuidado, torpedo inimigo se aproximando!!"
                    + "\nCoordenadas: " + LETRAS.charAt(linha) + (coluna + 1));

        }
    }
    
    private int getAdversario(String id_jogador){
        return id_jogador.equals(ID_JOGADOR_1) ? JOGADOR_2 : JOGADOR_1;
    }
    
    private int getJogador(String id_jogador){
        return id_jogador.equals(ID_JOGADOR_1) ? JOGADOR_1 : JOGADOR_2;
    }
    
    private boolean verificarSeHaVencedor(String info_jogada, String pontuacao_jogador_1, 
            String pontuacao_jogador_2){
        switch(info_jogada){
            case "-1": // empate
                ocorreuEmpate(pontuacao_jogador_1, pontuacao_jogador_2);
                return true;
            case "-2": // jogador ganhou
                ganhou(pontuacao_jogador_1, pontuacao_jogador_2);
                return true;
            case "-3": // jogador perdeu
                perdeu(pontuacao_jogador_1, pontuacao_jogador_2);    
                return true;
        }
        
        return false;
    }
    
    private void perdeu(String pontuacao_jogador_1, String pontuacao_jogador_2){        
        JOptionPane.showMessageDialog(null, "Você perdeu!"); 
        encerrarJogo();
    }
    
    private void ganhou(String pontuacao_jogador_1, String pontuacao_jogador_2){
        JOptionPane.showMessageDialog(null, "Parabéns!! Você ganhou!");                                
        encerrarJogo();
    }
    
    private void ocorreuEmpate(String pontuacao_jogador_1, String pontuacao_jogador_2){
        JOptionPane.showMessageDialog(null, "EMPATE!");                                
        encerrarJogo();
    }
    
    private void encerrarJogo(){
        jogada_liberada = false;  
        JanelaInicial.encerrarConexao();
    }

    public void enviarJogada(int linha, int coluna){
        try {
            enviarMensagemServidor(linha + " " + coluna);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(JanelaJogo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void inicializarPartidaJogador2(){
        if(jogada_liberada == false){
            String msg_servidor = "";
            try {
                msg_servidor = receberMensagemServidor();
            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(JanelaJogo.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(msg_servidor.isEmpty()){
                System.out.println("JJJJJJJJJzzzzzzzzzzz");
                return;
            }
            System.out.println(msg_servidor);
            StringTokenizer st = new StringTokenizer(msg_servidor, "\n");

            String info_jogada = st.nextToken();
            jogada_liberada = info_jogada.equals("1");    
            int pontuacao_jogador_1 = Integer.parseInt(st.nextToken());
            int pontuacao_jogador_2 = Integer.parseInt(st.nextToken());
            qtd_tentativas_Z = Integer.parseInt(st.nextToken()); 
            int[] qtd_embarcacoes_restantes = new int[QTD_JOGADORES];
            qtd_embarcacoes_restantes[JOGADOR_1] = Integer.parseInt(st.nextToken());
            qtd_embarcacoes_restantes[JOGADOR_2] = Integer.parseInt(st.nextToken());
            int qtd_max_embarcacoes = Integer.parseInt(st.nextToken());
            String[] s = st.nextToken().split(" ");
            int linha = Integer.parseInt(s[0]);
            int coluna = Integer.parseInt(s[1]);
            int novo_dado = Integer.parseInt(st.nextToken());
            atualizarInformacoesTela(pontuacao_jogador_1, pontuacao_jogador_2, JOGADOR_2,
                    JOGADOR_1, qtd_embarcacoes_restantes, qtd_max_embarcacoes);
            
            System.out.println("-> Jogador 2 sendo liberado!");
            System.out.println("linha: " + linha);
            System.out.println("coluna: " + coluna);
            System.out.println("novo_dado: " + novo_dado);
//            Utilitarios.imprimirMatriz(matriz);
            System.out.println("--------------");
            matriz[linha][coluna] = novo_dado;
//            Utilitarios.imprimirMatriz(matriz);
            construirCenario(jTable1, jPanel3, false);   
            JOptionPane.showMessageDialog(null, "Cuidado, torpedo inimigo se aproximando!!"
                    + "\nCoordenadas: " + LETRAS.charAt(linha) + (coluna + 1));            
            
            verificarSeHaVencedor(info_jogada, pontuacao_jogador_1 + "", pontuacao_jogador_2 + "");            
        }
    }
    
    private void atualizarInformacoesTela(int pontuacao_jogador_1, int pontuacao_jogador_2,
            int id_jogador, int id_adversario, int[] qtd_embarcacoes_restantes, int qtd_max_embarcacoes){
        atualizarInformacoesTela();
        if(id_jogador == JOGADOR_1){
            txtPontuacaoJogador.setText("" + pontuacao_jogador_1);
            txtPontuacaoAdversario.setText("" + pontuacao_jogador_2);
        } else{
            txtPontuacaoJogador.setText("" + pontuacao_jogador_2);
            txtPontuacaoAdversario.setText("" + pontuacao_jogador_1);
        }
        txtEmbarcacoesRestantes.setText("- Embarcações restantes: " 
                + qtd_embarcacoes_restantes[id_jogador] + " / " + qtd_max_embarcacoes);
        txtEmbarcacoesAfundadas.setText("- Embarcações afundadas: " 
                + (qtd_max_embarcacoes - qtd_embarcacoes_restantes[id_adversario]) + " / " + qtd_max_embarcacoes);          
    }
    
    private void atualizarInformacoesTela(){
        txtTentativasRestantes.setText("- Tentativas restantes: " + qtd_tentativas_Z);       
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        txtTentativasRestantes = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        txtPontuacaoAdversario = new javax.swing.JLabel();
        txtPontuacaoJogador = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        txtEmbarcacoesAfundadas = new javax.swing.JLabel();
        txtEmbarcacoesRestantes = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane(
            javax.swing.JSplitPane.HORIZONTAL_SPLIT,
            false,
            jPanel3,
            jPanel4
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"A", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"B", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"C", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"D", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"E", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"F", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"G", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"H", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"I", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"J", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"k", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"L", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"M", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"N", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"O", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"P", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Q", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"R", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"S", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"T", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"U", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"V", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"W", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"X", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Y", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Z", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable1.setRowSelectionAllowed(false);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.setTableHeader(null);
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"A", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"B", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"C", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"D", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"E", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"F", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"G", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"H", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"I", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"J", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"k", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"L", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"M", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"N", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"O", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"P", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Q", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"R", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"S", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"T", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"U", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"V", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"W", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"X", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Y", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Z", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable2.setRowSelectionAllowed(false);
        jTable2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable2.setTableHeader(null);
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Informações da partida"));

        txtTentativasRestantes.setText("- Tentativas restantes:");

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Placar"));

        txtPontuacaoAdversario.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtPontuacaoAdversario.setText("0");

        txtPontuacaoJogador.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtPontuacaoJogador.setText("0");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Sua pontuação:");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Pontuação do adversário:");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                    .addComponent(txtPontuacaoJogador, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtPontuacaoAdversario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPontuacaoJogador)
                    .addComponent(txtPontuacaoAdversario))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        txtEmbarcacoesAfundadas.setText("- Embarcações afundadas:");

        txtEmbarcacoesRestantes.setText("- Embarcações restantes:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTentativasRestantes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtEmbarcacoesRestantes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtEmbarcacoesAfundadas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(44, 44, 44)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtTentativasRestantes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEmbarcacoesRestantes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEmbarcacoesAfundadas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setOneTouchExpandable(true);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        atualizarInformacoesTela(0, 0, getJogador(id_jogador), 
                getAdversario(id_jogador),
                new int[] {qtd_embarcacoes_Y, qtd_embarcacoes_Y}, qtd_embarcacoes_Y);
        if(id_jogador.equals(ID_JOGADOR_1)) {
            JOptionPane.showMessageDialog(null, "Esperando pelo jogador 2...");
            String respostaConexaoJogador2 = "";
            try {
                respostaConexaoJogador2 = receberMensagemServidor(); // Esperando uma resposta do servidor para saber se o jogador 2 conectou
            } catch (IOException ex) {
                Logger.getLogger(JanelaJogo.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(respostaConexaoJogador2.equals(JOGADORES_CONECTADOS) == false){
                System.out.println("Jogador 2 não se conectou... ?");
                JOptionPane.showMessageDialog(null, "Jogador 2 não se conectou... ?");
            }
        }        
        JOptionPane.showMessageDialog(null, "Jogadores conectados... Partida se iniciando...");
        inicializarPartidaJogador2();
        
    }//GEN-LAST:event_formComponentShown

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JanelaJogo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JanelaJogo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JanelaJogo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JanelaJogo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JanelaJogo dialog = new JanelaJogo(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JLabel txtEmbarcacoesAfundadas;
    private javax.swing.JLabel txtEmbarcacoesRestantes;
    private javax.swing.JLabel txtPontuacaoAdversario;
    private javax.swing.JLabel txtPontuacaoJogador;
    private javax.swing.JLabel txtTentativasRestantes;
    // End of variables declaration//GEN-END:variables
}

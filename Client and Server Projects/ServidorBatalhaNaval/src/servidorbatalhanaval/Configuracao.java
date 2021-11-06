package servidorbatalhanaval;

import java.awt.AWTError;
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author freitas
 */
public class Configuracao extends javax.swing.JFrame {
    
    private static final int PORTA = 3322;
    private static final String CAMINHO_ICONE = "/img/Icones/captain.png";
    
    public static final int QTD_JOGADORES = 2;
    public static final int JOGADOR_1 = 0;
    public static final int JOGADOR_2 = 1;
    
    public static final String MARCADOR_FIM_MENSAGEM = ">";
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
    
    public static final int EMPATE = -1;
    public static final int JOGADOR_VENCEU = -2;
    public static final int ADVERSARIO_VENCEU = -3;
    
    public static final String ID_JOGADOR_1 = "9";
    public static final String ID_JOGADOR_2 = "8";
    public static final String JOGADORES_CONECTADOS = "7";
    public static final String JOGADA_LIBERADA = "1";
    public static final String JOGADA_NEGADA = "0";    
    
    private Socket[] cliente;
    private OutputStreamWriter[] osw;
    private PrintWriter[] printWriter;
    private BufferedReader[] br;
    private ServerSocket serverSocket;
    private int[][][] matriz;
    private int[][][] matriz_jogadas;
    private int[][][] matriz_embarcacoes_destruidas;
    private int[] pontuacao;
    
    private void mudaIcone() {
        try{
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(CAMINHO_ICONE)));
        } catch(AWTError | NullPointerException ex){
            JOptionPane.showMessageDialog(null, "Não foi possível carregar o ícone...");
        }
    }
    
    /**
     * Creates new form Configuracao
     */
    public Configuracao() {
        initComponents();
        mudaIcone();
        
        cliente = null;
        osw = null;
        printWriter = null;
        br = null;
        pontuacao = null;
    }
    
    /**
     * Captura uma mensagem que o jogador envia para o servidor.
     * @param index_jogador Index do socket do correspondente ao jogador a ser ouvido.
     * @return A String correspondente à mensagem enviada pelo jogador.
     * @throws IOException Se getInputStream() falhar.
     */
    private String receberMensagemJogador(int index_jogador) throws IOException{
        StringBuilder sb = new StringBuilder();
        String s;
        s = br[index_jogador].readLine();
        while(s.equals(MARCADOR_FIM_MENSAGEM) == false){
            sb.append(s);
            s = br[index_jogador].readLine();            
            if(s.equals(MARCADOR_FIM_MENSAGEM) == false){
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    private void inicializarMatrizEmbarcacoes(){
        int dimensaoN = (Integer) spinDimensaoN.getValue(); 
        int i, j, k;
        matriz_embarcacoes_destruidas = new int[QTD_JOGADORES][dimensaoN][dimensaoN];
        
        for(i = 0; i < QTD_JOGADORES; i++){
            matriz_embarcacoes_destruidas[i] = Utilitarios.inicializarMatriz(dimensaoN);            
        }
    }
    
    private int getEmbarcacoesRestantes(int id_jogador){
        int dimensaoN = (Integer) spinDimensaoN.getValue();
        int qtd_max_embarcacoes = (Integer) spinNumEmbarcacoesY.getValue();
        int i, j, k, qtd_embarcacoes_restantes = qtd_max_embarcacoes;
        for(i = 0; i < qtd_max_embarcacoes; i++){
            EXTERNO:
            for(j = 0; j < dimensaoN; j++){
                for(k = 0; k < dimensaoN; k++){
                    if(matriz_embarcacoes_destruidas[id_jogador][j][k] == (i+1))
                        break EXTERNO;
                }                
            }
            if(j == dimensaoN)
                qtd_embarcacoes_restantes--;
        }
        
        return qtd_embarcacoes_restantes;
    }
    
    /**
     * Envia uma mensagem para um jogador.
     * @param index_jogador Index do socket do correspondente ao jogador a receber a mensagem.
     * @param mensagem String correspondente à mensagem a ser enviada para o jogador.
     * @throws IOException Se getOutputStream() falhar.
     */
    private void enviarMensagemJogador(int index_jogador, String mensagem) throws IOException{
        printWriter[index_jogador].println(mensagem + "\n" + MARCADOR_FIM_MENSAGEM);
        osw[index_jogador].flush();
    }
    
    private void iniciarServidor(){
        try {
            cliente = new Socket[QTD_JOGADORES];
            osw = new OutputStreamWriter[QTD_JOGADORES];
            printWriter = new PrintWriter[QTD_JOGADORES];
            br = new BufferedReader[QTD_JOGADORES];
            pontuacao = new int[QTD_JOGADORES];
            matriz = null;
            matriz_jogadas = null;
            matriz_embarcacoes_destruidas = null;
            
            if(serverSocket == null || serverSocket.isBound() == false)
                serverSocket = new ServerSocket(PORTA);
            
            for(int i = 0; i < QTD_JOGADORES; i++)
                pontuacao[i] = 0;
            
            escreverMonitor("Iniciando servidor; iniciando espera pelo primeiro jogador...\n");
            
            inicializarMatrizEmbarcacoes();
            
            estabelecerConexao(JOGADOR_1);
            enviarMensagemJogador(JOGADOR_1, ID_JOGADOR_1);
            enviarMensagemJogador(JOGADOR_1, getParametrosParaEnvio(JOGADOR_1, true, true));
            
            estabelecerConexao(JOGADOR_2);
            enviarMensagemJogador(JOGADOR_2, ID_JOGADOR_2);
            enviarMensagemJogador(JOGADOR_2, getParametrosParaEnvio(JOGADOR_2, false, true));            
            
            enviarMensagemJogador(JOGADOR_1, JOGADORES_CONECTADOS);
            
            // jogador 1 começa
//            enviarMensagemJogador(JOGADOR_1, JOGADA_LIBERADA);
//            enviarMensagemJogador(JOGADOR_2, JOGADA_NEGADA);
            
            ArrayList<String> jogadas_feitas_jogador_1 = new ArrayList<>();
            ArrayList<String> jogadas_feitas_jogador_2 = new ArrayList<>();
            int qtd_tentativas_jogador_1 = (Integer) spinNumTentativasZ.getValue();
            int qtd_tentativas_jogador_2 = qtd_tentativas_jogador_1;
            int id_vencedor = EMPATE, linha, coluna;
            int[] qtd_embarcacoes_restantes = new int[QTD_JOGADORES];
            int qtd_embarcacoes = (Integer) spinNumEmbarcacoesY.getValue();
            boolean resposta;
            String[] ponto;
            String coordenadas;
            // TODO: Fazer loop para o jogo...
            do{
                coordenadas = receberMensagemJogador(JOGADOR_1);
                System.out.println("Coordenadas recebidas (jog 1): " + coordenadas);
                contabilizarJogada(JOGADOR_1, JOGADOR_2, jogadas_feitas_jogador_1, coordenadas);                
                qtd_tentativas_jogador_1--;                
                resposta = verificarSePerdeu(JOGADOR_2);
//                System.out.println("KKKKKKKKK");
                
                if(resposta){ 
                    // então JOGADOR_2 perdeu                    
                    id_vencedor = JOGADOR_1;                    
                    break;
                } else{
                    // senão.. é a vez do jogador 2 jogar     
                    
                    /////////// atualizando os cenários de ambos os jogadores...
                    ponto = coordenadas.split(" ");
                    linha = Integer.parseInt(ponto[0]); coluna = Integer.parseInt(ponto[1]);
                    System.out.println("Imprimindo dados para DEBUG: ");
                    System.out.println("linha: " + linha);
                    System.out.println("coluna: " + coluna);
                    System.out.println("novo_dado: " + matriz_jogadas[JOGADOR_2][coluna][linha]);

                    qtd_embarcacoes_restantes[JOGADOR_1] = getEmbarcacoesRestantes(JOGADOR_1);
                    qtd_embarcacoes_restantes[JOGADOR_2] = getEmbarcacoesRestantes(JOGADOR_2);
                    
                    enviarMensagemJogador(JOGADOR_1, "0" + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_1 + "\n" 
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n"                            
                            + matriz_jogadas[JOGADOR_2][linha][coluna]);
    //                System.out.println("VVVVVVVVVV");
                    enviarMensagemJogador(JOGADOR_2, resposta ? "0" : "1" + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_2 + "\n" 
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_2][linha][coluna]);
                    ////////////////////////
//                    System.out.println("*************************");
//                    Utilitarios.imprimirMatriz(matriz_embarcacoes_destruidas[JOGADOR_1]);
//                    System.out.println("---");
//                    Utilitarios.imprimirMatriz(matriz_embarcacoes_destruidas[JOGADOR_2]);
//                    System.out.println("*************************");
                    
                    coordenadas = receberMensagemJogador(JOGADOR_2);
                    System.out.println("Coordenadas recebidas (jog 2): " + coordenadas);
                    contabilizarJogada(JOGADOR_2, JOGADOR_1, jogadas_feitas_jogador_2, coordenadas);
                    qtd_tentativas_jogador_2--;
                    resposta = verificarSePerdeu(JOGADOR_1);
                    
                    qtd_embarcacoes_restantes[JOGADOR_1] = getEmbarcacoesRestantes(JOGADOR_1);
                    qtd_embarcacoes_restantes[JOGADOR_2] = getEmbarcacoesRestantes(JOGADOR_2);
                    
                    if(resposta){
                        // então JOGADOR_1 perdeu
                        id_vencedor = JOGADOR_2;
                        break;
                    }
                    
                    if(qtd_tentativas_jogador_2 == 0){
                        resposta = verificarSePerdeu(JOGADOR_2);        
                        if(resposta){ 
                            // então JOGADOR_2 perdeu                    
                            id_vencedor = JOGADOR_1;                                                
                        } else{
                            id_vencedor = EMPATE;
                        }
                        break;
                    }
                    // senão... é a vez do jogador 1 jogar de novo
                    
                    /////////// atualizando os cenários de ambos os jogadores...
                    ponto = coordenadas.split(" ");
                    linha = Integer.parseInt(ponto[0]);
                    coluna = Integer.parseInt(ponto[1]);                    
                    enviarMensagemJogador(JOGADOR_1, resposta ? "0" : "1" + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_1 + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_1][linha][coluna]);
                    enviarMensagemJogador(JOGADOR_2, "0" + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_2 + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_1][linha][coluna]);                    
                    /////////////////////////////
                    
                }
            } while(qtd_tentativas_jogador_1 > 0 || qtd_tentativas_jogador_2 > 0);
           
            if(resposta == false){ // acabaram-se as tentativas de cada jogador... Determinando vencedor ou empate
                if(pontuacao[JOGADOR_1] > pontuacao[JOGADOR_2]){
                    id_vencedor = JOGADOR_1;
                } else{
                    if(pontuacao[JOGADOR_1] == pontuacao[JOGADOR_2]){
                        id_vencedor = EMPATE;
                    } else{
                        id_vencedor = JOGADOR_2;
                    }
                }
            }
            System.out.println("*** Pontuações e vencedor ***");
            //////// mostrar pontuações e vencedor
            ponto = coordenadas.split(" ");
            linha = Integer.parseInt(ponto[0]);
            coluna = Integer.parseInt(ponto[1]);
            qtd_embarcacoes_restantes[JOGADOR_1] = getEmbarcacoesRestantes(JOGADOR_1);
            qtd_embarcacoes_restantes[JOGADOR_2] = getEmbarcacoesRestantes(JOGADOR_2);
            switch(id_vencedor){
                case JOGADOR_1:                    
                    System.out.println("Vencedor: JOGADOR 1");
                    enviarMensagemJogador(JOGADOR_1, JOGADOR_VENCEU + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_1 + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_2][linha][coluna]);
                    enviarMensagemJogador(JOGADOR_2, ADVERSARIO_VENCEU + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_2 + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_2][linha][coluna]); 
                    break;
                case JOGADOR_2:
                    System.out.println("Vencedor: JOGADOR 2");
                    enviarMensagemJogador(JOGADOR_1, ADVERSARIO_VENCEU + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_1 + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_1][linha][coluna]);
                    enviarMensagemJogador(JOGADOR_2, JOGADOR_VENCEU + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_2 + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_1][linha][coluna]); 
                    break;
                case EMPATE: default:
                    System.out.println("EMPATE");
                    enviarMensagemJogador(JOGADOR_1, EMPATE + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_1 + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_1][linha][coluna]);
                    enviarMensagemJogador(JOGADOR_2, EMPATE + "\n" + pontuacao[JOGADOR_1] + "\n" 
                            + pontuacao[JOGADOR_2] + "\n" + qtd_tentativas_jogador_2 + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_1] + "\n"
                            + qtd_embarcacoes_restantes[JOGADOR_2] + "\n"
                            + qtd_embarcacoes + "\n"
                            + linha + " " + coluna + "\n" 
                            + matriz_jogadas[JOGADOR_1][linha][coluna]); 
                    break;
            }
            
            ///////////////////////////////
            osw[JOGADOR_1].close();
            osw[JOGADOR_2].close();
            br[JOGADOR_1].close();
            br[JOGADOR_2].close();
            printWriter[JOGADOR_1].close();
            printWriter[JOGADOR_2].close();            
            cliente[JOGADOR_1].close();
            cliente[JOGADOR_2].close();
            
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(Configuracao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean verificarSePerdeu(int id_jogador){
        int i;
        int dimensaoN = (Integer) spinDimensaoN.getValue();
        
        EXTERNO:
        for(i = 0; i < dimensaoN; i++){
            for(int j = 0; j < dimensaoN; j++){
                if(matriz[id_jogador][i][j] != 0 && matriz_jogadas[id_jogador][i][j] < MARCADOR_DESTRUIDO){
                    break EXTERNO;
                }
            }
        }
        if(i == dimensaoN)
            return true;
        
        return false;
    }
    
    private boolean contabilizarJogada(int id_jogador, int id_adversario, ArrayList<String> jogadas_feitas, String coordenadas){
        if(jogadas_feitas.contains(coordenadas) == false){
            jogadas_feitas.add(coordenadas);
            String[] ponto = coordenadas.split(" ");
            int linha = Integer.parseInt(ponto[0]), coluna = Integer.parseInt(ponto[1]);
            System.out.println("l: " + linha + "\n" + "c: " + coluna);
//            Utilitarios.imprimirMatriz(matriz_jogadas[id_adversario]);
            int tam_embarcacao = (Integer) spinTamEmbarcacaoX.getValue();
            if(matriz[id_adversario][linha][coluna] != 0 && matriz_jogadas[id_adversario][linha][coluna] == 0){
                pontuacao[id_jogador]++;
                if(tam_embarcacao == 1)
                    matriz_jogadas[id_adversario][linha][coluna] = MARCADOR_DESTRUIDO;
                else
                    matriz_jogadas[id_adversario][linha][coluna] = matriz[id_adversario][linha][coluna] + 8; // este 8 é devido às constantes definidas lá em cima
                matriz_embarcacoes_destruidas[id_adversario][linha][coluna] = 0;
            } else{
                if(matriz[id_adversario][linha][coluna] == 0)
                    matriz_jogadas[id_adversario][linha][coluna] = MARCADOR_TIRO_AGUA;
            }
            return true;
        }
        return false;
    }
    
    private String getParametrosParaEnvio(int id_jogador, boolean jogada_liberada, boolean construirMatriz){
        StringBuilder sb = new StringBuilder(jogada_liberada ? "1" : "0");
        sb.append("\n").append(spinDimensaoN.getValue().toString());
        sb.append("\n").append(spinTamEmbarcacaoX.getValue().toString());
        sb.append("\n").append(spinNumEmbarcacoesY.getValue().toString());
        sb.append("\n").append(spinNumTentativasZ.getValue().toString());  
        sb.append("\n").append(spinLarguraGrid.getValue().toString());
        sb.append("\n").append(spinAlturaGrid.getValue().toString());
        
        
        if(construirMatriz){
            sb.append("\n").append(getMatriz(id_jogador));
        }
        
        return sb.toString();
    }
    
    private String getMatriz(int id_jogador){
        int dimensaoN = (Integer) spinDimensaoN.getValue();
        int tamEmbarcacaoX = (Integer) spinTamEmbarcacaoX.getValue();
        int qtdEmbarcacoesY = (Integer) spinNumEmbarcacoesY.getValue();
        
        int[][] matriz = Utilitarios.inicializarMatriz(dimensaoN);
        if(this.matriz_jogadas == null){
            this.matriz_jogadas = new int[QTD_JOGADORES][dimensaoN][dimensaoN];
            this.matriz_jogadas[0] = Utilitarios.inicializarMatriz(dimensaoN);
            this.matriz_jogadas[1] = Utilitarios.inicializarMatriz(dimensaoN);
        }
        int cont_embarcacoes = 0, x, y;                
        while(cont_embarcacoes < qtdEmbarcacoesY){
            boolean vertical;
            if(Math.random() > 0.5) // vertical ou horizontal será escolhido aleatoriamente
                vertical = true;
            else
                vertical = false;
            
            int max_tolerancia_loop = 0; // para evitar que entre num loop infinito...
            // encontrando o ponto de partida para tentar a inserção...
            boolean conflito = false;
            do{
                int linfX = 0, lsupX = dimensaoN - 1, linfY = 0, lsupY = dimensaoN - 1;
                if(vertical == false) // então x tem restrição...
                    lsupX = lsupX - tamEmbarcacaoX + 1;
                else // então y tem restrição
                    lsupY = lsupY - tamEmbarcacaoX + 1;
                
                x = Utilitarios.gerarValorEmIntervalo(linfX, lsupX);
                y = Utilitarios.gerarValorEmIntervalo(linfY, lsupY);
                System.out.println("lsupx: " + lsupX);
                System.out.println("x: " + x);
                System.out.println("lsupy: " + lsupY);
                System.out.println("y: " + y);
                
                int i;
                for(i = 0; i < tamEmbarcacaoX; i++){
                    if(vertical){
                        if(matriz[y + i][x] != 0){
                            conflito = true; 
                            break;
                        }
                    } else{
                        if(matriz[y][x + i] != 0){
                            conflito = true; 
                            break;
                        }
                    }
                }
                if(i == tamEmbarcacaoX){
                    conflito = false;
                } else{
                    if(Math.random() > 0.5) // vertical ou horizontal será escolhido aleatoriamente
                        vertical = true;
                    else
                        vertical = false;
                }
                
                if(++max_tolerancia_loop > 100000){
                    int op = JOptionPane.showConfirmDialog(null, "Tolerância máxima de loop alcançado. Deseja encerrar a procura por posições livres na matriz?");
                    if(op == JOptionPane.YES_OPTION)
                        break;
                    else
                        max_tolerancia_loop = 0;
                }
            } while(conflito); // enquanto a posição contiver pedaço de outro barco, deve-se continuar a busca...            
            
            if(max_tolerancia_loop > 100000)
                break;
            
            if(vertical){
                // tratar i == 0;
                matriz[y][x] = MARCADOR_INICIO_EMBARCACAO_VERTICAL;
                matriz_embarcacoes_destruidas[id_jogador][y][x] = cont_embarcacoes + 1;
                int i;
                for(i = 1; i < tamEmbarcacaoX - 1; i++){
                    matriz[y + i][x] = MARCADOR_MEIO_EMBARCACAO_VERTICAL;                    
                    matriz_embarcacoes_destruidas[id_jogador][y + i][x] = cont_embarcacoes + 1;
                }
                // tratar i == tamEmbarcacaoX - 1
                matriz[y + tamEmbarcacaoX - 1][x] = MARCADOR_FIM_EMBARCACAO_VERTICAL;
                matriz_embarcacoes_destruidas[id_jogador][y + tamEmbarcacaoX - 1][x] = cont_embarcacoes + 1;
            } else{
                // tratar i == 0;
                matriz[y][x] = MARCADOR_INICIO_EMBARCACAO_HORIZONTAL;
                matriz_embarcacoes_destruidas[id_jogador][y][x] = cont_embarcacoes + 1;
                int i;
                for(i = 1; i < tamEmbarcacaoX - 1; i++){
                    matriz[y][x + i] = MARCADOR_MEIO_EMBARCACAO_HORIZONTAL;
                    matriz_embarcacoes_destruidas[id_jogador][y][x + i] = cont_embarcacoes + 1;
                }
                // tratar i == tamEmbarcacaoX - 1
                matriz[y][x + tamEmbarcacaoX - 1] = MARCADOR_FIM_EMBARCACAO_HORIZONTAL;
                matriz_embarcacoes_destruidas[id_jogador][y][x + tamEmbarcacaoX - 1] = cont_embarcacoes + 1;
            }
            
            cont_embarcacoes++;
        }
        
        if(this.matriz == null){
            this.matriz = new int[QTD_JOGADORES][dimensaoN][dimensaoN];            
        }
        
        this.matriz[id_jogador] = matriz;
        return Utilitarios.matrizToString(matriz, dimensaoN, dimensaoN);
    }
    
    private void estabelecerConexao(int index_jogador) throws IOException{
        cliente[index_jogador] = serverSocket.accept();
        osw[index_jogador] = new OutputStreamWriter(cliente[index_jogador].getOutputStream());
        printWriter[index_jogador] = new PrintWriter(osw[index_jogador]);
        br[index_jogador] = new BufferedReader(new InputStreamReader(cliente[index_jogador].getInputStream()));

        escreverMonitor("Jogador " + (index_jogador+1) +" conectado:\nHost: " + cliente[index_jogador].getInetAddress().getHostName()
                + "\nIP: " + cliente[index_jogador].getInetAddress().getHostAddress()
                + "\nPort: " + cliente[index_jogador].getPort() + "\n\n");
    }
    
    
    /**
     * Escreve na janela de monitoramento do servidor.
     * @param monitor String a ser concatenada.
     */
    private void escreverMonitor(String monitor){
        System.out.println(monitor);
        txtMonitor.setText(txtMonitor.getText() +  monitor);
    }
    
    /**
     * Essa função irá analisar se o tamanho da dimensão é menor do que o tamanho
     * da embarcação. Em caso positivo, o tamanho da embarcação será alterado
     * para o valor da dimensão.
     */
    private void corrigirTamEmbarcacao(){
        Integer tamDimensao = (Integer) spinDimensaoN.getValue();
        int tamEmbarcacao = (Integer) spinTamEmbarcacaoX.getValue();
        
        if(tamEmbarcacao > tamDimensao)
            spinTamEmbarcacaoX.setValue(tamDimensao);
    }
    
    /**
     * Avalia se o número de tentativas (Z) é menor que Y*N.
     */
    private void corrigirNumTentativas(){
        int qtd_embarcacoes = (Integer) spinNumEmbarcacoesY.getValue();
        int N = (Integer) spinDimensaoN.getValue();
        Integer qtd_tentativas = (Integer) spinNumTentativasZ.getValue();
        
        if(qtd_tentativas < qtd_embarcacoes * N){
            qtd_tentativas = qtd_embarcacoes * N;
            spinNumTentativasZ.setValue(qtd_tentativas);
        }
    }
    
    /**
     * Avalia se Y > N. Em caso positivo, Y será igual a N
     */
    private void corrigirNumEmbarcacoes(){
        Integer qtd_embarcacoes = (Integer) spinNumEmbarcacoesY.getValue();
        int N = (Integer) spinDimensaoN.getValue();
        
        if(qtd_embarcacoes > N){
            // como isso não pode ocorrer (especificação)...
            qtd_embarcacoes = N;
            spinNumEmbarcacoesY.setValue(qtd_embarcacoes);            
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        spinDimensaoN = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        spinTamEmbarcacaoX = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        spinNumEmbarcacoesY = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        spinNumTentativasZ = new javax.swing.JSpinner();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        spinLarguraGrid = new javax.swing.JSpinner();
        spinAlturaGrid = new javax.swing.JSpinner();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMonitor = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jCheckBox1.setText("Remover restrições de parâmetros");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBox1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Parâmetros"));

        jButton1.setText("Iniciar servidor");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Dimensão da matriz (N):");

        spinDimensaoN.setModel(new javax.swing.SpinnerNumberModel(2, 2, 26, 1));
        spinDimensaoN.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinDimensaoNStateChanged(evt);
            }
        });

        jLabel2.setText("Tamanho embarcação (X):");

        spinTamEmbarcacaoX.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        spinTamEmbarcacaoX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinTamEmbarcacaoXStateChanged(evt);
            }
        });

        jLabel3.setText("Número embarcações (Y):");

        spinNumEmbarcacoesY.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        spinNumEmbarcacoesY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinNumEmbarcacoesYStateChanged(evt);
            }
        });

        jLabel4.setText("Número tentativas (Z):");

        spinNumTentativasZ.setModel(new javax.swing.SpinnerNumberModel(2, 2, null, 1));
        spinNumTentativasZ.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinNumTentativasZStateChanged(evt);
            }
        });

        jLabel5.setText("Largura Grid (pixels):");

        jLabel6.setText("Altura Grid (pixels):");

        spinLarguraGrid.setModel(new javax.swing.SpinnerNumberModel(50, 20, null, 1));

        spinAlturaGrid.setModel(new javax.swing.SpinnerNumberModel(50, 20, null, 1));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(jButton1))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(spinDimensaoN))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinLarguraGrid)
                            .addComponent(spinAlturaGrid)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinTamEmbarcacaoX)
                            .addComponent(spinNumEmbarcacoesY, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(spinNumTentativasZ, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinDimensaoN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinTamEmbarcacaoX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spinNumEmbarcacoesY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(spinNumTentativasZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(spinLarguraGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(spinAlturaGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Monitor"));

        txtMonitor.setColumns(20);
        txtMonitor.setRows(5);
        jScrollPane1.setViewportView(txtMonitor);

        jButton2.setText("Limpar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        iniciarServidor();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int op = JOptionPane.showConfirmDialog(null, "Deseja realmente limpar a área de monitoramento?");
        if(op == JOptionPane.YES_OPTION)
            txtMonitor.setText("");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void spinDimensaoNStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinDimensaoNStateChanged
        if(jCheckBox1.isSelected())
            return;
        corrigirTamEmbarcacao();
        corrigirNumEmbarcacoes();
        corrigirNumTentativas();
    }//GEN-LAST:event_spinDimensaoNStateChanged

    private void spinTamEmbarcacaoXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinTamEmbarcacaoXStateChanged
        if(jCheckBox1.isSelected())
            return;
        corrigirTamEmbarcacao();
    }//GEN-LAST:event_spinTamEmbarcacaoXStateChanged

    private void spinNumTentativasZStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinNumTentativasZStateChanged
        if(jCheckBox1.isSelected())
            return;
        corrigirNumTentativas();
    }//GEN-LAST:event_spinNumTentativasZStateChanged

    private void spinNumEmbarcacoesYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinNumEmbarcacoesYStateChanged
        if(jCheckBox1.isSelected())
            return;
        corrigirNumEmbarcacoes();
        corrigirNumTentativas();
    }//GEN-LAST:event_spinNumEmbarcacoesYStateChanged

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
            java.util.logging.Logger.getLogger(Configuracao.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Configuracao.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Configuracao.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Configuracao.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Configuracao().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner spinAlturaGrid;
    private javax.swing.JSpinner spinDimensaoN;
    private javax.swing.JSpinner spinLarguraGrid;
    private javax.swing.JSpinner spinNumEmbarcacoesY;
    private javax.swing.JSpinner spinNumTentativasZ;
    private javax.swing.JSpinner spinTamEmbarcacaoX;
    private javax.swing.JTextArea txtMonitor;
    // End of variables declaration//GEN-END:variables
}

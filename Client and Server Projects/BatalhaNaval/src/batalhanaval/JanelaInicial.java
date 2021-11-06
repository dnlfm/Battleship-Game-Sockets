package batalhanaval;

import java.awt.AWTError;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author freitas
 */
public class JanelaInicial extends javax.swing.JFrame {

    private static final int PORTA = 3322;
    private static final String CAMINHO_ICONE = "/img/Icones/ancora.png";
    
    public static final String MARCADOR_FIM_MENSAGEM = ">";
    
    public static final String ID_JOGADOR_1 = "9";
    public static final String ID_JOGADOR_2 = "8";
    public static final String JOGADORES_CONECTADOS = "7";
    
    
    private static Socket cliente;
    private static OutputStreamWriter osw;
    private static PrintWriter printWriter;
    private static BufferedReader br;
    
    private File soundFile;
    private AudioInputStream audioIn;
    private Clip clip;
    
    private void mudaIcone() {
        try{
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(CAMINHO_ICONE)));
        } catch(AWTError | NullPointerException ex){
            JOptionPane.showMessageDialog(null, "Não foi possível carregar o ícone...");
        }
    }
    
    /**
     * Creates new form JanelaInicial
     */
    public JanelaInicial() {
        initComponents();
        mudaIcone();
        URL url = null;
        try {            
            url = JanelaInicial.class.getResource("/img/Menus/menu_principal.gif").toURI().toURL();
        } catch (MalformedURLException | URISyntaxException ex) {
            ex.printStackTrace();
            Logger.getLogger(JanelaInicial.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(url == null){
            System.out.println("GIF nao carregada... (url não encontrada?)");
            return;
        }
        Icon icon = new ImageIcon(url);
        JLabel gif = new JLabel(icon);
        gif.setSize(jLayeredPane1.getSize());
        jLayeredPane1.add(gif, -1);
        
        btnJogar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                jogar();
            }
            
        });
        
        btnSair.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sair();
            }            
        });
        
        btnAlternarEstadoMusica.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(clip.isRunning())
                    clip.stop();
                else
                    clip.start();
            }            
        });
        
        iniciarMusica();        
    }
    
    private void iniciarMusica(){
        try {
            // Open an audio input stream.           
//            soundFile = new File(JanelaInicial.class.getResource("/img/Menus/naval_theme.wav").toURI()); //you could also get the sound file with an URL
            soundFile = new File(System.getProperty("user.dir") + "/naval_theme.wav");
            audioIn = AudioSystem.getAudioInputStream(soundFile);              
            // Get a sound clip resource.
            clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
         } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (LineUnavailableException e) {
            e.printStackTrace();
//         } catch (URISyntaxException ex) {
//            Logger.getLogger(JanelaInicial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void encerrarConexao(){
        try {
            cliente.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(JanelaInicial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Captura uma mensagem que o servidor envia para o jogador.
     * @return A String correspondente à mensagem enviada pelo servidor.
     * @throws IOException Se getInputStream() falhar.
     */
    public static String receberMensagemServidor() throws IOException{
        StringBuilder sb = new StringBuilder();
        String s;
        s = br.readLine();
        while(s.equals(MARCADOR_FIM_MENSAGEM) == false){
            sb.append(s);
            System.out.println("s: " + s);
            s = br.readLine();            
            if(s.equals(MARCADOR_FIM_MENSAGEM) == false){
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }
    
    private void sair(){
        int op = JOptionPane.showConfirmDialog(null, "Deseja mesmo abandonar o navio?", "Sair", JOptionPane.YES_NO_OPTION);
        if(op == JOptionPane.YES_OPTION){
            System.exit(0);
        }
    }
    
    /**
     * Envia uma mensagem para o servidor.
     * @param mensagem String correspondente à mensagem a ser enviada para o servidor.
     * @throws IOException Se getOutputStream() falhar.
     */
    public static void enviarMensagemServidor(String mensagem) throws IOException{
        printWriter.println(mensagem + "\n" + MARCADOR_FIM_MENSAGEM);
        osw.flush();
    }
    
    private void jogar(){
        String s = JOptionPane.showInputDialog(null, "Informe o IP do servidor:", "127.0.0.1");
        if(s == null || s.isEmpty())
            return;
        try {        
            clip.stop();
            cliente = new Socket(s, PORTA);
            osw = new OutputStreamWriter(cliente.getOutputStream());
            printWriter = new PrintWriter(osw);
            br = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            
            if(cliente.isConnected() == false){
                JOptionPane.showMessageDialog(null, "Não foi possível conectar ao servidor...");
                return;
            }
            
            // abrindo a janela do jogo
            JanelaJogo janelaJogo = new JanelaJogo(this, rootPaneCheckingEnabled);
            janelaJogo.setVisible(true);
            
            clip.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(JanelaInicial.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Não foi possível estabelecer a conexão com o servidor informado...");
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
        jLayeredPane1 = new javax.swing.JLayeredPane();
        btnJogar = new javax.swing.JLabel();
        btnSair = new javax.swing.JLabel();
        btnAlternarEstadoMusica = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btnJogar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnSair.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLayeredPane1.setLayer(btnJogar, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(btnSair, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(btnAlternarEstadoMusica, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane1Layout.createSequentialGroup()
                        .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                .addGap(218, 218, 218)
                                .addComponent(btnJogar, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 204, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnAlternarEstadoMusica, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAlternarEstadoMusica, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 362, Short.MAX_VALUE)
                .addComponent(btnJogar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1)
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

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        sair();        
    }//GEN-LAST:event_formWindowClosing

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
            java.util.logging.Logger.getLogger(JanelaInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JanelaInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JanelaInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JanelaInicial.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JanelaInicial().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel btnAlternarEstadoMusica;
    private javax.swing.JLabel btnJogar;
    private javax.swing.JLabel btnSair;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}

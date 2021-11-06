package servidorbatalhanaval;

import java.util.StringTokenizer;

/**
 *
 * @author freitas
 */
public class Utilitarios {
    
    public static double gerarValorEmIntervalo(double limite_inferior, double limite_superior) {
        return limite_inferior + Math.random() * (limite_superior - limite_inferior);
    }
    
    public static int gerarValorEmIntervalo(int limite_inferior, int limite_superior){
        return new Long(Math.round(limite_inferior + Math.random() * (limite_superior - limite_inferior))).intValue();
    }
    
    public static String matrizToString(int[][] matriz, int dimensaoN, int dimensaoM){
        StringBuilder sb = new StringBuilder();
        int i, j;
        for(i = 0; i < dimensaoN; i++){
            sb.append(matriz[i][0]); 
            for(j = 1; j < dimensaoM; j++){
               sb.append(" ").append(matriz[i][j]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public static int[] stringToVetor(String matrizString){
        StringBuilder sb = new StringBuilder();        
        StringTokenizer st1 = new StringTokenizer(matrizString, " ");
        int i, dimensao = st1.countTokens();
        int[] vetor = new int[dimensao];
        i = 0; 
        while(st1.hasMoreTokens()){
            vetor[i] = Integer.parseInt(st1.nextToken());
            i++;
        }
        
        return vetor;
    }
    
    public static int[][] stringToMatrizQuadrada(String matrizString){
        StringBuilder sb = new StringBuilder();        
        StringTokenizer st1 = new StringTokenizer(matrizString, "\n");
        int i, j, dimensao = st1.countTokens();
        int[][] matriz = new int[dimensao][dimensao];
        i = 0; 
        while(st1.hasMoreTokens()){
            j = 0;
            String linha = st1.nextToken();
            StringTokenizer st2 = new StringTokenizer(linha, " ");
            while(st2.hasMoreTokens()){
                String elemento = st2.nextToken();
                matriz[i][j] = Integer.parseInt(elemento);
                j++;
            }
            i++;
        }
        
        return matriz;
    }
    
    public static void imprimirMatriz(int[][] matriz){
        for(int i = 0; i < matriz.length; i++){
            for(int j = 0; j < matriz[i].length; j++){
                System.out.print(matriz[i][j] + " ");
            }
            System.out.print("\n");
        }
    }
    
    public static int[][] inicializarMatriz(int dimensao){
        int[][] matriz = new int[dimensao][dimensao];
        
        for(int i = 0; i < dimensao; i++){
            for(int j = 0; j < dimensao; j++){
                matriz[i][j] = 0;
            }
        }
        
        return matriz;
    }

}

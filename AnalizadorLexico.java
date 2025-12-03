import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnalizadorLexico {
    private int[][] matrizTransiciones;
    private Map<Character, Integer> mapaColumnas;
    private String programa;
    
    public AnalizadorLexico(String archivoMatriz, String archivoPrograma) throws IOException {
        cargarMatriz(archivoMatriz);
        cargarPrograma(archivoPrograma);
        inicializarMapaColumnas();
    }
    
    private void cargarMatriz(String archivo) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String primeraLinea = br.readLine();
        String[] lineas = new String[200];
        int numFilas = 0;
        
        String linea;
        while ((linea = br.readLine()) != null) {
            lineas[numFilas++] = linea;
        }
        br.close();
        
        matrizTransiciones = new int[numFilas][57];
        
        for (int i = 0; i < numFilas; i++) {
            String[] valores = lineas[i].split(",");
            for (int j = 1; j < valores.length; j++) {
                matrizTransiciones[i][j-1] = Integer.parseInt(valores[j]);
            }
        }
    }
    
    private void cargarPrograma(String archivo) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        StringBuilder sb = new StringBuilder();
        String linea;
        
        while ((linea = br.readLine()) != null) {
            sb.append(linea).append("\n");
        }
        br.close();
        
        programa = sb.toString();
    }
    
    private void inicializarMapaColumnas() {
        mapaColumnas = new HashMap<>();
        
        // Valores negativos para cada carácter
        // La columna real se calcula como: -valor - 1
        mapaColumnas.put('a', -1);
        mapaColumnas.put('b', -2);
        mapaColumnas.put('c', -3);
        mapaColumnas.put('d', -4);
        mapaColumnas.put('e', -5);
        mapaColumnas.put('f', -6);
        mapaColumnas.put('g', -7);
        mapaColumnas.put('h', -8);
        mapaColumnas.put('i', -9);
        mapaColumnas.put('j', -10);
        mapaColumnas.put('k', -11);
        mapaColumnas.put('l', -12);
        mapaColumnas.put('m', -13);
        mapaColumnas.put('n', -14);
        mapaColumnas.put('o', -15);
        mapaColumnas.put('p', -16);
        mapaColumnas.put('q', -17);
        mapaColumnas.put('r', -18);
        mapaColumnas.put('s', -19);
        mapaColumnas.put('t', -20);
        mapaColumnas.put('u', -21);
        mapaColumnas.put('v', -22);
        mapaColumnas.put('w', -23);
        mapaColumnas.put('x', -24);
        mapaColumnas.put('y', -25);
        mapaColumnas.put('z', -26);
        
        mapaColumnas.put('0', -27);
        mapaColumnas.put('1', -28);
        mapaColumnas.put('2', -29);
        mapaColumnas.put('3', -30);
        mapaColumnas.put('4', -31);
        mapaColumnas.put('5', -32);
        mapaColumnas.put('6', -33);
        mapaColumnas.put('7', -34);
        mapaColumnas.put('8', -35);
        mapaColumnas.put('9', -36);
        
        mapaColumnas.put('/', -37);
        mapaColumnas.put('*', -38);
        mapaColumnas.put('+', -39);
        mapaColumnas.put('-', -40);
        mapaColumnas.put(',', -41);
        mapaColumnas.put('>', -42);
        mapaColumnas.put('<', -43);
        mapaColumnas.put('=', -44);
        mapaColumnas.put('&', -45);
        mapaColumnas.put('|', -46);
        mapaColumnas.put(';', -47);
        mapaColumnas.put('.', -48);
        mapaColumnas.put('"', -49);
        mapaColumnas.put('{', -50);
        mapaColumnas.put('}', -51);
        mapaColumnas.put('(', -52);
        mapaColumnas.put(')', -53);
        mapaColumnas.put('[', -54);
        mapaColumnas.put(']', -55);
        mapaColumnas.put('$', -56);
    }
    
    private Integer obtenerColumna(char c) {
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            return null;
        }
        Integer valor = mapaColumnas.get(c);
        if (valor == null) {
            return null;
        }
        // Convertir valor negativo a índice de columna: -valor - 1
        return -valor - 1;
    }
    
    private static final int COLUMNA_DELIMITADOR = 40; // Columna de ',' usada para tokens de aceptación
    
    public void analizar() {
        int estado = 0;
        String lexema = "";
        
        for (int i = 0; i < programa.length(); i++) {
            char c = programa.charAt(i);
            
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (estado != 0) {
                    if (estado >= 1000) {
                        // Ya es un estado de aceptación
                        System.out.println(estado);
                    } else {
                        // Intentar obtener token de aceptación usando columna delimitador
                        int tokenFinal = matrizTransiciones[estado][COLUMNA_DELIMITADOR];
                        if (tokenFinal >= 1000 && tokenFinal != 9999) {
                            System.out.println(tokenFinal);
                        }
                    }
                    estado = 0;
                    lexema = "";
                }
                continue;
            }
            
            Integer columna = obtenerColumna(c);
            
            if (columna == null) {
                if (estado >= 1000) {
                    System.out.println(estado);
                } else if (estado != 0) {
                    int tokenFinal = matrizTransiciones[estado][COLUMNA_DELIMITADOR];
                    if (tokenFinal >= 1000 && tokenFinal != 9999) {
                        System.out.println(tokenFinal);
                    }
                }
                estado = 0;
                lexema = "";
                continue;
            }
            
            int siguienteEstado = matrizTransiciones[estado][columna];
            
            if (siguienteEstado == 9999) {
                if (estado >= 1000) {
                    System.out.println(estado);
                } else if (estado != 0) {
                    // Intentar obtener token de aceptación antes de reiniciar
                    int tokenFinal = matrizTransiciones[estado][COLUMNA_DELIMITADOR];
                    if (tokenFinal >= 1000 && tokenFinal != 9999) {
                        System.out.println(tokenFinal);
                    }
                }
                estado = 0;
                lexema = "";
                i--;
            } else {
                estado = siguienteEstado;
                lexema += c;
                
                if (estado >= 1000) {
                    System.out.println(estado);
                    estado = 0;
                    lexema = "";
                }
            }
        }
        
        // Al final del programa, verificar si queda un token pendiente
        if (estado >= 1000) {
            System.out.println(estado);
        } else if (estado != 0) {
            int tokenFinal = matrizTransiciones[estado][COLUMNA_DELIMITADOR];
            if (tokenFinal >= 1000 && tokenFinal != 9999) {
                System.out.println(tokenFinal);
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            AnalizadorLexico analizador = new AnalizadorLexico(
                "matriz_automatas.csv",
                "programa_prueba.txt"
            );
            analizador.analizar();
        } catch (IOException e) {
            System.err.println("Error al leer archivos: " + e.getMessage());
        }
    }
}

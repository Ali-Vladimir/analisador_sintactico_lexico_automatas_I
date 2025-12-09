import java.util.Scanner;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnalizadorSintactico {
    private String entrada;
    private int posicion;
    private Stack<String> pila;
    private ArrayList<String> movimientos;
    private Map<Integer, Integer> contadorReglas;
    
    public AnalizadorSintactico(String entrada) {
        this.entrada = entrada.trim();
        this.posicion = 0;
        this.pila = new Stack<>();
        this.movimientos = new ArrayList<>();
        this.contadorReglas = new HashMap<>();
        for (int i = 1; i <= 13; i++) {
            contadorReglas.put(i, 0);
        }
    }
    
    private char caracterActual() {
        if (posicion < entrada.length()) {
            return entrada.charAt(posicion);
        }
        return '\0';
    }
    
    private char siguienteCaracter() {
        if (posicion + 1 < entrada.length()) {
            return entrada.charAt(posicion + 1);
        }
        return '\0';
    }
    
    private void avanzar() {
        posicion++;
    }
    
    private void registrarMovimiento(String accion) {
        movimientos.add(accion + " | Pila: " + pila);
    }
    
    private void aplicarRegla(int numRegla) {
        contadorReglas.put(numRegla, contadorReglas.get(numRegla) + 1);
    }
    
    private void saltarEspacios() {
        while (posicion < entrada.length() && Character.isWhitespace(caracterActual())) {
            avanzar();
        }
    }
    
    private boolean esLetra(char c) {
        return Character.isLetter(c);
    }
    
    private boolean esOperador(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '>' || c == '<' || c == '=';
    }
    
    // Gramática:
    // REGLA 1: S  -> if ( E )
    // REGLA 2: E  -> E Op E
    // REGLA 3: E  -> Id
    // REGLA 4: Op -> +
    // REGLA 5: Op -> -
    // REGLA 6: Op -> *
    // REGLA 7: Op -> /
    // REGLA 8: Op -> <=
    // REGLA 9: Op -> >=
    // REGLA 10: Op -> ==
    // REGLA 11: Op -> >
    // REGLA 12: Op -> <
    // REGLA 13: Op -> %
    
    public boolean analizar() {
        System.out.println("\n=== ANALIZADOR SINTACTICO ===");
        System.out.println("Entrada: " + entrada + "\n");
        
        pila.push("$");
        pila.push("S");
        registrarMovimiento("Inicio");
        
        saltarEspacios();
        
        while (!pila.isEmpty() && !pila.peek().equals("$")) {
            String tope = pila.peek();
            char actual = caracterActual();
            
            // S -> if ( E )
            if (tope.equals("S")) {
                if (entrada.startsWith("if")) {
                    pila.pop();
                    pila.push(")");
                    pila.push("E");
                    pila.push("(");
                    pila.push("if");
                    aplicarRegla(1);
                    registrarMovimiento("Aplicar REGLA 1: S -> if ( E )");
                } else {
                    return error("Se esperaba 'if'");
                }
            }
            // Match terminal 'if'
            else if (tope.equals("if")) {
                if (matchPalabra("if")) {
                    pila.pop();
                    registrarMovimiento("Match 'if'");
                } else {
                    return error("Se esperaba 'if'");
                }
            }
            // Match '('
            else if (tope.equals("(")) {
                if (actual == '(') {
                    pila.pop();
                    avanzar();
                    saltarEspacios();
                    registrarMovimiento("Match '('");
                } else {
                    return error("Se esperaba '('");
                }
            }
            // Match ')'
            else if (tope.equals(")")) {
                if (actual == ')') {
                    pila.pop();
                    avanzar();
                    saltarEspacios();
                    registrarMovimiento("Match ')'");
                } else {
                    return error("Se esperaba ')'");
                }
            }
            // E -> Id | E Op E
            else if (tope.equals("E")) {
                if (!esLetra(actual)) {
                    return error("Se esperaba un identificador");
                }
                
                // Ver si después del Id hay un operador
                String id = verIdentificador();
                int posTmp = posicion + id.length();
                while (posTmp < entrada.length() && Character.isWhitespace(entrada.charAt(posTmp))) {
                    posTmp++;
                }
                
                boolean hayOperador = false;
                if (posTmp < entrada.length() && esOperador(entrada.charAt(posTmp))) {
                    hayOperador = true;
                }
                
                pila.pop();
                if (hayOperador) {
                    // E -> E Op E (pero el primer E lo derivamos a Id inmediatamente)
                    pila.push("E");
                    pila.push("Op");
                    pila.push("Id");
                    aplicarRegla(2);
                    aplicarRegla(3); // El primer E se deriva a Id
                    registrarMovimiento("Aplicar REGLA 2: E -> E Op E");
                } else {
                    // E -> Id
                    pila.push("Id");
                    aplicarRegla(3);
                    registrarMovimiento("Aplicar REGLA 3: E -> Id");
                }
            }
            // Match Id
            else if (tope.equals("Id")) {
                if (esLetra(actual)) {
                    String id = leerIdentificador();
                    pila.pop();
                    registrarMovimiento("Match Id: " + id);
                } else {
                    return error("Se esperaba un identificador");
                }
            }
            // Match Op
            else if (tope.equals("Op")) {
                if (esOperador(actual)) {
                    String op = leerOperador();
                    pila.pop();
                    
                    // Determinar qué regla de operador se aplicó
                    switch(op) {
                        case "+": aplicarRegla(4); break;
                        case "-": aplicarRegla(5); break;
                        case "*": aplicarRegla(6); break;
                        case "/": aplicarRegla(7); break;
                        case "<=": aplicarRegla(8); break;
                        case ">=": aplicarRegla(9); break;
                        case "==": aplicarRegla(10); break;
                        case ">": aplicarRegla(11); break;
                        case "<": aplicarRegla(12); break;
                        case "%": aplicarRegla(13); break;
                    }
                    registrarMovimiento("Match Op: " + op);
                } else {
                    return error("Se esperaba un operador");
                }
            }
            else {
                return error("Simbolo no reconocido en la pila: " + tope);
            }
        }
        
        // Verificar que se consumió toda la entrada
        if (caracterActual() != '\0') {
            return error("Caracteres extra al final");
        }
        
        mostrarResultado(true);
        return true;
    }
    
    private boolean error(String mensaje) {
        movimientos.add("ERROR: " + mensaje);
        mostrarResultado(false);
        return false;
    }
    
    private void mostrarResultado(boolean aceptada) {
        System.out.println("MOVIMIENTOS DE LA PILA:");
        for (int i = 0; i < movimientos.size(); i++) {
            System.out.println((i + 1) + ". " + movimientos.get(i));
        }
        
        System.out.println("\n=== RECUENTO DE REGLAS APLICADAS ===");
        System.out.println("REGLA 1 (S -> if ( E )): " + contadorReglas.get(1) + " veces");
        System.out.println("REGLA 2 (E -> E Op E): " + contadorReglas.get(2) + " veces");
        System.out.println("REGLA 3 (E -> Id): " + contadorReglas.get(3) + " veces");
        System.out.println("REGLA 4 (Op -> +): " + contadorReglas.get(4) + " veces");
        System.out.println("REGLA 5 (Op -> -): " + contadorReglas.get(5) + " veces");
        System.out.println("REGLA 6 (Op -> *): " + contadorReglas.get(6) + " veces");
        System.out.println("REGLA 7 (Op -> /): " + contadorReglas.get(7) + " veces");
        System.out.println("REGLA 8 (Op -> <=): " + contadorReglas.get(8) + " veces");
        System.out.println("REGLA 9 (Op -> >=): " + contadorReglas.get(9) + " veces");
        System.out.println("REGLA 10 (Op -> ==): " + contadorReglas.get(10) + " veces");
        System.out.println("REGLA 11 (Op -> >): " + contadorReglas.get(11) + " veces");
        System.out.println("REGLA 12 (Op -> <): " + contadorReglas.get(12) + " veces");
        System.out.println("REGLA 13 (Op -> %): " + contadorReglas.get(13) + " veces");
        
        System.out.println("\n" + (aceptada ? "✓ CADENA ACEPTADA" : "✗ CADENA RECHAZADA") + "\n");
    }
    
    
    private boolean matchPalabra(String palabra) {
        for (int i = 0; i < palabra.length(); i++) {
            if (caracterActual() != palabra.charAt(i)) {
                return false;
            }
            avanzar();
        }
        saltarEspacios();
        return true;
    }
    
    private String leerIdentificador() {
        StringBuilder sb = new StringBuilder();
        while (esLetra(caracterActual()) || Character.isDigit(caracterActual())) {
            sb.append(caracterActual());
            avanzar();
        }
        saltarEspacios();
        return sb.toString();
    }
    
    private String verIdentificador() {
        int pos = posicion;
        StringBuilder sb = new StringBuilder();
        while (pos < entrada.length() && (esLetra(entrada.charAt(pos)) || Character.isDigit(entrada.charAt(pos)))) {
            sb.append(entrada.charAt(pos));
            pos++;
        }
        return sb.toString();
    }
    
    private String leerOperador() {
        char actual = caracterActual();
        char siguiente = siguienteCaracter();
        
        // Operadores de dos caracteres
        if ((actual == '<' && siguiente == '=') || 
            (actual == '>' && siguiente == '=') || 
            (actual == '=' && siguiente == '=')) {
            String op = "" + actual + siguiente;
            avanzar();
            avanzar();
            saltarEspacios();
            return op;
        }
        
        // Operadores de un caracter
        if (actual == '+' || actual == '-' || actual == '*' || 
            actual == '/' || actual == '%' || actual == '>' || actual == '<') {
            String op = "" + actual;
            avanzar();
            saltarEspacios();
            return op;
        }
        
        return "";
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Ingrese la instruccion if: ");
        String entrada = scanner.nextLine();
        
        AnalizadorSintactico analizador = new AnalizadorSintactico(entrada);
        analizador.analizar();
        
        scanner.close();
    }
}
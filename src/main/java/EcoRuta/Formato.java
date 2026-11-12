package EcoRuta;

/* Formato de valores en pesos con punto como separador de miles: $24.200
Esta clase es opcional, solo para que quede más bonito */
final class Formato {

    private Formato() {
    }

    static String pesos(long valor) {
        String digitos = String.valueOf(valor);
        String resultado = "";
        int contador = 0;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            if (contador == 3) {
                resultado = "." + resultado;
                contador = 0;
            }
            resultado = digitos.charAt(i) + resultado;
            contador++;
        }
        return "$" + resultado;
    }
}
package EcoRuta;

// Validaciones comunes para no repetir código en cada clase
final class Validaciones {

    private Validaciones() {
    }

    /** Exige un texto con contenido y lo devuelve sin espacios en los extremos.
     * @param valor texto recibido
     * @param campo nombre del campo, solo para el mensaje de error */
    static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatoInvalidoException("El campo (" + campo + ") es obligatorio y no puede estar en blanco.");
        }
        return valor.trim();
    }
}
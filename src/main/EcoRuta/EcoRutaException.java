/* Base de todas las excepciones
Es no verificada (RuntimeException): las operaciones que se rechazan lanzan
una subclase de esta y dejan la información registrada sin cambios. */
public class EcoRutaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EcoRutaException(String mensaje) {
        super(mensaje);
    }
}
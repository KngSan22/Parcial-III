// Un dato no cumple su regla: texto vacío, peso no positivo, equipos negativos, valor nulo, etc
public class DatoInvalidoException extends EcoRutaException {

    private static final long serialVersionUID = 1L;

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
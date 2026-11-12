package EcoRuta;

// Se intenta registrar algo como un código o una identificación que ya existe (entrega, lote o aportante)
public class DuplicadoException extends EcoRutaException {

    private static final long serialVersionUID = 1L;

    public DuplicadoException(String mensaje) {
        super(mensaje);
    }
}
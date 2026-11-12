package EcoRuta;

// Se busca una entrega o un aportante que no existe
public class NoEncontradoException extends EcoRutaException {

    private static final long serialVersionUID = 1L;

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
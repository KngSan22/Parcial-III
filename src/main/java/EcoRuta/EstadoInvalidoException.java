package EcoRuta;

/* La operación no es posible en el estado actual de la entrega:
aceptar una entrega vacía o ya aceptada, agregar lotes a una entrega aceptada
o consultar el comprobante de una entrega que aún no se acepta */
public class EstadoInvalidoException extends EcoRutaException {

    private static final long serialVersionUID = 1L;

    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
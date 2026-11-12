package EcoRuta;

public record Comprobante(String codigoEntrega, String identificacionAportante, int pesoTotal, long valorTotal) {

    public Comprobante {
        codigoEntrega = Validaciones.texto(codigoEntrega, "código de la entrega");
        identificacionAportante = Validaciones.texto(identificacionAportante, "identificación del aportante");
        if (pesoTotal <= 0) {
            throw new DatoInvalidoException("El peso total del comprobante debe ser positivo.");
        }
        if (valorTotal < 0) {
            throw new DatoInvalidoException("El valor total del comprobante no puede ser negativo.");
        }
    }

    public String resumen() {
        return "Comprobante de la entrega " + codigoEntrega
                + "\nAportante: " + identificacionAportante
                + "\nPeso total: " + pesoTotal + " kg"
                + "\nValor total reconocido: " + Formato.pesos(valorTotal);
    }
}
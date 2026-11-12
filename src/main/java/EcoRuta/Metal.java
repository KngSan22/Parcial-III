package EcoRuta;

// Lote de metal: $2.200 por kg de aluminio o $900 por kg de acero, genera etiqueta
public class Metal extends Lote implements Etiquetable {

    private final TipoMetal tipo;

    public Metal(String codigo, String descripcion, int pesoKg, TipoMetal tipo) {
        super(codigo, descripcion, pesoKg);
        if (tipo == null) {
            throw new DatoInvalidoException("Debe indicar si el metal es ALUMINIO o ACERO.");
        }
        this.tipo = tipo;
    }

    public TipoMetal getTipo() {
        return tipo;
    }

    @Override
    public long calcularValor() {
        return getPesoKg() * tipo.getPrecioPorKg();
    }

    @Override
    public String generarEtiqueta() {
        return "METAL | código: " + getCodigo() + " | tipo: " + tipo;
    }
}
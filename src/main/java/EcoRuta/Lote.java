package EcoRuta;

/* Clase abstracta: no existe un lote "genérico" siempre es de papel y cartón, de metal o de residuos electrónicos,
cada tipo calcula su valor con su propia regla */
public abstract class Lote {

    private final String codigo;
    private final String descripcion;
    private final int pesoKg;

    protected Lote(String codigo, String descripcion, int pesoKg) {
        this.codigo = Validaciones.texto(codigo, "código del lote");
        this.descripcion = Validaciones.texto(descripcion, "descripción del lote");
        if (pesoKg <= 0) {
            throw new DatoInvalidoException("El peso del lote debe ser un entero positivo en kg; se recibió " + pesoKg + ".");
        }
        this.pesoKg = pesoKg;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getPesoKg() {
        return pesoKg;
    }

    /** @return valor en pesos que EcoRuta reconoce al aportante por este lote */
    public abstract long calcularValor();

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + codigo + " (" + pesoKg + " kg)";
    }
}
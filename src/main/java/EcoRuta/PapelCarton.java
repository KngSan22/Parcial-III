package EcoRuta;

// Lote de papel y cartón: $500 por kg, más $100 por kg si fue clasificado antes de entregarse
public class PapelCarton extends Lote {

    public static final long PRECIO_POR_KG = 500L;
    public static final long BONO_CLASIFICADO_POR_KG = 100L;

    private final boolean clasificado;

    public PapelCarton(String codigo, String descripcion, int pesoKg, boolean clasificado) {
        super(codigo, descripcion, pesoKg);
        this.clasificado = clasificado;
    }

    @Override
    public long calcularValor() {
        long precioPorKg = PRECIO_POR_KG + (clasificado ? BONO_CLASIFICADO_POR_KG : 0L);
        return getPesoKg() * precioPorKg;
    }
}
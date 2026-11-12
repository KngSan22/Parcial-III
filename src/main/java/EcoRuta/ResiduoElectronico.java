package EcoRuta;

// Lote de residuos electrónicos: $1.400 por kg, más $800 por cada equipo completo, genera etiqueta
public class ResiduoElectronico extends Lote implements Etiquetable {

    public static final long PRECIO_POR_KG = 1_400L;
    public static final long BONO_POR_EQUIPO = 800L;

    private final int equiposCompletos;

    public ResiduoElectronico(String codigo, String descripcion, int pesoKg, int equiposCompletos) {
        super(codigo, descripcion, pesoKg);
        if (equiposCompletos < 0) {
            throw new DatoInvalidoException("Los equipos completos deben ser un entero mayor o igual a cero; se recibió " + equiposCompletos + ".");
        }
        this.equiposCompletos = equiposCompletos;
    }

    @Override
    public long calcularValor() {
        return getPesoKg() * PRECIO_POR_KG + equiposCompletos * BONO_POR_EQUIPO;
    }

    @Override
    public String generarEtiqueta() {
        return "RESIDUO ELECTRÓNICO | código: " + getCodigo() + " | equipos completos: " + equiposCompletos;
    }
}
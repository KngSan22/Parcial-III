package EcoRuta;

// Tipos de metal que recibe EcoRuta, con el valor reconocido por kilogramo
public enum TipoMetal {
    ALUMINIO(2_200L),
    ACERO(900L);

    private final long precioPorKg;

    TipoMetal(long precioPorKg) {
        this.precioPorKg = precioPorKg;
    }

    public long getPrecioPorKg() {
        return precioPorKg;
    }
}
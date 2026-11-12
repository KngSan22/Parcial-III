package EcoRuta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/* Entrega de materiales de un aportante, nace ABIERTA y sin lotes, mientras esté abierta admite lotes
Pasa a ACEPTADA una sola vez, al generar su comprobante y desde entonces queda cerrada */
public class Entrega {

    private final String codigo;
    private final LocalDate fecha;
    private final Aportante aportante;
    private final List<Lote> lotes = new ArrayList<>();
    private EstadoEntrega estado = EstadoEntrega.ABIERTA;

    public Entrega(String codigo, LocalDate fecha, Aportante aportante) {
        this.codigo = Validaciones.texto(codigo, "código de la entrega");
        if (fecha == null) {
            throw new DatoInvalidoException("La fecha de la entrega es obligatoria.");
        }
        if (aportante == null) {
            throw new DatoInvalidoException("La entrega necesita un aportante.");
        }
        this.fecha = fecha;
        this.aportante = aportante;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Aportante getAportante() {
        return aportante;
    }

    public EstadoEntrega getEstado() {
        return estado;
    }

    /** @return copia de la lista de lotes: modificarla no altera la entrega */
    public List<Lote> getLotes() {
        return new ArrayList<>(lotes);
    }

    public int cantidadLotes() {
        return lotes.size();
    }

    // Agrega un lote a la entrega
    public void agregarLote(Lote lote) {
        if (lote == null) {
            throw new DatoInvalidoException("El lote no puede ser vacío.");
        }
        if (estado != EstadoEntrega.ABIERTA) {
            throw new EstadoInvalidoException("La entrega: " + codigo + "; está " + estado + " y no admite nuevos lotes.");
        }
        for (Lote existente : lotes) {
            if (existente.getCodigo().equals(lote.getCodigo())) {
                throw new DuplicadoException("Ya existe un lote con código: " + lote.getCodigo() + "; en la entrega: " + codigo + ".");
            }
        }
        lotes.add(lote);
    }

    /** @return suma de los kilogramos de todos los lotes */
    public int calcularPesoTotal() {
        int total = 0;
        for (Lote lote : lotes) {
            total += lote.getPesoKg();
        }
        return total;
    }

    /* Suma el valor de todos los lotes: cada lote ejecuta
    su propio calcularValor() sin que la entrega sepa de qué tipo es */
    public long calcularValorTotal() {
        long total = 0;
        for (Lote lote : lotes) {
            total += lote.calcularValor();
        }
        return total;
    }

    // Para obtener las etiquetas
    public List<String> obtenerEtiquetas() {
        List<String> etiquetas = new ArrayList<>();
        for (Lote lote : lotes) {
            if (lote instanceof Etiquetable etiquetable) {
                etiquetas.add(etiquetable.generarEtiqueta());
            }
        }
        return etiquetas;
    }

    /* Acepta la entrega: suma peso y valor, genera el comprobante y pasa a ACEPTADA
    siempre se calcula antes de cambiar el estado, así un rechazo no deja nada a medias. */
    public Comprobante aceptar() {
        if (estado != EstadoEntrega.ABIERTA) {
            throw new EstadoInvalidoException("La entrega: " + codigo + "; ya fue aceptada.");
        }
        if (lotes.isEmpty()) {
            throw new EstadoInvalidoException("La entrega: " + codigo + "; está vacía: necesita al menos un lote para aceptarse.");
        }
        Comprobante comprobante = new Comprobante(codigo, aportante.identificacion(), calcularPesoTotal(), calcularValorTotal());
        estado = EstadoEntrega.ACEPTADA;
        return comprobante;
    }

    @Override
    public String toString() {
        return "Entrega " + codigo + " [" + estado + ", " + lotes.size() + " lotes, aportante " + aportante.identificacion() + "]";
    }
}
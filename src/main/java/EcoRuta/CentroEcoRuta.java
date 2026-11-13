package EcoRuta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/* Guarda en memoria los aportantes, las entregas y los comprobantes y
es el único punto de entrada de la lógica: el menú solo llama a sus métodos */
public class CentroEcoRuta {

    private final List<Aportante> aportantes = new ArrayList<>();
    private final List<Entrega> entregas = new ArrayList<>();
    private final List<Comprobante> comprobantes = new ArrayList<>();

    public void registrarAportante(Aportante aportante) {
        if (aportante == null) {
            throw new DatoInvalidoException("El aportante no puede estar vacío.");
        }
        if (buscarAportante(aportante.identificacion()) != null) {
            throw new DuplicadoException("Ya existe un aportante con identificación: " + aportante.identificacion() + ".");
        }
        aportantes.add(aportante);
    }

    // Crea una entrega ABIERTA y sin lotes para un aportante registrado
    public Entrega crearEntrega(String codigo, LocalDate fecha, String identificacionAportante) {
        String codigoLimpio = Validaciones.texto(codigo, "código de la entrega");
        if (fecha == null) {
            throw new DatoInvalidoException("La fecha de la entrega es obligatoria.");
        }
        String idLimpio = Validaciones.texto(identificacionAportante, "identificación del aportante");
        if (buscarEntrega(codigoLimpio) != null) {
            throw new DuplicadoException("Ya existe una entrega con código: " + codigoLimpio + ".");
        }
        Aportante aportante = buscarAportante(idLimpio);
        if (aportante == null) {
            throw new NoEncontradoException("No existe un aportante registrado con identificación: " + idLimpio + ".");
        }
        Entrega entrega = new Entrega(codigoLimpio, fecha, aportante);
        entregas.add(entrega);
        return entrega;
    }

    public void agregarLote(String codigoEntrega, Lote lote) {
        consultarEntrega(codigoEntrega).agregarLote(lote);
    }

    // Acepta una entrega y guarda su comprobante
    public Comprobante aceptarEntrega(String codigoEntrega) {
        Entrega entrega = consultarEntrega(codigoEntrega);
        Comprobante comprobante = entrega.aceptar();
        comprobantes.add(comprobante);
        return comprobante;
    }

    // Consulta el comprobante de una entrega por su código
    public Comprobante consultarComprobante(String codigoEntrega) {
        Entrega entrega = consultarEntrega(codigoEntrega);
        Comprobante comprobante = buscarComprobante(entrega.getCodigo());
        if (comprobante == null) {
            throw new EstadoInvalidoException("La entrega " + entrega.getCodigo() + " aún no ha sido aceptada: no tiene comprobante.");
        }
        return comprobante;
    }

    // Consulta las etiquetas de una entrega aceptada
    public List<String> consultarEtiquetas(String codigoEntrega) {
        Entrega entrega = consultarEntrega(codigoEntrega);
        if (entrega.getEstado() != EstadoEntrega.ACEPTADA) {
            throw new EstadoInvalidoException("La entrega " + entrega.getCodigo() + " aún no ha sido aceptada: sus etiquetas no están disponibles.");
        }
        return entrega.obtenerEtiquetas();
    }

    /** Suma el valor de los comprobantes de un aportante, solo existen comprobantes de entregas aceptadas.
     * @return el valor acumulado, o 0 si no hay comprobantes para esa identificación */
    public long valorAcumulado(String identificacionAportante) {
        String idLimpio = Validaciones.texto(identificacionAportante, "identificación del aportante");
        long total = 0;
        for (Comprobante comprobante : comprobantes) {
            if (comprobante.identificacionAportante().equals(idLimpio)) {
                total += comprobante.valorTotal();
            }
        }
        return total;
    }

    /** Consulta la entrega
     *  @return la entrega con ese código */
    public Entrega consultarEntrega(String codigoEntrega) {
        String codigoLimpio = Validaciones.texto(codigoEntrega, "código de la entrega");
        Entrega entrega = buscarEntrega(codigoLimpio);
        if (entrega == null) {
            throw new NoEncontradoException("No existe una entrega con código: " + codigoLimpio + ".");
        }
        return entrega;
    }

    /** @return true si hay un comprobante para ese código de entrega (no lanza excepciones) */
    public boolean existeComprobante(String codigoEntrega) {
        return codigoEntrega != null && buscarComprobante(codigoEntrega.trim()) != null;
    }

    /** @return true si existe una entrega con ese código (no lanza excepciones) */
    public boolean existeEntrega(String codigoEntrega) {
        return codigoEntrega != null && buscarEntrega(codigoEntrega.trim()) != null;
    }

    public int cantidadEntregas() {
        return entregas.size();
    }

    /** @return copia de la lista de entregas, en el orden en que se crearon */
    public List<Entrega> listarEntregas() {
        return new ArrayList<>(entregas);
    }

    /** @return copia de la lista de aportantes, en el orden en que se registraron */
    public List<Aportante> listarAportantes() {
        return new ArrayList<>(aportantes);
    }

    // -------------- Búsquedas --------------

    private Aportante buscarAportante(String identificacion) {
        for (Aportante aportante : aportantes) {
            if (aportante.identificacion().equals(identificacion)) {
                return aportante;
            }
        }
        return null;
    }

    private Entrega buscarEntrega(String codigo) {
        for (Entrega entrega : entregas) {
            if (entrega.getCodigo().equals(codigo)) {
                return entrega;
            }
        }
        return null;
    }

    private Comprobante buscarComprobante(String codigoEntrega) {
        for (Comprobante comprobante : comprobantes) {
            if (comprobante.codigoEntrega().equals(codigoEntrega)) {
                return comprobante;
            }
        }
        return null;
    }
}
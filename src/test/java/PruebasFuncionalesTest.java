import EcoRuta.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas funcionales

class PruebasFuncionalesTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 10);

    // Centro con un aportante registrado
    private static CentroEcoRuta nuevoCentro() {
        CentroEcoRuta centro = new CentroEcoRuta();
        centro.registrarAportante(new Aportante("1001", "Ana Gómez", "ana.gomez@correo.com"));
        return centro;
    }

    // Crea la entrega y le agrega P01 (papel, 10 kg clasificado), M01 (aluminio, 5 kg) y R01 (electrónico, 4 kg, 2 equipos)
    private static Entrega nuevaEntregaConTresLotes(CentroEcoRuta centro, String codigoEntrega) {
        Entrega entrega = centro.crearEntrega(codigoEntrega, FECHA, "1001");
        centro.agregarLote(codigoEntrega, new PapelCarton("P01", "Papel de oficina clasificado", 10, true));
        centro.agregarLote(codigoEntrega, new Metal("M01", "Latas de aluminio", 5, TipoMetal.ALUMINIO));
        centro.agregarLote(codigoEntrega, new ResiduoElectronico("R01", "Computadores en desuso", 4, 2));
        return entrega;
    }

    // PRUEBA 1

    @Nested
    @DisplayName("PRUEBA 1. Registro de una entrega")
    class RegistroDeEntrega {

        private CentroEcoRuta centro;
        private Entrega e01;

        @BeforeEach
        void preparar() {
            centro = nuevoCentro();
            e01 = centro.crearEntrega("E01", FECHA, "1001");
        }

        @Test
        @DisplayName("1.1 Crear E01 con el aportante 1001: ABIERTA, 0 lotes, sin comprobante")
        void creaE01() {
            assertEquals(EstadoEntrega.ABIERTA, e01.getEstado());
            assertEquals(0, e01.cantidadLotes());
            assertFalse(centro.existeComprobante("E01"));
        }

        @Test
        @DisplayName("1.2 Crear otra entrega con el código E01 se rechaza con DuplicadoException")
        void rechazaCodigoRepetido() {
            assertThrows(DuplicadoException.class,
                    () -> centro.crearEntrega("E01", FECHA, "1001"));
        }

        @Test
        @DisplayName("1.3 Crear E99 con el aportante desconocido 9999 se rechaza con NoEncontradoException")
        void rechazaAportanteDesconocido() {
            assertThrows(NoEncontradoException.class,
                    () -> centro.crearEntrega("E99", FECHA, "9999"));
        }

        @Test
        @DisplayName("1.4 Tras los dos rechazos, E01 se conserva: ABIERTA, 0 lotes, sin comprobante")
        void conservaE01TrasLosRechazos() {
            assertThrows(DuplicadoException.class, () -> centro.crearEntrega("E01", FECHA, "1001"));
            assertThrows(NoEncontradoException.class, () -> centro.crearEntrega("E99", FECHA, "9999"));

            assertTrue(centro.existeEntrega("E01"));
            assertSame(e01, centro.consultarEntrega("E01"));
            assertEquals(EstadoEntrega.ABIERTA, e01.getEstado());
            assertEquals(0, e01.cantidadLotes());
            assertFalse(centro.existeComprobante("E01"));
        }
    }

    // PRUEBA 2

    @Nested
    @DisplayName("PRUEBA 2. Validación de lotes")
    class ValidacionDeLotes {

        private CentroEcoRuta centro;

        @BeforeEach
        void preparar() {
            centro = nuevoCentro();
            centro.crearEntrega("E02", FECHA, "1001");
        }

        private void agregarL01() {
            centro.agregarLote("E02", new PapelCarton("L01", "Cajas de cartón", 5, false));
        }

        @Test
        @DisplayName("2.1 Agregar el lote L01 de 5 kg a la entrega abierta E02 deja 1 lote")
        void agregaL01() {
            agregarL01();

            assertEquals(1, centro.consultarEntrega("E02").cantidadLotes());
        }

        @Test
        @DisplayName("2.2 Agregar otro lote con código L01 y 3 kg se rechaza con DuplicadoException")
        void rechazaCodigoDeLoteRepetido() {
            agregarL01();

            assertThrows(DuplicadoException.class, () ->
                    centro.agregarLote("E02", new PapelCarton("L01", "Otro lote", 3, false)));
        }

        @Test
        @DisplayName("2.3 Agregar el lote L02 con 0 kg se rechaza con DatoInvalidoException")
        void rechazaLoteDeCeroKg() {
            agregarL01();

            assertThrows(DatoInvalidoException.class, () ->
                    centro.agregarLote("E02", new PapelCarton("L02", "Sin peso", 0, false)));
        }

        @Test
        @DisplayName("2.4 Tras los dos rechazos, E02 conserva únicamente L01 de 5 kg")
        void conservaSoloElPrimerLote() {
            agregarL01();
            assertThrows(DuplicadoException.class, () ->
                    centro.agregarLote("E02", new PapelCarton("L01", "Otro lote", 3, false)));
            assertThrows(DatoInvalidoException.class, () ->
                    centro.agregarLote("E02", new PapelCarton("L02", "Sin peso", 0, false)));

            List<Lote> lotes = centro.consultarEntrega("E02").getLotes();
            assertEquals(1, lotes.size());
            assertEquals("L01", lotes.get(0).getCodigo());
            assertEquals(5, lotes.get(0).getPesoKg());
        }
    }

    // PRUEBA 3

    @Nested
    @DisplayName("PRUEBA 3. Rechazo de una entrega vacía")
    class EntregaVacia {

        private CentroEcoRuta centro;

        @BeforeEach
        void preparar() {
            centro = nuevoCentro();
            centro.crearEntrega("E03", FECHA, "1001");
        }

        @Test
        @DisplayName("3.1 Aceptar E03, que no tiene lotes, se rechaza con EstadoInvalidoException")
        void rechazaAceptarEntregaVacia() {
            assertThrows(EstadoInvalidoException.class, () -> centro.aceptarEntrega("E03"));
        }

        @Test
        @DisplayName("3.2 Tras el rechazo, E03 sigue ABIERTA, sin lotes y sin comprobante")
        void entregaQuedaAbierta() {
            assertThrows(EstadoInvalidoException.class, () -> centro.aceptarEntrega("E03"));

            Entrega e03 = centro.consultarEntrega("E03");
            assertEquals(EstadoEntrega.ABIERTA, e03.getEstado());
            assertEquals(0, e03.cantidadLotes());
            assertFalse(centro.existeComprobante("E03"));
        }
    }

    // PRUEBA 4

    @Nested
    @DisplayName("PRUEBA 4. Aceptación y cálculo con los tres tipos de lote")
    class AceptacionYCalculo {

        private CentroEcoRuta centro;
        private Entrega e04;

        @BeforeEach
        void preparar() {
            centro = nuevoCentro();
            e04 = nuevaEntregaConTresLotes(centro, "E04");
        }

        @Test
        @DisplayName("4.1 Lote de papel y cartón (10 kg clasificado) vale $6.000")
        void valorDelPapel() {
            assertEquals(6_000L, e04.getLotes().get(0).calcularValor());
        }

        @Test
        @DisplayName("4.2 Lote de metal (5 kg de aluminio) vale $11.000")
        void valorDelMetal() {
            assertEquals(11_000L, e04.getLotes().get(1).calcularValor());
        }

        @Test
        @DisplayName("4.3 Lote electrónico (4 kg, 2 equipos completos) vale $7.200")
        void valorDelElectronico() {
            assertEquals(7_200L, e04.getLotes().get(2).calcularValor());
        }

        @Test
        @DisplayName("4.4 Al aceptar E04, el estado es ACEPTADA")
        void estadoAceptada() {
            centro.aceptarEntrega("E04");

            assertEquals(EstadoEntrega.ACEPTADA, e04.getEstado());
        }

        @Test
        @DisplayName("4.5 El comprobante de E04 muestra un peso total de 19 kg")
        void pesoTotalDelComprobante() {
            Comprobante comprobante = centro.aceptarEntrega("E04");

            assertEquals(19, comprobante.pesoTotal());
        }

        @Test
        @DisplayName("4.6 El comprobante de E04 muestra un valor total de $24.200")
        void valorTotalDelComprobante() {
            Comprobante comprobante = centro.aceptarEntrega("E04");

            assertEquals(24_200L, comprobante.valorTotal());
        }
    }

    // PRUEBA 5

    @Nested
    @DisplayName("PRUEBA 5. Consulta de etiquetas")
    class ConsultaDeEtiquetas {

        private List<String> etiquetas;

        @BeforeEach
        void prepararEntregaAceptada() {
            CentroEcoRuta centro = nuevoCentro();
            nuevaEntregaConTresLotes(centro, "E04");
            centro.aceptarEntrega("E04");
            etiquetas = centro.consultarEtiquetas("E04");
        }

        private String buscar(String codigo) {
            return etiquetas.stream().filter(e -> e.contains(codigo)).findFirst().orElse(null);
        }

        @Test
        @DisplayName("5.1 E04 tiene exactamente 2 etiquetas")
        void dosEtiquetas() {
            assertEquals(2, etiquetas.size());
        }

        @Test
        @DisplayName("5.2 La etiqueta del metal incluye el código M01 y el tipo ALUMINIO")
        void etiquetaDelMetal() {
            String metal = buscar("M01");

            assertNotNull(metal, "No se encontró la etiqueta de M01");
            assertTrue(metal.contains("ALUMINIO"), "La etiqueta de M01 no incluye ALUMINIO: " + metal);
        }

        @Test
        @DisplayName("5.3 La etiqueta electrónica incluye el código R01 y 'equipos completos: 2'")
        void etiquetaDelElectronico() {
            String electronica = buscar("R01");

            assertNotNull(electronica, "No se encontró la etiqueta de R01");
            assertTrue(electronica.contains("equipos completos: 2"),
                    "La etiqueta de R01 no incluye los equipos completos: " + electronica);
        }
    }
}
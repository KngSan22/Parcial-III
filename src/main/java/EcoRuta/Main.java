package EcoRuta;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/* Solo pide datos y muestra resultados, toda la lógica y todas las reglas viven en CentroEcoRuta y Entrega,
los rechazos llegan como EcoRutaException y se muestran al usuario sin cerrar la aplicación */

class Main {

    private static final String TITULO = "EcoRuta";

    // Se dispara cuando el usuario cancela o cierra un cuadro de diálogo, vuelve al menú sin hacer nada.
    private static final class Cancelado extends RuntimeException {
        private static final long serialVersionUID = 1L;

        Cancelado() {
            super(null, null, false, false);
        }
    }

    static void main(String[] args) {
        CentroEcoRuta centro = new CentroEcoRuta();
        cargarAportantesIniciales(centro);
        ejecutarMenu(centro);
    }

    private static void cargarAportantesIniciales(CentroEcoRuta centro) {
        centro.registrarAportante(new Aportante("001", "Ana Gómez", "ana.gomez@gmail.com"));
        centro.registrarAportante(new Aportante("002", "Tienda de Jorge", "tiendajorge@hotmail.com"));
        centro.registrarAportante(new Aportante("003", "Colegio San José", "rectoria@sanjose.edu.co"));
    }

    // ------------------------ menú ------------------------

    private static void ejecutarMenu(CentroEcoRuta centro) {
        String[] opciones = {
                "Registrar aportante\n",
                "Consultar aportantes\n",
                "Crear entrega\n",
                "Agregar lote a una entrega\n",
                "Aceptar entrega\n",
                "Consultar entregas\n",
                "Consultar una entrega\n",
                "Consultar comprobante\n",
                "Consultar etiquetas\n",
                "Consultar valor acumulado de un aportante\n",
                "Salir\n"
        };
        while (true) {
            try {
                int eleccion = elegir("Menú principal", "Seleccione una opción:", opciones);
                switch (eleccion) {
                    case 0 -> registrarAportante(centro);
                    case 1 -> verAportantes(centro);
                    case 2 -> crearEntrega(centro);
                    case 3 -> agregarLote(centro);
                    case 4 -> aceptarEntrega(centro);
                    case 5 -> consultarEntregas(centro);
                    case 6 -> consultarUnaEntrega(centro);
                    case 7 -> consultarComprobante(centro);
                    case 8 -> consultarEtiquetas(centro);
                    case 9 -> consultarAcumulado(centro);
                    default -> {
                        return;
                    }
                }
            } catch (Cancelado c) {
                // el usuario canceló un cuadro, se vuelve al menú
            } catch (EcoRutaException e) {
                mostrar("Operación rechazada", e.getMessage(), JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException e) {
                mostrar("Error inesperado",
                        "Ocurrió un error no previsto: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void registrarAportante(CentroEcoRuta centro) {
        String identificacion = leerTexto("Identificación del aportante:");
        String nombre = leerTexto("Nombre del aportante:");
        String correo = leerTexto("Correo del aportante:");
        // el aportante válida los datos y el centro rechaza una identificación repetida
        Aportante aportante = new Aportante(identificacion, nombre, correo);
        centro.registrarAportante(aportante);
        mostrar("Aportante registrado", "Aportante " + aportante.identificacion() + " - " + aportante.nombre() + " registrado correctamente.", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void crearEntrega(CentroEcoRuta centro) {
        String codigo = leerTexto("Código de la entrega:");
        LocalDate fecha = leerFecha("Fecha de la entrega (aaaa-mm-dd):", LocalDate.now());
        String idAportante = leerTexto("Identificación del aportante:");
        Entrega entrega = centro.crearEntrega(codigo, fecha, idAportante);
        mostrar("Entrega creada", "Entrega " + entrega.getCodigo() + " registrada en estado " + entrega.getEstado() + ", sin lotes.", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void agregarLote(CentroEcoRuta centro) {
        String codigoEntrega = leerTexto("Código de la entrega:");
        // se verifica antes de pedir datos que la entrega exista y siga abierta
        Entrega entrega = centro.consultarEntrega(codigoEntrega);
        if (entrega.getEstado() != EstadoEntrega.ABIERTA) {
            throw new EstadoInvalidoException("La entrega " + entrega.getCodigo() + " está " + entrega.getEstado() + " y no admite nuevos lotes.");
        }
        int tipo = elegir("Tipo de lote", "¿Qué tipo de lote va a agregar?",
                new String[] {"Papel y cartón", "Metal", "Residuos electrónicos"});
        String codigoLote = leerTexto("Código del lote:");
        String descripcion = leerTexto("Descripción del lote:");
        int peso = leerPeso("Peso en kilogramos:");

        Lote lote;
        switch (tipo) {
            case 0 -> lote = new PapelCarton(codigoLote, descripcion, peso, leerSiNo("¿El material fue clasificado antes de entregarse?"));
            case 1 -> {
                TipoMetal[] tipos = TipoMetal.values();
                String[] nombres = new String[tipos.length];
                for (int i = 0; i < tipos.length; i++) {
                    nombres[i] = tipos[i].name();
                }
                lote = new Metal(codigoLote, descripcion, peso, tipos[elegir("Tipo de metal", "¿Qué metal es?", nombres)]);
            }
            default -> lote = new ResiduoElectronico(codigoLote, descripcion, peso, leerCantidad("Equipos completos en el lote:"));
        }
        centro.agregarLote(codigoEntrega, lote);
        mostrar("Lote agregado", lote + "\nValor reconocido por este lote: " + Formato.pesos(lote.calcularValor()) + "\nLa entrega "
                + entrega.getCodigo() + " tiene ahora " + entrega.cantidadLotes() + " lote(s).", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void aceptarEntrega(CentroEcoRuta centro) {
        Comprobante comprobante = centro.aceptarEntrega(leerTexto("Código de la entrega a aceptar:"));
        mostrar("Entrega aceptada", comprobante.resumen(), JOptionPane.INFORMATION_MESSAGE);
    }

    private static void consultarEntregas(CentroEcoRuta centro) {
        List<Entrega> entregas = centro.listarEntregas();
        if (entregas.isEmpty()) {
            mostrar("Entregas", "Todavía no hay entregas registradas.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String texto = "";
        for (Entrega e : entregas) {
            texto += e.getCodigo()
                    + "  -  " + e.getFecha()
                    + "  -  aportante " + e.getAportante().identificacion()
                    + "  -  " + e.getEstado()
                    + "  -  " + e.cantidadLotes() + " lote(s)"
                    + "\n";
        }
        mostrarLista("Entregas registradas (" + entregas.size() + ")", texto.trim());
    }

    private static void consultarUnaEntrega(CentroEcoRuta centro) {
        Entrega entrega = centro.consultarEntrega(leerTexto("Código de la entrega:"));
        String texto = "Entrega: " + entrega.getCodigo() + "\n"
                + "Fecha: " + entrega.getFecha() + "\n"
                + "Aportante: " + entrega.getAportante().identificacion()
                + " - " + entrega.getAportante().nombre() + "\n"
                + "Estado: " + entrega.getEstado() + "\n"
                + "Lotes (" + entrega.cantidadLotes() + "):\n";
        if (entrega.cantidadLotes() == 0) {
            texto += "   (sin lotes)\n";
        }
        for (Lote lote : entrega.getLotes()) {
            texto += "   " + lote
                    + " - " + lote.getDescripcion()
                    + " - " + Formato.pesos(lote.calcularValor()) + "\n";
        }
        texto += "Peso total: " + entrega.calcularPesoTotal() + " kg\n"
                + "Valor total de los lotes: " + Formato.pesos(entrega.calcularValorTotal());
        mostrarLista("Entrega " + entrega.getCodigo(), texto);
    }

    private static void consultarComprobante(CentroEcoRuta centro) {
        Comprobante comprobante = centro.consultarComprobante(leerTexto("Código de la entrega:"));
        mostrar("Comprobante", comprobante.resumen(), JOptionPane.INFORMATION_MESSAGE);
    }

    private static void consultarEtiquetas(CentroEcoRuta centro) {
        String codigo = leerTexto("Código de la entrega:");
        List<String> etiquetas = centro.consultarEtiquetas(codigo);
        if (etiquetas.isEmpty()) {
            mostrar("Etiquetas", "La entrega " + codigo + " no tiene lotes que generen etiqueta.",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String texto = "";
        for (String etiqueta : etiquetas) {
            texto += etiqueta + "\n";
        }
        mostrarLista("Etiquetas de la entrega " + codigo, texto.trim());
    }

    private static void consultarAcumulado(CentroEcoRuta centro) {
        String id = leerTexto("Identificación del aportante:");
        mostrar("Valor acumulado", "Valor acumulado del aportante " + id + ": " + Formato.pesos(centro.valorAcumulado(id)), JOptionPane.INFORMATION_MESSAGE);
    }

    private static void verAportantes(CentroEcoRuta centro) {
        String texto = "";
        for (Aportante a : centro.listarAportantes()) {
            texto += a.identificacion() + "  -  " + a.nombre() + "  -  " + a.correo() + "\n";
        }
        mostrarLista("Aportantes registrados", texto.trim());
    }

    // ------------------------ entrada y salida ------------------------

    // Muestra un menú de botones, devuelve la posición elegida o lanza Cancelado si se cierra el cuadro

    private static int elegir(String titulo, String mensaje, String[] opciones) {

        JList<String> lista = new JList<>(opciones);
        lista.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lista.setSelectedIndex(0);
        lista.setVisibleRowCount(Math.min(opciones.length, 12)); // que se vean todas las opciones sin desplazarse

        int respuesta = JOptionPane.showConfirmDialog(null, new JScrollPane(lista), mensaje,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (respuesta != JOptionPane.OK_OPTION) {
            if (titulo.equals("Menú principal")) {
                return opciones.length - 1;
            }

            throw new Cancelado();
        }

        /* con Ctrl+clic se puede quitar la selección (Me paso por error y me di cuenta)
        sin opción elegida no se hace nada (y no se sale del menú) */
        if (lista.getSelectedIndex() == -1) {
            throw new Cancelado();
        }
        return lista.getSelectedIndex();
    }

    private static String leerTexto(String mensaje) {
        String respuesta = JOptionPane.showInputDialog(null, mensaje, TITULO, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == null) {
            throw new Cancelado();
        }
        return respuesta;
    }

    // Lee un entero, si el texto no es un número, avisa y vuelve a preguntar
    private static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                mostrar("Dato no válido", "«" + texto + "» no es un número entero. Intente de nuevo.", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Lee el peso para evitar errores
    private static int leerPeso(String mensaje) {
        int peso;

        while (true) {
            peso = leerEntero(mensaje);

            if (peso > 0) {
                return peso;
            }
            mostrar("Dato no válido", "El peso debe ser mayor que cero.", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static int leerCantidad(String mensaje) {
        int cantidad;

        while (true) {
            cantidad = leerEntero(mensaje);

            if (cantidad >= 0) {
                return cantidad;
            }
            mostrar("Dato no válido", "La cantidad no puede ser negativa.", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Lee una fecha en formato aaaa-mm-dd, si no es válida, avisa y vuelve a preguntar
    private static LocalDate leerFecha(String mensaje, LocalDate sugerida) {
        while (true) {
            String respuesta = (String) JOptionPane.showInputDialog(null, mensaje, TITULO,
                    JOptionPane.QUESTION_MESSAGE, null, null, sugerida.toString());
            if (respuesta == null) {
                throw new Cancelado();
            }
            try {
                return LocalDate.parse(respuesta.trim());
            } catch (DateTimeParseException e) {
                mostrar("Fecha no válida", "«" + respuesta + "» no es una fecha válida. Use el formato aaaa-mm-dd.",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static boolean leerSiNo(String mensaje) {
        int respuesta = JOptionPane.showConfirmDialog(null, mensaje, TITULO, JOptionPane.YES_NO_CANCEL_OPTION);
        if (respuesta == JOptionPane.CANCEL_OPTION || respuesta == JOptionPane.CLOSED_OPTION) {
            throw new Cancelado();
        }
        return respuesta == JOptionPane.YES_OPTION;
    }

    private static void mostrar(String titulo, String mensaje, int tipo) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, tipo);
    }

    // Muestra texto largo en un área con barra de desplazamiento para que la ventana no se salga de la pantalla
    private static void mostrarLista(String titulo, String texto) {
        JTextArea area = new JTextArea(texto, Math.min(12, texto.split("\n").length + 1), 52);
        area.setEditable(false);
        JOptionPane.showMessageDialog(null, new JScrollPane(area), titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}
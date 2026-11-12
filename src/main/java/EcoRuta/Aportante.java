package EcoRuta;

public record Aportante(String identificacion, String nombre, String correo) {

    public Aportante {

        identificacion = Validaciones.texto(identificacion, "identificación");
        for (int i = 0; i < identificacion.length(); i++) {
            char c = identificacion.charAt(i);
            if (c < '0' || c > '9') {
                throw new DatoInvalidoException("La identificación solo puede contener números.");
            }
        }

        nombre = Validaciones.texto(nombre, "nombre");

        correo = Validaciones.texto(correo, "correo");
        int arroba = correo.indexOf('@');
        int punto = correo.lastIndexOf('.');
        if (arroba <= 0 || arroba != correo.lastIndexOf('@') || punto <= arroba + 1 || punto == correo.length() - 1 || correo.contains(" ")) {
            throw new DatoInvalidoException("El correo: " + correo + "; no tiene un formato válido.");
        }
    }
}
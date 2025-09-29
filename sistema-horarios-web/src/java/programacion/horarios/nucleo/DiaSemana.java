package programacion.horarios.nucleo;

public enum DiaSemana {
    LUNES(1, "Lun"),
    MARTES(2, "Mar"),
    MIERCOLES(3, "Mié"),
    JUEVES(4, "Jue"),
    VIERNES(5, "Vie"),
    SABADO(6, "Sáb"),
    DOMINGO(7, "Dom");
    
    private final int codigo;
    private final String abreviacion;
    
    DiaSemana(int codigo, String abreviacion) {
        this.codigo = codigo;
        this.abreviacion = abreviacion;
    }
    
    public static DiaSemana desdeCodigo(int codigo) {
        for (DiaSemana dia : values()) {
            if (dia.codigo == codigo) return dia;
        }
        throw new IllegalArgumentException("Código de día inválido: " + codigo);
    }
    
    public int obtenerCodigo() { return codigo; }
    public String obtenerAbreviacion() { return abreviacion; }
}

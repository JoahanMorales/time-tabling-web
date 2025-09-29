package programacion.horarios.nucleo;

public final class FranjaHoraria {
    private final int horaInicio;
    private final int horaFin;
    
    public FranjaHoraria(int horaInicio, int horaFin) {
        validarHora(horaInicio, horaFin);
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }
    
    public FranjaHoraria(String rango) {
        String[] partes = rango.replace(" ", "").split("-");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato inválido: " + rango);
        }
        this.horaInicio = Integer.parseInt(partes[0]);
        this.horaFin = Integer.parseInt(partes[1]);
        validarHora(horaInicio, horaFin);
    }
    
    private void validarHora(int inicio, int fin) {
        if (inicio >= fin) {
            throw new IllegalArgumentException("Inicio >= Fin: " + inicio + " >= " + fin);
        }
        int horaI = inicio/100, minI = inicio%100, horaF = fin/100, minF = fin%100;
        if (horaI < 0 || horaI > 23 || horaF < 0 || horaF > 23 || 
            minI < 0 || minI > 59 || minF < 0 || minF > 59) {
            throw new IllegalArgumentException("Hora fuera de rango");
        }
    }
    
    public boolean seSuperpone(FranjaHoraria otra) {
        return this.horaInicio < otra.horaFin && otra.horaInicio < this.horaFin;
    }
    
    public int obtenerDuracionMinutos() {
        int minutoInicio = (horaInicio/100) * 60 + (horaInicio%100);
        int minutoFin = (horaFin/100) * 60 + (horaFin%100);
        return minutoFin - minutoInicio;
    }
    
    public String formatear() {
        return String.format("%02d:%02d-%02d:%02d", 
            horaInicio/100, horaInicio%100, horaFin/100, horaFin%100);
    }
    
    public int obtenerHoraInicio() { return horaInicio; }
    public int obtenerHoraFin() { return horaFin; }
}

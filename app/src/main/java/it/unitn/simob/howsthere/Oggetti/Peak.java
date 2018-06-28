package it.unitn.simob.howsthere.Oggetti;

public class Peak {
    private String nome_picco;
    private Double azimuth;

    public Peak(){

    }
    public Peak(String nome, Double azi){
        this.nome_picco = nome;
        this.azimuth = azi;
    }

    public String getNome_picco() {
        return nome_picco;
    }

    public void setNome_picco(String nome_picco) {
        this.nome_picco = nome_picco;
    }

    public Double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(Double azimuth) {
        this.azimuth = azimuth;
    }
}

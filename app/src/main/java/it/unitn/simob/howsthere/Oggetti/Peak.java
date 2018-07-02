package it.unitn.simob.howsthere.Oggetti;

import java.io.Serializable;

public class Peak implements Serializable{
    private String nome_picco;
    private Double azimuth;
    private Double altezza;

    public Peak(){ }

    public Peak(String nome, Double azi, Double altezza){
        this.nome_picco = nome;
        this.azimuth = azi;
        this.altezza = altezza;
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

    public Double getAltezza() {
        return altezza;
    }

    public void setAltezza(Double altezza) {
        this.altezza = altezza;
    }
}

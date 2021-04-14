package it.unitn.lpmt.howsthere.objects;

import java.io.Serializable;

public class Peak implements Serializable {
    private String nome_picco;
    private double azimuth;
    private double altezza;

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

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(Double azimuth) {
        this.azimuth = azimuth;
    }

    public double getAltezza() {
        return altezza;
    }

    public void setAltezza(double altezza) {
        this.altezza = altezza;
    }
}
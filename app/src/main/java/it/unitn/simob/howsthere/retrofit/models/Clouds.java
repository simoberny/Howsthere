package it.unitn.simob.howsthere.retrofit.models;

import java.io.Serializable;

public class Clouds implements Serializable {
    private String all;

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }

    @Override
    public String toString() {
        return "ClassPojo [all = " + all + "]";
    }
}

package it.unitn.lpmt.howsthere.Weather.models;

import java.io.Serializable;

public class Weather implements Serializable{
    private String id;

    private String icon;

    private String description;

    private String main;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", icon = " + icon + ", description = " + description + ", main = " + main + "]";
    }
}

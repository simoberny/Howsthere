package it.unitn.simob.howsthere.Oggetti;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by matteo on 17/05/18.
 */
@Entity
public class Panorama_Database{

    @PrimaryKey(autoGenerate = true)

    private int ID;
    /*
    private String città;
    private Date data;
    private Bitmap grafico;
    private List sole;
    private List montagne;
    private String alba;
    private String tramonto;
    private int ore_sole;

    public int ID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public List getSole
    */
}
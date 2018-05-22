package it.unitn.simob.howsthere.Oggetti;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by matteo on 18/05/18.
 */

public class Panorama implements Serializable {
    public int ID;
    public String città;
    public Date data;
    public Bitmap grafico;
    public List sole;
    public List montagne;
    public String alba;
    public String tramonto;
    public int ore_sole;

}

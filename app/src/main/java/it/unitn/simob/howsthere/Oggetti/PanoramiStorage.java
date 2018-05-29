package it.unitn.simob.howsthere.Oggetti;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matteo on 18/05/18.
 */

public class PanoramiStorage {

    public static Context context;
    public static PanoramiStorage panorami_storage;

    private List<Panorama> Panorami = new ArrayList();

    public Panorama getPanoramabyID(String ID) {
        if (Panorami.size() == 0) {
            load();
        }
        for (int i = 0; i < Panorami.size(); i++) {
            System.out.println("Panorami.get(i).ID: " + Panorami.get(i).ID);
            if (Panorami.get(i).ID.equals(ID)) {
                return Panorami.get(i);
            }
        }
        return null;
    }

    public void delete(int posizione) {
        if (Panorami.size() == 0) {
            load();
        }
        Panorami.remove(posizione);
        save();
    }

    public void addPanorama(Panorama p) {
        load();
        Panorami.add(p);
        save();
    }

    public List<Panorama> getAllPanorama() {
        if (Panorami.size() == 0) {
            load();
        }
        return Panorami;
    }

    private void load() {
        ObjectInput in;
        try {
            File directory = new File(context.getFilesDir() + File.separator + "panorami");
            directory.mkdirs();
            File inFile = new File(context.getFilesDir(), "/panorami/appSaveState.data");
            in = new ObjectInputStream(new FileInputStream(inFile));
            Panorami = (List<Panorama>) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {        
                try {
                    File outFile = new File(context.getFilesDir(), "/panorami/appSaveState.data");
                    System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC file dir:  "+context.getFilesDir());
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(outFile));
                    out.writeObject(Panorami);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }


    }
}












    /*
    public static Context context;

    private List<Panorama> Panorami = new ArrayList();

    public Panorama getPanoramabyID(String ID){
        load();
        for(int i=0;i<Panorami.size();i++){
            System.out.println("Panorami.get(i).ID: "+ Panorami.get(i).ID);
            if(Panorami.get(i).ID.equals(ID)){
                return Panorami.get(i);
            }
        }
        return null;
    }

    public void delete(int posizione){
        load();
        Panorami.remove(posizione);
        save();
    }

    public void addPanorama(Panorama p){
        load();
        Panorami.add(p);
        save();
    }

    public List<Panorama> getAllPanorama(){
        load();
        return Panorami;
    }

    private void load(){
        ObjectInput in;
        try {
            File directory = new File(context.getFilesDir()+File.separator+"panorami");
            directory.mkdirs();
            File inFile = new File(context.getFilesDir(), "/panorami/appSaveState.data");
            in = new ObjectInputStream(new FileInputStream(inFile));
            Panorami=(List<Panorama>) in.readObject();
            in.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    private void save(){
        try {
            File outFile = new File(context.getFilesDir(), "/panorami/appSaveState.data");
            //System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC file dir:  "+context.getFilesDir());
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(Panorami);
            out.close();
        }catch (Exception e) {e.printStackTrace();}
    }
}
    */


package it.unitn.simob.howsthere.Oggetti;

import android.os.Environment;
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
    private List<Panorama> Panorami = new ArrayList();

    public Panorama getPanoramabyID(int ID){
        load();
        for(int i=0;i<Panorami.size();i++){
            if(Panorami.get(i).ID == ID){
                return Panorami.get(i);
            }
        }
        return null;
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
            in = new ObjectInputStream(new FileInputStream("appSaveState.data"));
            Panorami=(List<Panorama>) in.readObject();
            in.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    private void save(){
        try {
            File outFile = new File(Environment.getExternalStorageDirectory(), "appSaveState.data");
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(Panorami);
            out.close();
        }catch (Exception e) {e.printStackTrace();}
    }
}

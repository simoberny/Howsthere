package it.unitn.lpmt.howsthere.Oggetti;

import android.content.Context;
import android.os.Handler;

import com.facebook.imagepipeline.common.SourceUriType;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.MIN_PRIORITY;

/**
 * Created by matteo on 18/05/18.
 */

public class PanoramiStorage {

    public static Context context;
    public static PanoramiStorage panorami_storage;
    Handler handler = new Handler();
    Handler handler1 = new Handler();

    public List<Panorama> Panorami = new ArrayList();

    public Panorama getPanoramabyID(String ID) {
        load();
        for (int i = 0; i < Panorami.size(); i++) {
            System.out.println("Panorami.get(i).ID: " + Panorami.get(i).ID);
            if (Panorami.get(i).ID.equals(ID)) {
                return Panorami.get(i);
            }
        }
        return null;
    }

    public void delete(int posizione) {
        load();
        Panorami.remove(posizione);
        save();
    }

    public void delete_all() {
        load();
        Panorami.clear();
        save();
    }

    public void delete_by_id(String id){
        load();
        for (int i = 0; i < Panorami.size(); i++) {
            System.out.println("Panorami.get(i).ID: " + Panorami.get(i).ID);
            if (Panorami.get(i).ID.equals(id)) {
                Panorami.remove(i);
                save();
                break;
            }
        }
    }

    public void addPanorama(Panorama p) {
        load();
        Panorami.add(0,p);
        save();
    }


    public List<Panorama> getAllPanorama() {
        load();
        return Panorami;
    }

    private void load() {
        //System.err.println("llllllllllllllllllllllload: panorami size: "+Panorami.size());
        if(Panorami.size() == 0) {
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
    }

    public void initial_load() {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
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

                handler1.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                {
                    public void run()
                    {
                        //Code to update the UI
                    }
                });
            }
        };

        Thread t = new Thread(r);
        t.setPriority(MAX_PRIORITY);
        t.start();
    }

    public void save() {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    Thread.sleep(1000);
                    File outFile = new File(context.getFilesDir(), "/panorami/appSaveState.data");
                    //System.err.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC file dir:  "+context.getFilesDir());
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(outFile));
                    out.writeObject(Panorami);
                    out.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                {
                    public void run()
                    {
                        //Code to update the UI
                    }
                });
            }
        };

        Thread t = new Thread(r);
        t.setPriority(MIN_PRIORITY);
        t.start();
    }
}



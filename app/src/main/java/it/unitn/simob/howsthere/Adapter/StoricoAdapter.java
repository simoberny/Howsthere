package it.unitn.simob.howsthere.Adapter;

/**
 * Created by matteo on 21/05/18.
 */


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;
import it.unitn.simob.howsthere.R;

public class StoricoAdapter extends ArrayAdapter<Panorama>{

    public List<Panorama> l = null;

    public StoricoAdapter(Context context, int textViewResourceId, List<Panorama> objects) {
        super(context, textViewResourceId, objects);
        l = objects;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.singolo_storico, null);


        TextView nome_citta = (TextView) convertView.findViewById(R.id.nome_citta);
        TextView data = (TextView) convertView.findViewById(R.id.data);
        TextView ID = (TextView) convertView.findViewById(R.id.ID);

        Panorama p = l.get(position);
        nome_citta.setText(p.citta+ " ");
        String d = (String) DateFormat.format("dd",p.data)+"/"+ (String) DateFormat.format("MM",p.data)+"/"+ (String) DateFormat.format("yyyy",p.data);
        data.setText(d);
        ID.setText(p.ID);

        return convertView;
    }


    public void onClick_menu() {

    }
}



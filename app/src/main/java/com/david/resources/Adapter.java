package com.david.resources;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.david.games.R;
import com.david.modelo.Ficha;
import com.david.resources.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter to Ranking using ListView (Not used)
 */
class Adapter extends ArrayAdapter<Ficha> {
    private List<Ficha> mList;
    private Context mContext;
    private int resourceLayout;
    private int cont=0;

    public Adapter(@NonNull Context context, int resource, List<Ficha> objects) {
        super(context, resource, objects);
        mList = objects;
        mContext=context;
        resourceLayout=resource;
    }

    @NonNull
    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;

        if(view == null)
            view = LayoutInflater.from(mContext).inflate(R.layout.item_ranking, null);

        Ficha ficha = mList.get(pos);

        ImageView imagen = view.findViewById(R.id.photoRanking);
        imagen.setImageURI(Uri.parse(ficha.getPhoto()));
        if(ficha.getPhoto().startsWith("http"))
            Picasso.get().load(ficha.getPhoto()).error(R.mipmap.user).transform(new CircleTransform()).into(imagen);
        else
            Picasso.get().load(R.mipmap.user).transform(new CircleTransform()).into(imagen);

        TextView email = view.findViewById(R.id.email);
        email.setText(ficha.getEmail());

        TextView puntuacion = view.findViewById(R.id.puntuacion);
        puntuacion.setText(String.valueOf(ficha.getPuntuacion()));

        //Colores podio
        cont++;
        System.out.println("CONT: "+cont);
        if(cont==1) {
            System.out.println("ORO: "+cont);
            LinearLayout layout = view.findViewById(R.id.linearLayout);
            layout.setBackgroundColor(Color.parseColor("#FFCC00"));
        }
        if(cont==2) {
            System.out.println("PLATA: "+cont);
            LinearLayout layout = view.findViewById(R.id.linearLayout);
            layout.setBackgroundColor(Color.parseColor("#8A9597"));
        }
        if(cont==3) {
            System.out.println("BRONCE: "+cont);
            LinearLayout layout = view.findViewById(R.id.linearLayout);
            layout.setBackgroundColor(Color.parseColor("#CD7F32"));
        }

        return view;
    }

}

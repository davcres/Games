package com.david.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

/**
 * Adapter to Ranking using RecycledtView
 */
class AdapterRanking(listDatos: ArrayList<Ficha>): RecyclerView.Adapter<AdapterRanking.ViewHolderDatos>() {
    var listDatos=listDatos
    var cont = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDatos {
        var view= LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, null, false)
        return ViewHolderDatos(view)
    }

    override fun onBindViewHolder(holder: ViewHolderDatos, position: Int) {
        //establece la comunicacion entre nuestro adaptador y la clase ViewHolderDatos
        holder.email.setText(listDatos.get(position).email)
        /*Picasso.get()
            .load(listDatos.get(position).photo)
            .placeholder(R.mipmap.user)
            .error(R.mipmap.user)
            .transform(CircleTransform())
            .into(holder.photo)*/
        if(listDatos.get(position).photo.startsWith("http"))
            Picasso.get().load(listDatos.get(position).photo).error(R.mipmap.user).transform(
                CircleTransform()
            ).into(holder.photo)
        else
            Picasso.get().load(R.mipmap.user).transform(CircleTransform()).into(holder.photo)
        holder.puntuacion.setText(listDatos.get(position).puntuacion.toString())

        //Colores podio
        /*
        cont++
        println("CONT: $cont")
        if(cont==1) {
            println("ORO: $cont")
            holder.layout.setBackgroundColor(Color.parseColor("#FFCC00"))
        }
        if(cont==2) {
            println("PLATA: $cont")
            holder.layout.setBackgroundColor(Color.parseColor("#8A9597"))
        }
        if(cont==3) {
            println("BRONCE: $cont")
            holder.layout.setBackgroundColor(Color.parseColor("#CD7F32"))
        }*/
    }

    override fun getItemCount(): Int {
        return listDatos.size
    }

    class ViewHolderDatos(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var puntuacion = itemView.findViewById<TextView>(R.id.puntuacion) //aqui hay que hacer esto pq esta clase no esta relacionanda con el layout que tiene el textView
        var photo = itemView.findViewById<ImageView>(R.id.photoRanking)
        var email = itemView.findViewById<TextView>(R.id.email)
        var layout = itemView.findViewById<LinearLayout>(R.id.linearLayout)
        //var linea = itemView.findViewById<TextView>(R.id.linea)
    }
}
package com.david.firebase01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador para el RecyclerView de Ranking
 */
class AdapterRanking(listDatos: ArrayList<Pair<String, Int>>): RecyclerView.Adapter<AdapterRanking.ViewHolderDatos>() {
    var listDatos=listDatos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDatos {
        var view=LayoutInflater.from(parent.context).inflate(R.layout.item_ranking,null,false)
        return ViewHolderDatos(view)
    }

    override fun onBindViewHolder(holder: ViewHolderDatos, position: Int) {
        //establece la comunicacion entre nuestro adaptador y la xlase ViewHolderDatos
        //holder.asignarDatos(listDatos.get(position).toString() as String)
        holder.email.setText(listDatos.get(position).first)
        holder.dato.setText(listDatos.get(position).second.toString())//TODO NO SE SI NECESAIRO TO STRING
        //holder.linea.visibility=View.VISIBLE

    }

    override fun getItemCount(): Int {
        return listDatos.size
    }

    class ViewHolderDatos(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dato = itemView.findViewById<TextView>(R.id.puntuacion) //aqui hay que hacer esto pq esta clase no esta relacionanda con el layout que tiene el textView
        var email = itemView.findViewById<TextView>(R.id.email)
        var linea = itemView.findViewById<TextView>(R.id.linea)
    }
}
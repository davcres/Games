package com.david.firebase01

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_ranking.*


class RankingActivity: AppCompatActivity() {
    private var email: String? = ""
    private val db = FirebaseFirestore.getInstance() //instancia de la bd definida en remoto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        //recuperar los parametros de la otra activity
        val bundle = intent.extras
        email = bundle?.getString("email")

        // Create an ArrayAdapter using the string array and a default spinner layout
        val arrayAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.rankings,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        if (spinner != null) {
            //val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, R.array.rankings)
            spinner!!.adapter = arrayAdapter
            //var context = this //esta activity
            spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    ranking(spinner.selectedItem.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Code to perform some action when nothing is selected
                }
            }
        }

    }

    private fun ranking(item: String) {
        var puntuaciones = ArrayList<Array<String>>();
        var entrada: Array<String>
        when (item) {
            "Individual" -> {
                Toast.makeText(this, item, Toast.LENGTH_LONG).show()
                db.collection("users").document(email ?: "sin registrar").collection("puntuaciones")
                    .document("puntuaciones").get().addOnSuccessListener {
                        var partidas = it.get("numPartidas") as Long? ?: 0 //entra aunque no haya puntuaciones => '?'
                        while (partidas > 0) {
                            entrada = Array<String>(2) { _ -> ""} // hay que inicializarlo cada vez para que sean objetos diferentes, si no todos apuntan a uno solo que se actualiza
                            entrada[0]=email ?:"Sin registrar"
                            entrada[1] = java.lang.String.valueOf(it.get(partidas.toString())) as String
                            puntuaciones.add(entrada)
                            partidas--
                        }
                    }.addOnSuccessListener {
                        rankingRecycler.layoutManager = LinearLayoutManager(
                            this,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        var adaptador = AdapterRanking(puntuaciones)
                        rankingRecycler.adapter = adaptador
                    }
            }
            "Ciudad" -> {
                Toast.makeText(this, item, Toast.LENGTH_LONG).show()
            }
            "Pais" -> {
                Toast.makeText(this, item, Toast.LENGTH_LONG).show()
            }
            "Continente" -> {
                Toast.makeText(this, item, Toast.LENGTH_LONG).show()
            }
            "Global" -> {
                Toast.makeText(this, item, Toast.LENGTH_LONG).show()
                db.collection("users").get().addOnSuccessListener { users ->
                    for(documento in users){
                        documento.reference.collection("puntuaciones").document("puntuaciones").get().addOnSuccessListener {
                            var partidas = it.get("numPartidas") as Long? ?: 0
                            println("EMAIL: ${documento.toString()}, PARTIDAS: $partidas")
                            while (partidas > 0) {
                                entrada = Array<String>(2) { _ -> ""} // hay que inicializarlo cada vez para que sean objetos diferentes, si no todos apuntan a uno solo que se actualiza
                                entrada[0]=email ?:"Sin registrar"
                                entrada[1] = java.lang.String.valueOf(it.get(partidas.toString())) as String
                                puntuaciones.add(entrada)
                                partidas--
                            }
                        }
                    }
                }.addOnSuccessListener {
                    //ME LLEGA VACIO
                    println("ININININININININ")
                    rankingRecycler.layoutManager = LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    println("PUNTUACIONES $puntuaciones")
                    var adaptador = AdapterRanking(puntuaciones)
                    rankingRecycler.adapter = adaptador
                }
            }
        }
    }
}
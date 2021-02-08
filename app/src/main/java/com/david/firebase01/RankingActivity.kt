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
        var puntuaciones = ArrayList<Pair<String, Int>>()
        var entrada: Pair<String, Int> //par<email, puntuacion>
        when (item) {
            "Individual" -> {
                //puntuaciones = ArrayList<Array<String>>() inicializarlo para que no se acumulen las de otros no se si necesario creo que no
                Toast.makeText(this, item, Toast.LENGTH_LONG).show()
                db.collection("users").document(email ?: "sin registrar").collection("puntuaciones")
                    .document("puntuaciones").get().addOnSuccessListener {
                        var partidas = it.get("numPartidas") as Long?
                            ?: 0 //entra aunque no haya puntuaciones => '?'
                        while (partidas > 0) {
                            entrada = Pair<String, Int>(
                                email ?: "Sin registrar",
                                (it.get(partidas.toString()) as Long).toInt()
                            ) // hay que inicializarlo cada vez para que sean objetos diferentes, si no todos apuntan a uno solo que se actualiza

                            puntuaciones.add(entrada)
                            partidas--
                        }
                    }.addOnSuccessListener {
                        rankingRecycler.layoutManager = LinearLayoutManager(
                            this,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        puntuaciones = ordenarPuntuaciones(puntuaciones)
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
                    for (documento in users) {
                        documento.reference.collection("puntuaciones").document("puntuaciones")
                            .get()
                            .addOnSuccessListener {
                                var partidas = it.get("numPartidas") as Long? ?: 0
                                while (partidas > 0) {
                                    entrada = Pair<String, Int>(
                                        getEmail(documento.toString()),
                                        (it.get(partidas.toString()) as Long).toInt()
                                    ) // hay que inicializarlo cada vez para que sean objetos diferentes, si no todos apuntan a uno solo que se actualiza
                                    puntuaciones.add(entrada)
                                    partidas--
                                }
                            }.addOnSuccessListener {
                                rankingRecycler.layoutManager = LinearLayoutManager(
                                    this,
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                                puntuaciones = ordenarPuntuaciones(puntuaciones)
                                var adaptador = AdapterRanking(puntuaciones)
                                rankingRecycler.adapter = adaptador

                            }
                    }
                }

            }
        }
    }

    private fun getPuntuaciones(): ArrayList<Array<String>> {
        var puntuaciones = ArrayList<Array<String>>()
        return puntuaciones
    }

    private fun getEmail(documento: String): String {
        return documento.substringAfter('/').substringBefore(',')
    }

    //hubiera sido mejor arraylist de objeto, asi podria implementar el compareTo
    private fun ordenarPuntuaciones(puntuaciones: ArrayList<Pair<String, Int>>): ArrayList<Pair<String, Int>> {
        var array = array(puntuaciones)
        quicksort(array, 0, array.size - 1)
        return arrayList(array)!!
    }

    private fun array(arraylist: ArrayList<Pair<String, Int>>): Array<Pair<String, Int>?> {
        val resultado = arrayOfNulls<Pair<String, Int>>(arraylist.size)
        for (i in 0 until arraylist.size) resultado[i] = arraylist.get(i)
        return resultado
    }

    private fun arrayList(array: Array<Pair<String, Int>?>): ArrayList<Pair<String, Int>>? {
        val resultado: ArrayList<Pair<String, Int>> = ArrayList()
        for (i in array.indices) resultado.add(array[i]!!)
        return resultado
    }

    fun quicksort(A: Array<Pair<String, Int>?>, izq: Int, der: Int) {
        val pivote = A[izq]!!.second // tomamos primer elemento como pivote
        var i = izq // i realiza la búsqueda de izquierda a derecha
        var j = der // j realiza la búsqueda de derecha a izquierda
        while (i < j) {                          // mientras no se crucen las búsquedas
            while (A[i]!!.second <= pivote && i < j) i++ // busca elemento mayor que pivote
            while (A[j]!!.second > pivote) j-- // busca elemento menor que pivote
            if (i < j) {                        // si no se han cruzado
                intercambiar(A, i, j)
                //var aux = Pair(A[i]!!.first, A[i]!!.second) // los intercambia
                //A[i] = A[j]
                //A[j] = aux
            }
        }
        var aux = A[izq]!!.first
        A[izq] = A[j]  // se coloca el pivote en su lugar de forma que tendremos
        A[j] = A[j]!!.copy(first = aux)
        A[j] = A[j]!!.copy(second = pivote)// los menores a su izquierda y los mayores a su derecha
        if (izq < j - 1) quicksort(A, izq, j - 1) // ordenamos subarray izquierdo
        if (j + 1 < der) quicksort(A, j + 1, der) // ordenamos subarray derecho
    }

    private fun intercambiar(A: Array<Pair<String, Int>?>, i: Int, j: Int) {
        var aux = Pair(A[i]!!.first, A[i]!!.second) // los intercambia
        A[i] = A[j]
        A[j] = aux
    }
}

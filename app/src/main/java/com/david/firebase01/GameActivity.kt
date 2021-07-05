package com.david.firebase01

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.random.Random

class GameActivity: AppCompatActivity(), View.OnClickListener{
    private var b: Button? = null       //primer boton pulsado de esta jugada
    private var b1Ant: Button? = null   //primer boton pulsado de la jugada anterior
    private var b2Ant: Button? = null   //segund boton pulsado de la jugada anterior
    private var elementos = 15
    private val list = arrayListOf<Int>()
    private var puntuacion=0
    private val db = FirebaseFirestore.getInstance() //instancia de la bd definida en remoto
    private var email: String? = ""
    private var partida: Long = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        title="Game"

        //recuperar los parametros de la otra activity
        val bundle = intent.extras
        email = bundle?.getString("email")

        //recuperar el num de partidas de la bd
        db.collection("users").document(email ?:"sin registrar").collection("puntuaciones").document("puntuaciones").get().addOnSuccessListener {
            partida = it.get("numPartidas") as Long? ?:0
            if(partida==0.toLong())
                partida=1
            else
                partida += 1 //sumamos 1 para que guarde la siguiente partida a la ultima echada
        }

        clickers()
        generarParejas()
    }

    private fun clickers(){
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)
        button4.setOnClickListener(this)
        button5.setOnClickListener(this)
        button6.setOnClickListener(this)
        button7.setOnClickListener(this)
        button8.setOnClickListener(this)
        button9.setOnClickListener(this)
        button10.setOnClickListener(this)
        button11.setOnClickListener(this)
        button12.setOnClickListener(this)
        button13.setOnClickListener(this)
        button14.setOnClickListener(this)
        button15.setOnClickListener(this)
        button16.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view) {
            button1 -> {
                marcar(button1)
            }
            button2 -> {
                marcar(button2)
            }
            button3 -> {
                marcar(button3)
            }
            button4 -> {
                marcar(button4)
            }
            button5 -> {
                marcar(button5)
            }
            button6 -> {
                marcar(button6)
            }
            button7 -> {
                marcar(button7)
            }
            button8 -> {
                marcar(button8)
            }
            button9 -> {
                marcar(button9)
            }
            button10 -> {
                marcar(button10)
            }
            button11 -> {
                marcar(button11)
            }
            button12 -> {
                marcar(button12)
            }
            button13 -> {
                marcar(button13)
            }
            button14 -> {
                marcar(button14)
            }
            button15 -> {
                marcar(button15)
            }
            button16 -> {
                marcar(button16)
            }
        }
    }

    private fun marcar(button: Button){
        when(b){
            null -> {
                reiniciar()
                b = button
                setColor(b, "#00ffff")  //cian
            }
            button -> {
                setColor(button, "#84FFFF")
                b = null
            }
            else -> {
                b1Ant = b //para que si se pulsa otro boton rapido no se lie, y b sea null cuanto antes
                b2Ant = button //b y button van a cambiar antes, b1Ant y b2Ant tardan mas
                b=null
                setTextSize(b1Ant, 30F)
                setTextSize(b2Ant, 30F)
                if(b2Ant?.text.toString() == b1Ant!!.text.toString()) {
                    elementos -= 2
                    //para que no te borre los siguientes que pulses si los pulsas muy rapido
                    var borrar1 = b1Ant
                    var borrar2 = b2Ant
                    setColor(b1Ant, "#00FF00") //green
                    setColor(b2Ant, "#00FF00") //green
                    Handler().postDelayed(Runnable {
                        borrar1!!.visibility = View.INVISIBLE
                        borrar2?.visibility = View.INVISIBLE
                    }, 1000)
                    if(elementos==0){
                        puntuacionTV.setText("Tu puntuación es: $puntuacion puntos")
                        db.collection("users").document(email ?: "sin identificar").collection("puntuaciones").document("puntuaciones").get().addOnSuccessListener {
                            if(it.exists()){
                                db.collection("users").document(email ?: "sin identificar")
                                    .collection("puntuaciones").document("puntuaciones").update(
                                        mapOf(
                                            "numPartidas" to partida,
                                            partida.toString() to puntuacion
                                        )
                                    )
                            }else{
                                db.collection("users").document(email ?: "sin identificar")
                                    .collection("puntuaciones").document("puntuaciones").set(
                                        mapOf(
                                            "numPartidas" to partida,
                                            partida.toString() to puntuacion
                                        )
                                )
                            }
                            //tb puedo ponerlo aqui supongo como el partida += 1
                        }/*.addOnSuccessListener {
                            //lo que quiera ejecutar despues de la base de datos, si no pongo el addOnComplete se ejecuta antes que lo de arriba
                        }*/
                        var id =email+partida
                        db.collection("users").document("todos").update(
                            mapOf(
                                id to puntuacion
                            )
                        )
                    }
                }else{
                    puntuacion += 10
                    puntuacionTV.setText("punt: $puntuacion-part $partida")
                    setColor(b1Ant, "#FF0000") //red
                    setColor(b2Ant, "#FF0000") //red
                    Handler().postDelayed(Runnable {
                        reiniciar()
                    }, 1000)
                }
            }
        }
    }

    private fun reiniciar(){
        setColor(b1Ant, "#84FFFF")
        setColor(b2Ant, "#84FFFF")
        setTextSize(b1Ant, 0F)
        setTextSize(b2Ant, 0F)
    }

    private fun setColor(b: Button?, c: String){
        b?.setBackgroundColor(Color.parseColor(c))
    }

    private fun setTextSize(b: Button?, s: Float){
        b?.setTextSize(1, s)
    }

    private fun generarParejas(){
        list.addAll(listOf(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8))
        ponerNum(button1)
        ponerNum(button2)
        ponerNum(button3)
        ponerNum(button4)
        ponerNum(button5)
        ponerNum(button6)
        ponerNum(button7)
        ponerNum(button8)
        ponerNum(button9)
        ponerNum(button10)
        ponerNum(button11)
        ponerNum(button12)
        ponerNum(button13)
        ponerNum(button14)
        ponerNum(button15)
        ponerNum(button16)
        elementos=16
    }

    private fun ponerNum(b: Button){
        if(list.size>1) {
            val n = Random.nextInt(0..elementos)
            //println("$n, $list, ${list.get(n)}")
            b.setText(list.get(n).toString())
            list.remove(list.get(n))
            elementos--
        }else
            b.setText(list.get(0).toString())
    }

    fun Random.nextInt(range: IntRange): Int {
        return range.start + nextInt(range.last - range.start)
    }
}
package com.david.firebase01

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_game.*
import kotlin.random.Random
import kotlin.random.nextInt

class GameActivity: AppCompatActivity(), View.OnClickListener{
    private var b1: Button? = null
    private var elementos = 15
    private val list = arrayListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

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
        when(b1){
            null -> {
                b1 = button
                b1?.setBackgroundColor(Color.parseColor("#00ffff")) //cian
            }
            button -> {
                button.setBackgroundColor(Color.parseColor("#84FFFF"))
                b1=null
            }
            else -> {
                if(button.text.toString() == b1?.text.toString()) {
                    b1?.visibility=View.INVISIBLE
                    button.visibility=View.INVISIBLE
                }else{
                    button.setBackgroundColor(Color.parseColor("#FF0000"))
                    b1?.setBackgroundColor(Color.parseColor("#FF0000"))
                }
                b1=null
            }
        }
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
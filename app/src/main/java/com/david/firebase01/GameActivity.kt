package com.david.firebase01

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_game.*

class GameActivity: AppCompatActivity(), View.OnClickListener{
    private var b1: Button? = null
    //private var b2: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        clickers()
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

}
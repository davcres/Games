package com.david.modelo

import android.provider.ContactsContract.CommonDataKinds.Email




class Ficha(val email: String, val photo: String, val puntuacion: Int)/*: Comparable<Ficha> */{
    /*override operator fun compareTo(f: Ficha): Int {
        requireNotNull(f) { "El email es null" }
        if (puntuacion<f.puntuacion) return -1
        if (puntuacion>f.puntuacion) return 1
        return 0
    }*/


}
package com.david.modelo

//de momento no guardo las puntuaciones porque para eso tendria que acceder a la BD,y  para eso ya
//lo hago en el momento de ver el ranking (HomeActivity 237)
class Usuario(val email: String, var photo: String, val provider: ProviderType, var username: String, var puntuaciones: ArrayList<Ficha>){

    fun addPuntuacion(puntuacion: Int){
        puntuaciones.add(
            Ficha(
                email,
                photo,
                puntuacion
            )
        )
    }
}
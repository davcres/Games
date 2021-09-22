package com.david.games

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.david.modelo.Ficha
import com.david.modelo.ProviderType
import com.david.modelo.Usuario
import com.david.resources.CircleTransform
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    //Constante para la base de datos
    private val db = FirebaseFirestore.getInstance() //instancia de la bd definida en remoto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Remote Config
        //Cada vez que se entre en esta pantalla se recogen los valores que hay en la nube, y se "actualiza" la app
        btnForzarError.visibility=View.INVISIBLE
        //addOnCompleteListener hace que se llame a este bloque cuando se recupere esta informacion
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener {
            if(it.isSuccessful){
                //boolean asociado a la config de mostrar o no el boton de forzar error
                val showErrorButton=Firebase.remoteConfig.getBoolean("show_error_button")
                //string asociado a la config de que texto mostrar en el boton
                val errorButtonText=Firebase.remoteConfig.getString("error_button_text")
                //todo REMOTE CONFIG
                if(showErrorButton){
                    btnForzarError.visibility= View.VISIBLE
                    btnForzarError.text=errorButtonText
                }
            }
        }

        //recuperar los parametros de la otra activity
        val bundle = intent.extras

        val userString = bundle?.getString("user")
        val gson = Gson()
        val user = gson.fromJson(
            userString,
            Usuario::class.java
        )

        setup(user)

        /*db.collection("users").document(user.email).get().addOnSuccessListener {
            user.username = it.get("username") as String? ?:"username"
            setup(user)
        }*/

        //Guardar los datos del usuario autenticado para otras veces
        //constante con el gestor de preferencias de la app, que es el encargado de gestionar el guardado y la recuperacion de datos del tipo clave-valor
        val prefs = getSharedPreferences(
            getString(R.string.prefs_file)/*accedemos al fichero*/,
            Context.MODE_PRIVATE/*modo de acceso privado*/
        ).edit() //edit para poner en modo de edicion a nuestro share preferences y añadir los datos
        prefs.putString("user", userString)
        prefs.apply() //para asegurarnos de que se guarden los nuevos datos
        //si los guardamos, tambien tendremos que borrarlos en el momento en el que se cierre sesion(boton cerrar sesion)
    }

    /*override fun onStart(){ //se invoca cada vez que se vuelve a mostrar esta pantalla
        super.onStart()

        //Setup
        //recuperar los parametros de la otra activity
        val bundle = intent.extras
        //val email = bundle?.getString("email")
        //val provider = bundle?.getString("provider")
        //val photo = bundle?.getString("photo")
        //var username = ""

        val userString = bundle?.getString("user")
        val gson = Gson()
        val user = gson.fromJson(
            userString,
            Usuario::class.java
        )
        println("HOLA onStart() ${user.username}")
        setup(user)
/*
        db.collection("users").document(user.email).get().addOnSuccessListener {
            user.username = it.get("username") as String? ?:"username"
            setup(user)
        }*/

        //Guardar los datos del usuario autenticado para otras veces
        //constante con el gestor de preferencias de la app, que es el encargado de gestionar el guardado y la recuperacion de datos del tipo clave-valor
        val prefs = getSharedPreferences(
            getString(R.string.prefs_file)/*accedemos al fichero*/,
            Context.MODE_PRIVATE/*modo de acceso privado*/
        ).edit() //edit para poner en modo de edicion a nuestro share preferences y añadir los datos
        //prefs.putString("email", user.email) //Clave, valor
        //prefs.putString("provider", user.provider.name)
        //prefs.putString("photo", user.photo)
        //prefs.putString("username", user.username)
        prefs.putString("user", userString)
        prefs.apply() //para asegurarnos de que se guarden los nuevos datos
        //si los guardamos, tambien tendremos que borrarlos en el momento en el que se cierre sesion(boton cerrar sesion)

        //para que solo lo haga si recoge un atributo email de la actividad anterior
        //intent.extras?.getString("email")?.let { setPuntuaciones(it) }

        if(user != null)
            setPuntuaciones(user, user.email)
    }
    */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Setup
        //recuperar los parametros de la otra activity
        val bundle = intent.extras
        //val email = bundle?.getString("email")
        //val provider = bundle?.getString("provider")
        //val photo = bundle?.getString("photo")
        //var username = ""

        val userString = data?.getStringExtra("user")//bundle?.getString("user")
        val gson = Gson()
        val user = gson.fromJson(
            userString,
            Usuario::class.java
        )
        println("HOLA onActivityResult() ${user.username}, ${user.puntuaciones.size}")
        setup(user)
    }

    private fun setup(user: Usuario){
        title = "Home"
        tvEmail.text = user.email

        //db.collection("users").document(email).get().addOnSuccessListener {
        //var username = it.get("username") as String? ?:"username"
        tvUsername.text = user.username
        //}
        /*if(photo=="default")
            Picasso.get()
                .load(R.mipmap.user)            //la imagen que queremos poner (la por defecto)
                .transform(CircleTransform())   //forma circular
                .into(photoHome)                //donde
        else
            Picasso.get()
            .load(photo)                        //la imagen que queremos poner
            //.placeholder(R.mipmap.user)       //la imagen que se muestra hasta que se carga la que queremos
            .error(R.mipmap.user)               //la imagen que se muestra si da error nuestra imagen
            .transform(CircleTransform())       //forma circular
            .into(photoHome)                    //donde
*/
        if(user.photo.startsWith("http"))
            Picasso.get().load(user.photo).error(R.mipmap.user).transform(CircleTransform()).into(photoHome)
        else
            Picasso.get().load(R.mipmap.user).transform(CircleTransform()).into(photoHome)

        //para que solo lo haga si recoge un atributo email de la actividad anterior
        //intent.extras?.getString("email")?.let { setPuntuaciones(it) }
        if(user != null)
            setPuntuaciones(user, user.email)

        btnCerrarSesion.setOnClickListener {
            //Borrado de datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear() //Para borrar todas las preferencias que tenemos guardadas en la App
            prefs.apply()
            //Para cerrar sesion en facebook
            if(user.provider.equals(ProviderType.FACEBOOK)){
                LoginManager.getInstance().logOut()
            }
            //cierra sesion
            FirebaseAuth.getInstance().signOut()
            //vuelve a la pantalla anterior
            onBackPressed()
        }
        //Aqui va lo de crashlytics si me funcionara
/*
        //Guardar datos en base de datos
        btnGuardar.setOnClickListener {
            db.collection("users"/*nombre de la coleccion para almacenar a todos los usuarios*/).document(
                email/*clave del documento asociado al usuario de nuestra app => 1 documento por cada email(usuario) y los datos son el valor*/
            ).set( /*set sustituye si hubiera algo antes, con update podemos añadir sin borrar otros campos. si no hay nada, update falla*/
                //estos datos que incluimos en el documento del usuario a su vez van en forma de hashmap clave-valor
                mapOf(
                    "provider"/*clave*/ to provider/*valor*/,
                    "personal" to personalTV.text.toString(),
                    "posicion" to posicionTV.text.toString(),
                    "global" to globalTV.text.toString()
                )
            )
            //si un usuario inicia sesion en diferentes apps, con su email podemos recuperar su info, y da igual en que dispositivo esté


        }

        //Recuperar datos de base de datos
        btnRecuperar.setOnClickListener {
            db.collection("users").document(email).get().addOnSuccessListener {
                personalTV.setText(it.get("personal") as String? ?: "")
                posicionTV.setText(it.get("posicion") as String? ?: "") //toString si no hay nada te pone null, asi + bonito
                globalTV.setText(it.get("global") as String? ?: "")
            }
        }

        //Eliminar datos de base de datos
        btnEliminar.setOnClickListener {
            db.collection("users").document(email).delete()
        }
*/
        btnJugar.setOnClickListener {
            //btnGuardar.callOnClick()
            showGame(user)
        }

        RankingButton.setOnClickListener {
            showRanking(user)
        }

        photoHome.setOnClickListener {
            showProfile(user)
        }
    }

    private fun setPuntuaciones(user: Usuario, email: String) {
        var puntuaciones = ArrayList<Ficha>()
        var entrada: Ficha //<email, photo, puntuacion>
        // var numUsers: Int
        //var cont = 0
        db.collection("users").get().addOnSuccessListener { users ->
            //numUsers=users.size()
            for (documento in users) {
                /*//TODO ACTUALIZACION BD (Para cuando necesite actualizar la base de datos entera)
                documento.reference.update(
                    mapOf(
                        //"email" to email,
                        //"username" to "username",
                        //"photo" to "null",
                        //"provider" to "null"
                    )
                )*/
                //cont++
                documento.reference.collection("puntuaciones").document("puntuaciones")
                    .get().addOnSuccessListener { doc ->
                        var partidas = /*user.puntuaciones.size*/ doc.get("numPartidas") as Long? ?: 0
                        while (partidas > 0) {
                            entrada = Ficha(
                                documento.get("email") as String? ?:"null",
                                documento.get("photo") as String? ?:"default",
                                (doc.get(partidas.toString()) as Long).toInt()
                            ) // hay que inicializarlo cada vez para que sean objetos diferentes, si no todos apuntan a uno solo que se actualiza
                            puntuaciones.add(entrada)
                            partidas--
                        }
                    }.addOnSuccessListener {
                        //TODO COMO HACER QUE SOLO LLEGUE UNA VEZ. AHORA SE EJECUTA UNA VEZ POR CADA USER
                        //AL FINAL LA QUE CUENTA ES SOLO LA ULTIMA VEZ QUE SE EJECUTE Y VA A QUEDAR CON EL VALOR CORRECTO PERO SE EJECUTA users.size veces

                        //println("cont $cont - $numUsers")
                        //if(cont == numUsers) {
                        //  println("PUNTUACIONES ${puntuaciones.size} ${users.size()}")
                        puntuaciones = ordenarPuntuaciones(puntuaciones)
                        getPersonal(user, email, puntuaciones)
                        if(puntuaciones.size!=0)
                            globalTV.text = "Record Mundial: ${puntuaciones.get(0).puntuacion.toString()} puntos"
                        //}
                        println("USER: ${user.puntuaciones.size}")
                    }
            }
        }
    }

    private fun getPersonal(user: Usuario, email: String, puntuaciones: ArrayList<Ficha>): Int {
        //La lista de puntuaciones esta ordenada => La primera vez que se encuentre ese email es su mejor puntuacion
        var cont = 1
        var puntuacionAnterior = 0
        var flag = true
        var puntuacionesUser = ArrayList<Ficha>()
        for(i: Ficha in puntuaciones){
            if(i.email == email){
                if(flag) {
                    flag = false
                    getPosicion(i.puntuacion, puntuacionAnterior, cont, puntuaciones)
                    personalTV.text = "Record Personal: ${i.puntuacion.toString()} puntos"
                    //return 0
                }
                puntuacionesUser.add(i)
            }
            puntuacionAnterior = i.puntuacion
            cont++
        }
        user.puntuaciones = puntuacionesUser
        if(flag) {
            personalTV.text = "Aún no hay partidas"
            return -1
        }
        return 0
    }

    private fun getPosicion(puntuacion: Int, puntuacionAnterior: Int, contador: Int, puntuaciones: ArrayList<Ficha>) {
        var cont = contador
        if(puntuacion==puntuacionAnterior) {
            //cont-1 seria la puntuacion actual => la anterior a la anterior cont-3
            if (cont - 3 >= 0)
                getPosicion(puntuacion, puntuaciones.get(cont - 3).puntuacion, cont - 1, puntuaciones)
            else
                posicionTV.text = "Posición: ${cont.toString()}º  Top ${getTop(cont, puntuaciones.size)}"
        }else {
            posicionTV.text = "Posición: ${cont.toString()}º  Top ${getTop(cont, puntuaciones.size)}"
        }
    }

    private fun ordenarPuntuaciones(puntuaciones: ArrayList<Ficha>): ArrayList<Ficha> {
        return puntuaciones.sortedWith(compareBy({ it.puntuacion })).toMutableList() as ArrayList<Ficha>
    }

    private fun getTop(pos: Int, total: Int): String{
        var top = 100*pos/total
        return when{
            top <= 1 -> return "1%"
            top <= 2 -> return "2%"
            top <= 3 -> return "3%"
            top <= 4 -> return "4%"
            top <= 5 -> return "5%"
            top <= 10 -> return "10%"
            top <= 25 -> return "25%"
            top <= 50 -> return "50%"
            top <= 75 -> return "75%"
            else -> "100%"
        }
    }

    private fun showGame(user: Usuario){
        //convertimos el objeto user a String JSON para enviarlo a otra activity
        val gson = Gson()
        val userString = gson.toJson(user)

        val gameIntent = Intent(this, GameActivity::class.java).apply {
            //lo que le quiera pasar a la nueva activity
            putExtra("user", userString)
        }
        startActivityForResult(gameIntent, 1)
    }

    private fun showRanking(user: Usuario){
        //convertimos el objeto user a String JSON para enviarlo a otra activity
        val gson = Gson()
        val userString = gson.toJson(user)

        val rankingIntent = Intent(this, RankingActivity::class.java).apply {
            putExtra("user", userString)
        }
        startActivity(rankingIntent)
    }

    private fun showProfile(user: Usuario){
        //convertimos el objeto user a String JSON para enviarlo a otra activity
        val gson = Gson()
        val userString = gson.toJson(user)

        val profileIntent = Intent(this, ProfileActivity::class.java).apply {
            putExtra("user", userString)
        }
        startActivityForResult(profileIntent, 1)
    }
}
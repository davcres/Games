package com.david.firebase01

import android.content.Context
import android.content.Intent
import android.net.Uri.parse
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*

enum class  ProviderType{
    BASIC, //autenticacion basica por email y contraseña
    GOOGLE, //autenticacion con google
    FACEBOOK //autenticacion con facebook
}

class HomeActivity : AppCompatActivity() {
    //Constante para la base de datos
    private val db = FirebaseFirestore.getInstance() //instancia de la bd definida en remoto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Setup
        //recuperar los parametros de la otra activity
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        val photo = bundle?.getString("photo") ?:"null"
        setup(email ?: "", provider ?: "", photo)

        //Guardar los datos del usuario autenticado para otras veces
        //constante con el gestor de preferencias de la app, que es el encargado de gestionar el guardado y la recuperacion de datos del tipo clave-valor
        val prefs = getSharedPreferences(
            getString(R.string.prefs_file)/*accedemos al fichero*/,
            Context.MODE_PRIVATE/*modo de acceso privado*/
        ).edit() //edit para poner en modo de edicion a nuestro share preferences y añadir los datos
        prefs.putString("email", email) //Clave, valor
        prefs.putString("provider", provider)
        prefs.putString("photo", photo)
        prefs.apply() //para asegurarnos de que se guarden los nuevos datos
        //si los guardamos, tambien tendremos que borrarlos en el momento en el que se cierre sesion(boton cerrar sesion)

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
                //TODO REMOTE CONFIG
                if(showErrorButton){
                    btnForzarError.visibility= View.VISIBLE
                    btnForzarError.text=errorButtonText
                }

            }
        }
    }

    private fun setup(email: String, provider: String, photo: String){
        title = "Inicio"
        tvEmail.text=email
        tvProvider.text=provider
        Picasso.get()
            .load(photo)                    //la imagen que queremos poner
            .placeholder(R.mipmap.user)     //la imagen que se muestra hasta que se carga la que queremos
            .error(R.mipmap.user)           //la imagen que se muestra si da error nuestra imagen
            .transform(CircleTransform())   //forma circular
            .into(photoIV)                  //donde

        btnCerrarSesion.setOnClickListener {
            //Borrado de datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear() //Para borrar todas las preferencias que tenemos guardadas en la App
            prefs.apply()
            //Para cerrar sesion en facebook
            if(provider==ProviderType.FACEBOOK.name){
                LoginManager.getInstance().logOut()
            }
            //cierra sesion
            FirebaseAuth.getInstance().signOut()
            //vuelve a la pantalla anterior
            onBackPressed()
        }
        //Aqui va lo de crashlytics si me funcionara

        //Guardar datos en base de datos
        btnGuardar.setOnClickListener {
            db.collection("users"/*nombre de la coleccion para almacenar a todos los usuarios*/).document(
                email/*clave del documento asociado al usuario de nuestra app => 1 documento por cada email(usuario) y los datos son el valor*/
            ).set( /*set sustituye si hubiera algo antes, con update podemos añadir sin borrar otros campos. si no hay nada, update falla*/
                //estos datos que incluimos en el documento del usuario a su vez van en forma de hashmap clave-valor
                mapOf(
                    "provider"/*clave*/ to provider/*valor*/,
                    "address" to tvAddress.text.toString(),
                    "phone" to tvTelefono.text.toString()
                )
            )
            //si un usuario inicia sesion en diferentes apps, con su email podemos recuperar su info, y da igual en que dispositivo esté


        }

        //Recuperar datos de base de datos
        btnRecuperar.setOnClickListener {
            db.collection("users").document(email).get().addOnSuccessListener {
                tvAddress.setText(it.get("address") as String? ?: "")
                tvTelefono.setText(it.get("phone") as String? ?: "") //toString si no hay nada te pone null, asi + bonito
            }
        }

        //Eliminar datos de base de datos
        btnEliminar.setOnClickListener {
            db.collection("users").document(email).delete()
        }

        btnJugar.setOnClickListener {
            btnGuardar.callOnClick()
            showGame(email)
        }

        RankingButton.setOnClickListener {
            showRanking(email)
        }
    }

    private fun showGame(email: String){
        val gameIntent = Intent(this, GameActivity::class.java).apply {
            //lo que le quiera pasar a la nueva activity
            putExtra("email", email)
        }
        startActivity(gameIntent)
    }

    private fun showRanking(email: String){
        val rankingIntent = Intent(this, RankingActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(rankingIntent)
    }
}
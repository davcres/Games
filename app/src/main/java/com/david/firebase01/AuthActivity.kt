package com.david.firebase01

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_home.*

class AuthActivity : AppCompatActivity() {
    //Constante para la base de datos
    private val db = FirebaseFirestore.getInstance() //instancia de la bd definida en remoto
    private val GOOGLE_SIGN_IN = 100 //ID que queramos
    private val callbackManager = CallbackManager.Factory.create()
    private var photo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //Splash
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //En esta activity si quiero ocultar la ToolBar lo tengo que hacer manualmente ya que estamos diciendo que tiene el Splash Theme en vez de NoActionBar Theme
        getSupportActionBar()?.hide()

        //Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integracion de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        //Configurar Remote Config
        //hemos definido valores en un servidor remoto (en la nube) pero lo recomendable es definir valores por defecto para cunado nuestra app no se pueda conectar a internet
        //tambien es importante establecer cada cuanto tiempo la app tiene que actualizar esos valores (por defecto cada 12h)
        val configSettings= remoteConfigSettings {
            //cada cuanto se actualizan estos valores
            minimumFetchIntervalInSeconds=1 //segundos
        }
        val firebaseConfig= Firebase.remoteConfig
        firebaseConfig.setConfigSettingsAsync(configSettings)
        //valores por defecto por si hay algun problema recuperando los valores de la nube
        firebaseConfig.setDefaultsAsync(mapOf("show_error_button" to false, "error_button_text" to "Forzar error"))

        //Setup
        // Initialize Firebase Auth
        notification() //para recuperar la notificacion
        setup()
        session() //Comprueba si tenemos guardado un email y un provider para saber si se ha iniciado sesion previamente
    }

    override fun onStart(){ //se invoca cada vez que se vuelve a mostrar esta pantalla
        super.onStart()
        lytAuth.visibility=View.VISIBLE
        photo=null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data) //Desencadena una llamada a una de las operaciones, dependiendo del resultado del login con facebook
        super.onActivityResult(requestCode, resultCode, data)
        //si el requestCode es igual al que nosotros le pasamos a nuestra actividad, querra decir que la respuesta de esta activity se corresponde con la de la autenticacion de google
        //primero nos autenticamos en google, y luego le pasamos la credencial de esa autenticacion a firebase para autenticarnos alli tb
        if(requestCode==GOOGLE_SIGN_IN){
            //vamos a recuperar la respuesta
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            //account puede dar fallo
            try {
                //vamos a recuperar la cuenta autenticada
                val account = task.getResult(ApiException::class.java)
                //ahora ya tenemos la cuenta de google y lo que tenemos que hacer es autenticarnos en firebase igual que haciamos con email y password
                if (account != null) {
                    //obtenemos la credencial de la cuenta para firebase
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                photo = account.photoUrl.toString()
                                saveUser(account.email ?:"", ProviderType.GOOGLE,photo ?:"null")
                                showHome(account.email ?:"", ProviderType.GOOGLE, photo!!)
                            } else {
                                showAlert()
                            }
                        }
                }
            }catch (e: ApiException){
                showAlert()
            }
        }
    }

    private fun setup(){
        title = "Log in" //cambia el titulo de la pantalla con la propiedad title

        //Al pulsar en btn registrar se ejecuta lo que hay aqui
        btnRegistrar.setOnClickListener{
            if (etEmail.text.isNotEmpty() && etContrasena.text.isNotEmpty()){
                //print(etEmail.text.toString() + ", " + etContrasena.text.toString())
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        etEmail.text.toString(),
                        etContrasena.text.toString()
                    ).addOnCompleteListener(this) {
                    if(it.isSuccessful){
                        saveUser(etEmail.text.toString(), ProviderType.BASIC,"null")
                        //? para el caso en el que haya un email vacio
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC, photo ?:"null")

                        //no se podria poner?:
                        //showHome(etEmail.text.toString(), ProviderType.BASIC)
                    }else{
                        showAlert()
                    }
                }
            }
        }

        //Al pulsar en btn registrar se ejecuta lo que hay aqui
        btnAcceder.setOnClickListener{
            if (etEmail.text.isNotEmpty() && etContrasena.text.isNotEmpty()){
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(
                        etEmail.text.toString(),
                        etContrasena.text.toString()
                    ).addOnCompleteListener {
                    if(it.isSuccessful){
                        //? para el caso en el que haya un email vacio
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC, photo ?:"null")

                        //no se podria poner:
                        //showHome(etEmail.text.toString(), ProviderType.BASIC, photo)
                    }else{
                        showAlert()
                    }
                }
            }
        }

        btnGoogle.setOnClickListener {
            //Configuracion Autenticacion
            //login con google
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            //cliente de autenticacion de google
            val googleClient = GoogleSignIn.getClient(
                this/*contexto: nuestra activity*/,
                googleConf/*configuracion*/
            )

            //para cerrar sesion de otra cuenta que estuviera iniciada sesion antes
            googleClient.signOut()

            //mostrar la pantalla de autenticacion de google
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
            //como iniciamos un activity en la que esperamos que nos respondan algo, deberos reimplementar onActivityResult
        }

        btnFacebook.setOnClickListener {
            //Para abrir la pantalla de autenticacion nativa de facebook
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email")/*los permisos que queremos leer del usuario*/
            )
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        //si el resultado no es null, podremos obtener el token de autenticacion de facebook
                        result?.let {
                            val token = it.accessToken //el token de autenticacion de facebook
                            //obtenemos la credencial de la cuenta para firebase
                            val credential = FacebookAuthProvider.getCredential(token.token)
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        showHome(it.result?.user?.email ?:"",ProviderType.FACEBOOK, photo ?:"null")
                                    } else {
                                        showAlert()
                                    }
                                }
                        }
                    }

                    override fun onCancel() {
                        //No hacer nada
                    }

                    override fun onError(error: FacebookException?) {
                        showAlert()
                    }
                })
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType, photo: String){
        //Creamos un Intent a la nueva pantalla para navegar a ella, pasando el contexto(nosotros) y la pantalla a la que queremos navegar
        //Una vez se instancia, podemos poner apply para poder pasarle diferentes parametros
        val homeIntent = Intent(this, HomeActivity::class.java).apply{
            //como queremos que se llame la variable, la variable
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("photo", photo)
        }
        startActivity(homeIntent)
    }

    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email=prefs.getString("email" /*clave*/, null /*valor defecto*/) //recupera el email de prefs
        val provider = prefs.getString("provider", null)
        //println("PHOTOOOO $photo")
        val photo = prefs.getString("photo", "null")
        //println("PHOTOOOO $photo")
        if(email != null && provider != null) {
            lytAuth.visibility= View.INVISIBLE //Para no mostrarlo en caso de que existe la sesion iniciada
            showHome(email, ProviderType.valueOf(provider), photo ?:"null")
        }
    }

    private fun notification(){
        //TODO UN SOLO USUARIO
        FirebaseInstallations.getInstance().id.addOnCompleteListener {
        //FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            //comprobamos que tenemos un id de dispositivo unico
            //en caso de que exista el id unico lo imprimimos
            it.let {
            //it.result?.token?.let {
                println("Este es el id unico de nuestro dispositivo: ${it}")
                //=> para enviar notificaciones a un solo usuario, se haria mediante su id (su token)
            }
        }

        //TODO GRUPO DE USUARIOS
        //Se suscribe un usuario a un topic/tema
        //asi se suscribe a un topic
        FirebaseMessaging.getInstance().subscribeToTopic("notificacion")

        //TODO NOTIFICACIONES CON INFORMACION
        //Recuperar Informacion de una notif clave(url)-valor
        val url=intent.getStringExtra("url")
        url?.let {
            val toast = Toast.makeText(this, "Entra en "+url, Toast.LENGTH_LONG)
            toast.show()
        }
    }

    private fun saveUser(email: String, provider: ProviderType, photo: String){
        db.collection("users").document(email).get().addOnSuccessListener {
            //creo que el if.exists() no hace falta
            /*if(it.exists()){
                db.collection("users").document(email).update(
                    mapOf(
                        "email" to email,
                        "provider" to provider,
                        "photo" to photo
                        //"username" to "username"
                    )
                )
            }*/
            if(!it.exists()){
                db.collection("users").document(email).set(
                    mapOf(
                        "email" to email,
                        "provider" to ProviderType.BASIC,
                        "photo" to photo,
                        "username" to "username"
                    )
                )
            }
        }

    }
}
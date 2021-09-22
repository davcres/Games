package com.david.games

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.david.modelo.ProviderType
import com.david.modelo.Usuario
import com.david.resources.CircleTransform
import com.david.resources.NoticeDialogFragment
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity: FragmentActivity(), NoticeDialogFragment.NoticeDialogListener {
    private val db = FirebaseFirestore.getInstance() //instancia de la bd definida en remoto
    private lateinit var username: String
    //private lateinit var oldUsername: String
    private lateinit var email: String
    private lateinit var oldEmail: String
    private lateinit var photo: String
    private lateinit var provider: String
    private lateinit var user: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //el email lo cojo de la actividad anterior
        //val bundle = intent.extras
        //email = bundle?.getString("email") ?:"Sin registrar"
        //photo = bundle?.getString("photo") ?:"default"
        //username = bundle?.getString("username") ?:"username"
        //println(oldEmail+", "+photo+", "+username)

    }

    override fun onStart() {
        super.onStart()

        //el email lo cojo de la actividad anterior
        val bundle = intent.extras
        val userString = bundle?.getString("user")
        val gson = Gson()
        user = gson.fromJson(
            userString,
            Usuario::class.java
        )
        email = user.email

        //las demas cosas las cogemos de la BD (la primera vez tb entra)
        db.collection("users").document(email).get().addOnSuccessListener {
            //oldEmail=it.get("email") as String? ?:"sin registrar"
            //println("oldEmail: "+oldEmail)
            oldEmail=email
            //photo=it.get("photo") as String? ?:"default"
            photo = user.photo
            //username=it.get("username") as String? ?:"username"
            username = user.username
            //oldUsername=username
            //provider=it.get("provider") as String? ?:"default"
            provider = user.provider.name
        }.addOnSuccessListener {
            setup()
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()

        //convertimos el objeto user a String JSON para enviarlo a otra activity
        val gson = Gson()
        val userString = gson.toJson(user)

        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("user", userString)
        }
        setResult(RESULT_OK, homeIntent)
        finish()
    }

    private fun setup() {
        if(photo.startsWith("http"))
            Picasso.get().load(photo).error(R.mipmap.user).transform(CircleTransform()).into(photoProfile)
        else
            Picasso.get().load(R.mipmap.user).transform(CircleTransform()).into(photoProfile)
        /*if(photo=="default")
            Picasso.get().load(R.mipmap.user).transform(CircleTransform()).into(photoProfile)
        else
            Picasso.get().load(photo).error(R.mipmap.user).transform(CircleTransform()).into(photoProfile)*/

        userNameTXT.text=username
        userEmailTXT.text=email

        editImage.setOnClickListener {
            showEditProfile()
        }

        btnLogOut.setOnClickListener {
            //Borrado de datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear() //Para borrar todas las preferencias que tenemos guardadas en la App
            prefs.apply()
            //Para cerrar sesion en facebook
            if(provider == ProviderType.FACEBOOK.name){
                LoginManager.getInstance().logOut()
            }
            //cierra sesion
            FirebaseAuth.getInstance().signOut()

            //vuelve a la pantalla principal
            val authIntent = Intent(this, AuthActivity::class.java)
            authIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(authIntent)
        }
    }

    private fun showEditProfile(){
        /*var customDialog = Dialog(this)
        //deshabilitamos el título por defecto
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //obligamos al usuario a pulsar los botones para cerrarlo
        customDialog.setCancelable(false)
        //establecemos el contenido de nuestro dialog
        customDialog.setContentView(R.layout.edit_profile)
        customDialog.show()*/

        /*val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.edit_profile, null)
        builder.setView(view)
        builder.show()*/

        val builder = NoticeDialogFragment()
        builder.show(supportFragmentManager, "Perfil")
    }

    //CAMBIAR EMAIL -> NO IMPLEMENTADO BIEN
    override fun onDialogPositiveClick(dialog: DialogFragment, newUsername: String, newEmail: String, ok: Boolean) {
        println("OK: $ok")
        if(ok) {
            println("Viejo: $username, $email - Nuevo: $newUsername, $newEmail")
            if (username != newUsername && newUsername.trim() != "") {
                //username = newUsername
                userNameTXT.text = newUsername
                user.username = newUsername
                db.collection("users").document(email).update(
                    mapOf(
                        "username" to newUsername
                    )
                )
            }
            if (email != newEmail && newEmail.trim() != "") {
                userEmailTXT.text = newEmail
                //user.email = newEmail
                db.collection("users").document(email/*todo probar oldEmail*/).update(
                    mapOf(
                        "email" to newEmail
                    )
                ).addOnSuccessListener {
                    //para que cuando inicie sesion automaticamente coja el nuevo correo
                    val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                    prefs.remove("email")
                    prefs.putString("email", newEmail)
                    prefs.apply()

                    actualizarBD(newEmail)
                }
            }
        }else{ //como en NoticeDialogFragment hay un bucle recorriendo los documentos aqui entra muchas veces, pero la que vale es solo la ultima vez, entonces si finalmente ok=false hay que volverlo a dejar como estaba, con los valores de antes de actualizar
            println("Volver atras")
            //email=oldEmail
            if (username != newUsername && newUsername.trim() != "") {
                //username = oldUsername
                userNameTXT.text = username
                user.username = username
                db.collection("users").document(email).update(
                    mapOf(
                        "username" to username
                    )
                )
            }
            if (email != newEmail && newEmail.trim() != "") {
                userEmailTXT.text = email
                //user.email = email
                db.collection("users").document(email).update(
                    mapOf(
                        "email" to email
                    )
                ).addOnSuccessListener {
                    //para que cuando inicie sesion automaticamente coja el nuevo correo
                    val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                    prefs.remove("email")
                    prefs.putString("email", email)
                    prefs.apply()

                    actualizarBD(email)
                }
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {}

    /**
     * Funcion para crear un nuevo documento (usuario) con el email nuevo y eliminar el anterior
     * ya que no se puede editar el nombre del documento
     * Una vez se actualiza el email, oldEmail siempre el antiguo, newEmail el nuevo y email depende
     */
    private fun actualizarBD(newEmail: String){
        var photo = "default"
        var provider = "default"
        var username = "username"
        db.collection("users").document(email).get().addOnSuccessListener {
            photo = it.get("photo") as String? ?:"default"
            provider= it.get("provider") as String? ?:"default"
            username= it.get("username") as String? ?:"username"
        }.addOnSuccessListener {
            //TODO PASAR LAS PUNTUACIONES
            /*FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(
                    newEmail,
                    etContrasena.text.toString()
                ).addOnCompleteListener(this) {
                    if(it.isSuccessful){
                        saveUser(etEmail.text.toString(), "null")
                    }else{
                        showAlert()
                    }
                }*/
            db.collection("users").document(newEmail).set(
                mapOf(
                    "email" to newEmail,
                    "provider" to provider,
                    "photo" to photo,
                    "username" to username
                )
            )
        }.addOnSuccessListener {
            db.collection("users").document(oldEmail).delete()
            email=newEmail
        }
    }
}
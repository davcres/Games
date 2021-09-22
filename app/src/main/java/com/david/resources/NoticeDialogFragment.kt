package com.david.resources

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.david.games.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.edit_profile.view.*

class NoticeDialogFragment : DialogFragment() {
    // Use this instance of the interface to deliver action events
    internal lateinit var listener: NoticeDialogListener
    private val db = FirebaseFirestore.getInstance()

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, name: String, email: String, ok: Boolean)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            val v=inflater.inflate(R.layout.edit_profile, null)
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(v)
                //.setTitle("Editar Perfil")

                // Add action buttons
                .setPositiveButton("ACEPTAR",
                    DialogInterface.OnClickListener { dialog, id ->
                        val nuevoNombre = v.usernameDialog.text.toString()
                        val nuevoEmail = v.emailDialog.text.toString()
                        builder.setView(v)
                        //aceptamos nuevoNombre="" y nuevoEmail="" porque pueden no cambiarse
                        println("NUEVO $nuevoNombre, $nuevoEmail")

                        var contador=HashMap<String, Int>() //contador para asegurarme de que cada username y email esta solo una vez
                        var usernamesBD: String
                        var emailsBD: String
                        var ok = true
                        contador.put(nuevoNombre, 1)
                        contador.put(nuevoEmail, 1)
                        if((nuevoNombre.trim()!="" || nuevoEmail.trim()!="") && (usernameValidation(nuevoNombre) && emailValidation(nuevoEmail))) {
                            db.collection("users").get().addOnSuccessListener { users ->
                                for (doc in users) {
                                    doc.reference.get().addOnSuccessListener {
                                        usernamesBD = it.get("username") as String //datos de cada user
                                        emailsBD = it.get("email") as String
                                        println("username: $usernamesBD, email: $emailsBD")
                                        if(usernamesBD != "username") { //ya tenia username
                                            if (!contador.containsKey(usernamesBD)) //ese username aun no esta en el hashmap
                                                contador.put(usernamesBD, 1)
                                            else                                    //ese username ya esta en el hashmap
                                                contador.put(usernamesBD, contador.get(usernamesBD)!! + 1)

                                            if (!contador.containsKey(emailsBD))
                                                contador.put(emailsBD, 1)
                                            else
                                                contador.put(emailsBD, contador.get(emailsBD)!! + 1)

                                            //todo HAY QUE SACAR DEL SACO EL ACTUAL YA QUE COMO SE ACTUALIZA MUCHAS VECES, CUANDO LLEGA AL ACTUAL DICE QUE YA EXISTE
                                            //si alguno esta mas de una vez a la mierda
                                            if(contador.get(usernamesBD)!! > 1 || contador.get(emailsBD)!! > 1) {
                                                ok = false
                                                println("$usernamesBD usado por $emailsBD")
                                            }
                                        }
                                    }.addOnSuccessListener {
                                        listener.onDialogPositiveClick(this, nuevoNombre, nuevoEmail, ok)
                                    }
                                }
                            }
                        }else{
                            val toast = Toast.makeText(activity, "El username o el email no son válidos", Toast.LENGTH_LONG)
                            //toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                        }

                        /*if (!userUsado(nuevoNombre).contains(nuevoNombre) && !emailUsado(nuevoEmail) && usernameValidation(nuevoNombre) && emailValidation(nuevoEmail)) {
                            println(userUsado(nuevoNombre))
                            listener.onDialogPositiveClick(this, nuevoNombre, nuevoEmail)
                        }else{
                            val toast = Toast.makeText(activity, "El username o el email no son válidos", Toast.LENGTH_LONG)
                            //toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show()
                        }*/
                    })
                .setNegativeButton("CANCELAR",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    /**
     * Lista con los emails y usernames de la BD
     * todo DEVUELVE LISTA VACIA PORQUE NO VA EN ORDEN
     */
    private fun getLista(): ArrayList<Pair<String, String>>{
        var lista = ArrayList<Pair<String, String>>()
        var par: Pair<String, String>
        db.collection("users").get().addOnSuccessListener { users ->
            for(doc in users){
                doc.reference.get().addOnSuccessListener {
                    //if(user==it.get("username"))
                    par=Pair(
                        it.get("email") as String? ?:"email",
                        it.get("username") as String? ?:"username"
                    )
                    lista.add(par)
                }
            }
        }
        return lista
    }

    /**
     * Funcion para validar que el username no esté usado por otra persona
     * @param user El nuevo nombre de usuario
     * TODO Necesito devolver el arraylist con los usernames porque no puedo poner return true en el if de si el user ya es usado
     * todo tampoco me deja poner break
     * @return true si el nuevo nombre ya está siendo usado. False en caso contrario
     */
    private fun userUsado(user: String): ArrayList<String>{
        var usernames = ArrayList<String>()
        if(user.trim()=="") return usernames
        db.collection("users").get().addOnSuccessListener { users ->
             for(doc in users){
                doc.reference.get().addOnSuccessListener {
                    usernames.add(it.get("username") as String? ?:"username")
                    //if(user==it.get("username")) break
                }
            }
        }
        return usernames
    }

    /**
     * Funcion para validar que el nuevo email no esté usado por otra persona
     * @param user El nuevo email de usuario
     * @return true si el nuevo email ya está siendo usado. False en caso contrario
     */
    private fun emailUsado(email: String): Boolean{
        var emails = ArrayList<String>()
        if(email.trim()=="") return false
        db.collection("users").get().addOnSuccessListener { users ->
            for(doc in users){
                doc.reference.get().addOnSuccessListener {
                    //if(user==it.get("username"))
                    emails.add(it.get("email") as String? ?:"default")
                }
            }
        }.addOnSuccessListener {
            println("EMAILS: ${emails.toString()}")
        }
        return false
    }

    /**
     * Función para validar el nombre de usuario.
     * Solo permite letras mayúsculas y minúsculas y '-', '_', '.'
     * @param cadena El nuevo nombre
     * @return true si el nuevo nombre es válido. False en caso contrario
     */
    private fun usernameValidation(cadena: String): Boolean {
        if(cadena.trim()=="") return true //"" es que no lo ha cambiado
        if(cadena.trim()=="username") return false
        for (x in 0 until cadena.length) {
            val c = cadena[x]
            // Si no está entre a y z, ni entre A y Z,...
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '-' || c == '_' || c == '.' || c.toInt()>=0 || c.toInt()<=9)) {
                return false
            }
        }
        return true
    }

    /**
     * Comprueba si una direccion es valida
     * @return True si es valida, False en cualquier otro caso
     */
    private fun emailValidation(email: String): Boolean{
        if(email.trim()=="") return true
        val partes = email.split('@')
        if(partes.size!=2) return false
        val nombre=partes[0]
        val dominio=partes[1]
        if (nombre == null || dominio == null)
            return false
        if (nombre.trim().length == 0 || dominio.trim().length == 0)
            return false
        if (nombre.contains("@")|| dominio.contains("@"))
            return false

        if (nombre.startsWith("_") || nombre.startsWith("-") || nombre.startsWith(".") || nombre.endsWith("_") || nombre.endsWith("-") || nombre.endsWith(".")) return false
        if (nombre.contains("__") || nombre.contains("_-") || nombre.contains("_.") || nombre.contains("-_") || nombre.contains("--") || nombre.contains("-.") || nombre.contains("._") || nombre.contains(".-") || nombre.contains("..") || dominio.contains("__") || dominio.contains("_-") || dominio.contains("_.") || dominio.contains("-_") || dominio.contains("--") || dominio.contains("-.") || dominio.contains("._") || dominio.contains(".-") || dominio.contains("..")) return false
        if (nombre.contains("#") || dominio.contains("#")) return false
        if (!dominio.contains(".")) return false
        if (dominio.substring(dominio.lastIndexOf('.') + 1).length < 2) return false
        if(dominio.contains("_")) return false

        return true
    }

    /* todo LO DEJO POR SI NO ME FUNCIONA LO DE ARRIBA
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return crearDialogo()
    }

    private fun crearDialogo(): AlertDialog {
        activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater

            val inflater = requireActivity().layoutInflater
            val v = inflater.inflate(R.layout.edit_profile, null)
            /*builder.setView(v)

            eventosBotones(v)

            return builder.create()*/
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(v)
                //.setTitle("Editar Perfil")
                // Add action buttons
                .setPositiveButton("ACEPTAR",
                    DialogInterface.OnClickListener() { dialog, id ->
                        //val v = inflater.inflate(R.layout.edit_profile, null)
                        val nuevoNombre2= v.nameDialog.text.toString()
                        val nuevoNombre = view?.nameDialog?.text.toString()
                        val nuevoEmail = view?.emailDialog?.text.toString()
                        builder.setView(view)
                        if (nuevoNombre != "") {
                            println("Nuevo Nombre: ${v.nameDialog.text.toString()}")
                            println("Nuevo Nombre2: $nuevoNombre2")
                        } else {
                            println("Necesitas escribir un nombre")
                        }
                        //builder.setView(view)
                        //listener.onDialogPositiveClick(this, nuevoNombre, nuevoEmail)
                    })
                .setNegativeButton("CANCELAR",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
            return builder.create()
        //} ?: throw IllegalStateException("Activity cannot be null")
        }?: throw IllegalStateException("Activity cannot be null")
    }

    private fun eventosBotones(v: View) {
        v.aceptarBtn.setOnClickListener {
            println("Name ${v.nameDialog.text.toString()}")
        }
    }*/
}
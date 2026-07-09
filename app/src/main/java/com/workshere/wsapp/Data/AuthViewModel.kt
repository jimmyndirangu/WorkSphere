package com.workshere.wsapp.Data

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.workshere.wsapp.Model.Boss
import com.workshere.wsapp.Model.Worker
import com.workshere.wsapp.Navigation.ROUTE_BOSS
import com.workshere.wsapp.Navigation.ROUTE_CHOICE
import com.workshere.wsapp.Navigation.ROUTE_FILLING_WORKER
import com.workshere.wsapp.Navigation.ROUTE_LOGIN1
import com.workshere.wsapp.Navigation.ROUTE_SIGNUP1
import com.workshere.wsapp.Navigation.ROUTE_SIGNUP2


class AuthViewModel (var navController: NavHostController, var context: Context ){
    var MAuth: FirebaseAuth
    init {
        MAuth= FirebaseAuth.getInstance()
    }

    fun registerboss(bossName: String, bossEmail: String, bossPass: String, bossConfirmpass: String){
        val joincode=generatejoincode()
        if (bossName.isBlank()||bossEmail.isBlank()||bossPass.isBlank()||bossConfirmpass.isBlank()){
            Toast.makeText(context,"Input all fields",
                Toast.LENGTH_LONG).show()
            return
        }else if(bossPass!=bossConfirmpass){
            Toast.makeText(context,"Password do not match",
                Toast.LENGTH_LONG).show()
            return
        }else{
            MAuth.createUserWithEmailAndPassword(bossEmail,bossPass).addOnCompleteListener {
                if (it.isSuccessful){
                    val bossdata= Boss(bossName,bossEmail,bossPass,bossConfirmpass,
                        MAuth.currentUser!!.uid,
                        joincode)
                    val regRef= FirebaseDatabase.getInstance()
                        .getReference("Boss")
                        .child(MAuth.currentUser!!.uid)
                    regRef.setValue(bossdata).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context, "Saved Successfully", Toast.LENGTH_LONG).show()
                            navController.navigate(ROUTE_BOSS)
                        } else {
                            Toast.makeText(context, "Failed to save data: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }else{
                    Toast.makeText(context,"Registration failed: ${it.exception!!.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    fun loginboss(bossEmail: String, bossPass: String){
        MAuth.signInWithEmailAndPassword(bossEmail, bossPass).addOnCompleteListener {
            if (it.isSuccessful){
                val uid = MAuth.currentUser!!.uid

                FirebaseDatabase.getInstance()
                    .getReference("Boss")
                    .child(uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()){
                            Toast.makeText(context, "Successfully logged in", Toast.LENGTH_LONG).show()
                            navController.navigate(ROUTE_BOSS)
                        } else {
                            MAuth.signOut()
                            Toast.makeText(context, "No boss account found. Please register first.", Toast.LENGTH_LONG).show()
                            navController.navigate(ROUTE_SIGNUP1)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error checking account: ${e.message}", Toast.LENGTH_LONG).show()
                        navController.navigate(ROUTE_LOGIN1)
                    }
            } else {
                Toast.makeText(context, "${it.exception!!.message}", Toast.LENGTH_LONG).show()
                navController.navigate(ROUTE_LOGIN1)
            }
        }
    }
    fun generatejoincode(): String{
        val characters="ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789"

        return(1..6)
            .map { characters.random() }
            .joinToString("")
    }
    fun registerworker(workerName: String,workerEmail: String,workerPass: String,workerConfirmpass: String, joincode: String){
        if (workerName.isBlank()||workerEmail.isBlank()||workerPass.isBlank()||workerConfirmpass.isBlank()||joincode.isBlank()){
            Toast.makeText(context,"Input all fields",
                Toast.LENGTH_LONG).show()
            return
        }else if(workerPass!=workerConfirmpass){
            Toast.makeText(context,"Password do not match",
                Toast.LENGTH_LONG).show()
            return
        }

        val database= FirebaseDatabase.getInstance().reference
        database.child("Boss")
            .orderByChild("joincode")
            .equalTo(joincode)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()){
                    Toast.makeText(context,"Invalid Join Code",
                        Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                MAuth.createUserWithEmailAndPassword(workerEmail,workerPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val bossId=snapshot.children.first().key!!
                            val workerId= MAuth.currentUser!!.uid
                            val worker= Worker(
                                workerName=workerName,
                                workerEmail=workerEmail,
                                workerId = workerId,
                                bossId = bossId,
                                phone = "",
                                salary = "",
                                imageURL = ""
                            )
                            database.child("Boss")
                                .child(bossId)
                                .child("Workers")
                                .child(workerId)
                                .setValue(worker)
                                .addOnSuccessListener { 
                                    Toast.makeText(context,"Registration successful", Toast.LENGTH_LONG).show()
                                    navController.navigate("$ROUTE_FILLING_WORKER/$bossId")
                                }
                                .addOnFailureListener { 
                                    Toast.makeText(context,"Failed to save worker details: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        }else{
                            Toast.makeText(context,"Auth failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { 
                        Toast.makeText(context,"Authentication error: ${it.message}", Toast.LENGTH_LONG).show() 
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Database query failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}


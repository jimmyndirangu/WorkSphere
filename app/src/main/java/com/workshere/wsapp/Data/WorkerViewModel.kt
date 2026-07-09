package com.workshere.wsapp.Data

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.operation.ListenComplete
import com.workshere.wsapp.Model.Worker
import com.workshere.wsapp.Navigation.ROUTE_BOSS
import com.workshere.wsapp.Navigation.ROUTE_CHOICE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream


class WorkerViewModel: ViewModel() {
    val cloudinaryUrl = "https://api.cloudinary.com/v1_1/dsyxjpsbl/image/upload"
    val uploadPreset="ws_image"

    fun fillingdetails(bossId: String, imageUri: Uri?, workername: String, workeroccupation: String, location: String,
                       experience: String, salary: String, workerage: String, phonenumber: String,
                       context: Context, navController: NavController, onComplete: () -> Unit){
        if(workername.isBlank()|| workeroccupation.isBlank()||location.isBlank()||experience.isBlank()||salary.isBlank()
            ||workerage.isBlank()||phonenumber.isBlank()){
            Toast.makeText(context,"Please fill all fields", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }
        if (imageUri==null){
            Toast.makeText(context,"Please select image", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }
        val workerId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (workerId == null) {
            Toast.makeText(context,"Not logged in", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }
        viewModelScope.launch(Dispatchers.IO){
            try {
                val selectedImageUrl = uploadToCloudinary(context,imageUri)
                val ref = FirebaseDatabase.getInstance()
                    .getReference("Boss")
                    .child(bossId)
                    .child("Workers")
                    .child(workerId)
                val workerData = mapOf(
                    "workerId" to workerId,
                    "bossId" to bossId,
                    "workerName" to workername,
                    "workeroccupation" to workeroccupation,
                    "location" to location,
                    "experience" to experience,
                    "salary" to salary,
                    "workerage" to workerage,
                    "phonenumber" to phonenumber,
                    "imageurl" to selectedImageUrl
                )
                ref.updateChildren(workerData).await()
                withContext(Dispatchers.Main){
                    Toast.makeText(context,"Worker saved successfully. Your boss can view your details now",
                        Toast.LENGTH_LONG).show()
                    onComplete()
                    navController.navigate(ROUTE_CHOICE)
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(context,"Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
                    onComplete()
                }
            }
        }
    }
    private fun uploadToCloudinary(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val fileBytes = inputStream?.readBytes() ?: throw Exception("Failed to read image")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg",
                fileBytes.toRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        val request = Request.Builder()
            .url(cloudinaryUrl)
            .post(requestBody)
            .build()

        val response = OkHttpClient().newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Upload failed: ${response.message}")

        val responseBody = response.body?.string()
        val secureUrl = Regex("\"secure_url\":\"(.*?)\"")
            .find(responseBody ?: "")?.groupValues?.get(1)

        return secureUrl ?: throw Exception("Failed to get secure URL from Cloudinary")
    }
    private val _workers = mutableStateListOf<Worker>()
    val worker: List<Worker> =_workers


    fun fetchworker(context: Context, bossId: String){
        val ref = FirebaseDatabase.getInstance()
            .getReference("Boss")
            .child(bossId)
            .child("Workers")
        ref.get().addOnSuccessListener { snapshot ->
            _workers.clear()
            for (child in snapshot.children){
                val worker = child.getValue(Worker::class.java)
                worker?.let{
                    it.workerId = child.key
                    _workers.add(it)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context,"Failed to load workers", Toast.LENGTH_LONG).show()
        }
    }
    fun updateworker(
        bossId: String,
        workerId: String,
        imageUri: Uri?,
        workername: String,
        workeroccupation: String,
        location: String,
        experience: String,
        salary: String,
        workerage: String,
        phonenumber: String,
        context: Context, navController: NavController, onComplete: () -> Unit){

        if(workername.isBlank() || workeroccupation.isBlank() || location.isBlank() ||
            experience.isBlank() || salary.isBlank() || workerage.isBlank() || phonenumber.isBlank()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        viewModelScope.launch(Dispatchers.IO){
            try {
                val selectedImageUrl = imageUri?.let { uploadToCloudinary(context,it) }
                val updatedata = mutableMapOf<String, Any?>(
                    "workername" to workername,
                    "workeroccupation" to workeroccupation,
                    "location" to location,
                    "experience" to experience,
                    "salary" to salary,
                    "workerage" to workerage,
                    "phonenumber" to phonenumber)
                if (selectedImageUrl != null){
                    updatedata["imageurl"] = selectedImageUrl
                }
                val ref = FirebaseDatabase.getInstance()
                    .getReference("Boss").child(bossId).child("Workers").child(workerId)
                ref.updateChildren(updatedata).await()
                fetchworker(context, bossId)
                withContext(Dispatchers.Main){
                    Toast.makeText(context,"Worker updated successfully", Toast.LENGTH_LONG).show()
                    navController.navigate(ROUTE_BOSS)
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(context,"Worker update failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun deleteworker(bossId: String, workerId: String, context: Context){
        val ref = FirebaseDatabase.getInstance()
            .getReference("Boss").child(bossId).child("Workers").child(workerId)
        ref.removeValue().addOnSuccessListener {
            _workers.removeAll { it.workerId == workerId }
            Toast.makeText(context,"Worker deleted", Toast.LENGTH_LONG).show()
        }
            .addOnFailureListener {
                Toast.makeText(context,"Worker deletion unsuccessful", Toast.LENGTH_LONG).show()
            }
    }
}
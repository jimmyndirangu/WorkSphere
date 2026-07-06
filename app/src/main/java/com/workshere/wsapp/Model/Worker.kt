package com.workshere.wsapp.Model

import com.google.firebase.database.PropertyName

data class Worker (
    @get:PropertyName("workername") @set:PropertyName("workername") var workerName: String?=null,
    @get:PropertyName("workeremail") @set:PropertyName("workeremail") var workerEmail: String?=null,
    var workerPass: String?=null,
    var workerConfirmPass: String?=null,
    var workerId: String?=null,
    var bossId: String?=null,
    var joincode: String?=null,
    @get:PropertyName("phonenumber") @set:PropertyName("phonenumber") var phone: String?=null,
    var salary: String?=null,
    @get:PropertyName("imageurl") @set:PropertyName("imageurl") var imageURL: String?=null,

    // Additional fields matching WorkerViewModel
    var workeroccupation: String?=null,
    var location: String?=null,
    var experience: String?=null,
    var workerage: String?=null
)

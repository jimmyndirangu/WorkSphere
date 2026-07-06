package com.workshere.wsapp.Model

import androidx.annotation.Nullable
import java.net.URL

data class Worker (
    var workerName: String?=null,
    var workerEmail: String?=null,
    var workerPass: String?=null,
    var workerConfirmPass: String?=null,
    var workerId: String?=null,
    var bossId: String?=null,
    var joincode: String?=null,
    var phone: String?=null,
    var salary: String?=null,
    var imageURL: String?=null
    )
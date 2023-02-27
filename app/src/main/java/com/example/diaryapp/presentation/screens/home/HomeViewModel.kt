package com.example.diaryapp.presentation.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.connectivity.ConnectivityObserver
import com.example.diaryapp.connectivity.NetworkConnectivityObserver
import com.example.diaryapp.data.database.ImageToDeleteDao
import com.example.diaryapp.data.database.entity.ImageToDelete
import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.data.repository.MongoDB
import com.example.diaryapp.model.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.time.ZonedDateTime
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectivityObserver: NetworkConnectivityObserver,
    private val imageToDeleteDao: ImageToDeleteDao
): ViewModel() {

    private lateinit var allDiariesJob: Job
    private lateinit var filteredDiariesJob: Job

    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)
    private var network by mutableStateOf(ConnectivityObserver.Status.Unavailable)
    var dateIsSelected by mutableStateOf(false)
        private set

    init {
        getDiaries()
        viewModelScope.launch {
            connectivityObserver.observe().collect{
                network = it
            }
        }
    }

    fun getDiaries(zonedDateTime: ZonedDateTime? = null){
        dateIsSelected = zonedDateTime != null
        diaries.value = RequestState.Loading
        if(dateIsSelected && zonedDateTime != null){
            observerFilteredDiaries(zonedDateTime = zonedDateTime)
        } else {
            observeAllDiaries()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeAllDiaries(){
        allDiariesJob = viewModelScope.launch {
            if(::filteredDiariesJob.isInitialized){
                filteredDiariesJob.cancelAndJoin()
            }
            MongoDB.getAllDiaries().collect{ result ->
                diaries.value = result

            }
        }
    }

    private fun observerFilteredDiaries(zonedDateTime: ZonedDateTime){
        filteredDiariesJob = viewModelScope.launch {
            if(::allDiariesJob.isInitialized){
                allDiariesJob.cancelAndJoin()
            }
            MongoDB.getFilteredDiaries(zonedDateTime).collect{ result ->
                diaries.value = result
            }
        }
    }

    fun deleteAllDiaries(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ){
        if(network == ConnectivityObserver.Status.Available){
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val imagesDirectory = "/images/$userId"
            val storage =  FirebaseStorage.getInstance().reference
            storage.child(imagesDirectory)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach { ref ->
                        val imagePath = "images/$userId/${ref.name}"
                        storage.child(imagePath).delete()
                            .addOnFailureListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    imageToDeleteDao.addImageToDelete(
                                        imageToDelete = ImageToDelete(
                                            remoteImagePath = imagePath
                                        )
                                    )
                                }
                            }
                    }
                    viewModelScope.launch (Dispatchers.IO) {
                        val result = MongoDB.deleteAllDiaries()
                        if(result is RequestState.Success){
                            withContext(Dispatchers.Main){
                                onSuccess()
                            }
                        } else if (result is RequestState.Error){
                            withContext(Dispatchers.Main){
                                onError(result.error)
                            }
                        }

                    }
                }
                .addOnFailureListener { onError(it) }
        } else {
            onError(Exception("No internet connection"))
        }

    }
}
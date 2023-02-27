package com.example.diaryapp.presentation.screens.auth

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.diaryapp.util.Constants.CLIENT_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import java.lang.Exception

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    loadingState: Boolean,
    oneTapSignInState: OneTapSignInState,
    authenticated: Boolean,
    messageBarState: MessageBarState,
    onButtonClicked: () -> Unit,
    onSuccessfulFirebaseSignIn: (String) -> Unit,
    onFailedFirebaseSignIn: (Exception) -> Unit,
    onDialogDismissed: (String) -> Unit,
    navigateToHome: () -> Unit
){
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        content = {
            ContentWithMessageBar(messageBarState = messageBarState) {
                AuthenticationContent(loadingState = loadingState) {
                    onButtonClicked()
                }
                
            }
        }
    )

    OneTapSignInWithGoogle(
        state = oneTapSignInState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { tokenId ->
            val credential = GoogleAuthProvider.getCredential(tokenId, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        onSuccessfulFirebaseSignIn(tokenId)
                    } else {
                        task.exception?.let { it -> onFailedFirebaseSignIn(it) }
                    }
                }
        },
        onDialogDismissed = { message ->
            onDialogDismissed(message)
        }
    )

    LaunchedEffect(key1 = authenticated){
        if(authenticated){
            navigateToHome()
        }
    }
}
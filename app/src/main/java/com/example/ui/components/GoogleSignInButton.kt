package com.example.ui.components

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun GoogleSignInButton(
    text: String,
    onIdTokenReceived: (String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    onIdTokenReceived(idToken)
                } else {
                    onError("Google Sign-In failed: No ID Token found")
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Google sign in failed", e)
                onError("Google Sign-In failed: ${e.message}")
            }
        } else {
            // Cancelled or failed
            Log.e("GoogleSignIn", "Google sign in cancelled or failed. Result code: ${result.resultCode}")
        }
    }

    LokivoSecondaryButton(
        text = text,
        onClick = {
            try {
                // Determine client ID
                val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                
                if (clientId == "YOUR_WEB_CLIENT_ID" || clientId.isBlank()) {
                    onError("Please configure GOOGLE_WEB_CLIENT_ID in the Secrets panel.")
                    return@LokivoSecondaryButton
                }

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build()
                
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
            } catch (e: Exception) {
                onError("Error launching Google Sign In: ${e.message}")
            }
        }
    )
}

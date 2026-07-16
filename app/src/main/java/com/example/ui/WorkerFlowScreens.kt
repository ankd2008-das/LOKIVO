package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun WorkerPendingApprovalScreen(onCheckStatus: () -> Unit) {
    var isApproved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("workers").document(user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val verified = snapshot.getBoolean("isVerified") ?: false
                        val status = snapshot.getString("approvalStatus")
                        if (verified || status == "approved") {
                            isApproved = true
                        }
                    }
                }
        }
    }

    LaunchedEffect(isApproved) {
        if (isApproved) {
            onCheckStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                "Registration Complete",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Pending Approval", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
        }
        
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Your profile is under review by the admin. We will notify you once you are approved.", style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onCheckStatus, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Check Status (Simulate Approval)")
                }
            }
        }
    }
}

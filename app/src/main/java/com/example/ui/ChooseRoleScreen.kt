package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.LokivoSecondaryButton

@Composable
fun ChooseRoleScreen(
    onNavigateToCustomer: () -> Unit,
    onNavigateToWorker: () -> Unit,
    onNavigateToRegisterWorker: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Welcome to Lokivo 👋",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "How would you like to use Lokivo today?\nChoose your experience. You can switch anytime later from your profile.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        PremiumRoleCard(
            title = "I Need a Service",
            subtitle = "Find trusted professionals near you for home repairs, maintenance, cleaning, tutoring and many other local services.",
            buttonText = "Continue as Customer",
            imageRes = R.drawable.img_hero_banner,
            onClick = onNavigateToCustomer
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PremiumRoleCard(
            title = "I Want to Work",
            subtitle = "Join thousands of local professionals and receive genuine customer enquiries from your nearby area.",
            buttonText = "Continue as Worker",
            imageRes = R.drawable.img_hero_banner,
            onClick = onNavigateToWorker
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text("  OR  ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LokivoSecondaryButton(
            text = "Register as a Professional",
            onClick = onNavigateToRegisterWorker,
            icon = { Icon(Icons.Filled.Construction, contentDescription = null, modifier = Modifier.size(20.dp)) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Don't have a worker profile yet?\nRegister your business in just a few minutes.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun PremiumRoleCard(
    title: String,
    subtitle: String,
    buttonText: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, animationSpec = tween(150), label = "scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        ))
                )
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                )
            }
            
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(buttonText, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}


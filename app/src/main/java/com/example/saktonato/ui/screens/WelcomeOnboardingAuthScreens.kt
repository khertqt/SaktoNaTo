package com.example.saktonato.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saktonato.data.Store
import com.example.saktonato.data.Tier
import com.example.saktonato.navigation.Nav
import com.example.saktonato.ui.components.*

@Composable
fun WelcomeScreen(nav: Nav) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF4A555C), Color(0xFF1F2A30))))) {
            ShirtShape(Color(0xFFE9EDF0), Modifier.align(Alignment.Center).size(170.dp))
            Canvas(Modifier.fillMaxSize()) {
                val y = size.height * 0.4f
                drawLine(Color.White, Offset(size.width * 0.2f, y), Offset(size.width * 0.8f, y), 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
            }
            Text("Chest width · 52 cm · 20.5 in", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E),
                modifier = Modifier.align(Alignment.TopStart).padding(14.dp).clip(RoundedCornerShape(50)).background(Color.White).padding(12.dp, 6.dp))
            Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Sample listing · Carousell", color = Color.White, fontSize = 14.sp)
                    Text("Boxy tee, ₱150", color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                TierBadge(Tier.VERIFIED)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF25718F)), Alignment.Center) {
                Icon(Icons.Filled.Straighten, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("Sakto Na ‘To", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color(0xFF1E1E1E))
        }
        Text("See it on you. Know if it fits.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
            fontSize = 20.sp, color = Color(0xFF25718F))
        Body("Upload a preloved clothing screenshot, compare it with your measurements, and see how honest the fit result is, before you buy.")
        Spacer(Modifier.height(8.dp))
        Btn("Get Started", { nav.go("onboarding") }, icon = Icons.Filled.ArrowForward)
        Btn("I already have an account", { nav.go("auth") }, primary = false)
    }
}

@Composable
fun OnboardingScreen(st: Store, nav: Nav) {
    var i by remember { mutableIntStateOf(0) }
    val slides = listOf(
        Triple<ImageVector, String, String>(Icons.Filled.Straighten, "Save your measurements", "Add your height and usual size. Chest, waist and a favorite shirt make results better."),
        Triple<ImageVector, String, String>(Icons.Filled.Upload, "Upload any preloved listing", "Use a screenshot from Facebook, Shopee, Instagram or Carousell, or try a sample."),
        Triple<ImageVector, String, String>(Icons.Filled.VerifiedUser, "Check the fit and preview the look", "Every result shows how reliable it is: Style Preview, Estimated Fit or Verified Fit.")
    )
    fun finish(profile: Boolean) { st.setOnboarded(); nav.tab("home"); if (profile) nav.go("profileedit") }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                slides.indices.forEach { k ->
                    Box(Modifier.height(8.dp).width(if (k == i) 28.dp else 8.dp).clip(RoundedCornerShape(4.dp))
                        .background(if (k <= i) Color(0xFF25718F) else Color(0xFFCFC8B8)))
                }
            }
            TextButton({ finish(false) }, Modifier.heightIn(min = 48.dp)) { Text("Skip", fontSize = 16.sp, color = Color(0xFF25718F)) }
        }
        val (ic, title, text) = slides[i]
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFD7EBE4)), Alignment.Center) {
                Icon(ic, null, tint = Color(0xFF25718F), modifier = Modifier.size(56.dp))
            }
            Text(title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, textAlign = TextAlign.Center)
            Body(text, align = TextAlign.Center)
            Body("Privacy: your measurements stay on this phone.", color = Color(0xFF25718F), align = TextAlign.Center)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (i == 2) {
                Btn("Set up my profile", { finish(true) }, icon = Icons.Filled.Person)
                Btn("Maybe later", { finish(false) }, primary = false)
            } else Btn("Next", { i++ }, icon = Icons.Filled.ArrowForward)
            if (i > 0) TextButton({ i-- }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Back", fontSize = 16.sp) }
        }
    }
}

@Composable
fun AuthScreen(st: Store, nav: Nav) {
    var create by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    Page(if (create) "Create account" else "Log in", "Account", onBack = { nav.back() }) {
        Body("Your account email and password are stored on this phone only (password is hashed, never shown).")
        Field("Email", email, { email = it }, keyboard = KeyboardType.Email)
        if (create) Field("Name (optional)", name, { name = it })
        OutlinedTextField(pw, { pw = it }, Modifier.fillMaxWidth(), label = { Text("Password (min. 8 characters)", fontSize = 14.sp) },
            singleLine = true, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        err?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
        Btn(if (create) "Create account" else "Log in", {
            val r = if (create) st.register(email, pw, name) else st.login(email, pw)
            if (r != null) err = r else {
                st.msg = if (create) "Account created. You are signed in." else "Welcome back!"
                if (!st.onboarded) { st.setOnboarded(); nav.tab("home") } else nav.back()
            }
        }, enabled = email.isNotBlank() && pw.isNotBlank())
        TextButton({ create = !create; err = null }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(if (create) "I already have an account" else "Create a new account", fontSize = 16.sp)
        }
    }
}

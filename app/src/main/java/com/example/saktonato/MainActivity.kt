package com.example.saktonato

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saktonato.data.Store
import com.example.saktonato.navigation.Nav
import com.example.saktonato.ui.screens.*

val Teal = Color(0xFF25718F)
val Green = Color(0xFF4DB38A)
val Ink = Color(0xFF1E1E1E)
val Cream = Color(0xFFF6F1E7)
val CardBg = Color(0xFFFFFDF8)
val Muted = Color(0xFF454545)

@Composable
fun SaktoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Teal, onPrimary = Color.White, secondary = Green, onSecondary = Ink,
            background = Cream, surface = Cream, onBackground = Ink, onSurface = Ink,
            error = Color(0xFFB3261E)
        ),
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SaktoTheme { SaktoApp() } }
    }
}

@Composable
fun SaktoApp() {
    val ctx = LocalContext.current
    val st = remember { Store(ctx.applicationContext) }
    val nav = remember { Nav(if (st.onboarded) "home" else "welcome") }
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(st.msg) { st.msg?.let { snack.showSnackbar(it); st.msg = null } }
    BackHandler(enabled = nav.stack.size > 1) { nav.back() }
    val showBar = nav.route != "welcome" && nav.route != "onboarding"
    val tabs = listOf(
        Triple<String, String, ImageVector>("home", "Home", Icons.Filled.Home),
        Triple<String, String, ImageVector>("tryon", "Try On", Icons.Filled.CameraAlt),
        Triple<String, String, ImageVector>("saved", "Saved", Icons.Filled.Bookmark),
        Triple<String, String, ImageVector>("profile", "Profile", Icons.Filled.Person)
    )

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            if (showBar) NavigationBar(containerColor = CardBg) {
                tabs.forEach { (r, label, icon) ->
                    NavigationBarItem(
                        selected = nav.stack.first() == r,
                        onClick = { nav.tab(r) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Teal, selectedTextColor = Teal,
                            unselectedIconColor = Ink, unselectedTextColor = Ink,
                            indicatorColor = Color(0xFFD7EBE4)
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (nav.route) {
                "welcome" -> WelcomeScreen(nav)
                "onboarding" -> OnboardingScreen(st, nav)
                "home" -> HomeScreen(st, nav)
                "upload" -> UploadScreen(st, nav)
                "review" -> ReviewScreen(st, nav)
                "verdict" -> VerdictScreen(st, nav)
                "ask" -> AskScreen(st, nav)
                "guide" -> GuideScreen(st, nav)
                "compare" -> CompareScreen(st, nav)
                "tryon" -> TryOnScreen(st, nav)
                "capture" -> CaptureScreen(st, nav)
                "saved" -> SavedScreen(st, nav)
                "card" -> CardScreen(st, nav)
                "seller" -> SellerScreen(st, nav)
                "profile" -> ProfileScreen(st, nav)
                "profileedit" -> ProfileEditScreen(st, nav)
                "auth" -> AuthScreen(st, nav)
                "privacy" -> PrivacyScreen(nav)
            }
            SnackbarHost(snack, Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        }
    }
}

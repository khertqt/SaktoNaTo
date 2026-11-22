package com.example.saktonato.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saktonato.data.*
import com.example.saktonato.navigation.Nav
import com.example.saktonato.ui.components.*
import java.util.UUID
import kotlin.math.floor

@Composable
fun SellerScreen(st: Store, nav: Nav) {
    var adding by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val mine = st.listings.filter { it.mine }
    Page("Seller Tools", "Seller", onBack = { nav.back() }) {
        Body("Add measurements to your own listings so buyers get Verified Fit, then make a Sakto Card.")
        if (mine.isEmpty()) Body("No listings yet.")
        mine.forEach { l ->
            Card2(onClick = { st.setCurrent(l.id); st.sellerMode = true; nav.go("review") }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ListingImage(l, Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(l.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        TierBadge(l.tier)
                        Text(if (l.chestCm != null) "${cmIn(l.chestCm)} chest" else "Tap to add measurements", fontSize = 14.sp, color = Color(0xFF454545))
                    }
                }
            }
        }
        Btn("Add a listing", { adding = true }, icon = Icons.Filled.Add)
        Btn("Seller measuring guide", { st.guideStep = 0; mine.firstOrNull()?.let { st.setCurrent(it.id) }; if (mine.isEmpty()) st.msg = "Add a listing first." else nav.go("guide") }, primary = false, icon = Icons.Filled.Straighten)
    }
    if (adding) AlertDialog(onDismissRequest = { adding = false }, title = { Text("New listing") },
        text = { Field("Item name", newName, { newName = it }) },
        confirmButton = {
            TextButton(enabled = newName.isNotBlank(), onClick = {
                val l = Listing(id = UUID.randomUUID().toString(), name = newName.trim(), seller = "You", source = "My listing", mine = true, color = 0xFF9AA7B0)
                st.update(l); newName = ""; adding = false; st.msg = "Listing added."
            }) { Text("Add") }
        }, dismissButton = { TextButton({ adding = false }) { Text("Cancel") } })
}

@Composable
fun ProfileScreen(st: Store, nav: Nav) {
    val p = st.profile
    var confirmClear by remember { mutableStateOf(false) }
    var camInfo by remember { mutableStateOf(false) }
    var help by remember { mutableStateOf(false) }
    Page("Your space", "Profile") {
        Card2 {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, null, tint = Color(0xFF25718F), modifier = Modifier.size(32.dp)); Spacer(Modifier.width(12.dp))
                Column {
                    Text("Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(if (st.user == null) "Log in or create an account" else "Signed in", fontSize = 14.sp, color = Color(0xFF454545))
                    if (st.user != null) {
                        Text(st.user!!, fontSize = 14.sp)
                        if (st.userName.isNotBlank()) Text(st.userName, fontSize = 14.sp)
                    }
                }
            }
            if (st.user == null) Btn("Log In or Create Account", { nav.go("auth") }, primary = false, icon = Icons.Filled.Login)
            else Btn("Log out", { st.logout(); st.msg = "Logged out. Your measurements are still on this phone." }, primary = false, icon = Icons.Filled.Logout)
        }
        Card2 {
            Text("MEASUREMENT PROFILE", fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
            if (p.heightCm == null) Body("No measurements saved yet.") else {
                Text("Height: ${cmIn(p.heightCm)}", fontSize = 15.sp)
                Text("Usual size: ${p.size.ifBlank { "—" }}", fontSize = 15.sp)
                p.weightKg?.let { Text("Weight: ${cm(it)} kg", fontSize = 15.sp) }
                p.chestCm?.let { Text("Chest: ${cmIn(it)}", fontSize = 15.sp) }
                p.waistCm?.let { Text("Waist: ${cmIn(it)}", fontSize = 15.sp) }
                p.favChest?.let { Text("Favorite shirt chest: ${cmIn(it)}", fontSize = 15.sp) }
                p.favLength?.let { Text("Favorite shirt length: ${cmIn(it)}", fontSize = 15.sp) }
            }
            Btn(if (p.heightCm == null) "Add Measurements" else "Edit Measurements", { nav.go("profileedit") }, primary = false)
        }
        Card2 {
            RowItem(Icons.Filled.Bookmark, "Saved items", "${st.listings.count { it.saved }} saved") { nav.tab("saved") }
            HorizontalDivider()
            RowItem(Icons.Filled.Store, "Seller Tools", "Add measurements, make a Sakto Card") { nav.go("seller") }
            HorizontalDivider()
            RowItem(Icons.Filled.Straighten, "Seller measuring guide", "How to measure a top laid flat") {
                val c = st.current ?: st.listings.firstOrNull()
                if (c == null) st.msg = "Check or add a listing first." else { st.setCurrent(c.id); st.guideStep = 0; nav.go("guide") }
            }
        }
        Head("Privacy")
        Card2 {
            RowItem(Icons.Filled.CameraAlt, "Camera permission", "Only used inside Try On, when you allow it") { camInfo = true }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Security, null, tint = Color(0xFF25718F)); Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Share fit feedback", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Anonymous, only if you opt in", fontSize = 14.sp, color = Color(0xFF454545))
                }
                Switch(st.shareFeedback, { st.shareFeedback = it; st.persistPrefs() })
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, null, tint = Color(0xFF25718F)); Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Keep photos on my phone", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Snapshots are never uploaded", fontSize = 14.sp, color = Color(0xFF454545))
                }
                Switch(st.keepPhotos, { st.keepPhotos = it; st.persistPrefs() })
            }
            HorizontalDivider()
            RowItem(Icons.Filled.Description, "Privacy policy", "How your data is handled") { nav.go("privacy") }
            HorizontalDivider()
            RowItem(Icons.Filled.Delete, "Clear saved data", "Delete profile, saved items and feedback") { confirmClear = true }
        }
        Card2 { RowItem(Icons.Filled.Help, "Help and feedback", "Tips and contact") { help = true } }
        Text("Sakto Na ‘To · TriSync · Prototype v0.1", fontSize = 14.sp, color = Color(0xFF454545), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Simulated features are labeled in the app.", fontSize = 14.sp, color = Color(0xFF454545), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
    if (confirmClear) AlertDialog(onDismissRequest = { confirmClear = false }, title = { Text("Clear saved data?") },
        text = { Text("This deletes your measurements, saved items and feedback from this phone. Your account is not deleted.") },
        confirmButton = { TextButton({ st.clearData(); confirmClear = false; st.msg = "Device data cleared." }) { Text("Clear") } },
        dismissButton = { TextButton({ confirmClear = false }) { Text("Cancel") } })
    if (camInfo) AlertDialog(onDismissRequest = { camInfo = false }, title = { Text("Camera permission") },
        text = { Text("The camera is only used inside Try On, and only after you allow it. Video is never recorded or uploaded. You can change this in your phone's Settings > Apps.") },
        confirmButton = { TextButton({ camInfo = false }) { Text("OK") } })
    if (help) AlertDialog(onDismissRequest = { help = false }, title = { Text("Help and feedback") },
        text = { Text("Start with Check a Listing. If a result says Estimated Fit, ask the seller for chest width and length. This is a prototype, so feedback is not sent anywhere.") },
        confirmButton = { TextButton({ help = false }) { Text("OK") } })
}

@Composable
fun ProfileEditScreen(st: Store, nav: Nav) {
    val p = st.profile
    var unit by remember { mutableStateOf("cm") }
    var cmT by remember { mutableStateOf(p.heightCm?.let { cm(it) } ?: "") }
    var ftT by remember { mutableStateOf("") }
    var inT by remember { mutableStateOf("") }
    var size by remember { mutableStateOf(p.size) }
    var wt by remember { mutableStateOf(p.weightKg?.let { cm(it) } ?: "") }
    var ch by remember { mutableStateOf(p.chestCm?.let { cm(it) } ?: "") }
    var wa by remember { mutableStateOf(p.waistCm?.let { cm(it) } ?: "") }
    var fc by remember { mutableStateOf(p.favChest?.let { cm(it) } ?: "") }
    var fl by remember { mutableStateOf(p.favLength?.let { cm(it) } ?: "") }

    fun toImperial() {
        val c = cmT.toDoubleOrNull() ?: return
        val tot = c / 2.54; val ft = floor(tot / 12).toInt(); val inch = tot - ft * 12
        ftT = ft.toString(); inT = if (inch < 0.05) "" else f1(inch)
    }
    fun toMetric() {
        val ft = ftT.toDoubleOrNull() ?: return
        cmT = cm(ft * 30.48 + (inT.toDoubleOrNull() ?: 0.0) * 2.54)
    }
    val height: Double? = if (unit == "cm") cmT.toDoubleOrNull() else ftT.toDoubleOrNull()?.let { it * 30.48 + (inT.toDoubleOrNull() ?: 0.0) * 2.54 }
    val heightOk = height != null && height in 90.0..230.0
    fun opt(s: String, lo: Double, hi: Double) = s.isBlank() || (s.toDoubleOrNull()?.let { it in lo..hi } == true)
    val allOk = heightOk && size.isNotBlank() && opt(wt, 20.0, 250.0) && opt(ch, 50.0, 200.0) && opt(wa, 40.0, 200.0) && opt(fc, 30.0, 90.0) && opt(fl, 40.0, 100.0)

    Page("Your measurements", "Profile", onBack = { nav.back() }) {
        Body("Height and usual size are required. Everything else is optional and stays on this phone.")
        Text("Height", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = unit == "cm", onClick = { if (unit != "cm") { toMetric(); unit = "cm" } }, label = { Text("Centimeters", fontSize = 15.sp) })
            FilterChip(selected = unit == "ft", onClick = { if (unit != "ft") { toImperial(); unit = "ft" } }, label = { Text("Feet + inches", fontSize = 15.sp) })
        }
        if (unit == "cm") Field("Height (cm)", cmT, { cmT = it.filter { c -> c.isDigit() || c == '.' } }, keyboard = KeyboardType.Decimal)
        else Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Feet (required)", ftT, { ftT = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.weight(1f), KeyboardType.Decimal)
            Field("Inches (optional)", inT, { inT = it.filter { c -> c.isDigit() || c == '.' } }, Modifier.weight(1f), KeyboardType.Decimal)
        }
        if (height != null) Text("= ${cmIn(height)}", fontSize = 15.sp, color = if (heightOk) Color(0xFF25718F) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
        Text("Allowed: ${cmIn(90.0)} to ${cmIn(230.0)}", fontSize = 14.sp, color = Color(0xFF454545))
        Text("Usual clothing size", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SIZES.forEach { s -> FilterChip(selected = size == s, onClick = { size = s }, label = { Text(s, fontSize = 15.sp) }) }
        }
        Card2 {
            Body("Body measurements go around your body. Garment chest width is measured flat, from underarm to underarm. That is only one side, so it is doubled when compared with your body.")
        }
        Field("Weight, optional (kg)", wt, { wt = it.filter { c -> c.isDigit() || c == '.' } }, keyboard = KeyboardType.Decimal)
        NumField("Chest or bust, around the body, optional (cm)", ch, { ch = it }, 50.0, 200.0)
        NumField("Waist, optional (cm)", wa, { wa = it }, 40.0, 200.0)
        NumField("Favorite shirt chest width, flat, optional (cm)", fc, { fc = it }, 30.0, 90.0)
        NumField("Favorite shirt length, optional (cm)", fl, { fl = it }, 40.0, 100.0)
        Btn("Save profile", {
            st.saveProfile(Profile(height, size, wt.toDoubleOrNull(), ch.toDoubleOrNull(), wa.toDoubleOrNull(), fc.toDoubleOrNull(), fl.toDoubleOrNull()))
            st.msg = "Saved. Height ${cmIn(height ?: 0.0)}."
            nav.back()
        }, enabled = allOk)
    }
}

@Composable
fun PrivacyScreen(nav: Nav) {
    Page("Privacy policy", "Profile", onBack = { nav.back() }) {
        Body("Sakto Na ‘To follows Republic Act No. 10173, the Philippine Data Privacy Act of 2012, and its implementing rules.")
        Card2 {
            listOf(
                "Your account email and optional name are stored for login. Passwords are hashed and never displayed.",
                "Logging out does not delete your account.",
                "Measurements, checked listings and saved items stay on this device.",
                "Camera access is optional. Camera video is not recorded or uploaded.",
                "Feedback is only shared if you opt in. In this prototype, nothing is actually transmitted.",
                "You can clear device data any time from Profile > Clear saved data."
            ).forEach { Row { Text("•  ", fontSize = 15.sp); Body(it) } }
        }
    }
}

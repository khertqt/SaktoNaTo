package com.example.saktonato.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saktonato.data.*
import com.example.saktonato.navigation.Nav
import com.example.saktonato.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(st: Store, nav: Nav) {
    val p = st.profile
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF25718F)), Alignment.Center) {
                Icon(Icons.Filled.Straighten, null, tint = Color.White)
            }
            Spacer(Modifier.width(10.dp))
            Text("Kumusta${if (st.userName.isNotBlank()) ", ${st.userName}" else ""}! Sakto kaya sa akin ‘to?",
                fontSize = 15.sp, modifier = Modifier.weight(1f))
            IconButton({ nav.tab("profile") }, Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1E1E1E))) {
                Icon(Icons.Filled.Person, "Profile", tint = Color.White)
            }
        }
        Text("Find clothes that fit you.", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp)
        Btn("Check a Listing", { nav.go("upload") }, icon = Icons.Filled.Upload)
        Card2(onClick = { nav.go("upload") }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Upload, null, tint = Color(0xFF25718F), modifier = Modifier.size(32.dp)); Spacer(Modifier.width(14.dp))
                Column {
                    Text("Upload a screenshot or paste a link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Facebook · Shopee · Instagram · Carousell", fontSize = 14.sp, color = Color(0xFF454545))
                }
            }
        }
        Btn("Try a Saved Item", { nav.tab("tryon") }, primary = false, icon = Icons.Filled.Videocam)
        if (p.heightCm == null) Card2 {
            Text("Add your measurements", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Body("Needed for Estimated and Verified Fit.")
            Btn("Add", { nav.go("profileedit") })
        } else Card2(onClick = { nav.go("profileedit") }) {
            Text("Your measurement profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Body("Height ${cmIn(p.heightCm)} · Size ${p.size.ifBlank { "—" }}" + (p.chestCm?.let { " · Chest ${cmIn(it)}" } ?: ""))
        }
        val recent = st.listings.filter { it.checked.isNotBlank() }
        Head("Recently checked")
        if (recent.isEmpty()) Body("Nothing checked yet.")
        else Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            recent.forEach { l -> MiniCard(l) { st.setCurrent(l.id); nav.go("verdict") } }
        }
        val saved = st.listings.filter { it.saved }
        Head("Saved items")
        if (saved.isEmpty()) Body("Nothing saved yet.")
        else Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            saved.forEach { l -> MiniCard(l) { st.setCurrent(l.id); nav.go("verdict") } }
        }
        Head("How we show confidence")
        Card2 {
            TierBadge(Tier.STYLE); Body("Screenshot only. Shows the look. Makes no fit claim.")
            TierBadge(Tier.ESTIMATED); Body("Size tag only. A rough estimate. Ask the seller to confirm.")
            TierBadge(Tier.VERIFIED); Body("Real chest width and length. Compared with your profile. Still not a guarantee.")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun UploadScreen(st: Store, nav: Nav) {
    var draft by remember { mutableStateOf<Listing?>(null) }
    var link by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val l = st.importImage(uri)
            if (l == null) err = "Could not read that image. Try a PNG or JPG." else { draft = l; err = null }
        }
    }
    Page("Check a listing", "Upload", onBack = { nav.back() }) {
        Card2 {
            Text("Please note", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Body("This prototype does not connect to Facebook, Shopee, Instagram or Carousell, and it cannot see private listings. Pasted links only return sample data.")
        }
        val d = draft
        if (d != null) {
            ListingImage(d, Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(20.dp)))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Btn("Replace", { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, Modifier.weight(1f), primary = false)
                Btn("Remove", { draft = null }, Modifier.weight(1f), primary = false)
            }
            Btn("Continue", { st.update(d); st.setCurrent(d.id); st.sellerMode = false; nav.go("review") }, icon = Icons.Filled.ArrowForward)
        } else {
            Btn("Choose a PNG or JPG screenshot", { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, icon = Icons.Filled.Upload)
        }
        err?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
        Head("Or paste a listing link")
        Field("Listing link (simulated)", link, { link = it; err = null }, keyboard = KeyboardType.Uri)
        Btn(if (loading) "Looking up sample…" else "Use link", {
            if (!link.contains(".")) err = "That does not look like a link. Example: https://shopee.ph/item"
            else scope.launch {
                loading = true; delay(900)
                val s = samples()[abs(link.hashCode()) % 3]
                st.openSample(s); st.sellerMode = false; st.msg = "Simulated link: showing sample data."
                loading = false; nav.go("review")
            }
        }, enabled = link.isNotBlank() && !loading, primary = false)
        Head("Or pick a sample")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            samples().forEach { s -> MiniCard(s) { st.openSample(s); st.sellerMode = false; nav.go("review") } }
        }
    }
}

@Composable
fun ReviewScreen(st: Store, nav: Nav) {
    val l = st.current ?: return EmptyPick(nav)
    var name by remember(l.id) { mutableStateOf(l.name) }
    var size by remember(l.id) { mutableStateOf(l.sizeTag) }
    var chest by remember(l.id) { mutableStateOf(l.chestCm?.let { cm(it) } ?: "") }
    var length by remember(l.id) { mutableStateOf(l.lengthCm?.let { cm(it) } ?: "") }
    var src by remember(l.id) { mutableStateOf(l.measureSource.ifBlank { "Seller listing" }) }
    var prog by remember(l.id) { mutableFloatStateOf(0f) }
    LaunchedEffect(l.id) { while (prog < 1f) { delay(60); prog = (prog + 0.05f).coerceAtMost(1f) } }
    val c = chest.toDoubleOrNull(); val len = length.toDoubleOrNull()
    val chestOk = chest.isBlank() || (c != null && c in 30.0..90.0)
    val lenOk = length.isBlank() || (len != null && len in 40.0..100.0)
    val pairOk = (chest.isBlank() && length.isBlank()) || (c != null && len != null)

    fun save(): Listing {
        val nl = l.copy(name = name.ifBlank { "Untitled item" }, sizeTag = size,
            chestCm = if (chest.isBlank()) null else c, lengthCm = if (length.isBlank()) null else len,
            measureSource = if (chest.isBlank()) "" else src)
        st.update(nl); return nl
    }
    Page(if (st.sellerMode) "Edit listing" else "Review the garment", if (st.sellerMode) "Seller tools" else "Step 2", onBack = { nav.back() }) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Listing screenshot", fontSize = 14.sp)
                ListingImage(l, Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)))
            }
            Column(Modifier.weight(1f)) {
                Text("Garment cutout (simulated)", fontSize = 14.sp)
                Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE5DFD2)), Alignment.Center) {
                    if (prog < 1f) CircularProgressIndicator(progress = { prog }, color = Color(0xFF25718F))
                    else ShirtShape(Color(l.color), Modifier.fillMaxSize(0.7f))
                }
            }
        }
        if (prog < 1f) Body("Removing background… ${(prog * 100).roundToInt()}% (simulated)")
        Body("A screenshot has no reliable physical scale, so measurements are never guessed from the image. Type them in from the seller or measure the garment yourself.")
        Field("Item name", name, { name = it })
        Text("Size tag (optional)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SIZES.forEach { s -> FilterChip(selected = size == s, onClick = { size = if (size == s) "" else s }, label = { Text(s, fontSize = 15.sp) }) }
        }
        NumField("Garment chest width, flat (cm)", chest, { chest = it }, 30.0, 90.0)
        NumField("Garment length (cm)", length, { length = it }, 40.0, 100.0)
        if (c != null) Body("Flat width ${cmIn(c)} goes around about ${cmIn(c * 2)}.")
        if (chest.isNotBlank()) {
            Text("Measurement source", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Seller listing", "Seller message", "I measured it").forEach { s ->
                    FilterChip(selected = src == s, onClick = { src = s }, label = { Text(s, fontSize = 15.sp) })
                }
            }
        }
        if (!pairOk) Text("Add both chest width and length for Verified Fit.", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
        val ok = chestOk && lenOk && pairOk
        if (st.sellerMode) {
            Btn("Save measurements", { save(); st.msg = "Saved." + (if (chest.isNotBlank()) " ${cmIn(c ?: 0.0)} chest." else "") }, enabled = ok)
            Btn("Generate Sakto Card", { save(); nav.go("card") }, enabled = ok, primary = false)
            Btn("Open measuring guide", { st.guideStep = 0; nav.go("guide") }, primary = false, icon = Icons.Filled.Straighten)
        } else {
            Btn("See Fit Verdict", { save(); nav.go("verdict") }, enabled = ok, icon = Icons.Filled.ArrowForward)
            Btn("Ask seller for measurements", { save(); nav.go("ask") }, enabled = ok, primary = false)
            Btn("Open measuring guide", { save(); st.guideStep = 0; nav.go("guide") }, primary = false, icon = Icons.Filled.Straighten)
        }
    }
}

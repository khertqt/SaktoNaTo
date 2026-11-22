package com.example.saktonato.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saktonato.data.*
import com.example.saktonato.domain.computeVerdict
import com.example.saktonato.navigation.Nav
import com.example.saktonato.ui.components.*
import kotlin.math.abs

@Composable
fun VerdictScreen(st: Store, nav: Nav) {
    val l = st.current ?: return EmptyPick(nav)
    var showFb by remember { mutableStateOf(false) }
    LaunchedEffect(l.id) { if (l.checked != today()) st.update(l.copy(checked = today())) }
    val p = st.profile
    val v = computeVerdict(l, p)
    Page("Fit verdict", "Result", onBack = { nav.back() }) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ListingImage(l, Modifier.size(96.dp).clip(RoundedCornerShape(16.dp)))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(l.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(l.source, fontSize = 14.sp, color = Color(0xFF454545))
                TierBadge(l.tier)
            }
        }
        Card2 {
            when (l.tier) {
                Tier.STYLE -> {
                    Head("Style preview only")
                    Body("We only have a screenshot. This shows how the garment could look. It is visual only and makes no claim about fit.")
                }
                else -> {
                    if (v != null) {
                        Text(v.label, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF25718F))
                        Body(v.detail)
                        Text("Evidence: ${v.source}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (l.tier == Tier.ESTIMATED) {
            Card2 {
                Text("Generic sample size chart (flat chest width)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                SIZE_CHART.forEach { (k, w) -> Text("$k: ${cmIn(w)}", fontSize = 14.sp) }
            }
        }
        if (l.tier == Tier.VERIFIED) {
            val chest = l.chestCm ?: 0.0
            val len = l.lengthCm ?: 0.0
            Card2 {
                Text("Measurements", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Garment chest width (flat): ${cmIn(chest)}", fontSize = 15.sp)
                Text("Garment length: ${cmIn(len)}", fontSize = 15.sp)
                Text("Doubled around the garment: ${cmIn(chest * 2)}", fontSize = 15.sp)
                p.chestCm?.let { Text("Your chest: ${cmIn(it)}", fontSize = 15.sp) }
                Text("Source: ${l.measureSource.ifBlank { "Entered by you" }}", fontSize = 14.sp, color = Color(0xFF454545))
                val max = maxOf(chest * 2, p.chestCm ?: 0.0) * 1.1
                Bar("Garment around", chest * 2, max, Color(0xFF25718F))
                p.chestCm?.let { Bar("Your chest", it, max, Color(0xFF4DB38A)) }
            }
        }
        Card2 {
            Text("Limits", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Body(when (l.tier) {
                Tier.STYLE -> "No size or measurements yet, so there is no fit result."
                Tier.ESTIMATED -> "Sizing varies between brands, and preloved clothes may stretch or shrink. Please confirm with the seller."
                Tier.VERIFIED -> "Measurement-based results are still not guarantees. Fabric stretch and your posture also matter."
            })
            if (l.isSample) Body("This listing is sample data.")
        }
        Btn(if (l.saved) "Saved ✓ (tap to unsave)" else "Save item", {
            st.update(l.copy(saved = !l.saved))
            st.msg = if (l.saved) "Removed from saved." else "Saved."
        }, primary = false, icon = if (l.saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder)
        when (l.tier) {
            Tier.STYLE -> {
                Btn("Add size or measurements", { st.sellerMode = false; nav.go("review") }, icon = Icons.Filled.Edit)
                Btn("Try It On (visual only)", { st.tryId = l.id; nav.tab("tryon") }, primary = false)
            }
            Tier.ESTIMATED -> {
                Btn("Ask Seller to Confirm", { nav.go("ask") }, icon = Icons.Filled.Share)
                Btn("Try It On", { st.tryId = l.id; nav.tab("tryon") }, primary = false)
            }
            Tier.VERIFIED -> {
                Btn("Try It On", { st.tryId = l.id; nav.tab("tryon") }, icon = Icons.Filled.CameraAlt)
                Btn("Compare to favorite shirt", { nav.go("compare") }, primary = false)
                Btn("Make Sakto Card", { nav.go("card") }, primary = false)
            }
        }
        TextButton({ showFb = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("How did it fit? Give feedback", fontSize = 16.sp) }
    }
    if (showFb) FeedbackDialog(l, st) { showFb = false }
}

@Composable
fun AskScreen(st: Store, nav: Nav) {
    val ctx = LocalContext.current
    val en = "Hi! I’m interested in this item. Could you please share the garment measurements in cm or inches, especially the chest width and length? Thank you!"
    val bi = "Hi! Interesado ko ani nga item. Pwede mangayo sa garment measurements in cm or inches, labi na ang chest width ug length? Salamat!"
    var lang by remember { mutableStateOf("en") }
    var text by remember { mutableStateOf(en) }
    var copied by remember { mutableStateOf(false) }
    Page("Ask the seller", "Confirm the fit", onBack = { nav.back() }) {
        Body("Pick a language, edit the message if you want, then paste it into your chat with the seller.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = lang == "en", onClick = { lang = "en"; text = en; copied = false }, label = { Text("English", fontSize = 15.sp) })
            FilterChip(selected = lang == "bi", onClick = { lang = "bi"; text = bi; copied = false }, label = { Text("Bisaya", fontSize = 15.sp) })
        }
        Field("Message (editable)", text, { text = it; copied = false }, singleLine = false, minLines = 5)
        Btn(if (copied) "Copied ✓" else "Copy Message", {
            copyText(ctx, text)
            copied = true
            st.msg = "Message copied. Paste it in your chat with the seller."
        }, icon = Icons.Filled.ContentCopy)
        Btn("Open measuring guide", { st.guideStep = 0; nav.go("guide") }, primary = false, icon = Icons.Filled.Straighten)
        Btn("Seller replied? Enter measurements", { st.guideStep = 3; nav.go("guide") }, primary = false)
    }
}

@Composable
fun GuideScreen(st: Store, nav: Nav) {
    val l = st.current ?: return EmptyPick(nav)
    val step = st.guideStep
    var chest by remember(l.id) { mutableStateOf(l.chestCm?.let { cm(it) } ?: "") }
    var length by remember(l.id) { mutableStateOf(l.lengthCm?.let { cm(it) } ?: "") }
    val c = chest.toDoubleOrNull(); val len = length.toDoubleOrNull()
    val chestOk = c != null && c in 30.0..90.0
    val lenOk = len != null && len in 40.0..100.0
    val titles = listOf("Lay the garment flat", "Measure chest width", "Measure garment length", "Enter the numbers", "Review and save")
    Page(titles[step], "Step ${step + 1} of 5", onBack = { nav.back() }) {
        LinearProgressIndicator(progress = { (step + 1) / 5f }, Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = Color(0xFF25718F))
        Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFFFFDF8)), Alignment.Center) {
            ShirtShape(Color(0xFFD9D3C4), Modifier.size(180.dp))
            Canvas(Modifier.size(180.dp)) {
                val hl = Color(0xFF25718F); val dim = Color(0x55000000)
                val chestOn = step == 1 || step >= 3
                val lenOn = step == 2 || step >= 3
                drawLine(if (chestOn) hl else dim, Offset(size.width * 0.24f, size.height * 0.4f), Offset(size.width * 0.76f, size.height * 0.4f), if (chestOn) 8f else 3f)
                drawLine(if (lenOn) hl else dim, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, size.height), if (lenOn) 8f else 3f)
            }
        }
        when (step) {
            0 -> Body("Lay the top flat on a table, smooth out wrinkles, and button or zip it closed.")
            1 -> {
                Body("Measure straight across, from one underarm seam to the other. This is the flat chest width, which is only half of the way around.")
                Body("Example: 52 cm · 20.5 in flat width goes around approximately 104 cm · 40.9 in.", color = Color(0xFF25718F))
            }
            2 -> Body("Measure from the highest point of the shoulder, next to the collar, straight down to the bottom hem.")
            3 -> {
                NumField("Chest width, flat (cm)", chest, { chest = it }, 30.0, 90.0)
                NumField("Garment length (cm)", length, { length = it }, 40.0, 100.0)
                if (c != null) Body("Flat width ${cmIn(c)} goes around approximately ${cmIn(c * 2)}.")
            }
            4 -> {
                if (chestOk && lenOk) {
                    Card2 {
                        Text("Chest width: ${cmIn(c ?: 0.0)}", fontSize = 16.sp)
                        Text("Length: ${cmIn(len ?: 0.0)}", fontSize = 16.sp)
                        Text("Around the garment: ${cmIn((c ?: 0.0) * 2)}", fontSize = 16.sp)
                    }
                } else Text("Go back and enter valid chest width and length.", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 0) Btn("Back", { st.guideStep-- }, Modifier.weight(1f), primary = false)
            if (step < 4) Btn("Next", { st.guideStep++ }, Modifier.weight(1f), enabled = step != 3 || (chestOk && lenOk))
            else Btn("Save", {
                st.update(l.copy(chestCm = c, lengthCm = len, measureSource = if (l.mine) "Seller's own measurements" else "I measured it"))
                st.msg = "Saved: ${cmIn(c ?: 0.0)} chest, ${cmIn(len ?: 0.0)} length."
                nav.back()
            }, Modifier.weight(1f), enabled = chestOk && lenOk)
        }
    }
}

@Composable
fun CompareScreen(st: Store, nav: Nav) {
    val l = st.current ?: return EmptyPick(nav)
    val p = st.profile
    var fc by remember { mutableStateOf(p.favChest?.let { cm(it) } ?: "") }
    var fl by remember { mutableStateOf(p.favLength?.let { cm(it) } ?: "") }
    var lc by remember { mutableStateOf(l.chestCm?.let { cm(it) } ?: "") }
    var ll by remember { mutableStateOf(l.lengthCm?.let { cm(it) } ?: "") }
    val a = fc.toDoubleOrNull(); val b = fl.toDoubleOrNull(); val c = lc.toDoubleOrNull(); val d = ll.toDoubleOrNull()
    Page("Compare to your favorite shirt", "Compare", onBack = { nav.back() }) {
        Body("Lay your favorite shirt flat and measure it the same way as the listing.")
        Head("Favorite shirt")
        NumField("Chest width, flat (cm)", fc, { fc = it }, 30.0, 90.0)
        NumField("Length (cm)", fl, { fl = it }, 40.0, 100.0)
        Head("This listing")
        NumField("Chest width, flat (cm)", lc, { lc = it }, 30.0, 90.0)
        NumField("Length (cm)", ll, { ll = it }, 40.0, 100.0)
        if (a != null && c != null) Card2 {
            val dc = c - a
            Text(if (abs(dc) < 0.05) "Same chest width as your favorite shirt." else "Listing is ${cmIn(abs(dc))} ${if (dc > 0) "wider" else "narrower"}.", fontSize = 16.sp)
            if (b != null && d != null) {
                val dl = d - b
                Text(if (abs(dl) < 0.05) "Same length." else "Listing is ${cmIn(abs(dl))} ${if (dl > 0) "longer" else "shorter"}.", fontSize = 16.sp)
            }
        }
        Btn("Save favorite shirt to my profile", {
            st.saveProfile(p.copy(favChest = a, favLength = b))
            st.msg = if (a != null) "Saved favorite shirt: ${cmIn(a)} chest." else "Cleared favorite shirt."
        }, primary = false, enabled = (fc.isBlank() || a != null) && (fl.isBlank() || b != null))
        Btn("Continue to full verdict", {
            st.saveProfile(st.profile.copy(favChest = a, favLength = b))
            if (c != null && d != null) st.update(l.copy(chestCm = c, lengthCm = d, measureSource = l.measureSource.ifBlank { "Entered by you" }))
            nav.go("verdict")
        }, icon = Icons.Filled.ArrowForward)
    }
}

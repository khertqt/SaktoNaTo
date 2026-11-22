package com.example.saktonato.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saktonato.data.*
import com.example.saktonato.navigation.Nav

@Composable
fun Page(title: String, eyebrow: String? = null, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (onBack != null) TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Icon(Icons.Filled.ArrowBack, null, tint = Color(0xFF25718F)); Spacer(Modifier.width(6.dp))
            Text("Back", color = Color(0xFF25718F), fontSize = 16.sp)
        }
        if (eyebrow != null) Text(eyebrow.uppercase(), color = Color(0xFF25718F), fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.5.sp)
        Text(title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Color(0xFF1E1E1E), lineHeight = 36.sp)
        content()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun Body(t: String, color: Color = Color(0xFF454545), align: TextAlign? = null) =
    Text(t, fontSize = 15.sp, color = color, lineHeight = 22.sp, textAlign = align)

@Composable
fun Head(t: String) =
    Text(t, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1E1E1E))

@Composable
fun Btn(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null,
        enabled: Boolean = true, primary: Boolean = true) {
    val m = modifier.fillMaxWidth().heightIn(min = 52.dp)
    if (primary) Button(onClick, m, enabled, shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25718F), contentColor = Color.White)) { BtnRow(text, icon) }
    else OutlinedButton(onClick, m, enabled, shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (enabled) Color(0xFF25718F) else Color.Gray),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25718F))) { BtnRow(text, icon) }
}

@Composable
private fun BtnRow(text: String, icon: ImageVector?) {
    if (icon != null) { Icon(icon, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun Card2(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val m = modifier.fillMaxWidth()
    val colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8))
    val inner: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
    if (onClick != null) Card(onClick = onClick, modifier = m, shape = RoundedCornerShape(22.dp), colors = colors, content = inner)
    else Card(modifier = m, shape = RoundedCornerShape(22.dp), colors = colors, content = inner)
}

@Composable
fun TierBadge(t: Tier) {
    val bg: Color
    val fg: Color
    val ic: ImageVector
    when (t) {
        Tier.STYLE -> {
            bg = Color(0xFFDCE9F5)
            fg = Color(0xFF1F4E79)
            ic = Icons.Filled.Visibility
        }
        Tier.ESTIMATED -> {
            bg = Color(0xFFFBE3B5)
            fg = Color(0xFF6B4300)
            ic = Icons.Filled.Straighten
        }
        Tier.VERIFIED -> {
            bg = Color(0xFF4DB38A)
            fg = Color(0xFF1E1E1E)
            ic = Icons.Filled.VerifiedUser
        }
    }
    Row(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(ic, null, tint = fg, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
        Text(t.label, color = fg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun NumField(label: String, value: String, onChange: (String) -> Unit, min: Double, max: Double) {
    val v = value.toDoubleOrNull()
    val bad = value.isNotBlank() && (v == null || v < min || v > max)
    OutlinedTextField(
        value = value, onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label, fontSize = 14.sp) }, singleLine = true, isError = bad,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            Text(when {
                bad -> "Enter between ${cmIn(min)} and ${cmIn(max)}"
                v != null -> cmIn(v)
                else -> "Allowed: ${cmIn(min)} to ${cmIn(max)}"
            }, fontSize = 14.sp)
        }
    )
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier,
          keyboard: KeyboardType = KeyboardType.Text, singleLine: Boolean = true, minLines: Int = 1) {
    OutlinedTextField(value, onChange, modifier.fillMaxWidth(), label = { Text(label, fontSize = 14.sp) },
        singleLine = singleLine, minLines = minLines,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard))
}

@Composable
fun RowItem(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF25718F)); Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1E1E1E))
            Text(sub, fontSize = 14.sp, color = Color(0xFF454545))
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF1E1E1E))
    }
}

@Composable
fun ShirtShape(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val p = Path().apply {
            moveTo(w * 0.30f, 0f); lineTo(w * 0.40f, 0f)
            quadraticTo(w * 0.5f, h * 0.12f, w * 0.60f, 0f)
            lineTo(w * 0.70f, 0f); lineTo(w, h * 0.22f); lineTo(w * 0.86f, h * 0.38f)
            lineTo(w * 0.76f, h * 0.30f); lineTo(w * 0.76f, h); lineTo(w * 0.24f, h)
            lineTo(w * 0.24f, h * 0.30f); lineTo(w * 0.14f, h * 0.38f); lineTo(0f, h * 0.22f); close()
        }
        drawPath(p, color)
    }
}

@Composable
fun GarmentGraphicScaled(color: Color, dims: GarmentDimensions?, modifier: Modifier = Modifier) {
    val chestRatio = if (dims != null) (dims.chestWidthCm / 52.0).toFloat().coerceIn(0.7f, 1.4f) else 1f
    val shoulderRatio = if (dims != null) (dims.shoulderWidthCm / 46.0).toFloat().coerceIn(0.7f, 1.4f) else 1f
    val lengthRatio = if (dims != null) (dims.lengthCm / 68.0).toFloat().coerceIn(0.7f, 1.4f) else 1f

    Canvas(modifier) {
        val w = size.width * chestRatio; val h = size.height * lengthRatio
        val sw = size.width * shoulderRatio
        val p = Path().apply {
            moveTo(w * 0.30f, 0f); lineTo(w * 0.40f, 0f)
            quadraticTo(w * 0.5f, h * 0.12f, w * 0.60f, 0f)
            lineTo(w * 0.70f, 0f); lineTo(sw, h * 0.22f); lineTo(sw * 0.86f, h * 0.38f)
            lineTo(w * 0.76f, h * 0.30f); lineTo(w * 0.76f, h); lineTo(w * 0.24f, h)
            lineTo(w * 0.24f, h * 0.30f); lineTo(sw * 0.14f, h * 0.38f); lineTo(0f, h * 0.22f); close()
        }
        drawPath(p, color)
    }
}

@Composable
fun TrackingConfidenceBadge(confidence: Float) {
    val (bg, fg, label) = when {
        confidence >= 0.8f -> Triple(Color(0xFF4DB38A), Color(0xFF1E1E1E), "High Tracking Confidence")
        confidence >= 0.5f -> Triple(Color(0xFFFBE3B5), Color(0xFF6B4300), "Moderate Tracking")
        else -> Triple(Color(0xFFFFCDD2), Color(0xFFB71C1C), "Low Tracking — Stand 1-2m in view")
    }
    Row(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = fg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun CalibrationOverlay() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val rectWidth = w * 0.55f
        val rectHeight = h * 0.65f
        val left = (w - rectWidth) / 2f
        val top = (h - rectHeight) / 2f
        // Draw dashed guide box for upper body
        drawRect(
            color = Color(0xAA25718F),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 4f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
            )
        )
    }
}

@Composable
fun ListingImage(l: Listing, modifier: Modifier = Modifier) {
    val bmp = remember(l.imageUri) {
        l.imageUri?.let { p ->
            runCatching { BitmapFactory.decodeFile(p, BitmapFactory.Options().apply { inSampleSize = 2 })?.asImageBitmap() }.getOrNull()
        }
    }
    if (bmp != null) Image(bmp, l.name, modifier, contentScale = ContentScale.Crop)
    else Box(modifier.background(Brush.verticalGradient(listOf(Color(0xFF4A555C), Color(0xFF1F2A30)))), Alignment.Center) {
        ShirtShape(Color(l.color), Modifier.fillMaxWidth(0.6f).aspectRatio(1f))
    }
}

@Composable
fun MiniCard(l: Listing, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(160.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8))) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ListingImage(l, Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(14.dp)))
            TierBadge(l.tier)
            Text(l.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E), maxLines = 2)
        }
    }
}

@Composable
fun Bar(label: String, v: Double, max: Double, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$label: ${cmIn(v)}", fontSize = 14.sp, color = Color(0xFF1E1E1E))
        Box(Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE5DFD2))) {
            Box(Modifier.fillMaxWidth((v / max).toFloat().coerceIn(0.03f, 1f)).fillMaxHeight().background(color))
        }
    }
}

@Composable
fun EmptyPick(nav: Nav, text: String = "Pick a listing first.") {
    Page("Nothing here yet") {
        Body(text)
        Btn("Check a Listing", { nav.go("upload") }, icon = Icons.Filled.Upload)
    }
}

fun copyText(ctx: Context, text: String) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("sakto", text))
}

@Composable
fun ShareDialog(onDone: () -> Unit, st: Store) {
    AlertDialog(onDismissRequest = onDone,
        title = { Text("Share (simulated)") },
        text = {
            Column {
                Body("This is a prototype share sheet. Nothing is actually sent.")
                listOf("Messenger", "Facebook", "Instagram", "Save to gallery").forEach { app ->
                    TextButton({ st.msg = "Simulated: nothing was sent to $app."; onDone() },
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(app, fontSize = 16.sp) }
                }
            }
        },
        confirmButton = { TextButton(onDone) { Text("Close") } })
}

@Composable
fun FeedbackDialog(l: Listing, st: Store, onDone: () -> Unit) {
    var choice by remember { mutableStateOf(l.feedback) }
    var consent by remember { mutableStateOf(st.shareFeedback) }
    val opts = listOf("expected" to "Fit as expected", "small" to "Ran small", "large" to "Ran large")
    AlertDialog(onDismissRequest = onDone,
        title = { Text("How did it fit?") },
        text = {
            Column {
                opts.forEach { (k, label) ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .selectable(selected = choice == k, onClick = { choice = k }, role = Role.RadioButton),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = choice == k, onClick = null); Spacer(Modifier.width(10.dp))
                        Text(label, fontSize = 16.sp)
                    }
                }
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { consent = !consent },
                    verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(consent, null); Spacer(Modifier.width(10.dp))
                    Text("Share anonymously (optional)", fontSize = 15.sp)
                }
                Body("Prototype note: your feedback is saved on this phone only. It is not sent anywhere and does not train anything.")
            }
        },
        confirmButton = {
            TextButton(enabled = choice.isNotBlank(), onClick = {
                st.update(l.copy(feedback = choice))
                st.shareFeedback = consent
                st.persistPrefs()
                st.msg = "Feedback saved on this phone. Nothing was sent."
                onDone()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onDone) { Text("Cancel") } })
}

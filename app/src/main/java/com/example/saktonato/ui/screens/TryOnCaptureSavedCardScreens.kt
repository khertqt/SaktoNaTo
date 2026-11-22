package com.example.saktonato.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.saktonato.data.*
import com.example.saktonato.navigation.Nav
import com.example.saktonato.ui.components.*
import kotlin.math.roundToInt

@Composable
fun SampleModel() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFFCFE3EA), Color(0xFFEFE6D6))))
        val cx = size.width / 2
        drawCircle(Color(0xFFC99B7B), size.width * 0.11f, Offset(cx, size.height * 0.2f))
        drawRoundRect(Color(0xFFC99B7B), Offset(cx - size.width * 0.04f, size.height * 0.27f), androidx.compose.ui.geometry.Size(size.width * 0.08f, size.height * 0.06f))
        drawRoundRect(Color(0xFFB88768), Offset(cx - size.width * 0.27f, size.height * 0.32f),
            androidx.compose.ui.geometry.Size(size.width * 0.54f, size.height * 0.68f), androidx.compose.ui.geometry.CornerRadius(80f, 80f))
    }
}

@Composable
fun GarmentGraphic(l: Listing, selectedSize: String, modifier: Modifier) {
    val dims = l.sizeChart[selectedSize.uppercase()]
    GarmentGraphicScaled(Color(l.color).copy(alpha = 0.92f), dims, modifier)
}

@Composable
fun Stage(bg: ImageBitmap?, cam: (@Composable () -> Unit)?, l: Listing, fx: Float, fy: Float, sc: Float,
          selectedSize: String, showCalibration: Boolean,
          modifier: Modifier = Modifier, onGesture: ((Offset, Float) -> Unit)? = null) {
    var box by remember { mutableStateOf(IntSize(1, 1)) }
    val d = LocalDensity.current
    val g by rememberUpdatedState(onGesture)
    Box(modifier.fillMaxWidth().aspectRatio(3f / 4f).clip(RoundedCornerShape(24.dp)).background(Color(0xFF2B3A42))
        .onSizeChanged { box = it }
        .pointerInput(onGesture != null) {
            if (onGesture != null) detectTransformGestures { _, pan, zoom, _ -> g?.invoke(Offset(pan.x / box.width, pan.y / box.height), zoom) }
        }) {
        if (cam != null) {
            cam()
            if (showCalibration) CalibrationOverlay()
        } else if (bg != null) {
            Image(bg, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            SampleModel()
        }
        val w = box.width * 0.6f * sc
        val wDp = with(d) { w.toDp() }
        Box(Modifier.align(Alignment.Center).offset { IntOffset((fx * box.width).roundToInt(), (fy * box.height).roundToInt()) }.size(wDp, wDp * 1.1f)) {
            GarmentGraphic(l, selectedSize, Modifier.fillMaxSize())
        }
    }
}

@Composable
fun CameraPreview(onView: (PreviewView) -> Unit) {
    val ctx = LocalContext.current
    AndroidView(factory = { c ->
        val pv = PreviewView(c).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE }
        val fut = ProcessCameraProvider.getInstance(c)
        fut.addListener({
            runCatching {
                val prov = fut.get()
                val prev = Preview.Builder().build().also { it.setSurfaceProvider(pv.surfaceProvider) }
                prov.unbindAll()
                prov.bindToLifecycle(ctx as androidx.lifecycle.LifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, prev)
            }
        }, ContextCompat.getMainExecutor(c))
        onView(pv); pv
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun TryOnScreen(st: Store, nav: Nav) {
    var started by remember { mutableStateOf(false) }
    val tops = st.listings
    if (!started) {
        Page("Pick a top to preview", "Try on") {
            Body("The prototype works with tops only. The camera stays off until you allow it.")
            if (tops.isEmpty()) {
                Body("No items yet. Check a listing first.")
                Btn("Check a Listing", { nav.go("upload") })
            } else {
                tops.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { l ->
                            Card(onClick = { st.tryId = l.id }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8)),
                                border = if (st.tryId == l.id) BorderStroke(3.dp, Color(0xFF25718F)) else null) {
                                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ListingImage(l, Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(14.dp)))
                                    TierBadge(l.tier)
                                    Text(l.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                Btn("Start Try On", { started = true }, enabled = st.tryId != null && tops.any { it.id == st.tryId }, icon = Icons.Filled.CameraAlt)
            }
        }
        return
    }
    val l = tops.firstOrNull { it.id == st.tryId } ?: run { started = false; return }
    val ctx = LocalContext.current
    var hasCam by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var useCam by remember { mutableStateOf(false) }
    var selectedSize by remember(l.id) { mutableStateOf(l.sizeTag.ifBlank { st.profile.size.ifBlank { "M" } }) }
    var showCalibration by remember { mutableStateOf(true) }
    var showComparison by remember { mutableStateOf(false) }
    val trackingConfidence by remember { mutableStateOf(0.88f) }

    val dims = l.sizeChart[selectedSize.uppercase()] ?: GarmentDimensions(52.0, 46.0, 68.0, 20.0, 18.0, 52.0)
    val startScale = ((dims.chestWidthCm) / 52.0).toFloat().coerceIn(0.6f, 1.5f)
    var fx by remember(l.id) { mutableFloatStateOf(0f) }
    var fy by remember(l.id) { mutableFloatStateOf(0.12f) }
    var sc by remember(l.id, selectedSize) { mutableFloatStateOf(startScale) }
    var pv by remember { mutableStateOf<PreviewView?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        hasCam = ok; useCam = ok
        if (!ok) st.msg = "Camera not allowed. Using the sample model."
    }

    val verdict = com.example.saktonato.domain.computeVerdict(l, st.profile, selectedSize)

    Page("Try on & Fit Analysis", "${l.brand} • ${l.name}", onBack = { started = false }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TierBadge(l.tier)
            TrackingConfidenceBadge(trackingConfidence)
        }
        Stage(null, if (useCam) ({ CameraPreview { pv = it } }) else null, l, fx, fy, sc, selectedSize, showCalibration,
            onGesture = { pan, zoom -> fx += pan.x; fy += pan.y; sc = (sc * zoom).coerceIn(0.4f, 2.5f) })

        Card2 {
            Text("Selected Size: $selectedSize", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SIZES.forEach { sz ->
                    FilterChip(
                        selected = selectedSize == sz,
                        onClick = { selectedSize = sz },
                        label = { Text(sz, fontSize = 14.sp) }
                    )
                }
            }
            if (verdict != null) {
                Spacer(Modifier.height(4.dp))
                Text("Fit Verdict: ${verdict.label}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF25718F))
                Text(verdict.detail, fontSize = 13.sp, color = Color(0xFF454545), lineHeight = 18.sp)
            }
            Text("Garment Dimensions ($selectedSize): Chest ${cmIn(dims.chestWidthCm)} • Length ${cmIn(dims.lengthCm)} • Shoulders ${cmIn(dims.shoulderWidthCm)}", fontSize = 13.sp, color = Color(0xFF454545))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (useCam) Btn("Sample model", { useCam = false }, Modifier.weight(1f), primary = false)
            else Btn("Use camera", { if (hasCam) { useCam = true; showCalibration = false } else launcher.launch(Manifest.permission.CAMERA) }, Modifier.weight(1f), primary = false, icon = Icons.Filled.Videocam)
            Btn("Side-by-side comparison", { showComparison = true }, Modifier.weight(1f), primary = false, icon = Icons.Filled.Straighten)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Btn("Reset", { fx = 0f; fy = 0.12f; sc = startScale }, Modifier.weight(1f), primary = false, icon = Icons.Filled.Refresh)
            Btn("Toggle Guide", { showCalibration = !showCalibration }, Modifier.weight(1f), primary = false)
        }
        Body("Stand 1-2m away facing camera. The 3D model adapts non-uniformly to body shape, shoulder width, and product size chart dimensions.")
        Btn("Take snapshot", {
            st.capture = Capture(if (useCam) pv?.bitmap?.asImageBitmap() else null, fx, fy, sc, selectedSize)
            nav.go("capture")
        }, icon = Icons.Filled.CameraAlt)
    }

    if (showComparison) {
        AlertDialog(
            onDismissRequest = { showComparison = false },
            title = { Text("Size Comparison (${l.name})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Body("Compare flat chest widths across sizes for ${l.brand}:")
                    SIZES.forEach { sz ->
                        val dSize = l.sizeChart[sz]
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Size $sz", fontWeight = if (selectedSize == sz) FontWeight.Bold else FontWeight.Normal)
                            Text(if (dSize != null) "${cmIn(dSize.chestWidthCm)} chest" else "N/A")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton({ showComparison = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun CaptureScreen(st: Store, nav: Nav) {
    val l = st.listings.firstOrNull { it.id == st.tryId } ?: return EmptyPick(nav)
    val cap = st.capture ?: return EmptyPick(nav, "Take a snapshot first.")
    var share by remember { mutableStateOf(false) }
    Page("Your snapshot", "Preview", onBack = { nav.back() }) {
        Stage(cap.bmp, null, l, cap.fx, cap.fy, cap.scale, cap.selectedSize, false)
        TierBadge(l.tier)
        Body(when (l.tier) {
            Tier.STYLE -> "Based on a screenshot only. This is a visual preview with no fit claim."
            Tier.ESTIMATED -> "Based on a size tag (${l.sizeTag}) only. This is a visual preview, not a confirmed fit."
            Tier.VERIFIED -> "Based on listing measurements (${cmIn(l.chestCm ?: 0.0)} chest, ${cmIn(l.lengthCm ?: 0.0)} length). The placement is still set by hand, so this is not a guarantee."
        })
        Btn(if (l.saved) "Saved ✓" else "Save item", { st.update(l.copy(saved = true)); st.msg = "Saved." }, enabled = !l.saved, icon = Icons.Filled.Bookmark)
        Btn("Share preview", { share = true }, primary = false, icon = Icons.Filled.Share)
        Btn("Retake", { nav.back() }, primary = false)
        Btn("Check another item", { nav.go("upload") }, primary = false)
    }
    if (share) ShareDialog({ share = false }, st)
}

@Composable
fun SavedScreen(st: Store, nav: Nav) {
    val saved = st.listings.filter { it.saved }
    var fb by remember { mutableStateOf<Listing?>(null) }
    Page("My items", "Saved") {
        if (saved.isEmpty()) Card2 {
            Text("Nothing saved yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Body("Check a listing, then tap Save item to keep it here.")
            Btn("Check a Listing", { nav.go("upload") })
        }
        saved.forEach { l ->
            Card2 {
                ListingImage(l, Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(16.dp)))
                TierBadge(l.tier)
                Text(l.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                    Text(if (l.checked.isBlank()) "Not checked yet" else "Checked ${l.checked}", fontSize = 14.sp, color = Color(0xFF454545))
                }
                if (l.feedback.isNotBlank()) Text("Your feedback: " + when (l.feedback) { "small" -> "ran small"; "large" -> "ran large"; else -> "fit as expected" }, fontSize = 14.sp, color = Color(0xFF25718F))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Btn("Verdict", { st.setCurrent(l.id); nav.go("verdict") }, Modifier.weight(1f), primary = false)
                    Btn("Try on", { st.tryId = l.id; nav.tab("tryon") }, Modifier.weight(1f), primary = false)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton({ fb = l }, Modifier.heightIn(min = 48.dp)) { Text("How did it fit?", fontSize = 15.sp) }
                    IconButton({ st.update(l.copy(saved = false)); st.msg = "Removed from saved." }, Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Delete, "Remove ${l.name} from saved", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
    fb?.let { FeedbackDialog(it, st) { fb = null } }
}

@Composable
fun CardScreen(st: Store, nav: Nav) {
    val ctx = LocalContext.current
    val l = st.current ?: return EmptyPick(nav)
    var share by remember { mutableStateOf(false) }
    val link = "saktonato.example/prototype/${l.id.take(6)}"
    Page("Sakto Card", "Share", onBack = { nav.back() }) {
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color(0xFF25718F))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF25718F)), Alignment.Center) { Icon(Icons.Filled.Straighten, null, tint = Color.White) }
                    Spacer(Modifier.width(10.dp))
                    Text("Sakto Na ‘To", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                ListingImage(l, Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)))
                Text(l.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Seller: ${l.seller}", fontSize = 15.sp)
                TierBadge(l.tier)
                if (l.chestCm != null) Text("Chest width: ${cmIn(l.chestCm)}", fontSize = 15.sp)
                if (l.lengthCm != null) Text("Length: ${cmIn(l.lengthCm)}", fontSize = 15.sp)
                if (l.chestCm == null) Text("No measurements yet.", fontSize = 15.sp)
                if (l.isSample) Text("Sample data, not a real listing.", fontSize = 14.sp, color = Color(0xFF454545))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Canvas(Modifier.size(84.dp)) {
                        val n = 21; val cell = size.width / n
                        for (x in 0 until n) for (y in 0 until n) {
                            val finder = (x < 7 && y < 7) || (x > 13 && y < 7) || (x < 7 && y > 13)
                            val on = if (finder) (x % 6 == 0 || y % 6 == 0 || (x % 7 in 2..4 && y % 7 in 2..4)) else (x * 7 + y * 13 + x * y) % 3 == 0
                            if (on) drawRect(Color.Black, Offset(x * cell, y * cell), androidx.compose.ui.geometry.Size(cell, cell))
                        }
                    }
                    Column {
                        Text("Placeholder QR", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(link, fontSize = 14.sp, color = Color(0xFF25718F))
                    }
                }
            }
        }
        Btn("Share", { share = true }, icon = Icons.Filled.Share)
        Btn("Copy link", { copyText(ctx, "https://$link"); st.msg = "Prototype link copied." }, primary = false, icon = Icons.Filled.ContentCopy)
    }
    if (share) ShareDialog({ share = false }, st)
}

package com.example.saktonato.data

import androidx.compose.ui.graphics.ImageBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

// ===================== HELPERS =====================
fun f1(x: Double): String = String.format(Locale.US, "%.1f", x)
fun cm(x: Double): String = if (abs(x - x.roundToInt()) < 0.05) x.roundToInt().toString() else f1(x)
fun cmIn(x: Double): String = "${cm(x)} cm · ${f1(x / 2.54)} in"
fun today(): String = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())

enum class Tier(val label: String) {
    STYLE("Style Preview"),
    ESTIMATED("Estimated Fit"),
    VERIFIED("Verified Fit")
}

data class GarmentDimensions(
    val chestWidthCm: Double,
    val shoulderWidthCm: Double,
    val lengthCm: Double,
    val sleeveLengthCm: Double,
    val sleeveOpeningCm: Double,
    val hemWidthCm: Double
)

enum class FitType {
    SLIM, REGULAR, RELAXED, OVERSIZED, BOXU
}

enum class FabricType {
    COTTON_LIGHT, COTTON_HEAVY, POLYESTER_BLEND, DENIM, STRETCH_COTTON
}

data class Listing(
    val id: String,
    val name: String,
    val seller: String = "Unknown seller",
    val source: String = "Screenshot",
    val color: Long = 0xFFB0BEC5,
    val sizeTag: String = "",
    val chestCm: Double? = null,
    val lengthCm: Double? = null,
    val imageUri: String? = null,
    val isSample: Boolean = false,
    val saved: Boolean = false,
    val checked: String = "",
    val feedback: String = "",
    val mine: Boolean = false,
    val measureSource: String = "",
    val brand: String = "Generic Brand",
    val fitType: FitType = FitType.REGULAR,
    val fabricType: FabricType = FabricType.COTTON_LIGHT,
    val sizeChart: Map<String, GarmentDimensions> = mapOf(
        "XS" to GarmentDimensions(46.0, 42.0, 64.0, 18.0, 16.0, 46.0),
        "S" to GarmentDimensions(49.0, 44.0, 66.0, 19.0, 17.0, 49.0),
        "M" to GarmentDimensions(52.0, 46.0, 68.0, 20.0, 18.0, 52.0),
        "L" to GarmentDimensions(55.0, 48.0, 70.0, 21.0, 19.0, 55.0),
        "XL" to GarmentDimensions(58.0, 50.0, 72.0, 22.0, 20.0, 58.0),
        "XXL" to GarmentDimensions(61.0, 52.0, 74.0, 23.0, 21.0, 61.0)
    )
)

val Listing.tier: Tier
    get() = when {
        chestCm != null && lengthCm != null -> Tier.VERIFIED
        sizeTag.isNotBlank() -> Tier.ESTIMATED
        else -> Tier.STYLE
    }

data class BodyMeasurements(
    val shoulderWidthCm: Double? = null,
    val chestCircumferenceCm: Double? = null,
    val waistCircumferenceCm: Double? = null,
    val torsoLengthCm: Double? = null,
    val sleeveLengthCm: Double? = null,
    val neckCm: Double? = null,
    val upperArmCircumferenceCm: Double? = null
)

data class Profile(
    val heightCm: Double? = null,
    val size: String = "",
    val weightKg: Double? = null,
    val chestCm: Double? = null,
    val waistCm: Double? = null,
    val favChest: Double? = null,
    val favLength: Double? = null,
    val bodyMeasurements: BodyMeasurements = BodyMeasurements()
)

data class Capture(val bmp: ImageBitmap?, val fx: Float, val fy: Float, val scale: Float, val selectedSize: String = "M")

val SIZES = listOf("XS", "S", "M", "L", "XL", "XXL")
val SIZE_CHART = mapOf("XS" to 46.0, "S" to 49.0, "M" to 52.0, "L" to 55.0, "XL" to 58.0, "XXL" to 61.0)

fun samples(): List<Listing> = listOf(
    Listing(
        id = "s1",
        name = "Plain white boxy tee",
        seller = "Sample seller",
        source = "Sample · Carousell",
        color = 0xFFE6E6E6,
        isSample = true,
        checked = "Oct 5, 2026",
        brand = "Uniqlo U",
        fitType = FitType.BOXU,
        fabricType = FabricType.COTTON_HEAVY,
        sizeChart = mapOf(
            "XS" to GarmentDimensions(48.0, 44.0, 63.0, 19.0, 17.0, 48.0),
            "S" to GarmentDimensions(51.0, 46.0, 65.0, 20.0, 18.0, 51.0),
            "M" to GarmentDimensions(54.0, 48.0, 67.0, 21.0, 19.0, 54.0),
            "L" to GarmentDimensions(57.0, 50.0, 69.0, 22.0, 20.0, 57.0),
            "XL" to GarmentDimensions(60.0, 52.0, 71.0, 23.0, 21.0, 60.0),
            "XXL" to GarmentDimensions(63.0, 54.0, 73.0, 24.0, 22.0, 63.0)
        )
    ),
    Listing(
        id = "s2",
        name = "Cotton crew-neck tee, tag M",
        seller = "Sample seller",
        source = "Sample · Shopee",
        color = 0xFFF2F2F2,
        sizeTag = "M",
        isSample = true,
        saved = true,
        checked = "Oct 7, 2026",
        brand = "H&M",
        fitType = FitType.SLIM,
        fabricType = FabricType.COTTON_LIGHT,
        sizeChart = mapOf(
            "XS" to GarmentDimensions(44.0, 40.0, 62.0, 17.0, 15.0, 44.0),
            "S" to GarmentDimensions(47.0, 42.0, 64.0, 18.0, 16.0, 47.0),
            "M" to GarmentDimensions(50.0, 44.0, 66.0, 19.0, 17.0, 50.0),
            "L" to GarmentDimensions(53.0, 46.0, 68.0, 20.0, 18.0, 53.0),
            "XL" to GarmentDimensions(56.0, 48.0, 70.0, 21.0, 19.0, 56.0),
            "XXL" to GarmentDimensions(59.0, 50.0, 72.0, 22.0, 20.0, 59.0)
        )
    ),
    Listing(
        id = "s3",
        name = "Washed denim jacket",
        seller = "Sample seller",
        source = "Sample · Facebook",
        color = 0xFF3B5B85,
        sizeTag = "L",
        chestCm = 58.0,
        lengthCm = 66.0,
        isSample = true,
        saved = true,
        checked = "Oct 9, 2026",
        measureSource = "Seller listing (sample data)",
        brand = "Levi's",
        fitType = FitType.RELAXED,
        fabricType = FabricType.DENIM,
        sizeChart = mapOf(
            "XS" to GarmentDimensions(50.0, 46.0, 62.0, 58.0, 12.0, 48.0),
            "S" to GarmentDimensions(53.0, 48.0, 64.0, 59.0, 13.0, 51.0),
            "M" to GarmentDimensions(56.0, 50.0, 66.0, 60.0, 14.0, 54.0),
            "L" to GarmentDimensions(59.0, 52.0, 68.0, 61.0, 15.0, 57.0),
            "XL" to GarmentDimensions(62.0, 54.0, 70.0, 62.0, 16.0, 60.0),
            "XXL" to GarmentDimensions(65.0, 56.0, 72.0, 63.0, 17.0, 63.0)
        )
    )
)

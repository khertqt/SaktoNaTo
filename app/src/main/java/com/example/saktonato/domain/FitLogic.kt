package com.example.saktonato.domain

import com.example.saktonato.data.*
import kotlin.math.abs

data class Verdict(val label: String, val detail: String, val source: String)

fun easeLabel(ease: Double): String = when {
    ease < -2 -> "Likely too tight"
    ease < 3 -> "Slim fit"
    ease < 8 -> "Regular fit"
    ease < 15 -> "Relaxed fit"
    else -> "Roomy / oversized"
}

fun computeVerdict(l: Listing, p: Profile, selectedSize: String = "M"): Verdict? {
    val effectiveSize = selectedSize.ifBlank { l.sizeTag.ifBlank { p.size.ifBlank { "M" } } }
    val dims = l.sizeChart[effectiveSize.uppercase()] ?: GarmentDimensions(
        chestWidthCm = l.chestCm ?: 52.0,
        shoulderWidthCm = 46.0,
        lengthCm = l.lengthCm ?: 68.0,
        sleeveLengthCm = 20.0,
        sleeveOpeningCm = 18.0,
        hemWidthCm = 52.0
    )
    val chest = dims.chestWidthCm
    val circ = chest * 2

    when (l.tier) {
        Tier.STYLE -> {
            if (l.sizeChart.containsKey(effectiveSize.uppercase())) {
                val userChest = p.chestCm ?: p.bodyMeasurements.chestCircumferenceCm
                if (userChest != null) {
                    val ease = circ - userChest
                    return Verdict(
                        easeLabel(ease),
                        "Using size $effectiveSize dimensions (${cmIn(chest)} flat chest, ${cmIn(circ)} around). Your chest is ${cmIn(userChest)}. Ease: ${cmIn(ease)}.",
                        "Product size chart ($effectiveSize) vs your chest"
                    )
                }
            }
            return null
        }
        Tier.ESTIMATED -> {
            val a = SIZES.indexOf(effectiveSize.uppercase())
            val b = SIZES.indexOf(p.size.uppercase())
            val userChest = p.chestCm ?: p.bodyMeasurements.chestCircumferenceCm
            if (userChest != null) {
                val ease = circ - userChest
                return Verdict(
                    "${easeLabel(ease)} (Size $effectiveSize)",
                    "Evaluated size $effectiveSize from ${l.brand}'s product size chart (${cmIn(circ)} chest circumference) against your chest (${cmIn(userChest)}).",
                    "Product-specific size chart ($effectiveSize)"
                )
            }
            if (a < 0 || b < 0) return Verdict(
                "Not enough info",
                "Add your usual size in your profile, and select a size from XS to XXL, to get an estimate.",
                "Size tag only"
            )
            val label = when (a - b) {
                0 -> "Regular fit"
                -1 -> "Slim fit"
                1 -> "Relaxed fit"
                else -> if (a < b) "Likely too tight" else "Roomy / oversized"
            }
            return Verdict(
                "$label (estimate)",
                "The selected size is $effectiveSize and your usual size is ${p.size}. This uses ${l.brand}'s size chart.",
                "Product size chart + user profile"
            )
        }
        Tier.VERIFIED -> {
            val userChest = p.chestCm ?: p.bodyMeasurements.chestCircumferenceCm
            if (userChest != null) {
                val ease = circ - userChest
                val detail = "The garment in size $effectiveSize goes around about ${cmIn(circ)} (${cmIn(chest)} flat chest). Your chest is ${cmIn(userChest)}. " +
                        if (ease >= 0) "That leaves about ${cmIn(ease)} of room." else "It is about ${cmIn(-ease)} short of your chest."
                return Verdict(easeLabel(ease), detail, l.measureSource.ifBlank { "${l.brand} size chart / measurements" } + " vs your chest")
            }
            if (p.favChest != null) {
                val diff = chest - p.favChest
                val label = when {
                    diff < -2 -> "Likely too tight"
                    diff < 1 -> "Slim fit"
                    diff < 5 -> "Relaxed fit"
                    else -> "Roomy / oversized"
                }
                return Verdict(
                    label,
                    "Comparing against your favorite shirt width (${cmIn(p.favChest)}): listing flat chest is ${cmIn(abs(diff))} ${if (diff >= 0) "wider" else "narrower"}.",
                    l.measureSource.ifBlank { "${l.brand} measurements" } + " vs favorite shirt"
                )
            }
            return Verdict("Add your chest or favorite shirt", "Save your chest or a favorite shirt's width to see how this garment compares.", "Garment measurements only")
        }
    }
}

package com.example.saktonato.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.UUID

class Store(private val ctx: Context) {
    private val sp = ctx.getSharedPreferences("sakto", Context.MODE_PRIVATE)
    var msg by mutableStateOf<String?>(null)
    var onboarded by mutableStateOf(sp.getBoolean("onboarded", false))
    var profile by mutableStateOf(loadProfile())
    val listings = mutableStateListOf<Listing>()
    var currentId by mutableStateOf(sp.getString("current", null))
    var user by mutableStateOf(sp.getString("session", null))
    var userName by mutableStateOf(sp.getString("sessionName", "") ?: "")
    var shareFeedback by mutableStateOf(sp.getBoolean("shareFb", false))
    var keepPhotos by mutableStateOf(sp.getBoolean("keepPhotos", true))
    var sellerMode by mutableStateOf(false)
    var guideStep by mutableStateOf(0)
    var tryId by mutableStateOf<String?>(null)
    var capture by mutableStateOf<Capture?>(null)

    init { listings.addAll(loadListings()) }

    val current: Listing? get() = listings.firstOrNull { it.id == currentId }

    private fun JSONObject.putN(k: String, v: Double?): JSONObject {
        if (v != null) put(k, v)
        return this
    }
    private fun JSONObject.dbl(k: String): Double? = if (isNull(k)) null else getDouble(k)

    private fun loadProfile(): Profile {
        val s = sp.getString("profile", null) ?: return Profile()
        val o = JSONObject(s)
        return Profile(o.dbl("h"), o.optString("size", ""), o.dbl("w"), o.dbl("c"), o.dbl("wa"), o.dbl("fc"), o.dbl("fl"))
    }

    fun saveProfile(p: Profile) {
        profile = p
        val o = JSONObject().put("size", p.size)
            .putN("h", p.heightCm).putN("w", p.weightKg).putN("c", p.chestCm)
            .putN("wa", p.waistCm).putN("fc", p.favChest).putN("fl", p.favLength)
        sp.edit().putString("profile", o.toString()).apply()
    }

    private fun loadListings(): List<Listing> {
        val s = sp.getString("listings", null)
        if (s == null) {
            return samples().filter { it.saved || it.id == "s1" } +
                    Listing(id = "m1", name = "Vintage polo shirt", seller = "You", source = "My listing", color = 0xFF6D8B74, mine = true)
        }
        val arr = JSONArray(s)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Listing(
                id = o.getString("id"),
                name = o.getString("name"),
                seller = o.optString("seller"),
                source = o.optString("source"),
                color = o.getLong("color"),
                sizeTag = o.optString("size"),
                chestCm = o.dbl("chest"),
                lengthCm = o.dbl("length"),
                imageUri = if (o.isNull("img")) null else o.getString("img"),
                isSample = o.optBoolean("sample"),
                saved = o.optBoolean("saved"),
                checked = o.optString("checked"),
                feedback = o.optString("fb"),
                mine = o.optBoolean("mine"),
                measureSource = o.optString("msrc")
            )
        }
    }

    private fun saveListings() {
        val arr = JSONArray()
        listings.forEach { l ->
            val o = JSONObject().put("id", l.id).put("name", l.name).put("seller", l.seller)
                .put("source", l.source).put("color", l.color).put("size", l.sizeTag)
                .putN("chest", l.chestCm).putN("length", l.lengthCm)
                .put("sample", l.isSample).put("saved", l.saved).put("checked", l.checked)
                .put("fb", l.feedback).put("mine", l.mine).put("msrc", l.measureSource)
            if (l.imageUri != null) o.put("img", l.imageUri)
            arr.put(o)
        }
        sp.edit().putString("listings", arr.toString()).apply()
    }

    fun update(l: Listing) {
        val i = listings.indexOfFirst { it.id == l.id }
        if (i >= 0) listings[i] = l else listings.add(0, l)
        saveListings()
    }

    fun remove(id: String) {
        listings.removeAll { it.id == id }
        saveListings()
    }

    fun setCurrent(id: String?) {
        currentId = id
        sp.edit().putString("current", id).apply()
    }

    fun openSample(s: Listing) {
        val existing = listings.firstOrNull { it.id == s.id }
        if (existing == null) update(s)
        setCurrent(s.id)
    }

    fun setOnboarded() {
        onboarded = true
        sp.edit().putBoolean("onboarded", true).apply()
    }

    fun persistPrefs() {
        sp.edit().putBoolean("shareFb", shareFeedback).putBoolean("keepPhotos", keepPhotos).apply()
    }

    fun clearData() {
        listings.clear()
        profile = Profile()
        currentId = null
        capture = null
        sp.edit().remove("profile").remove("current").putString("listings", "[]").apply()
    }

    fun importImage(uri: Uri): Listing? = runCatching {
        val id = UUID.randomUUID().toString()
        val file = File(ctx.filesDir, "img_$id.jpg")
        ctx.contentResolver.openInputStream(uri)!!.use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        val b = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = 4 })
        val px = b?.getPixel(0, 0) ?: 0xFFB0BEC5.toInt()
        Listing(
            id = id,
            name = "My listing screenshot",
            source = "Screenshot upload",
            color = px.toLong() and 0xFFFFFFFFL,
            imageUri = file.path
        )
    }.getOrNull()

    private fun hash(pw: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        var b = (salt + pw).toByteArray()
        repeat(10000) { b = md.digest(b) }
        return b.joinToString("") { "%02x".format(it) }
    }

    fun register(email: String, pw: String, name: String): String? {
        val e = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) return "Enter a valid email."
        if (pw.length < 8) return "Password must be at least 8 characters."
        val acc = JSONObject(sp.getString("accounts", "{}")!!)
        if (acc.has(e)) return "That email already has an account. Try logging in."
        val salt = UUID.randomUUID().toString()
        acc.put(e, JSONObject().put("salt", salt).put("hash", hash(pw, salt)).put("name", name.trim()))
        sp.edit().putString("accounts", acc.toString()).putString("session", e).putString("sessionName", name.trim()).apply()
        user = e
        userName = name.trim()
        return null
    }

    fun login(email: String, pw: String): String? {
        val now = System.currentTimeMillis()
        val lock = sp.getLong("lockUntil", 0L)
        if (now < lock) return "Too many tries. Wait ${(lock - now) / 1000 + 1} seconds."
        val e = email.trim().lowercase()
        val acc = JSONObject(sp.getString("accounts", "{}")!!)
        val o = acc.optJSONObject(e)
        if (o == null || o.getString("hash") != hash(pw, o.getString("salt"))) {
            val fails = sp.getInt("fails", 0) + 1
            if (fails >= 5) sp.edit().putInt("fails", 0).putLong("lockUntil", now + 60_000).apply()
            else sp.edit().putInt("fails", fails).apply()
            return "Wrong email or password."
        }
        sp.edit().putInt("fails", 0).putString("session", e).putString("sessionName", o.optString("name")).apply()
        user = e
        userName = o.optString("name")
        return null
    }

    fun logout() {
        sp.edit().remove("session").remove("sessionName").apply()
        user = null
        userName = ""
    }
}

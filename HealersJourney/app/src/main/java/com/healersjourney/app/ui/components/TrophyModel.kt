package com.healersjourney.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class TrophyVertex(val x: Float, val y: Float, val z: Float)
private data class TrophyFace(val a: TrophyVertex, val b: TrophyVertex, val c: TrophyVertex, val color: FloatArray)

/** Tiny, cached software renderer for the packaged glTF meshes. No GL surface per shelf item. */
private object TrophyMeshes {
    private val models = mutableMapOf<String, List<TrophyFace>>()
    private val images = object : android.util.LruCache<String, Bitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun name(skill: String) = when (skill) {
        "medicineXp" -> "work"
        "strengthXp" -> "fitness"
        "socialXp" -> "social"
        "hobbiesXp" -> "creativity"
        else -> "learning"
    }

    @Synchronized
    private fun load(context: Context, name: String): List<TrophyFace> = models.getOrPut(name) {
        val bytes = context.assets.open("trophies/$name.glb").use { it.readBytes() }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        require(buffer.getInt(0) == 0x46546C67 && buffer.getInt(4) == 2)
        val length = buffer.getInt(12)
        val json = JSONObject(String(bytes, 20, length, Charsets.UTF_8))
        val binaryStart = 20 + length + 8
        val faces = mutableListOf<TrophyFace>()
        val primitives = json.getJSONArray("meshes").getJSONObject(0).getJSONArray("primitives")
        for (p in 0 until primitives.length()) {
            val primitive = primitives.getJSONObject(p)
            val accessor = json.getJSONArray("accessors").getJSONObject(primitive.getJSONObject("attributes").getInt("POSITION"))
            val view = json.getJSONArray("bufferViews").getJSONObject(accessor.getInt("bufferView"))
            val start = binaryStart + view.optInt("byteOffset") + accessor.optInt("byteOffset")
            val rgba = json.getJSONArray("materials").getJSONObject(primitive.getInt("material"))
                .getJSONObject("pbrMetallicRoughness").getJSONArray("baseColorFactor")
            val color = FloatArray(3) { rgba.getDouble(it).toFloat() }
            fun vertex(index: Int) = TrophyVertex(buffer.getFloat(start + index * 12), buffer.getFloat(start + index * 12 + 4), buffer.getFloat(start + index * 12 + 8))
            for (v in 0 until accessor.getInt("count") step 3) faces.add(TrophyFace(vertex(v), vertex(v + 1), vertex(v + 2), color))
        }
        faces
    }

    fun render(context: Context, skill: String, rotation: Int, size: Int): Bitmap {
        val name = name(skill)
        val key = "$name:$rotation:$size"
        images.get(key)?.let { return it }
        val angle = Math.toRadians(rotation.toDouble()).toFloat()
        val tilt = .16f
        fun transform(v: TrophyVertex): TrophyVertex {
            val x = v.x * cos(angle) + v.z * sin(angle)
            val z = -v.x * sin(angle) + v.z * cos(angle)
            val y = v.y - .85f
            return TrophyVertex(x, y * cos(tilt) - z * sin(tilt) + .85f, y * sin(tilt) + z * cos(tilt))
        }
        val projected = load(context, name).map { TrophyFace(transform(it.a), transform(it.b), transform(it.c), it.color) }
            .sortedBy { (it.a.z + it.b.z + it.c.z) / 3 }
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.argb(45, 5, 9, 24)
        canvas.drawOval(size*.22f, size*.88f, size*.78f, size*.96f, paint)
        val path = Path()
        fun px(v: TrophyVertex) = size * (.5f + v.x * .60f * (4f / (4f - v.z)))
        fun py(v: TrophyVertex) = size * (.91f - v.y * .44f * (4f / (4f - v.z)))
        projected.forEach { f ->
            val ux = f.b.x - f.a.x; val uy = f.b.y - f.a.y; val uz = f.b.z - f.a.z
            val vx = f.c.x - f.a.x; val vy = f.c.y - f.a.y; val vz = f.c.z - f.a.z
            var nx = uy*vz - uz*vy; var ny = uz*vx - ux*vz; var nz = ux*vy - uy*vx
            val length = sqrt(nx*nx + ny*ny + nz*nz)
            if (length > .000001f && nz > 0f) {
                nx /= length; ny /= length; nz /= length
                val light = max(0f, -.40f*nx + .65f*ny + .64f*nz)
                val specular = max(0f, -.25f*nx + .35f*ny + .90f*nz).pow(18) * .30f
                fun channel(i: Int) = ((f.color[i]*(.38f + .64f*light) + specular)*255).toInt().coerceIn(0,255)
                paint.color = android.graphics.Color.rgb(channel(0), channel(1), channel(2))
                path.reset(); path.moveTo(px(f.a), py(f.a)); path.lineTo(px(f.b), py(f.b)); path.lineTo(px(f.c), py(f.c)); path.close()
                canvas.drawPath(path, paint)
            }
        }
        images.put(key, bitmap)
        return bitmap
    }
}

/** Actual .glb geometry, rasterized only when its size or rotation changes. Drag in detail views. */
@Composable
fun TrophyModel(skill: String, modifier: Modifier = Modifier, interactive: Boolean = false, description: String? = null, alpha: Float = 1f) {
    val context = LocalContext.current
    var size by remember { mutableStateOf(IntSize(192,192)) }
    var rotation by remember(skill) { mutableFloatStateOf(-20f) }
    val pixels = maxOf(size.width, size.height).coerceIn(64, 640)
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, skill, rotation.toInt() / 3, pixels) {
        value = withContext(Dispatchers.Default) { TrophyMeshes.render(context, skill, rotation.toInt() / 3 * 3, pixels).asImageBitmap() }
    }
    val gestures = if (interactive) Modifier.pointerInput(skill) {
        detectHorizontalDragGestures { change, amount -> change.consume(); rotation = (rotation + amount * .7f) % 360f }
    } else Modifier
    Box(modifier.onSizeChanged { size = it }.then(gestures)) {
        bitmap?.let { Image(it, description, Modifier.fillMaxSize(), alpha = alpha) }
    }
}

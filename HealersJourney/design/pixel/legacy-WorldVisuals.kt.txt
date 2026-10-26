package com.healersjourney.app.ui.screens

import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

internal fun sceneryPalette(id: String?): Pair<Color, Color> = when (id) {
    "moonlight" -> Color(0xFF172946) to Color(0xFF314E62)
    "sunset" -> Color(0xFFFFC29A) to Color(0xFFC3CC8E)
    "forest" -> Color(0xFFB6D7C7) to Color(0xFF87B696)
    "beach" -> Color(0xFFA5E2EC) to Color(0xFFE6D3A0)
    "snow" -> Color(0xFFBFDAEF) to Color(0xFFE4EDF4)
    "aurora" -> Color(0xFF182B4C) to Color(0xFF4D6A78)
    "sky_islands" -> Color(0xFFCBC4F0) to Color(0xFFC7D7BD)
    else -> Color(0xFFB7E4F3) to Color(0xFFB5D79C)
}

internal fun DrawScope.sceneryDetails(id: String?) {
    val w = size.width; val h = size.height
    when (id) {
        "forest" -> repeat(7) { i ->
            val x = w * (i / 6f); val y = h * (.2f + (i % 3) * .08f)
            drawRoundRect(Color(0xFF7B8E70), Offset(x - 4, y), Size(8f, h * .45f), CornerRadius(4f))
            drawOval(Color(0xFF69977B).copy(alpha = .65f), Offset(x - w * .12f, y - h * .1f), Size(w * .24f, h * .28f))
        }
        "beach" -> {
            drawOval(Color(0xFF70C5D4), Offset(-w * .35f, h * .7f), Size(w * 1.7f, h * .5f))
            repeat(4) { i -> drawArc(Color(0xFFE5F7F1), 190f, 120f, false, Offset(-w * .1f, h * (.75f + i * .045f)), Size(w * 1.2f, h * .16f), style = Stroke(2f)) }
        }
        "snow" -> {
            repeat(14) { i -> drawCircle(Color.White.copy(alpha = .8f), 2f + i % 3, Offset(w * ((i * 23 % 97) / 100f), h * ((i * 17 % 89) / 100f))) }
            drawOval(Color.White.copy(alpha = .65f), Offset(-w * .2f, h * .81f), Size(w * 1.4f, h * .25f))
        }
        "aurora" -> repeat(4) { band ->
            val path = Path()
            repeat(60) { i ->
                val x = w * i / 59f; val y = h * (.13f + band * .035f + sin(i * .1f + band * .5f) * .07f)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, (if (band % 2 == 0) Color(0xFF81E5C3) else Color(0xFFBD9AEA)).copy(alpha = .28f), style = Stroke(h * .04f))
        }
        "sky_islands" -> repeat(4) { i ->
            val x = w * (.05f + i * .27f); val y = h * (.15f + (i % 2) * .12f)
            drawOval(Color.White.copy(alpha = .7f), Offset(x - w * .1f, y), Size(w * .32f, h * .07f))
            drawOval(Color(0xFF91B6A3), Offset(x, y - h * .025f), Size(w * .2f, h * .06f))
            drawPath(Path().apply { moveTo(x, y + h * .01f); lineTo(x + w * .2f, y + h * .01f); lineTo(x + w * .1f, y + h * .11f); close() }, Color(0xFFAE9CBA))
        }
    }
}

/** Physical props drawn in both the room and the camp; not catalog-only unlocks. */
internal fun DrawScope.extraDecoration(id: String?, indoors: Boolean) {
    if (id == null || id in listOf("plain", "flowers", "lanterns")) return
    val w = size.width; val h = size.height
    val x = w * if (indoors) .83f else .84f
    val y = h * if (indoors) .61f else .83f
    val unit = w * .13f
    val wood = Color(0xFF8D6E54)
    when (id) {
        "bookshelf" -> {
            drawRoundRect(wood, Offset(x - unit * .65f, y - unit * 1.5f), Size(unit * 1.3f, unit * 1.5f), CornerRadius(3f))
            repeat(2) { row -> repeat(4) { book ->
                drawRect(listOf(Color(0xFFB98570), Color(0xFF7EA3BA), Color(0xFFB2B581), Color(0xFFCAAF76))[book],
                    Offset(x - unit * .5f + book * unit * .26f, y - unit * (1.3f - row * .65f)), Size(unit * .19f, unit * .49f))
            } }
        }
        "rug" -> {
            drawOval(Color(0xFFBE809A), Offset(w * .22f, h * .82f), Size(w * .53f, h * .14f))
            drawOval(Color(0xFFF0CFA2), Offset(w * .28f, h * .85f), Size(w * .41f, h * .08f), style = Stroke(3f))
            repeat(2) { i -> drawRoundRect(if (i == 0) Color(0xFFD2A66C) else Color(0xFF86ACAA), Offset(x - unit + i * unit * .8f, y - unit * .4f), Size(unit * .7f, unit * .5f), CornerRadius(unit * .15f)) }
        }
        "plants" -> repeat(3) { i ->
            val px = x - unit + i * unit * .65f
            drawRoundRect(Color(0xFFCB987D), Offset(px, y - unit * .4f), Size(unit * .4f, unit * .4f), CornerRadius(3f))
            repeat(3) { leaf -> drawOval(Color(0xFF67946C), Offset(px - unit * .1f + leaf * unit * .12f, y - unit * (.9f + leaf * .14f)), Size(unit * .35f, unit * .8f)) }
        }
        "aquarium" -> {
            drawRoundRect(Color(0xFF6F9CA8), Offset(x - unit * .8f, y - unit), Size(unit * 1.5f, unit), CornerRadius(5f))
            drawRect(Color(0xFF9BD3DD), Offset(x - unit * .68f, y - unit * .86f), Size(unit * 1.25f, unit * .68f))
            repeat(3) { i ->
                val fx = x - unit * .4f + i * unit * .3f; val fy = y - unit * (.35f + (i % 2) * .3f)
                drawOval(Color(0xFFE7AD61), Offset(fx, fy), Size(unit * .2f, unit * .12f))
            }
        }
        "telescope" -> {
            drawLine(wood, Offset(x, y - unit * .7f), Offset(x - unit * .45f, y), 4f)
            drawLine(wood, Offset(x, y - unit * .7f), Offset(x + unit * .45f, y), 4f)
            drawLine(wood, Offset(x, y - unit * .7f), Offset(x, y), 4f)
            drawLine(Color(0xFF7C8CAE), Offset(x - unit * .5f, y - unit * .95f), Offset(x + unit * .55f, y - unit * 1.5f), unit * .24f, StrokeCap.Round)
        }
        "fireplace" -> {
            drawRoundRect(Color(0xFFAD8E7E), Offset(x - unit * .7f, y - unit * 1.2f), Size(unit * 1.4f, unit * 1.2f), CornerRadius(5f))
            drawRoundRect(Color(0xFF5E5354), Offset(x - unit * .45f, y - unit * .8f), Size(unit * .9f, unit * .8f), CornerRadius(10f))
            drawOval(Color(0xFFED9B54), Offset(x - unit * .3f, y - unit * .6f), Size(unit * .6f, unit * .6f))
            drawOval(Color(0xFFFFD687), Offset(x - unit * .15f, y - unit * .42f), Size(unit * .3f, unit * .4f))
        }
        "trophy" -> {
            drawRect(wood, Offset(x - unit * .7f, y - unit * .12f), Size(unit * 1.4f, unit * .15f))
            drawRoundRect(Color(0xFFE8BD69), Offset(x - unit * .4f, y - unit * 1.1f), Size(unit * .8f, unit * .6f), CornerRadius(unit * .2f))
            drawRect(Color(0xFFE8BD69), Offset(x - unit * .09f, y - unit * .5f), Size(unit * .18f, unit * .4f))
            drawRect(Color(0xFFDBB36F), Offset(x - unit * .3f, y - unit * .18f), Size(unit * .6f, unit * .12f))
        }
    }
}

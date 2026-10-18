package com.ogapirlo.takken.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogapirlo.takken.ui.theme.TColors

/** Duolingo風の「押し込める」立体ボタン。 */
@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = TColors.Green,
    shadow: Color = TColors.GreenDark,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(if (enabled) shadow else TColors.Gray)
            .clickable(interaction, indication = null, enabled = enabled, onClick = onClick),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .offset(y = if (pressed && enabled) 4.dp else 0.dp)
                .clip(shape)
                .background(if (enabled) color else TColors.Gray),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                color = if (enabled) contentColor else TColors.GrayDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
            )
        }
    }
}

/** 枠線と下側の厚みで立体感を出すカード。 */
@Composable
fun ChunkyCard(
    modifier: Modifier = Modifier,
    borderColor: Color = TColors.Gray,
    fill: Color = Color.White,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .clip(shape)
            .background(borderColor)
            .then(
                if (onClick != null) Modifier.clickable(interaction, indication = null, onClick = onClick)
                else Modifier,
            )
            .padding(bottom = 4.dp)
            .offset(y = if (pressed) 2.dp else 0.dp),
    ) {
        Box(
            Modifier
                .clip(shape)
                .background(fill)
                .border(2.dp, borderColor, shape)
                .padding(16.dp),
        ) { content() }
    }
}

@Composable
fun ProgressBar(value: Float, modifier: Modifier = Modifier, color: Color = TColors.Green, height: Dp = 16.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(TColors.Gray),
    ) {
        Box(
            Modifier
                .fillMaxWidth(value.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color),
        )
    }
}

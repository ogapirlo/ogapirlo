package com.ogapirlo.takken.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogapirlo.takken.domain.Categories
import com.ogapirlo.takken.domain.QuestionType
import com.ogapirlo.takken.ui.theme.TColors

@Composable
fun LessonScreen(l: LessonState, vm: AppViewModel) {
    var confirmQuit by remember { mutableStateOf(false) }
    BackHandler { confirmQuit = true }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(l.checked, l.index) {
        if (l.checked && !l.isCorrect) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val item = l.current
    Column(
        Modifier
            .fillMaxSize()
            .background(TColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { confirmQuit = true }) {
                Icon(Icons.Filled.Close, contentDescription = "やめる", tint = TColors.GrayDark)
            }
            ProgressBar(l.progress, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Text("${l.index + 1}/${l.items.size}", color = TColors.SubText, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (l.combo >= 2) {
                Text("🔥 ${l.combo}問連続正解！", color = TColors.Orange, fontWeight = FontWeight.ExtraBold)
            }
            Text(
                "${Categories.emoji(item.question.category)} ${item.question.category}" +
                    (item.question.source?.let { "　|　$it" } ?: ""),
                color = TColors.Purple, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
            Text(
                if (item.question.type == QuestionType.OX) "正しければ〇、誤りなら×を選ぼう" else "正しいものを1つ選ぼう",
                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
            )
            Text(item.question.text, fontSize = 16.sp, lineHeight = 24.sp)
            Spacer(Modifier.height(4.dp))

            if (item.question.type == QuestionType.OX) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item.choices.forEachIndexed { i, c ->
                        ChoiceCard(c, i, l, vm, Modifier.weight(1f), big = true)
                    }
                }
            } else {
                item.choices.forEachIndexed { i, c -> ChoiceCard(c, i, l, vm, Modifier.fillMaxWidth()) }
            }
            Spacer(Modifier.height(12.dp))
        }

        FeedbackPanel(l, vm)
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text("ちょっと待って！", fontWeight = FontWeight.Bold) },
            text = { Text("あと${l.items.size - l.results.size}問で今日の🔥がつきます。本当にやめますか？") },
            confirmButton = {
                TextButton(onClick = { confirmQuit = false }) { Text("続ける", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmQuit = false; vm.quitLesson() }) {
                    Text("やめる", color = TColors.Red)
                }
            },
        )
    }
}

@Composable
private fun ChoiceCard(text: String, index: Int, l: LessonState, vm: AppViewModel, modifier: Modifier, big: Boolean = false) {
    val isAnswer = index == l.current.answerIndex
    val isSelected = l.selected == index
    val (border, fill) = when {
        l.checked && isAnswer -> TColors.Green to TColors.GreenLight
        l.checked && isSelected -> TColors.Red to TColors.RedLight
        isSelected -> TColors.Blue to TColors.BlueLight
        else -> TColors.Gray to Color.White
    }
    ChunkyCard(modifier, borderColor = border, fill = fill, onClick = { vm.select(index) }) {
        Box(
            Modifier.fillMaxWidth().heightIn(min = if (big) 72.dp else 0.dp),
            contentAlignment = if (big) Alignment.Center else Alignment.CenterStart,
        ) {
            Text(
                text,
                fontSize = if (big) 22.sp else 15.sp,
                lineHeight = if (big) 28.sp else 22.sp,
                fontWeight = if (big) FontWeight.ExtraBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun FeedbackPanel(l: LessonState, vm: AppViewModel) {
    val ok = l.checked && l.isCorrect
    val praise = remember(l.index) { listOf("正解！🎉", "すばらしい！✨", "その調子！💪", "ナイス！👍").random() }
    val panelColor = when {
        !l.checked -> Color.White
        ok -> TColors.GreenLight
        else -> TColors.RedLight
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(panelColor)
            .padding(16.dp),
    ) {
        AnimatedVisibility(visible = l.checked, enter = slideInVertically { it }) {
            Column(Modifier.padding(bottom = 12.dp)) {
                Text(
                    if (ok) praise else "おしい！",
                    color = if (ok) TColors.GreenDark else TColors.RedDark,
                    fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                )
                if (!ok) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "正解：${l.current.choices[l.current.answerIndex]}",
                        color = TColors.RedDark, fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Column(Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
                    Text(l.current.question.explanation, fontSize = 14.sp, lineHeight = 21.sp, color = TColors.Text)
                }
            }
        }
        if (!l.checked) {
            ChunkyButton("チェック", onClick = vm::check, enabled = l.selected != null)
        } else if (ok) {
            ChunkyButton("続ける", onClick = vm::next)
        } else {
            ChunkyButton("続ける", onClick = vm::next, color = TColors.Red, shadow = TColors.RedDark)
        }
    }
}

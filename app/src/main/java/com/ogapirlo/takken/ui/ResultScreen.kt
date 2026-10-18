package com.ogapirlo.takken.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogapirlo.takken.domain.LessonSummary
import com.ogapirlo.takken.ui.theme.TColors

@Composable
fun ResultScreen(s: LessonSummary, onAgain: () -> Unit, onHome: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(
        if (shown) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pop",
    )
    val perfect = s.correct == s.total

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            if (s.streakExtended) {
                Text("🔥", fontSize = 96.sp, modifier = Modifier.scale(scale))
                Text(
                    "${s.streak}日連続！",
                    fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = TColors.Orange,
                )
                Text(
                    s.milestone?.let { "🎊 ${it}日達成！ここまで続けたあなたは本物です。" }
                        ?: if (s.streak == 1) "最初の一歩！明日も5問だけやってみよう。" else "今日の分はクリア。明日も記録を伸ばそう！",
                    textAlign = TextAlign.Center, color = TColors.SubText,
                )
            } else {
                Text(if (perfect) "🏆" else "🎉", fontSize = 96.sp, modifier = Modifier.scale(scale))
                Text(
                    if (perfect) "パーフェクト！" else "レッスン完了！",
                    fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = TColors.Gold,
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("獲得XP", "⚡ ${s.xp}", TColors.Gold, Modifier.weight(1f))
                StatTile("正答", "🎯 ${s.correct}/${s.total}", TColors.Green, Modifier.weight(1f))
                StatTile("ジェム", "💎 +${s.gems}", TColors.Blue, Modifier.weight(1f))
            }

            ChunkyCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        if (s.todayXp >= s.dailyGoal) "🏆 今日の目標を達成！" else "🎯 今日の目標まであと${s.dailyGoal - s.todayXp}XP",
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    ProgressBar(s.todayXp.toFloat() / s.dailyGoal, color = TColors.Gold)
                    if (s.goalReachedNow) {
                        Spacer(Modifier.height(6.dp))
                        Text("目標達成ボーナス 💎+10", color = TColors.Blue, fontSize = 13.sp)
                    }
                }
            }
            if (!perfect) {
                Text(
                    "間違えた問題は「復習」に入りました。次のレッスンにも少し混ざります。",
                    color = TColors.SubText, fontSize = 13.sp, textAlign = TextAlign.Center,
                )
            }
        }
        ChunkyButton("もう5問！", onClick = onAgain)
        Spacer(Modifier.height(10.dp))
        ChunkyButton(
            "ホームへ", onClick = onHome,
            color = Color.White, shadow = TColors.Gray, contentColor = TColors.Blue,
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color, modifier: Modifier) {
    ChunkyCard(modifier, borderColor = color) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
        }
    }
}

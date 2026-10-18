package com.ogapirlo.takken.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogapirlo.takken.domain.Categories
import com.ogapirlo.takken.ui.theme.TColors
import java.time.LocalDate

@Composable
fun StatsScreen(vm: AppViewModel) {
    val p = vm.progress
    val today = vm.today
    val answered = p.stats.values.sumOf { it.correct + it.wrong }
    val correct = p.stats.values.sumOf { it.correct }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("学習の記録", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("🔥", "${p.streak.current}日", "連続記録", TColors.Orange, Modifier.weight(1f))
            Tile("👑", "${p.streak.best}日", "最高記録", TColors.Gold, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("⚡", "${p.totalXp}", "累計XP", TColors.Gold, Modifier.weight(1f))
            Tile("📚", "${p.lessonsCompleted}回", "レッスン", TColors.Green, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("✍️", "${answered}問", "解いた数", TColors.Blue, Modifier.weight(1f))
            Tile(
                "🎯", if (answered == 0) "-" else "${correct * 100 / answered}%", "正答率",
                TColors.Purple, Modifier.weight(1f),
            )
        }

        Text("過去4週間", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        ChunkyCard(Modifier.fillMaxWidth()) {
            val monday = today - (LocalDate.ofEpochDay(today).dayOfWeek.value - 1)
            val labels = listOf("月", "火", "水", "木", "金", "土", "日")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (w in 3 downTo 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (i in 0 until 7) DayDot(labels[i], monday - w * 7 + i, p, today)
                    }
                }
            }
        }

        Text("分野別の正答率", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Categories.all.forEach { cat ->
            val ids = vm.questions.filter { it.category == cat }.map { it.id }.toSet()
            val st = p.stats.filterKeys { it in ids }.values
            val a = st.sumOf { it.correct + it.wrong }
            val c = st.sumOf { it.correct }
            val rate = if (a == 0) 0f else c.toFloat() / a
            ChunkyCard(Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${Categories.emoji(cat)} $cat", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(if (a == 0) "未挑戦" else "${(rate * 100).toInt()}%（${c}/${a}）", color = TColors.SubText)
                    }
                    Spacer(Modifier.height(8.dp))
                    ProgressBar(rate, height = 10.dp, color = if (rate >= 0.7f) TColors.Green else TColors.Orange)
                }
            }
        }
        Text("合格ラインの目安は7割。苦手な分野は分野別レッスンで5問ずつ攻めよう。", color = TColors.SubText, fontSize = 13.sp)
    }
}

@Composable
private fun Tile(emoji: String, value: String, label: String, color: Color, modifier: Modifier) {
    ChunkyCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
                Text(label, color = TColors.SubText, fontSize = 12.sp)
            }
        }
    }
}

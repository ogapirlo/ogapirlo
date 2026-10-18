package com.ogapirlo.takken.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogapirlo.takken.domain.Categories
import com.ogapirlo.takken.domain.Progress
import com.ogapirlo.takken.ui.theme.TColors
import java.time.LocalDate

@Composable
fun LearnScreen(vm: AppViewModel) {
    val p = vm.progress
    val today = vm.today
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusBar(p, today)
        WeekStrip(p, today)
        DailyGoalCard(p, today)

        val doneToday = p.doneOn(today)
        ChunkyButton(
            text = if (doneToday) "▶ もう5問やる（おまかせ）" else "▶ 今日の5問をはじめる",
            onClick = { vm.startLesson(LessonMode.Mix) },
        )
        if (!doneToday) {
            Text(
                "1レッスン＝5問・約2分。1回やれば今日の🔥がつきます。",
                color = TColors.SubText, fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (p.reviewCount > 0) {
            ChunkyCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = TColors.Purple,
                onClick = { vm.startLesson(LessonMode.Review) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔁", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("間違えた問題を復習", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("${p.reviewCount}問が復習待ち", color = TColors.SubText, fontSize = 13.sp)
                    }
                }
            }
        }

        Text("分野別に5問", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Categories.all.forEach { cat ->
            val qs = vm.questions.filter { it.category == cat }
            if (qs.isEmpty()) return@forEach
            val mastered = qs.count { p.stats[it.id]?.lastCorrect == true }
            ChunkyCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { vm.startLesson(LessonMode.Category(cat)) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Categories.emoji(cat), fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(cat, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(Modifier.height(6.dp))
                        ProgressBar(mastered.toFloat() / qs.size, height = 10.dp, color = TColors.Gold)
                        Spacer(Modifier.height(4.dp))
                        Text("習得 $mastered / ${qs.size}問", color = TColors.SubText, fontSize = 12.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatusBar(p: Progress, today: Long) {
    val active = p.doneOn(today)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("宅建まいにち", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TColors.Green)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "🔥 ${p.streak.current}",
                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
                color = if (active) TColors.Orange else TColors.GrayDark,
            )
            Text("🧊 ${p.streak.freezes}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TColors.Blue)
            Text("💎 ${p.gems}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TColors.Blue)
        }
    }
}

private val WEEK_LABELS = listOf("月", "火", "水", "木", "金", "土", "日")

@Composable
fun WeekStrip(p: Progress, today: Long) {
    val todayDate = LocalDate.ofEpochDay(today)
    val monday = today - (todayDate.dayOfWeek.value - 1)
    ChunkyCard(Modifier.fillMaxWidth()) {
        Column {
            val alive = p.streak.current > 0
            Text(
                when {
                    p.doneOn(today) -> "🔥 ${p.streak.current}日連続！今日も達成済み"
                    alive -> "🔥 ${p.streak.current}日連続中。今日やれば${p.streak.current + 1}日に！"
                    else -> "今日から連続記録をスタートしよう"
                },
                fontWeight = FontWeight.Bold,
                color = if (alive || p.doneOn(today)) TColors.Orange else TColors.Text,
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (i in 0 until 7) {
                    val day = monday + i
                    DayDot(WEEK_LABELS[i], day, p, today)
                }
            }
        }
    }
}

@Composable
fun DayDot(label: String, day: Long, p: Progress, today: Long) {
    val (bg, mark) = when {
        p.doneOn(day) -> TColors.Orange to "🔥"
        day in p.frozenDays -> TColors.BlueLight to "🧊"
        else -> TColors.Gray to ""
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label, fontSize = 12.sp,
            color = if (day == today) TColors.Orange else TColors.SubText,
            fontWeight = if (day == today) FontWeight.ExtraBold else FontWeight.Normal,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (day > today) Color.Transparent else bg)
                .border(2.dp, if (day == today) TColors.Orange else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(mark, fontSize = 16.sp) }
    }
}

@Composable
private fun DailyGoalCard(p: Progress, today: Long) {
    val xp = p.xpOn(today)
    val reached = xp >= p.dailyGoal
    ChunkyCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (reached) "🏆" else "🎯", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (reached) "今日の目標達成！" else "今日の目標",
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),
                )
                Text("$xp / ${p.dailyGoal} XP", color = TColors.SubText, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            ProgressBar(xp.toFloat() / p.dailyGoal, color = TColors.Gold)
        }
    }
}

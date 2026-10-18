package com.ogapirlo.takken.ui

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogapirlo.takken.domain.ProgressRules
import com.ogapirlo.takken.domain.StreakRules
import com.ogapirlo.takken.ui.theme.TColors

@Composable
fun SettingsScreen(vm: AppViewModel, requestNotificationPermission: () -> Unit) {
    val p = vm.progress
    val context = LocalContext.current
    var confirmReset by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("設定", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)

        Text("🧊 ストリークフリーズ", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        ChunkyCard(Modifier.fillMaxWidth(), borderColor = TColors.Blue) {
            Column {
                Text("1日休んでも連続記録を守ってくれるアイテム。休んだ日に自動で使われます。", fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "所持 ${p.streak.freezes} / ${StreakRules.MAX_FREEZES}　・　💎 ${p.gems}",
                    color = TColors.SubText, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                ChunkyButton(
                    "💎${ProgressRules.FREEZE_PRICE}で購入",
                    onClick = {
                        val msg = if (vm.buyFreeze()) "フリーズを購入しました 🧊" else "ジェムが足りないか、上限まで持っています"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    color = TColors.Blue, shadow = TColors.BlueDark,
                    enabled = p.gems >= ProgressRules.FREEZE_PRICE && p.streak.freezes < StreakRules.MAX_FREEZES,
                )
            }
        }

        Text("🎯 1日の目標", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        GoalPicker(p.dailyGoal, vm::setGoal)

        Text("🔔 リマインダー", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        ChunkyCard(Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("毎日お知らせ", fontWeight = FontWeight.Bold)
                        Text("その日まだ解いていないときだけ通知します", color = TColors.SubText, fontSize = 12.sp)
                    }
                    Switch(
                        checked = p.reminderEnabled,
                        onCheckedChange = {
                            if (it) requestNotificationPermission()
                            vm.setReminder(it)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = TColors.Green),
                    )
                }
                if (p.reminderEnabled) {
                    Spacer(Modifier.height(10.dp))
                    ChunkyButton(
                        "通知時刻 %02d:%02d を変更".format(p.reminderHour, p.reminderMinute),
                        onClick = {
                            TimePickerDialog(context, { _, h, m -> vm.setReminder(true, h, m) },
                                p.reminderHour, p.reminderMinute, true).show()
                        },
                        color = androidx.compose.ui.graphics.Color.White, shadow = TColors.Gray, contentColor = TColors.Blue,
                    )
                }
            }
        }

        Text("📘 問題について", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        ChunkyCard(Modifier.fillMaxWidth()) {
            Text(
                "収録 ${vm.questions.size}問。宅建試験の過去問で繰り返し出題されている論点をもとにした問題です。" +
                    "法改正により内容が変わることがあるため、最新の法令・公式情報もあわせて確認してください。",
                fontSize = 13.sp, color = TColors.SubText, lineHeight = 20.sp,
            )
        }

        TextButton(onClick = { confirmReset = true }) {
            Text("学習記録をリセット", color = TColors.Red)
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("記録をリセットしますか？") },
            text = { Text("連続記録・XP・ジェム・正答履歴がすべて消えます。元に戻せません。") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; vm.resetAll() }) { Text("リセット", color = TColors.Red) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("キャンセル") } },
        )
    }
}

@Composable
fun GoalPicker(selected: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProgressRules.GOAL_OPTIONS.forEach { (xp, label) ->
            val on = xp == selected
            ChunkyCard(
                Modifier.fillMaxWidth(),
                borderColor = if (on) TColors.Blue else TColors.Gray,
                fill = if (on) TColors.BlueLight else androidx.compose.ui.graphics.Color.White,
                onClick = { onSelect(xp) },
            ) {
                Row {
                    Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("$xp XP/日（約${(xp + 9) / 10}レッスン）", color = TColors.SubText)
                }
            }
        }
    }
}

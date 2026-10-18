package com.ogapirlo.takken.ui

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ogapirlo.takken.ui.theme.TColors

@Composable
fun OnboardingScreen(onDone: (goal: Int, reminder: Boolean) -> Unit) {
    var goal by remember { mutableIntStateOf(10) }
    var reminder by remember { mutableStateOf(true) }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text("🏠", fontSize = 72.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text(
                "宅建まいにち",
                fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = TColors.Green,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            )
            Text(
                "1日5問、約2分。\n大事なのは「毎日ちょっとでも続けること」。",
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = TColors.SubText,
            )
            Spacer(Modifier.height(8.dp))
            Text("1日の目標を選ぼう", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text("迷ったら「ゆるっと」がおすすめ。あとから変えられます。", color = TColors.SubText, fontSize = 13.sp)
            GoalPicker(goal) { goal = it }
            ChunkyCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("🔔 毎日20時にリマインド", fontWeight = FontWeight.Bold)
                        Text("やり忘れた日だけ通知します", color = TColors.SubText, fontSize = 12.sp)
                    }
                    Switch(
                        checked = reminder, onCheckedChange = { reminder = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = TColors.Green),
                    )
                }
            }
        }
        ChunkyButton("最初の5問をはじめる", onClick = { onDone(goal, reminder) })
    }
}

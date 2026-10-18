package com.ogapirlo.takken.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ogapirlo.takken.domain.StreakCheck
import com.ogapirlo.takken.ui.theme.TColors

@Composable
fun TakkenApp(vm: AppViewModel, requestNotificationPermission: () -> Unit) {
    when (val s = vm.screen) {
        Screen.Onboarding -> OnboardingScreen { goal, reminder ->
            if (reminder) requestNotificationPermission()
            vm.finishOnboarding(goal, reminder)
        }
        Screen.Lesson -> vm.lesson?.let { LessonScreen(it, vm) }
        is Screen.Result -> ResultScreen(s.summary, onAgain = vm::again, onHome = vm::goHome)
        is Screen.Main -> MainScaffold(s.tab, vm, requestNotificationPermission)
    }

    vm.streakNotice?.let { notice ->
        val (title, body) = when (notice) {
            is StreakCheck.FreezeUsed ->
                "🧊 フリーズが記録を守りました" to
                    "昨日はお休みでしたが、ストリークフリーズを${notice.frozenDays.size}個使って連続記録をキープしました。今日の5問で記録を伸ばしましょう！"
            is StreakCheck.Lost ->
                "連続記録が途切れました…" to
                    "${notice.previous}日の記録はリセットされました。でも大丈夫。今日の5問からまた始めましょう 💪"
            StreakCheck.Ok -> return@let
        }
        AlertDialog(
            onDismissRequest = vm::dismissNotice,
            confirmButton = { TextButton(onClick = vm::dismissNotice) { Text("OK", fontWeight = FontWeight.Bold) } },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = { Text(body) },
        )
    }
}

@Composable
private fun MainScaffold(tab: Tab, vm: AppViewModel, requestNotificationPermission: () -> Unit) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = TColors.Background) {
                val colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TColors.Blue,
                    selectedTextColor = TColors.Blue,
                    indicatorColor = TColors.BlueLight,
                )
                NavigationBarItem(
                    selected = tab == Tab.Learn, onClick = { vm.selectTab(Tab.Learn) },
                    icon = { Icon(Icons.Filled.Home, null) }, label = { Text("学習") }, colors = colors,
                )
                NavigationBarItem(
                    selected = tab == Tab.Stats, onClick = { vm.selectTab(Tab.Stats) },
                    icon = { Icon(Icons.Filled.DateRange, null) }, label = { Text("記録") }, colors = colors,
                )
                NavigationBarItem(
                    selected = tab == Tab.Settings, onClick = { vm.selectTab(Tab.Settings) },
                    icon = { Icon(Icons.Filled.Settings, null) }, label = { Text("設定") }, colors = colors,
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.Learn -> LearnScreen(vm)
                Tab.Stats -> StatsScreen(vm)
                Tab.Settings -> SettingsScreen(vm, requestNotificationPermission)
            }
        }
    }
}

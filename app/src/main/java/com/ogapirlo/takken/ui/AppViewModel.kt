package com.ogapirlo.takken.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.ogapirlo.takken.data.ProgressStore
import com.ogapirlo.takken.data.QuestionRepository
import com.ogapirlo.takken.domain.LessonItem
import com.ogapirlo.takken.domain.LessonPicker
import com.ogapirlo.takken.domain.LessonSummary
import com.ogapirlo.takken.domain.Progress
import com.ogapirlo.takken.domain.ProgressRules
import com.ogapirlo.takken.domain.Question
import com.ogapirlo.takken.domain.StreakCheck
import com.ogapirlo.takken.domain.StreakRules
import com.ogapirlo.takken.notify.ReminderScheduler
import java.time.LocalDate
import kotlin.random.Random

sealed interface LessonMode {
    data object Mix : LessonMode
    data class Category(val name: String) : LessonMode
    data object Review : LessonMode
}

enum class Tab { Learn, Stats, Settings }

sealed interface Screen {
    data object Onboarding : Screen
    data class Main(val tab: Tab) : Screen
    data object Lesson : Screen
    data class Result(val summary: LessonSummary) : Screen
}

data class LessonState(
    val mode: LessonMode,
    val items: List<LessonItem>,
    val index: Int = 0,
    val selected: Int? = null,
    val checked: Boolean = false,
    val results: List<Pair<String, Boolean>> = emptyList(),
    val combo: Int = 0,
) {
    val current: LessonItem get() = items[index]
    val isCorrect: Boolean get() = selected == current.answerIndex
    val progress: Float get() = (results.size).toFloat() / items.size
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = ProgressStore(app)
    val questions: List<Question> = QuestionRepository.load(app)

    var progress by mutableStateOf(store.load())
        private set
    var screen by mutableStateOf<Screen>(if (progress.onboarded) Screen.Main(Tab.Learn) else Screen.Onboarding)
        private set
    var lesson by mutableStateOf<LessonState?>(null)
        private set
    var streakNotice by mutableStateOf<StreakCheck?>(null)
        private set

    val today: Long get() = LocalDate.now().toEpochDay()

    init {
        refreshDay()
    }

    /** アプリを開いたときに、サボった日の判定（フリーズ消費 or 記録リセット）を行う。 */
    fun refreshDay() {
        val (streak, check) = StreakRules.reconcile(progress.streak, today)
        if (check is StreakCheck.Ok) return
        val frozen = (check as? StreakCheck.FreezeUsed)?.frozenDays.orEmpty()
        update(progress.copy(streak = streak, frozenDays = progress.frozenDays + frozen))
        streakNotice = check
    }

    fun dismissNotice() {
        streakNotice = null
    }

    fun finishOnboarding(goal: Int, reminder: Boolean) {
        update(progress.copy(dailyGoal = goal, reminderEnabled = reminder, onboarded = true))
        ReminderScheduler.apply(getApplication())
        startLesson(LessonMode.Mix)
    }

    fun selectTab(tab: Tab) {
        screen = Screen.Main(tab)
    }

    fun poolFor(mode: LessonMode): List<Question> = when (mode) {
        LessonMode.Mix -> questions
        is LessonMode.Category -> questions.filter { it.category == mode.name }
        LessonMode.Review -> questions.filter { progress.stats[it.id]?.needsReview == true }
    }

    fun startLesson(mode: LessonMode) {
        val pool = poolFor(mode)
        val picked = LessonPicker.pick(
            pool, progress.stats,
            maxReview = if (mode == LessonMode.Review) LessonPicker.LESSON_SIZE else 2,
        )
        if (picked.isEmpty()) return
        lesson = LessonState(mode, picked.map { LessonItem.from(it, Random.Default) })
        screen = Screen.Lesson
    }

    fun select(index: Int) {
        val l = lesson ?: return
        if (!l.checked) lesson = l.copy(selected = index)
    }

    fun check() {
        val l = lesson ?: return
        if (l.selected == null || l.checked) return
        val ok = l.isCorrect
        lesson = l.copy(
            checked = true,
            results = l.results + (l.current.question.id to ok),
            combo = if (ok) l.combo + 1 else 0,
        )
    }

    fun next() {
        val l = lesson ?: return
        if (!l.checked) return
        if (l.index + 1 < l.items.size) {
            lesson = l.copy(index = l.index + 1, selected = null, checked = false)
            return
        }
        val (p, summary) = ProgressRules.applyLesson(progress, today, l.results)
        update(p)
        screen = Screen.Result(summary)
    }

    fun quitLesson() {
        lesson = null
        screen = Screen.Main(Tab.Learn)
    }

    fun again() {
        val mode = lesson?.mode ?: LessonMode.Mix
        val next = if (mode == LessonMode.Review && poolFor(mode).isEmpty()) LessonMode.Mix else mode
        startLesson(next)
    }

    fun goHome() {
        lesson = null
        screen = Screen.Main(Tab.Learn)
    }

    fun setGoal(goal: Int) = update(progress.copy(dailyGoal = goal))

    fun setReminder(enabled: Boolean, hour: Int = progress.reminderHour, minute: Int = progress.reminderMinute) {
        update(progress.copy(reminderEnabled = enabled, reminderHour = hour, reminderMinute = minute))
        ReminderScheduler.apply(getApplication())
    }

    fun buyFreeze(): Boolean {
        val p = ProgressRules.buyFreeze(progress) ?: return false
        update(p)
        return true
    }

    fun resetAll() {
        store.clear()
        progress = Progress(onboarded = true)
        store.save(progress)
        ReminderScheduler.apply(getApplication())
    }

    private fun update(p: Progress) {
        progress = p
        store.save(p)
    }
}

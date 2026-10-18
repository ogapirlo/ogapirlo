package com.ogapirlo.takken.domain

data class Progress(
    val streak: StreakState = StreakState(freezes = 1),
    val gems: Int = 0,
    val totalXp: Int = 0,
    val dailyGoal: Int = 10,
    val todayXp: Int = 0,
    val todayDay: Long = -1,
    val lessonsCompleted: Int = 0,
    val activeDays: Set<Long> = emptySet(),
    val frozenDays: Set<Long> = emptySet(),
    val stats: Map<String, QuestionStat> = emptyMap(),
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val onboarded: Boolean = false,
) {
    fun xpOn(day: Long): Int = if (todayDay == day) todayXp else 0
    fun doneOn(day: Long): Boolean = day in activeDays
    val reviewCount: Int get() = stats.values.count { it.needsReview }
}

data class LessonSummary(
    val correct: Int,
    val total: Int,
    val xp: Int,
    val gems: Int,
    val streakExtended: Boolean,
    val streak: Int,
    val milestone: Int?,
    val goalReachedNow: Boolean,
    val todayXp: Int,
    val dailyGoal: Int,
)

object ProgressRules {
    const val XP_LESSON = 5
    const val XP_PER_CORRECT = 1
    const val XP_PERFECT = 5
    const val GEMS_LESSON = 5
    const val GEMS_PERFECT = 5
    const val GEMS_GOAL = 10
    const val FREEZE_PRICE = 50
    val GOAL_OPTIONS = listOf(10 to "ゆるっと", 20 to "ふつう", 30 to "しっかり", 50 to "ガチ")

    /** 正誤に関係なく、最後まで解けば必ずXPが入る（続けること自体を評価する）。 */
    fun xpFor(correct: Int, total: Int): Int =
        XP_LESSON + correct * XP_PER_CORRECT + if (correct == total && total > 0) XP_PERFECT else 0

    fun applyLesson(p: Progress, today: Long, results: List<Pair<String, Boolean>>): Pair<Progress, LessonSummary> {
        val correct = results.count { it.second }
        val total = results.size
        val xp = xpFor(correct, total)
        val (streak, extended) = StreakRules.recordLesson(p.streak, today)
        val before = p.xpOn(today)
        val after = before + xp
        val goalNow = before < p.dailyGoal && after >= p.dailyGoal
        val gems = GEMS_LESSON + (if (correct == total) GEMS_PERFECT else 0) + (if (goalNow) GEMS_GOAL else 0)

        val stats = p.stats.toMutableMap()
        for ((id, ok) in results) {
            val s = stats[id] ?: QuestionStat()
            stats[id] = if (ok) s.copy(correct = s.correct + 1, lastCorrect = true)
            else s.copy(wrong = s.wrong + 1, lastCorrect = false)
        }

        val next = p.copy(
            streak = streak,
            gems = p.gems + gems,
            totalXp = p.totalXp + xp,
            todayXp = after,
            todayDay = today,
            lessonsCompleted = p.lessonsCompleted + 1,
            activeDays = (p.activeDays + today).filter { it > today - 400 }.toSet(),
            stats = stats,
        )
        val milestone = streak.current.takeIf { extended && it in StreakRules.MILESTONES }
        return next to LessonSummary(
            correct, total, xp, gems, extended, streak.current, milestone, goalNow, after, p.dailyGoal,
        )
    }

    fun buyFreeze(p: Progress): Progress? {
        if (p.gems < FREEZE_PRICE || p.streak.freezes >= StreakRules.MAX_FREEZES) return null
        return p.copy(gems = p.gems - FREEZE_PRICE, streak = p.streak.copy(freezes = p.streak.freezes + 1))
    }
}

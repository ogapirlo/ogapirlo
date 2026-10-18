package com.ogapirlo.takken.domain

import kotlin.math.max

/** 日付はすべて LocalDate.toEpochDay() の値で扱う。 */
data class StreakState(
    val current: Int = 0,
    val best: Int = 0,
    val lastActiveDay: Long? = null,
    val freezes: Int = 0,
)

sealed interface StreakCheck {
    data object Ok : StreakCheck
    data class FreezeUsed(val frozenDays: List<Long>) : StreakCheck
    data class Lost(val previous: Int) : StreakCheck
}

object StreakRules {
    const val MAX_FREEZES = 2
    val MILESTONES = listOf(3, 7, 14, 30, 50, 100, 200, 365, 500, 1000)

    /** サボった日があれば、ストリークフリーズで穴埋めするか、記録をリセットする。 */
    fun reconcile(state: StreakState, today: Long): Pair<StreakState, StreakCheck> {
        val last = state.lastActiveDay ?: return state to StreakCheck.Ok
        val missed = today - last - 1
        if (missed <= 0 || state.current == 0) return state to StreakCheck.Ok
        return if (missed <= state.freezes) {
            val frozen = (last + 1 until today).toList()
            state.copy(freezes = state.freezes - frozen.size, lastActiveDay = today - 1) to
                StreakCheck.FreezeUsed(frozen)
        } else {
            state.copy(current = 0) to StreakCheck.Lost(state.current)
        }
    }

    /** レッスン完了を記録する。戻り値の Boolean は「今日初めてストリークが伸びたか」。 */
    fun recordLesson(state: StreakState, today: Long): Pair<StreakState, Boolean> {
        val s = reconcile(state, today).first
        if (s.lastActiveDay == today) return s to false
        val next = if (s.current > 0 && s.lastActiveDay == today - 1) s.current + 1 else 1
        return s.copy(current = next, best = max(s.best, next), lastActiveDay = today) to true
    }
}

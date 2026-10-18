package com.ogapirlo.takken.data

import android.content.Context
import com.ogapirlo.takken.domain.Progress
import com.ogapirlo.takken.domain.QuestionStat
import com.ogapirlo.takken.domain.StreakState
import org.json.JSONArray
import org.json.JSONObject

/** 学習記録を SharedPreferences に JSON で保存する。 */
class ProgressStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("progress", Context.MODE_PRIVATE)

    fun load(): Progress {
        val raw = prefs.getString(KEY, null) ?: return Progress()
        return runCatching { decode(JSONObject(raw)) }.getOrElse { Progress() }
    }

    fun save(p: Progress) {
        prefs.edit().putString(KEY, encode(p).toString()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    private fun encode(p: Progress) = JSONObject().apply {
        put("streak", p.streak.current)
        put("best", p.streak.best)
        put("lastActive", p.streak.lastActiveDay ?: -1L)
        put("freezes", p.streak.freezes)
        put("gems", p.gems)
        put("totalXp", p.totalXp)
        put("dailyGoal", p.dailyGoal)
        put("todayXp", p.todayXp)
        put("todayDay", p.todayDay)
        put("lessons", p.lessonsCompleted)
        put("activeDays", JSONArray(p.activeDays.toList()))
        put("frozenDays", JSONArray(p.frozenDays.toList()))
        put("stats", JSONObject().apply {
            p.stats.forEach { (id, s) ->
                put(id, JSONArray(listOf(s.correct, s.wrong, when (s.lastCorrect) { true -> 1; false -> 0; null -> -1 })))
            }
        })
        put("reminderEnabled", p.reminderEnabled)
        put("reminderHour", p.reminderHour)
        put("reminderMinute", p.reminderMinute)
        put("onboarded", p.onboarded)
    }

    private fun decode(o: JSONObject): Progress {
        val statsJson = o.optJSONObject("stats") ?: JSONObject()
        val stats = statsJson.keys().asSequence().associateWith { id ->
            val a = statsJson.getJSONArray(id)
            QuestionStat(a.getInt(0), a.getInt(1), when (a.getInt(2)) { 1 -> true; 0 -> false; else -> null })
        }
        return Progress(
            streak = StreakState(
                current = o.optInt("streak"),
                best = o.optInt("best"),
                lastActiveDay = o.optLong("lastActive", -1L).takeIf { it >= 0 },
                freezes = o.optInt("freezes"),
            ),
            gems = o.optInt("gems"),
            totalXp = o.optInt("totalXp"),
            dailyGoal = o.optInt("dailyGoal", 10),
            todayXp = o.optInt("todayXp"),
            todayDay = o.optLong("todayDay", -1L),
            lessonsCompleted = o.optInt("lessons"),
            activeDays = o.optJSONArray("activeDays").toLongSet(),
            frozenDays = o.optJSONArray("frozenDays").toLongSet(),
            stats = stats,
            reminderEnabled = o.optBoolean("reminderEnabled", true),
            reminderHour = o.optInt("reminderHour", 20),
            reminderMinute = o.optInt("reminderMinute", 0),
            onboarded = o.optBoolean("onboarded", false),
        )
    }

    private fun JSONArray?.toLongSet(): Set<Long> =
        if (this == null) emptySet() else (0 until length()).map { getLong(it) }.toSet()

    private companion object {
        const val KEY = "progress_v1"
    }
}

package com.ogapirlo.takken.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakRulesTest {
    private val day = 20_000L

    @Test
    fun firstLessonStartsStreak() {
        val (s, extended) = StreakRules.recordLesson(StreakState(), day)
        assertEquals(1, s.current)
        assertEquals(1, s.best)
        assertTrue(extended)
    }

    @Test
    fun secondLessonSameDayDoesNotExtend() {
        val (s1, _) = StreakRules.recordLesson(StreakState(), day)
        val (s2, extended) = StreakRules.recordLesson(s1, day)
        assertEquals(1, s2.current)
        assertFalse(extended)
    }

    @Test
    fun consecutiveDaysExtend() {
        var s = StreakState()
        for (d in 0 until 5) s = StreakRules.recordLesson(s, day + d).first
        assertEquals(5, s.current)
        assertEquals(5, s.best)
    }

    @Test
    fun freezeCoversOneMissedDay() {
        val s = StreakState(current = 4, best = 4, lastActiveDay = day, freezes = 1)
        val (r, check) = StreakRules.reconcile(s, day + 2)
        assertEquals(StreakCheck.FreezeUsed(listOf(day + 1)), check)
        assertEquals(0, r.freezes)
        assertEquals(4, r.current)
        assertEquals(5, StreakRules.recordLesson(r, day + 2).first.current)
    }

    @Test
    fun streakLostWhenNotEnoughFreezes() {
        val s = StreakState(current = 10, best = 10, lastActiveDay = day, freezes = 1)
        val (r, check) = StreakRules.reconcile(s, day + 3)
        assertEquals(StreakCheck.Lost(10), check)
        assertEquals(0, r.current)
        assertEquals(1, r.freezes)
        val (after, _) = StreakRules.recordLesson(r, day + 3)
        assertEquals(1, after.current)
        assertEquals(10, after.best)
    }

    @Test
    fun reconcileIsIdempotent() {
        val s = StreakState(current = 3, lastActiveDay = day, freezes = 2)
        val (r1, _) = StreakRules.reconcile(s, day + 3)
        val (r2, check) = StreakRules.reconcile(r1, day + 3)
        assertEquals(StreakCheck.Ok, check)
        assertEquals(r1, r2)
    }
}

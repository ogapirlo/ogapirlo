package com.ogapirlo.takken.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressRulesTest {
    private val day = 20_000L
    private fun results(correct: Int) = (0 until 5).map { "q$it" to (it < correct) }

    @Test
    fun anyFinishedLessonGivesXpAndKeepsStreak() {
        val (p, s) = ProgressRules.applyLesson(Progress(), day, results(0))
        assertEquals(ProgressRules.XP_LESSON, s.xp)
        assertTrue(s.streakExtended)
        assertEquals(1, p.streak.current)
        assertTrue(p.doneOn(day))
        assertEquals(5, p.reviewCount)
    }

    @Test
    fun perfectLessonReachesDefaultGoal() {
        val (p, s) = ProgressRules.applyLesson(Progress(), day, results(5))
        assertEquals(15, s.xp)
        assertTrue(s.goalReachedNow)
        assertEquals(ProgressRules.GEMS_LESSON + ProgressRules.GEMS_PERFECT + ProgressRules.GEMS_GOAL, p.gems)
        assertEquals(0, p.reviewCount)
    }

    @Test
    fun goalBonusOnlyOncePerDay() {
        val (p1, _) = ProgressRules.applyLesson(Progress(), day, results(5))
        val (p2, s2) = ProgressRules.applyLesson(p1, day, results(5))
        assertFalse(s2.goalReachedNow)
        assertEquals(30, p2.xpOn(day))
        assertEquals(0, p2.xpOn(day + 1))
    }

    @Test
    fun wrongThenCorrectClearsReview() {
        val (p1, _) = ProgressRules.applyLesson(Progress(), day, listOf("a" to false))
        assertEquals(1, p1.reviewCount)
        val (p2, _) = ProgressRules.applyLesson(p1, day, listOf("a" to true))
        assertEquals(0, p2.reviewCount)
        assertEquals(QuestionStat(correct = 1, wrong = 1, lastCorrect = true), p2.stats["a"])
    }

    @Test
    fun milestoneOnThirdDay() {
        var p = Progress()
        var last: LessonSummary? = null
        for (d in 0 until 3) {
            val r = ProgressRules.applyLesson(p, day + d, results(3))
            p = r.first; last = r.second
        }
        assertEquals(3, last!!.milestone)
    }

    @Test
    fun buyFreezeRespectsPriceAndCap() {
        assertNull(ProgressRules.buyFreeze(Progress(gems = 10)))
        val bought = ProgressRules.buyFreeze(Progress(gems = 120, streak = StreakState(freezes = 1)))!!
        assertEquals(2, bought.streak.freezes)
        assertEquals(70, bought.gems)
        assertNull(ProgressRules.buyFreeze(bought))
    }
}

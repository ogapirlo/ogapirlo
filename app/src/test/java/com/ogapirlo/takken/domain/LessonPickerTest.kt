package com.ogapirlo.takken.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LessonPickerTest {
    private val pool = (1..20).map {
        Question("q$it", "宅建業法", QuestionType.CHOICE, "問$it", listOf("a", "b", "c", "d"), 0, "")
    }

    @Test
    fun picksFiveDistinct() {
        val picked = LessonPicker.pick(pool, emptyMap(), random = Random(1))
        assertEquals(5, picked.size)
        assertEquals(5, picked.map { it.id }.toSet().size)
    }

    @Test
    fun smallPoolReturnsAll() {
        assertEquals(3, LessonPicker.pick(pool.take(3), emptyMap()).size)
    }

    @Test
    fun mixesInUpToTwoReviewQuestionsAndPrefersUnseen() {
        val stats = buildMap {
            for (i in 1..4) put("q$i", QuestionStat(wrong = 1, lastCorrect = false))
            for (i in 5..17) put("q$i", QuestionStat(correct = 1, lastCorrect = true))
        }
        val ids = LessonPicker.pick(pool, stats, random = Random(7)).map { it.id }.toSet()
        assertEquals(2, ids.count { it in setOf("q1", "q2", "q3", "q4") })
        assertTrue(ids.containsAll(setOf("q18", "q19", "q20")))
    }

    @Test
    fun choiceShuffleKeepsCorrectAnswer() {
        val q = pool.first()
        repeat(20) {
            val item = LessonItem.from(q, Random(it))
            assertEquals(q.choices[q.answer], item.choices[item.answerIndex])
        }
    }
}

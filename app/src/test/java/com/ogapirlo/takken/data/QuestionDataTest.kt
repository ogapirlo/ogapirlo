package com.ogapirlo.takken.data

import com.ogapirlo.takken.domain.Categories
import com.ogapirlo.takken.domain.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 問題データ（assets/questions.json）の整合性チェック。問題を追加したらこのテストで確認できる。 */
class QuestionDataTest {
    private val questions = QuestionRepository.parse(File("src/main/assets/questions.json").readText())

    @Test
    fun questionsAreValid() {
        assertTrue(questions.size >= 50)
        assertEquals(questions.size, questions.map { it.id }.toSet().size)
        for (q in questions) {
            assertTrue(q.id, q.category in Categories.all)
            assertTrue(q.id, q.answer in q.choices.indices)
            assertTrue(q.id, q.text.isNotBlank() && q.explanation.isNotBlank())
            if (q.type == QuestionType.OX) assertEquals(q.id, 2, q.choices.size)
            else assertTrue(q.id, q.choices.size in 3..5)
        }
    }

    @Test
    fun everyCategoryHasAtLeastOneLesson() {
        for (c in Categories.all) assertTrue(c, questions.count { it.category == c } >= 5)
    }
}

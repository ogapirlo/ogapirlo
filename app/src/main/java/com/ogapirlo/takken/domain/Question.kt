package com.ogapirlo.takken.domain

enum class QuestionType { OX, CHOICE }

data class Question(
    val id: String,
    val category: String,
    val type: QuestionType,
    val text: String,
    val choices: List<String>,
    val answer: Int,
    val explanation: String,
    val source: String? = null,
)

object Categories {
    val all = listOf("権利関係", "宅建業法", "法令上の制限", "税・その他")

    fun emoji(category: String): String = when (category) {
        "権利関係" -> "⚖️"
        "宅建業法" -> "🏢"
        "法令上の制限" -> "🏗️"
        "税・その他" -> "💴"
        else -> "📘"
    }
}

data class QuestionStat(
    val correct: Int = 0,
    val wrong: Int = 0,
    val lastCorrect: Boolean? = null,
) {
    val needsReview: Boolean get() = lastCorrect == false
}

/** 1問分の出題。4択は選択肢をシャッフルし、正解位置を付け替える。 */
data class LessonItem(
    val question: Question,
    val choices: List<String>,
    val answerIndex: Int,
) {
    companion object {
        fun from(question: Question, random: kotlin.random.Random): LessonItem {
            if (question.type == QuestionType.OX) {
                return LessonItem(question, question.choices, question.answer)
            }
            val order = question.choices.indices.shuffled(random)
            return LessonItem(
                question = question,
                choices = order.map { question.choices[it] },
                answerIndex = order.indexOf(question.answer),
            )
        }
    }
}

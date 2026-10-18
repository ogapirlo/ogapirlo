package com.ogapirlo.takken.data

import android.content.Context
import com.ogapirlo.takken.domain.Question
import com.ogapirlo.takken.domain.QuestionType
import org.json.JSONArray

object QuestionRepository {
    private const val FILE = "questions.json"

    fun load(context: Context): List<Question> {
        val json = context.assets.open(FILE).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parse(json)
    }

    fun parse(json: String): List<Question> {
        val root = JSONArray(json)
        return (0 until root.length()).map { i ->
            val o = root.getJSONObject(i)
            val choices = o.getJSONArray("choices")
            Question(
                id = o.getString("id"),
                category = o.getString("category"),
                type = if (o.getString("type") == "ox") QuestionType.OX else QuestionType.CHOICE,
                text = o.getString("question"),
                choices = (0 until choices.length()).map { choices.getString(it) },
                answer = o.getInt("answer"),
                explanation = o.getString("explanation"),
                source = o.optString("source").ifBlank { null },
            )
        }
    }
}

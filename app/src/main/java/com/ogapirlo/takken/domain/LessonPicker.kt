package com.ogapirlo.takken.domain

import kotlin.random.Random

/**
 * 5問1セットの出題を組み立てる。
 * 前回間違えた問題を少し混ぜつつ、未出題 → 正解済みの順で優先してランダムに選ぶ。
 */
object LessonPicker {
    const val LESSON_SIZE = 5

    fun pick(
        pool: List<Question>,
        stats: Map<String, QuestionStat>,
        size: Int = LESSON_SIZE,
        maxReview: Int = 2,
        random: Random = Random.Default,
    ): List<Question> {
        if (pool.isEmpty()) return emptyList()
        val review = pool.filter { stats[it.id]?.needsReview == true }.shuffled(random)
        val unseen = pool.filter { stats[it.id] == null }.shuffled(random)
        val seen = pool.filter { stats[it.id]?.let { s -> !s.needsReview } == true }.shuffled(random)

        val picked = LinkedHashSet<Question>()
        picked += review.take(maxReview)
        for (q in unseen + seen + review) {
            if (picked.size >= size) break
            picked += q
        }
        return picked.toList().shuffled(random)
    }
}

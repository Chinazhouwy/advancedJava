package com.advancedjava.interview.concurrency;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * ConcurrentSkipListMap（跳表）示例。
 *
 * 学习点：
 * 1. 跳表是有序并发容器，适合“边写边查 TopN / 范围查询”场景。
 * 2. 相比 ConcurrentHashMap，跳表维护了全局有序性，但写入成本更高。
 */
public final class ConcurrentSkipListMapRankingDemo {

    private ConcurrentSkipListMapRankingDemo() {
    }

    public static void run() {
        System.out.println("\n=== 9) ConcurrentSkipListMap 跳表排名 ===");
        // key 为分数（倒序），value 为该分数下的候选人列表。
        ConcurrentSkipListMap<Integer, List<String>> ranking =
                new ConcurrentSkipListMap<>(java.util.Comparator.reverseOrder());

        addScore(ranking, 95, "Alice");
        addScore(ranking, 87, "Bob");
        addScore(ranking, 95, "Carla");
        addScore(ranking, 91, "David");
        addScore(ranking, 87, "Eve");

        int topN = 3;
        int count = 0;
        for (Map.Entry<Integer, List<String>> entry : ranking.entrySet()) {
            for (String name : entry.getValue()) {
                System.out.printf("rank-%d: %s, score=%d%n", ++count, name, entry.getKey());
                if (count >= topN) {
                    return;
                }
            }
        }
    }

    private static void addScore(ConcurrentSkipListMap<Integer, List<String>> ranking, int score, String name) {
        ranking.compute(score, (key, names) -> {
            if (names == null) {
                names = new java.util.concurrent.CopyOnWriteArrayList<>();
            }
            names.add(name);
            return names;
        });
    }
}

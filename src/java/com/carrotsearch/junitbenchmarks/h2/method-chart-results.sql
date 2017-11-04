
SELECT 
  NAME AS "method",
  ROUND(ROUND_AVG, 6) AS "Average time [s]", 
  ROUND(ROUND_STDDEV, 6) AS "StdDev",
  GC_TIME AS "GC time [s]",
  ROUND(GC_AVG, 2) AS "GC average [s]",
  ROUND(GC_STDDEV, 2) AS "StdDev",
  GC_INVOCATIONS AS "GC calls",
  ROUND(BENCHMARK_ROUNDS,6) AS "benchmark rounds",
  ROUND(ROUND(WARMUP_ROUNDS,6) AS "warmup rounds",
  ROUND(TIME_BENCHMARK ,6)AS "Total benchmark time",
  ROUND(TIME_WARMUP,6) AS "Total warmup time"
FROM TESTS T, RUNS R
WHERE R.ID = ?
  AND T.RUN_ID = R.ID
  AND CLASSNAME = ?

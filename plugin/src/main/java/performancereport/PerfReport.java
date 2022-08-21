package performancereport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerfReport {

    private static final Map<String, List<Long>> executionTimesPerId = new HashMap<>();
    private static final Map<String, Long> currentlyActiveTimers = new HashMap<>();

    public static void clear() {
        executionTimesPerId.clear();
    }

    public static void startTimer(String id) {
        currentlyActiveTimers.put(id, System.currentTimeMillis());
    }

    public static void endTimer(String id) {
        long startTime = currentlyActiveTimers.remove(id);
        if (startTime > 0) {
            executionTimesPerId.computeIfAbsent(id, ignored -> new ArrayList<>());
            executionTimesPerId.get(id).add(System.currentTimeMillis() - startTime);
        }
    }

    public static Map<String, Double> calculateAverageExecutionTimes() {
        Map<String, Double> averageExecutionTimes = new HashMap<>();
        executionTimesPerId.entrySet().forEach(entry -> {
            double averageTime = entry.getValue().stream().collect(Collectors.averagingLong(l -> l));
            averageExecutionTimes.put(entry.getKey(), averageTime);
        });

        return averageExecutionTimes;
    }
}

package de.b4sh.byter.utils.measurements.evaluators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.b4sh.byter.utils.measurements.Measurement;
import de.b4sh.byter.utils.measurements.PerformanceTimer;

/**
 * Builds the average on base of 50 Percent middle data.
 * 25 Percent in the beginning and 25 pecent at the end is ignored.
 * Evaluates measurements if there are 10 or more entries.
 */
public final class MidFieldAverageEvaluator implements Evaluator {
    @Override
    public Map<String, Float> evaluate(final PerformanceTimer perfTimer) {
        final List<Measurement> data =  perfTimer.getData();
        //check if the data list is below ten - if yes return new
        //an quantil evaluation with less then 10 values is not worth it
        if (data.size() < 10)
            return new HashMap<>();
        //do quantil check
        final Map<String, Float> response = new HashMap<>();
        float avgBytesPerSecond = 0;
        float avgMegaBytesPerSecond = 0;
        final int quantilStart = (int) (data.size() * 0.25);
        final int quantilStop = (int) (data.size() * 0.75);
        for(int i = quantilStart; i < quantilStop; i++){
            avgBytesPerSecond = (float) (avgBytesPerSecond + data.get(i).getBytePerSecound());
            avgMegaBytesPerSecond = avgMegaBytesPerSecond + data.get(i).getMegaBytePerSecond();
        }
        avgBytesPerSecond = avgBytesPerSecond / data.size();
        avgMegaBytesPerSecond = avgMegaBytesPerSecond / data.size();
        response.put("quantil25AvgBytesPerSecond", avgBytesPerSecond);
        response.put("quantil25AvgMegaBytesPerSecond", avgMegaBytesPerSecond);
        return response;
    }
}

package de.b4sh.byter.utils.measurements.evaluators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.b4sh.byter.utils.measurements.Measurement;
import de.b4sh.byter.utils.measurements.PerformanceTimer;

/**
 * Evaluator to calculate the Average processing-speed.
 */
public final class AverageEvaluator implements Evaluator {
    @Override
    public Map<String, Float> evaluate(final PerformanceTimer perfTimer) {
        final List<Measurement> data =  perfTimer.getData();
        final Map<String, Float> response = new HashMap<>();
        float avgBytesPerSecond = 0;
        float avgMegaBytesPerSecond = 0;
        //sum up
        for(Measurement m: data){
            avgBytesPerSecond += m.getBytePerSecound();
            avgMegaBytesPerSecond += m.getMegaBytePerSecond();
        }
        //get avg
        avgBytesPerSecond = avgBytesPerSecond / data.size();
        avgMegaBytesPerSecond = avgMegaBytesPerSecond / data.size();
        response.put("avgBytesPerSecond", avgBytesPerSecond);
        response.put("avgMegaBytesPerSecond", avgMegaBytesPerSecond);
        return response;
    }
}

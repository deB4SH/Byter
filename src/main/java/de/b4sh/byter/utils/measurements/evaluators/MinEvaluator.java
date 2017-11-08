package de.b4sh.byter.utils.measurements.evaluators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.b4sh.byter.utils.measurements.Measurement;
import de.b4sh.byter.utils.measurements.PerformanceTimer;

/**
 * Find the min-value inside the tracked measurements.
 */
public final class MinEvaluator implements Evaluator {
    @Override
    public Map<String, Float> evaluate(final PerformanceTimer perfTimer) {
        final List<Measurement> data = perfTimer.getData();
        float lowestTransmissionRate = Float.MAX_VALUE;
        int lowestTransmissionPackage = Integer.MAX_VALUE;
        for(Measurement m: data){
            if(m.getBytePerSecound() < lowestTransmissionRate)
                lowestTransmissionRate = (float) m.getBytePerSecound();

            if(m.getDataLength() < lowestTransmissionPackage)
                lowestTransmissionPackage = m.getDataLength();
        }
        //build response
        final Map<String, Float> response = new HashMap<>();
        response.put("lowestTransmissionRate",lowestTransmissionRate);
        response.put("lowestPackageSize", (float) lowestTransmissionPackage);
        return response;
    }
}

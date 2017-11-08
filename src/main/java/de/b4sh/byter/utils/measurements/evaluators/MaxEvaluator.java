package de.b4sh.byter.utils.measurements.evaluators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.b4sh.byter.utils.measurements.Measurement;
import de.b4sh.byter.utils.measurements.PerformanceTimer;

/**
 * Find the max-value inside the tracked measurements.
 */
public final class MaxEvaluator implements Evaluator {
    @Override
    public Map<String, Float> evaluate(final PerformanceTimer perfTimer) {
        final List<Measurement> data = perfTimer.getData();
        float highestTransmissionRate = 0;
        int highestTransmissionPackage = 0;
        for(Measurement m: data){
            if(m.getBytePerSecound() > highestTransmissionRate)
                highestTransmissionRate = (float) m.getBytePerSecound();

            if(m.getDataLength() > highestTransmissionPackage)
                highestTransmissionPackage = m.getDataLength();
        }
        //build response
        final Map<String, Float> response = new HashMap<>();
        response.put("highestTransmissionRate",highestTransmissionRate);
        response.put("highestPackageSize", (float) highestTransmissionPackage);
        return response;
    }
}

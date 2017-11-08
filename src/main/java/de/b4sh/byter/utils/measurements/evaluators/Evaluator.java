package de.b4sh.byter.utils.measurements.evaluators;

import java.util.Map;

import de.b4sh.byter.utils.measurements.PerformanceTimer;

/**
 * Evaluator is an interface for evaluation on performance measurements.
 */
public interface Evaluator {
    /**
     * evaluate given performance timer.
     * @param perfTimer performance timer with tracked informations
     * @return Map with Key:Value of evaluation
     */
    Map<String,Float> evaluate(PerformanceTimer perfTimer);
}

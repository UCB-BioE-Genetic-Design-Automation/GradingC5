package org.ucb.bio134.gradingc5.model;

import java.util.Map;

/**
 *
 * @author J. Christopher Anderson
 */
public class Report {
    private final Map<String, Result> tesstclassToResult;

    public Report(Map tesstclassToResult) {
        this.tesstclassToResult = tesstclassToResult;
    }

    public Map<String, Result> getTesstclassToResult() {
        return tesstclassToResult;
    }
    
    
}

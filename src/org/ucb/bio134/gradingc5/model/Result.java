package org.ucb.bio134.gradingc5.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author J. Christopher Anderson
 */
public class Result {
    private final String testname;
    private final long runtime;
    private final List<String> failures;

    public Result(String testname, long runtime, List<String> failures) {
        this.testname = testname;
        this.runtime = runtime;
        this.failures = failures;
    }

    public long getRuntime() {
        return runtime;
    }

    public List<String> getFailures() {
        return failures;
    }

    public String getTestname() {
        return testname;
    }
}

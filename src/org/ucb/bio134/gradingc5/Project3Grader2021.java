package org.ucb.bio134.gradingc5;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ucb.bio134.gradingc5.model.Report;
import org.ucb.bio134.gradingc5.model.Result;

/**
 *
 * @author J. Christopher Anderson
 */
public class Project3Grader2021 {

    private Map<String, Report> nameToReport;

    private Map<String, Integer> testToGradingDeduction;
    private Map<String, Integer> testToMousepadDeduction;

    public void initiate() throws Exception {
        nameToReport = new HashMap<>();

        //Parse the test results
        String subs = FileUtils.readFile("/Users/jca20n/Downloads/submissions.txt");
        String[] stdts = subs.split(">>");

        for (String stdtsub : stdts) {
            if (stdtsub.isEmpty()) {
                continue;
            }
            if(stdtsub.indexOf(">", 2) == -1) {
//                nameToReport.put(stdtsub.substring(2), null);
                continue;
            }
            try {
                int cutoff = stdtsub.indexOf(">");
                String studentname = stdtsub.substring(0, cutoff).trim();
                stdtsub = stdtsub.substring(cutoff);
                Report report = parseReport(stdtsub);
                nameToReport.put(studentname, report);
            } catch (Exception err) {
                System.out.println(stdtsub);
                throw err;
            }
        }

        //Parse the rubric
        testToGradingDeduction = new HashMap<>();
        testToMousepadDeduction = new HashMap<>();

        String data = readResourceFile("rubric3.txt");
        String[] lines = data.split("\\r|\\r?\\n");
        for (int i = 1; i < lines.length; i++) {
            String aline = lines[i];
            if (aline.trim().isEmpty()) {
                continue;
            }
            String[] tabs = aline.split("\t");
            String testname = tabs[0];
            int gradingDeduction = Integer.parseInt(tabs[4]);
            int mousepadDeduction = Integer.parseInt(tabs[5]);

            testToGradingDeduction.put(testname, gradingDeduction);
            testToMousepadDeduction.put(testname, mousepadDeduction);
        }
    }

    private Report parseReport(String text) {
        Map<String, Result> results = new HashMap<>();

        String[] regions = text.split(">");
        for (int i = 0; i < regions.length; i++) {
            String region = regions[i].trim();
            if (region.length() < 4) {
                continue;
            }
            Result res = parseResult(region);
            results.put(res.getTestname(), res);
        }

        return new Report(results);
    }

    private Result parseResult(String region) {
        Long runtime = null;
        List<String> failures = new ArrayList<>();

        //Parse it
        String[] lines = region.split("\\r|\\r?\\n");
        String testname = lines[0];

        //Find the runtime line
        for (String line : lines) {
            if (line.contains("runtime")) {
                String[] colons = line.split(":");
                runtime = Long.parseLong(colons[1].trim());
            }
        }

        //Parse the failures
        int failIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("failure")) {
                failIndex = i;
                break;
            }
        }
        for (int i = failIndex + 1; i < lines.length; i++) {
            failures.add(lines[i].trim());
        }

        if (runtime == null) {
            System.err.println(region);
        }

        return new Result(testname, runtime, failures);
    }

    public void run() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("name").append("\t").append("grade_deduction").append("\t").append("mousepad_deduction").append("\t").append("rnntime").append("\n");

        //For each student submission
        for (String studentName : nameToReport.keySet()) {
            Report report = nameToReport.get(studentName);
            List<String> allFails = extractAllFails(report);
            int gradeDeduction = scoreGrading(allFails);
            String gradesummary = null;
            try {
                gradesummary = aggregateGradingFails(allFails);
            } catch (Exception err) {
                System.out.println("Failed on studentName: " + studentName);
                throw err;
            }
            int mousepadDeduction = scoreMousepad(allFails);
            String mousesummary = aggregateMousepadFails(allFails);
            long totalRuntime = scoreRuntime(report);

            sb.append(studentName).append("\t").append(gradeDeduction).append("\t").append(mousepadDeduction).append("\t").append(totalRuntime).append("\t").append(gradesummary).append("\t").append(mousesummary).append("\n");
        }

        FileUtils.writeFile(sb.toString(), "Project3Results2021.txt");
    }

    private void countUpAndPrintResults() throws Exception {
        //Count up the number of fails on each test
        Map<String, Integer> testnameToCount = new HashMap<>();

        for (String student : nameToReport.keySet()) {
            Report report = nameToReport.get(student);
            if(report == null) {
                continue;
            }
            for (String testclass : report.getTesstclassToResult().keySet()) {
                Result result = report.getTesstclassToResult().get(testclass);
                List<String> failures = result.getFailures();
                for (String testname : failures) {
                    int value = 0;
                    if (testnameToCount.containsKey(testname)) {
                        value = testnameToCount.get(testname);
                    }
                    value++;
                    testnameToCount.put(testname, value);
                }
            }
        }

        for (String testname : testnameToCount.keySet()) {
            int count = testnameToCount.get(testname);
            System.out.println(testname + "  " + count);
        }
    }

    private List<String> extractAllFails(Report report) {
        List<String> out = new ArrayList<>();
        for (String testClass : report.getTesstclassToResult().keySet()) {
            Result result = report.getTesstclassToResult().get(testClass);
            out.addAll(result.getFailures());
        }
        return out;
    }

    private static String readResourceFile(String relPath) throws Exception {
        URL url = new Project3Grader2021().getClass().getResource(relPath);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream()));

        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine).append("\n");
        }
        in.close();

        return sb.toString();
    }

    private int scoreGrading(List<String> allFails) {
        int out = 0;
        for (String testname : allFails) {
            int deduction = 0;
            try {
                deduction = this.testToGradingDeduction.get(testname);
            } catch (Exception err) {
                System.err.println("-- error on " + testname);
            }
            out += deduction;
        }
        return out;
    }

    private int scoreMousepad(List<String> allFails) {
        int out = 0;
        for (String testname : allFails) {
            if (!testToMousepadDeduction.containsKey(testname)) {
                continue;
            }
            int deduction = this.testToMousepadDeduction.get(testname);
            out += deduction;
        }
        return out;
    }

    private long scoreRuntime(Report report) {
        long out = 0;
        for (String testClass : report.getTesstclassToResult().keySet()) {
            Result result = report.getTesstclassToResult().get(testClass);
            long runtime = result.getRuntime();
            out += runtime;
        }
        return out;
    }

    private String aggregateGradingFails(List<String> allFails) {
        StringBuilder out = new StringBuilder();
        for (String testname : allFails) {
            if (!testToGradingDeduction.containsKey(testname)) {
                System.out.println("Unknown testname: " + testname);
                continue;
            }
            if (this.testToGradingDeduction.get(testname) > 0) {
                out.append(testname).append(", ");
            }
        }
        return out.toString();
    }

    private String aggregateMousepadFails(List<String> allFails) {
        StringBuilder out = new StringBuilder();
        for (String testname : allFails) {
            if (!testToMousepadDeduction.containsKey(testname)) {
                continue;
            }
            if (this.testToMousepadDeduction.get(testname) > 0) {
                out.append(testname).append(", ");
            }
        }
        return out.toString();
    }

    public static void main(String[] args) throws Exception {
        Project3Grader2021 grader = new Project3Grader2021();
        grader.initiate();

        //Print out the test counts
        grader.countUpAndPrintResults();

        //Grade each student submission
        grader.run();

        System.out.println("done");
    }

}

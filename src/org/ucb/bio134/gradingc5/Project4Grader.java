package org.ucb.bio134.gradingc5;

import java.io.BufferedReader;
import java.io.File;
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
public class Project4Grader {
    private  Map<String, Report> nameToReport;
    
    private Map<String, Integer> testToGradingDeduction;
    private Map<String, Integer> testToMousepadDeduction;
    private Map<String, Prices> nameToPrices;
    

    public void initiate() throws Exception {
        nameToPrices = new HashMap<>();
        nameToReport = new HashMap<>();
        
        //Parse the test results
        File dir = new File("/Users/jca20n/Downloads/submissions");
        for (File afile : dir.listFiles()) {
            
            try {
                //Use JSoup to read the HTML
                String text = FileUtils.readFile(afile.getAbsolutePath());
                String studentname = afile.getName().replaceAll(".txt", "");
                String[] sections = text.split("<><><><><>");
                if(sections.length!=3) {
                    System.out.println();
                }
                
                try {
                    Prices prices = new Prices(sections[0].trim());
                    nameToPrices.put(studentname, prices);
                } catch(Exception err) {
                    System.out.println("Error on student " + studentname);
                    System.out.println("price info:\n" + sections[0]);
                    throw err;
                }
                
                Report report = parseReport(sections[2].trim());
                nameToReport.put(studentname, report);
            } catch(Exception err) {
                System.out.println("Err:  " + afile.getName());
                err.printStackTrace();
            }
        }
        
//        countUpAndPrintResults();
        
        //Parse the rubric
        testToGradingDeduction = new HashMap<>();
        testToMousepadDeduction = new HashMap<>();
        
        String data = readResourceFile("rubric4.txt");
        String[] lines = data.split("\\r|\\r?\\n");
        for(int i=1; i<lines.length; i++) {
            String aline = lines[i];
            if(aline.trim().isEmpty()) {
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
        for(int i=0; i<regions.length; i++) {
            String region = regions[i].trim();
            if(region.length()<4) {
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
        for(String line : lines) {
            if(line.contains("runtime")) {
                String[] colons = line.split(":");
                runtime = Long.parseLong(colons[1].trim());
            }
        }
        
        //Parse the failures
        int failIndex = 0;
        for(int i=0; i<lines.length; i++) {
            if(lines[i].contains("failure")) {
                failIndex = i;
                break;
            }
        }
        for(int i=failIndex+1; i<lines.length; i++) {
            String afail = lines[i].trim();
            if(!afail.isEmpty()) {
                failures.add(afail);
            }
        }
        
        if(runtime == null) {
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
            String gradesummary = aggregateGradingFails(allFails);
            int mousepadDeduction = scoreMousepad(allFails);
            String mousesummary = aggregateMousepadFails(allFails);
            long totalRuntime = scoreRuntime(report);

            sb.append(studentName).append("\t").append(gradeDeduction).append("\t").append(mousepadDeduction).append("\t").append(totalRuntime).append("\t").append(gradesummary).append("\t").append(mousesummary).append("\n");
        }

        FileUtils.writeFile(sb.toString(), "Project4Results.txt");
        
        sb = new StringBuilder();
        sb.append("student\tBasicConversionTest\tMastermixTest\tOptimizationTest\tPriceCalculatorTest\tSemiprotocolPriceSimulatorTest\n");
        for(String studentName : nameToPrices.keySet()) {
            Prices prices = nameToPrices.get(studentName);
            sb.append(studentName).append("\t");
            sb.append(prices.BasicConversionTest).append("\t");
            sb.append(prices.MastermixTest).append("\t");
            sb.append(prices.OptimizationTest).append("\t");
            sb.append(prices.PriceCalculatorTest).append("\t");
            sb.append(prices.SemiprotocolPriceSimulatorTest).append("\n");
        }
        FileUtils.writeFile(sb.toString(), "Project4_prices.txt");
    }
    
    private void countUpAndPrintResults() throws Exception {
         //Count up the number of fails on each test
        Map<String,Integer> testnameToCount = new HashMap<>();
        
        for (String student : nameToReport.keySet()) {
            Report report = nameToReport.get(student);
            for(String testclass : report.getTesstclassToResult().keySet()) {
                Result result = report.getTesstclassToResult().get(testclass);
                List<String> failures = result.getFailures();
                for(String testname : failures) {
                    int value = 0;
                    if(testnameToCount.containsKey(testname)) {
                        value = testnameToCount.get(testname);
                    }
                    value++;
                    testnameToCount.put(testname, value);
                }
            }
        }
        
        for(String testname : testnameToCount.keySet()) {
            int count = testnameToCount.get(testname);
            System.out.println(testname + "  " + count);
        }
    }
    
    private List<String> extractAllFails(Report report) {
        List<String> out = new ArrayList<>();
        for(String testClass : report.getTesstclassToResult().keySet()) {
            Result result = report.getTesstclassToResult().get(testClass);
            out.addAll(result.getFailures());
        }
        return out;
    }
    
    private static String readResourceFile(String relPath) throws Exception {
        URL url = new Project3Grader().getClass().getResource(relPath);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream()));

        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            sb.append(inputLine).append("\n");
        in.close();
        
        return sb.toString();
    }
    
    
    public static void main(String[] args) throws Exception {
        Project4Grader grader = new Project4Grader();
        grader.initiate();
        
        //Print out the test counts
        grader.countUpAndPrintResults();
        
        //Grade each student submission
        grader.run();
    }

    private int scoreGrading(List<String> allFails) {
        int out = 0;
        for(String testname : allFails) {
            int deduction = this.testToGradingDeduction.get(testname);
            out += deduction;
        }
        return out;
    }

    private int scoreMousepad(List<String> allFails) {
        int out = 0;
        for (String testname : allFails) {
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
        for(String testname : allFails) {
            if(this.testToGradingDeduction.get(testname) > 0) {
                out.append(testname).append(", ");
            }
        }
        return out.toString();
    }
    
    private String aggregateMousepadFails(List<String> allFails) {
        StringBuilder out = new StringBuilder();
        for(String testname : allFails) {
            if(this.testToMousepadDeduction.get(testname) > 0) {
                out.append(testname).append(", ");
            }
        }
        return out.toString();
    }

}

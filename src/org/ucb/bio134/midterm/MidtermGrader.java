package org.ucb.bio134.midterm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ucb.bio134.gradingc5.FileUtils;

/**
 *
 * @author J. Christopher Anderson
 */
public class MidtermGrader {
    private Map<String,Map<String,Integer>> questionToAnswers;
    private Map<String,Map<String,String>> studentToResponses;
    private final int BASEGRADE = 35;
            
    public void initiate() throws Exception {
        populateRubric();
        populateExamAnswers();
    }
    
    private void populateRubric() throws Exception {
        questionToAnswers = new HashMap<>();
        String keytext = FileUtils.readFile("/Users/jca20n/Dropbox (Personal)/Course - Genetic Design Automation/Grading/midterm_key.txt");
        String[] regions = keytext.split(">");
        for(String region : regions) {
            String[] lines = region.split("\\r|\\r?\\n");
            String q1 = lines[0];
            Map<String,Integer> answers = questionToAnswers.get(q1);
            if(answers==null) {
                answers = new HashMap<>();
            }
            
            for(int i=1; i<lines.length; i++) {
                try {
                String[] colons = lines[i].trim().split(":");
                String abcd = colons[0].trim();
                int score = Integer.parseInt(colons[1].trim());
                answers.put(abcd,score);
                } catch(Exception err) {
                    System.out.println("error on" + lines[i]);
                    throw err;
                }
            }
            questionToAnswers.put(q1, answers);
        }
    }
    
    private void populateExamAnswers() throws Exception {
        studentToResponses = new HashMap<>();
        String examstext = FileUtils.readFile("/Users/jca20n/Dropbox (Personal)/Course - Genetic Design Automation/Grading/midterm_answers.txt");
        String[] lines = examstext.split("\\r|\\r?\\n");
        
        //Process the question names
        String[] qnames = lines[0].split("\t");
        
        for(int i=1; i<lines.length; i++) {
            String aline = lines[i];
            String[] tabs = aline.split("\t");
            String student = tabs[0];
            
            //Populate the student's exam answers
            Map<String,String> answers = new HashMap<>();
            for(int j=4; j<tabs.length; j++) {
                String answer = tabs[j];
                String qname = qnames[j];
                answers.put(qname, answer);
            }
            studentToResponses.put(student, answers);
        }
    }
    
    public void run() throws Exception {
        StringBuilder report = new StringBuilder();
        for(String student : studentToResponses.keySet()) {
            int grade = gradeStudent(report, student, studentToResponses.get(student));
        }
        FileUtils.writeFile(report.toString(), "BioE134-2017-midterm.txt");
    }
    
    private int gradeStudent(StringBuilder report, String student, Map<String, String> responses) {
        int out = BASEGRADE;
        
        StringBuilder answers = new StringBuilder();
        for(String question : responses.keySet()) {
            String answer = responses.get(question);
            Integer score = this.questionToAnswers.get(question).get(answer);
            if(score == null) {
                System.out.println("Missing score: " + student + " " + question + " " + answer);
                System.exit(0);
            }
            
            answers.append(question).append(":").append(answer).append(" +").append(score).append("\n");
            out+=score;
        }
        
        report.append(">").append(student).append("\t").append(out).append("\n");
        report.append(answers).append("\n\n\n");
        return out;
    }
    
    public static void main(String[] args) throws Exception {
        MidtermGrader grader = new MidtermGrader();
        grader.initiate();
        grader.run();
    }


}

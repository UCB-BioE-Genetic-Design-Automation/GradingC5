package org.ucb.bio134.midterm;

import java.util.HashMap;
import java.util.Map;
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
        StringBuilder gradebook = new StringBuilder();
        for(String student : studentToResponses.keySet()) {
            int grade = gradeStudent(gradebook, student, studentToResponses.get(student));
        }
        FileUtils.writeFile(gradebook.toString(), "BioE134-2017-midterm.txt");
        
        //Collect statistics
        StringBuilder report = collectStatistics();
        FileUtils.writeFile(report.toString(), "BioE134-2017-midterm-stats.txt");
    }
    
    private int gradeStudent(StringBuilder gradebook, String student, Map<String, String> responses) {
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
        
        gradebook.append(">").append(student).append("\t").append(out).append("\n");
        gradebook.append(answers).append("\n\n\n");
        return out;
    }
    
    private StringBuilder collectStatistics() {
        StringBuilder out = new StringBuilder();
        
        HashMap<String, Map<String, Integer>> questionToCounts = new HashMap<>();
        
        //Count up each answer's occurance
        for(String student : studentToResponses.keySet()) {
            Map<String,String> responses = studentToResponses.get(student);
            for(String question : responses.keySet()) {
                String answer = responses.get(question);
                Map<String, Integer> answerToCount = questionToCounts.get(question);
                if(answerToCount == null) {
                    answerToCount = new HashMap<>();
                }
                Integer count = answerToCount.get(answer);
                if(count == null) {
                    count = 0;
                }
                count++;
                answerToCount.put(answer, count);
                questionToCounts.put(question,answerToCount);
            }
        }
        
        //Create the report
        for(String question : questionToCounts.keySet()) {
            out.append("\n>").append(question).append("\n");
            
            Map<String, Integer> answerToCount = questionToCounts.get(question);
            for(String answer : answerToCount.keySet()) {
                Integer count = answerToCount.get(answer);
                double percentage = 1.0*count/studentToResponses.size();
//                double rounded = Math.round(percentage*100)/100;
                out.append("").append(answer).append("\t").append(percentage).append("\n");
            }
        }
        return out;
    }
    
    public static void main(String[] args) throws Exception {
        MidtermGrader grader = new MidtermGrader();
        grader.initiate();
        grader.run();
    }



}

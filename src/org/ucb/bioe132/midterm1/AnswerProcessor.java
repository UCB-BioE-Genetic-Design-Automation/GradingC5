package org.ucb.bioe132.midterm1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.ucb.bio134.gradingc5.FileUtils;

/**
 *
 * @author J. Christopher Anderson
 */
public class AnswerProcessor {

    Map<String, Set<String>> questionsToAnswerSet = new HashMap<>();
    
    public void run() throws Exception {
        String examstext = FileUtils.readFile("/Users/jca20n/Dropbox (Personal)/Courses/132/Devices 18/Grading/Spr18_BioE132_answers.txt");
        examstext = examstext.replaceAll("\"", "");
        String[] lines = examstext.toUpperCase().split("\\r|\\r?\\n");

        //Process the question names
        String[] qnames = lines[0].split("\t");

        for (int i = 1; i < lines.length; i++) {
            String aline = lines[i];
            String[] tabs = aline.split("\t");
            String student = tabs[0];

            //Populate the student's exam answers
            for (int j = 3; j < tabs.length; j++) {
                String answer = tabs[j];
                String qname = qnames[j];
                Set<String> answers = questionsToAnswerSet.get(qname);
                if(answers==null) {
                    answers = new HashSet<>();
                }
                answers.add(answer);
                questionsToAnswerSet.put(qname, answers);
            }
        }
        
        //Output the results
        StringBuilder out = new StringBuilder();
        for(String qname : questionsToAnswerSet.keySet()) {
            out.append(">").append(qname).append("\n");
            Set<String> answers = questionsToAnswerSet.get(qname);
            for(String ans : answers) {
                out.append(ans).append(":0\n");
            }
        }
        
        FileUtils.writeFile(out.toString(), "all_answers_132.txt");
    }

    public static void main(String[] args) throws Exception {
        AnswerProcessor grader = new AnswerProcessor();
        grader.run();
    }
}

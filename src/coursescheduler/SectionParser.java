package coursescheduler;

import java.util.ArrayList;
import java.util.List;
import org.ucb.bio134.gradingc5.FileUtils;

/**
 *
 * @author J. Christopher Anderson
 */
public class SectionParser {

    public List<LabSection> run(String data) throws Exception {
        List<LabSection> out = new ArrayList<>();
        
        //Read each line, parse a LabSection
        String[] lines = data.trim().split("\\r|\\r?\\n");
        for (int i = 1; i < lines.length; i++) {
            String aline = lines[i];
            LabSection sect = parseSection(aline.trim());
            out.add(sect);
        }
        
        return out;
    }

    private LabSection parseSection(String data) {
        String[] tabs  = data.split("\t");
        
        //Handle the name of the section
        String name = tabs[0];
        
        //Handle the days
        String days = tabs[1];
        boolean[] daysdata = new boolean[5];
        for(int i=0; i<5; i++) {
            daysdata[i] = false;
        }
        
        //Handle thursday then delete it
        if(days.contains("Th")) {
            daysdata[3] = true;
        }
        days = days.replaceAll("Th", "");
        
        if(days.contains("M")) {
            daysdata[0] = true;
        }
        if(days.contains("T")) {
            daysdata[1] = true;
        }
        if(days.contains("W")) {
            daysdata[2] = true;
        }
        if(days.contains("F")) {
            daysdata[4] = true;
        }
              
        //Handle times
        double start = Double.parseDouble(tabs[2]);
        double end = Double.parseDouble(tabs[3]);
        
        //Handle number of students
        int numStuds = Integer.parseInt(tabs[4]);
        
        return new LabSection(name, daysdata, start, end, numStuds);
    }
    
    public static void main(String[] args) throws Exception {
        String data = FileUtils.readFile("lab_class_sections.txt");
        List<LabSection> sections = new SectionParser().run(data);

        System.out.println();
    }

}

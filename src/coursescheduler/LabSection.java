package coursescheduler;

/**
 *
 * @author J. Christopher Anderson
 */
public class LabSection {
    private final String name;
    private final boolean[] days; //ie, [tftftf] is MWF
    private final double starttime;  //ie, 9.5 is 9:30, 15 is 3pm
    private final double endtime; //ie, 11 is 11 am
    private final int numStudents;

    public LabSection(String name, boolean[] days, double starttime, double endtime, int numStudents) {
        this.name = name;
        this.days = days;
        this.starttime = starttime;
        this.endtime = endtime;
        this.numStudents = numStudents;
    }

    public String getName() {
        return name;
    }

    public boolean[] getDays() {
        return days;
    }

    public double getStarttime() {
        return starttime;
    }

    public double getEndtime() {
        return endtime;
    }
}

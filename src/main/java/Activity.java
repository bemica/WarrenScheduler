// import Camper;
import java.util.ArrayList;
import java.util.Comparator;

public class Activity implements Comparable<Activity>{
  public String name;
  public int period;
  public ArrayList<Camper> campers;
  private double camperCounselorRatio;
  public double maxCampers;

  public Activity(String name, int period, double maxCampers){
    this.name = name;
    this.period = period;
    this.maxCampers = maxCampers;
    this.campers = new ArrayList<Camper>();
  }

  public Activity(String name, int period, double ratio, int max){
    System.out.println("activity compliling right");
  }

  public double getRatio(){
    return camperCounselorRatio;
  }

  public boolean putCamper(Camper newCamper){
    campers.add(newCamper);
    return false;
  }

  public int compareTo(Activity otherAct){
    if (otherAct.campers.size() - this.campers.size() < 0){
      return -1;
    } else {
      return 1;
    }
  }

  public static class ActComparator implements Comparator<Activity>{
    public int compare(Activity act1, Activity act2){
      if (act1.campers.size() - act2.campers.size() < 0){
        return -1;
      } else {
        return 1;
      }
    }
  }

  public String toString(){
    return name + period;
  }
}

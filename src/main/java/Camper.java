//import Activity;
import java.util.ArrayList;
import java.util.List;

class Camper {

  public String firstName;
  public String lastName;
  private Activity[] activities;
  private String firstChoice;
  private String secondChoice;
  private List<String> choices;
  public int age;
  public int id;

  /*
    Constructor for Camper
    Give first and last name, and list all their activity choices
  */
  public Camper(String firstName, String lastName, ArrayList<String> allChoices, int id){
    firstChoice = allChoices.get(0);
    secondChoice = allChoices.get(1);
    choices = allChoices.subList(2, allChoices.size());
    activities = new Activity[4];
    this.firstName = firstName;
    this.lastName = lastName;
    this.id = id;
  }

//  public String stringActs(){
//    for (int i = 0; i < 4)
//  }

  public void place(int choiceNum, List<Activity> acts) throws TooManyCampersException, NoMatchingActivitiesFoundException{

    if (choiceNum == 1 && this.emph()) {
      this.placeString(this.firstChoice + "emph1", acts);
      this.placeString(this.firstChoice + "emph2", acts);
    } else if (choiceNum == 2 && this.emph()) {
      // do nothing??
    } else if (choiceNum == 1) {
      this.placeString(firstChoice, acts);
    } else if (choiceNum == 2) {
      this.placeString(secondChoice, acts);
    } else {
      this.placeString(choices.get(choiceNum - 3), acts);
    }

  }

  //places camper in matching act with lowest ratio
  private void placeString(String actName, List<Activity> acts) throws TooManyCampersException, NoMatchingActivitiesFoundException{
    ArrayList<Activity> matching = new ArrayList<Activity>();

    for (Activity act : acts){
      if (act.name.equals(actName)){
        matching.add(act);
      }
    }

    matching.sort(new Activity.ActComparator());
    //System.out.println(actName);
    if (matching.size() == 0){
      throw new NoMatchingActivitiesFoundException(actName);
    }
    Activity act = matching.get(0);

    for(int i = 0; i < matching.size(); i++) {
      if (act.campers.size() >= act.maxCampers) {
        throw new TooManyCampersException();
      }else if (activities[act.period - 1] != null) {
      } else {
        act.putCamper(this);
        activities[act.period - 1] = act;
        break;
      }
    }
  }

  // returns true if the camper has four activities scheduled
  public boolean scheduled(){
    return activities[0] != null && activities[1] != null
            && activities[2] != null && activities[3] != null;
  }

  //returns true if a camper put the same activity for their first and second
  public boolean emph(){
    return firstChoice.equals(secondChoice);
  }

  public String toString(){
    return firstName + lastName + activities[0] + activities[1] + activities[2] + activities[3];
  }

  public String getAct(int i){
    if (activities[i - 1] == null){
      return "";
    }

    return activities[i - 1].name;
  }
}

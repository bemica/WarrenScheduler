import java.util.Comparator;

class Camper implements Comparable<Camper> {

	private String firstName;
	private String lastName;
	private Activity[] activities;

	private String[] choices;
	private boolean isEmph;
	private int age;
	private int id;

	public Camper(String firstName, String lastName, String[] choices, 
				  int id, boolean isEmph) {
		this.choices = choices.clone();
		activities = new Activity[4];
		this.firstName = firstName;
		this.lastName = lastName;
		this.id = id;
		this.isEmph = isEmph; 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + age;
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + id;
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Camper other = (Camper) obj;
		if (age != other.age)
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (id != other.id)
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		return true;
	}

	// Adds an activity to a camper's schedule.
	public void addActivity(Activity act, int period) {
		activities[period] = act;
	}

	// returns true if the camper has four activities scheduled
	public boolean isScheduled(){
		return activities[0] != null && activities[1] != null
				&& activities[2] != null && activities[3] != null;
	}
	
	// Compares campers based on buddy number.
	public static class CamperComparator implements Comparator<Camper>{
		public int compare(Camper camper1, Camper camper2){
			if (camper1.getID() - camper2.getID() < 0) return -1;
			else return 1;
		}
	}

	// Compares campers based on buddy number.
	@Override
	public int compareTo(Camper otherCamper) {
		if(this.id - otherCamper.getID() < 0) return -1;
		else return 1;
	}
	
	public String toString(){
		return firstName+ " " + lastName + this.sayActivities();
	}

	public String sayActivities() {
		StringBuilder sb = new StringBuilder();
		sb.append("  [");
		for(Activity a : activities) sb.append(a + ",");
		sb.append("]");
		return sb.toString();
	}
	
	// Says activity choices.
	public String sayChoices() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t");
		for(String a : choices) {
			if(a.equals("")) {
				sb.append("NULL, ");
			} else {
				sb.append(a + ", ");
			}
		}
		return sb.toString();
	}
	
	public void setChoice(int i, String s) {
		choices[i] = s;
	}

	public Activity getActivity(int i) {
		return activities[i];
	}
	
	public String getAct(int i){
		if (activities[i - 1] == null){
			return "";
		}
		return activities[i - 1].name;
	}
	
	public String[] getChoices() {
		return choices;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public boolean getIsEmph() {
		return isEmph;
	}
	
	public String getChoice(int choice) {
		return choices[choice];
	}
	
	public int getID() {
		return id;
	}
}

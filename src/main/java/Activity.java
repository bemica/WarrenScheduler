import java.util.ArrayList;
import java.util.Comparator;

public class Activity implements Comparable<Activity>{

	public String name;
	public int period;
	public ArrayList<Camper> campers;

	public double maxCampers;
	public boolean isEmph;
	private boolean isVariable;
	
	// TODO Move to Schedule should it become necessary.
	private int activityCount;

	public Activity(String name, int period, double maxCampers, boolean isVariable){
		this.name = name;
		this.period = period;
		this.maxCampers = maxCampers;
		this.isVariable = isVariable;
		this.campers = new ArrayList<Camper>();
	}

	public Activity(String name, int period, double maxCampers, boolean isEmph, boolean isVariable) {
		this.name = name;
		this.period = period;
		this.maxCampers = maxCampers;
		this.isEmph = isEmph;
		this.campers = new ArrayList<Camper>();
	}

	// Adds a camper to the list of campers.
	public void putCamper(Camper newCamper){
		campers.add(newCamper);
		newCamper.addActivity(this, this.period);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + period;
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
		Activity other = (Activity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (period != other.period)
			return false;
		return true;
	}

	/**
	 * Method checks if camper exists in activity already.
	 * @param camper : Camper we're looking for.
	 * @return : True if camper is in activity, false otherwise.
	 */
	public boolean findCamper(Camper camper) {
		for(Camper c : campers) {
			if(c.equals(camper)) return true;
		}
		return false;
	}
	
	public String toString(){
		return name + " " +
			   (period + 1)
//			   +
//			   " with: (" +
//			   campers.size() +
//			   "/" +
//			   maxCampers +
//			   ")"
			   ;
	}

	// Lists campers in activity.
	public void listCampers() {
		for(Camper c : campers)
			System.out.print(c + ", ");
	}

	// Compares activities based on their size.
	public static class ActComparator implements Comparator<Activity>{
		public int compare(Activity act1, Activity act2){
			if (act1.campers.size() - act2.campers.size() < 0) return -1;
			else return 1;
		}
	}
	
	// Compares activities based on their size.
	public int compareTo(Activity otherAct){
		if(otherAct == null) return 1;

		if (otherAct.campers.size() - this.campers.size() < 0)  return -1;
	    else if(otherAct.campers.size() - this.campers.size() == 0) return 0;
		else return 1;
	}

	public int getPeriod() {
		return period;
	}
	
	public boolean getIsVariable() {
		return this.isVariable;
	}
	
	public int getActivityCount() {
		return this.activityCount;
	}

	public double getMaxCampers() {
		return this.maxCampers;
	}

	public String getName() {
		return name;
	}

	public int getActivitySize() {
		return campers.size();
	}
	
	public void setActivityCount(int activityCount) {
		this.activityCount = activityCount;
	}
	
	public void setPeriod(int period) {
		this.period = period;
	}
}

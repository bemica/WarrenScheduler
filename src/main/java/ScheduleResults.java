import java.util.ArrayList;

public class ScheduleResults {
	private boolean isValid;
	public ArrayList<Camper> problemCampers;
	
	public ScheduleResults() {
		problemCampers = new ArrayList<Camper>();
	}
	
	public boolean getIsValid() {
		return this.isValid;
	}
	
	public void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}
}

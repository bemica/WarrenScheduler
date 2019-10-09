public class ScheduleResults {
	private boolean isValid;
	private int unscheduledCampers;
	
	public ScheduleResults() {
		unscheduledCampers = 0;
	}
	
	public boolean getIsValid() {
		return this.isValid;
	}
	
	public void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}
	
	public int getUnscheduledCampers() {
		return unscheduledCampers;
	}
	
	public void incrementUnscheduledCampers() {
		unscheduledCampers++;
	}
}

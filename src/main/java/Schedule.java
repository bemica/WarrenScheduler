import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Schedule {
	public HashMap<String, Activity[]> allActivities;
	public ArrayList<Camper> allCampers;
	private int optimizedScore;
	private Random rng;

	public Schedule(ArrayList<Camper> allCampers, HashMap<String, Activity[]> allActivities) {
		rng = new Random();

		this.allCampers = Scheduler.copyCampers(allCampers);
		this.allActivities = Scheduler.copyActivities(allActivities);

		optimizedScore = 0;
	}

	/**
	 * This method schedules the first two activities, the ones
	 * that the campers are guaranteed to have. 
	 */
	public void scheduleGuaranteed(ArrayList<Camper> problemCampers) {

		// TODO Determine whether this makes sense algorithmically.
		shuffleCampers(problemCampers);

		Activity[] firstChoiceSchedule = null;
		Activity[] secondChoiceSchedule = null;
		for(int i = 0; i < allCampers.size(); i++) {
			Camper current = allCampers.get(i);

			// Gets the schedule and the activity count of each choices.
			firstChoiceSchedule = this.allActivities.get(current.getChoice(0));
			int firstCount = getActivityCount(firstChoiceSchedule);

			secondChoiceSchedule = this.allActivities.get(current.getChoice(1));
			int secondCount = getActivityCount(secondChoiceSchedule);

			// Schedules the activity with the fewest periods first.
			try {
				if(firstCount >= secondCount) {
					scheduleCamper(current, secondChoiceSchedule);
					scheduleCamper(current, firstChoiceSchedule);
				} else {
					scheduleCamper(current, firstChoiceSchedule);
					scheduleCamper(current, secondChoiceSchedule);
				}
			} catch(IndexOutOfBoundsException e) {
				// Nothing happens.
				// TODO Cleanup.
			}
		}
	}

	/**
	 * This method fills out the rest of the schedule.
	 * @return : a boolean representing whether the schedule is valid or not.
	 */
	public ScheduleResults fillOutSchedule() {
		ScheduleResults toReturn = new ScheduleResults();

		// Schedule remaining activities.
		ArrayList<Integer> activities = new ArrayList<Integer>();
		for(int i = 2; i < 6; i++) {
			activities.add(i);
		}

		// Randomly selects which activity choice to schedule next.
		// TODO Room for algorithmic optimization.
		for(int i = 0; i < 4; i++) {
			//			shuffleCampers(new ArrayList<Camper>());
			int random = rng.nextInt(activities.size());
			scheduleActivity(activities.get(random));
			activities.remove(random);
		}

		// Schedule Validity Check
		for(Camper c : allCampers) {
			if(!c.isScheduled()) {

				for(int i = 0; i < allCampers.size(); i++) {
					if(!allCampers.get(i).isScheduled()) {
						toReturn.problemCampers.add(allCampers.get(i));
						allCampers.remove(i);
						i--;
					}
				}

				toReturn.setIsValid(false);

				//System.out.println("Score: " + "(" + optimizedScore + "/"+ theoreticalMax() + ")");
				//System.out.println("Campers not scheduled: " + count);
				return toReturn;
			}
		}

		System.out.println("\n\nGO FUCKING CRAZY THE SCHEDULE WORKS");
		System.out.println("Optimized score: " + "(" + optimizedScore + "/"+ theoreticalMax() + "\n\n");
		toReturn.setIsValid(true);
		return toReturn;
	}

	/**
	 * Calculates the theoretical max value of each camper getting their ideal schedule:
	 * Activity choices 1, 2, 3, 4.
	 * @return : An integer representing that value.
	 */
	private int theoreticalMax() {
		return (3*allCampers.size()) + (4*allCampers.size());
	}

	/**
	 * This method schedules a specific activity choice.
	 * @param act : The activity choice we're attempting to schedule.
	 */
	private void scheduleActivity(int choice) {
		Activity[] activitySchedule = null;

		for(int i = 0; i < allCampers.size(); i++) {
			Camper current = allCampers.get(i);

			activitySchedule = allActivities.get(current.getChoice(choice));

			try { 
				scheduleCamper(current, activitySchedule);
			} catch(IndexOutOfBoundsException e) {
				// Couldn't schedule camper in given activity. Moving on...
				int value = Scheduler.failedActivitySchedule.get(current.getChoice(choice)) + 1;
				Scheduler.failedActivitySchedule.put(current.getChoice(choice), value);
				continue;
			}

			optimizedScore += choice;
		}
	}

	/**
	 * Shuffles the array of campers using Fisher-Yates.
	 */
	private void shuffleCampers(ArrayList<Camper> problemCampers) {
		int switchIndex = allCampers.size() - 1;
		int roll; 
		Camper temp;


		while(switchIndex != 0) {
			roll = rng.nextInt(switchIndex);
			temp = allCampers.get(roll);

			allCampers.set(roll, allCampers.get(switchIndex));
			allCampers.set(switchIndex, temp);

			switchIndex--;
		}
	}

	// Shuffles activities part 1.
	public void shuffleActivities() {
		Scheduler.actPeriodCounter = new int[4];

		for(Map.Entry<String, Activity[]> item : allActivities.entrySet()) {
			Activity[] actSchedule = item.getValue();
			inner:
				for(Activity a : actSchedule) {
					if(a != null) {
						if(a.getIsVariable()) {
							allActivities.put(item.getKey(), shuffleActivity(item.getValue()));
							break inner;
						} else {
							Scheduler.actPeriodCounter[a.getPeriod()]++;
						}
					}
				}
		}
	}

	// Shuffles activities part 2. 
	private Activity[] shuffleActivity(Activity[] act) {
		ArrayList<Activity> temp = new ArrayList<Activity>(3);
		Activity[] toReturn = new Activity[4];

		for(Activity a : act) {
			if(a != null) temp.add(a);
		}

		// Important. This prevents the activity shuffler from backing itself 
		// into a corner and endlessly looping. 
		int failCount = 0;

		outer:
			for(Activity a : temp) {
				int targetPlace;
				failCount = 0;

				inner:
					while(failCount < 200) {
						targetPlace = rng.nextInt(4);

						if(toReturn[targetPlace] == null &&
								Scheduler.actPeriodCounter[targetPlace] < 12) {
							toReturn[targetPlace] = a;
							a.setPeriod(targetPlace);
							Scheduler.actPeriodCounter[targetPlace]++;
							continue outer;
						} else {
							failCount++;
						}

						continue inner;
					}
			}

		// We've failed at shuffling. Need to fix counts. 
		if(failCount == 200) {
			cleanupActivityShuffle(toReturn, act);
			return act;
		}

		return toReturn;
	}

	/**
	 * This method cleans up our failed activity shuffle. 
	 * @param toReturn : What we're returning.
	 * @param acts : Activity we need to clean.
	 */
	private static void cleanupActivityShuffle(Activity[] toReturn, Activity[] acts) {
		for(int i = 0; i < acts.length; i++) {
			if(acts[i] != null) {
				acts[i].setPeriod(i);
				Scheduler.actPeriodCounter[i]++;
			}
		}

		for(int i = 0; i < toReturn.length; i++) {
			if(toReturn[i] != null) {
				Scheduler.actPeriodCounter[i]--;
			}
		}
	}

	// What's better...
	// 1. Randomly selecting an activity regardless of size.
	// 2. Randomly selecting an activity from list of similarly sized ones?
	// For our use case, I'm thinking the second one. 
	/**
	 * This method schedules the camper in an activity.
	 * @param camper : The camper we're scheduling.
	 * @param activities : The activities available.
	 * @throws IndexOutOfBoundsException : Camper cannot be scheduled in this activity.
	 */
	private void scheduleCamper(Camper camper, Activity[] activities) throws IndexOutOfBoundsException {
		ArrayList<Activity> sorted = findSmallestAct(activities);

		int sameActivitySize = findSimilarSize(sorted);

		// Checking activities of the same size.
		while(sameActivitySize != -1) {
			int target = sameActivitySize == 0 ? 0 : rng.nextInt(sameActivitySize);

			// condition ? result : otherwise
			if(canSchedule(sorted.get(target), camper)) {
				sorted.get(target).putCamper(camper);
				return;
			}

			sorted.remove(target);
			sameActivitySize--;
		}

		// Checking all other activities.
		if(sorted.size() != 0) {
			for(Activity a : sorted) {
				if(canSchedule(a, camper)) {
					a.putCamper(camper);
					return;
				}
			}
		}

		throw new IndexOutOfBoundsException();
	}

	private int findSimilarSize(ArrayList<Activity> activities) {
		int counter = 0;

		for(int i = 1; i < activities.size(); i++) {
			if(activities.get(i).getActivitySize() ==
					activities.get(0).getActivitySize()) counter++;
		}

		return counter;
	}

	/**
	 * Checks whether the camper can be scheduled in this activity or not.
	 * @param a : The activity we're trying to schedule the camper in.
	 * @param camper : The camper we're trying to schedule. 
	 * @return : Boolean representing whether or not we can schedule the camper.
	 */
	private boolean canSchedule(Activity a, Camper camper) {
		// Camper isn't already in activity, camper has a spot in schedule, activity isn't at capacity.
		return !(a.findCamper(camper)) &&
				(camper.getActivity(a.getPeriod()) == null) &&
				a.getActivitySize() + 1 <= a.getMaxCampers();
	}

	/**
	 * This method sorts the activities passed in based on size.
	 * @param activity : The array of activities.
	 * @return : The activities sorted from smallest to largest size.
	 */
	private ArrayList<Activity> findSmallestAct(Activity[] activity) {

		// Copying.
		ArrayList<Activity> ordered = new ArrayList<Activity>();
		for(Activity a : activity) {
			if(a != null) ordered.add(a);
		}

		// Comparing.
		Activity.ActComparator ac = new Activity.ActComparator();
		ordered.sort(ac);
		return ordered;
	}

	/**
	 * Looks for the first non-null activity in the array for activity count.
	 * @param act : The activities we're looking through.
	 * @return : The number of this activity that occur during the day.
	 */
	private int getActivityCount(Activity[] act) {
		for(Activity a : act) {
			if(a != null) {
				return a.getActivityCount();
			}
		}
		return 0;
	}

	public int getOptimizedScore() {
		return optimizedScore;
	}
}

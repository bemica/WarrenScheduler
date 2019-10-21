import java.awt.Color;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;

public class Scheduler implements Runnable {

	public static HashMap<String, Activity[]> allActivitiesMaster;
	public static ArrayList<Camper> allCampersMaster;

	public static int[] actPeriodCounter;
	private static int[] activitySpotCounter;

	public static HashMap<String, Integer> failedActivitySchedule;

	private static ArrayList<Integer> changedChoices;

	private static OutputStream fileout = null;
	private static FileInputStream filein = null;
	private static File inputFile = null;

	private static XSSFWorkbook workbook = null;

	private static SchedulerGUI display;
	
	public static double[] finalData = new double[3];


	private Thread t;
	private String threadName;

	public Scheduler(String name) {
		threadName = name;
	}

	public void start () {
		if (t == null) {
			t = new Thread (this, threadName);
			t.start ();
		}
	}

	public static void main(String[] args) {
		display = new SchedulerGUI();
		display.begin();
	}

	@Override
	public void run() {
		this.inputFile = display.getInputFile();
		schedule(display.getIterations(), inputFile);

	}


	/**
	 * This method schedules campers in activities.
	 * @param iterations : The number of iterations we are doing.
	 * @param inputFile : The file from which we're getting our info.
	 */
	public static void schedule(int iterations, File inputFile){
		actPeriodCounter = new int[4];
		changedChoices = new ArrayList<Integer>();

		finalData[2] = iterations;
		initializeInputFile(inputFile);

		XSSFSheet activities = null;
		XSSFSheet camperChoices = null;

		// Assuming first sheet is activities and second sheet is camper choices.
		activities = workbook.getSheetAt(0);
		camperChoices = workbook.getSheetAt(1);

		// Reading in our data.
		parseCampers(camperChoices);
		parseActivities(activities);
		checkActivitySpots();

		try {
			filein.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// First attempt will hold our first valid schedule.
		Schedule keepSchedule = null;
		ScheduleResults keepResults = null;

		// Optimized will hold our schedule that is valid and an attempt to optimize.
		Schedule variableSchedule = null;
		ScheduleResults variableResults = null;

		// Trying to create our first schedule.
		System.out.println("First numbers are number of unscheduled campers,\n second numbers are optimized scores");
		for(int i = 0; i < iterations; i++) {

			if(i % (iterations/100) == 0) { 
				display.setProgressBarValue(display.getProgressBarValue() + 1);
			}

			if(i % 1000 == 0)System.out.println("Iteration: " + i);

			//System.out.println(i);
			variableSchedule = new Schedule(allCampersMaster, allActivitiesMaster);

			// If we have a failed schedule, pass in the problem campers from that iteration.
			variableSchedule.shuffleActivities();
			variableSchedule.scheduleGuaranteed(new ArrayList<Camper>());

			// Schedule works.
			variableResults = variableSchedule.fillOutSchedule();
			if(variableResults.getIsValid()) {
				System.out.println("Successfully created one valid schedule!");
				System.out.println("Attempting to optimize...");
				System.exit(1);
				break;
			} 
			// Schedule doesn't work, check if it's the best we've found so far.
			// then trash it. 
			else {
				if(keepSchedule == null) {
					keepSchedule = variableSchedule;
					keepResults = variableResults;
					continue;
				}

				if(keepResults.getUnscheduledCampers() > variableResults.getUnscheduledCampers()) {
					System.out.println("Found better schedule: " + keepResults.getUnscheduledCampers() + " to " + variableResults.getUnscheduledCampers() +
							" " + keepSchedule.getOptimizedScore() + "->" + variableSchedule.getOptimizedScore());
					keepSchedule = variableSchedule;
					keepResults = variableResults;
					
				} else if(keepResults.getUnscheduledCampers() == variableResults.getUnscheduledCampers() &&
						keepSchedule.getOptimizedScore() > variableSchedule.getOptimizedScore()) {
					System.out.println("Found better schedule: " + keepResults.getUnscheduledCampers() + " to " + variableResults.getUnscheduledCampers() +
							" " + keepSchedule.getOptimizedScore() + "->" + variableSchedule.getOptimizedScore());
					keepSchedule = variableSchedule;
					keepResults = variableResults;
				} else continue;
			}

		}
		
		finalData[0] = keepResults.getUnscheduledCampers();
		finalData[1] = ((double)keepSchedule.getOptimizedScore() / (double)keepSchedule.theoreticalMax());


		System.out.println("\nThese are the number of times I failed scheduling campers in activities:");
		System.out.println("A significantly large number means increasing spots in this activity could \n"
				+ "greatly increase the accuracy of the schedule.");
		for(Map.Entry<String, Integer> item : failedActivitySchedule.entrySet()) {
			System.out.println(item.getKey() + ": " + item.getValue());
		}
		System.out.println("\n");

		if(keepSchedule != null) {
			writeToFile(keepSchedule.allCampers, camperChoices, activities, keepSchedule.allActivities);
		}

		cleanup();
		sayFinished();
	}

	/**
	 * Deep copies the ArrayList passed in. 
	 * @param campers : ArrayList to be copied. 
	 * @return : Copy of the parameter ArrayList.
	 */
	public static ArrayList<Camper> copyCampers(ArrayList<Camper> campers) {
		ArrayList<Camper> toReturn = new ArrayList<Camper>();

		for(Camper c : campers) {
			Camper copy = new Camper(c.getFirstName(), 
					c.getLastName(), 
					c.getChoices(),
					c.getID(),
					c.getIsEmph());
			toReturn.add(copy);
		}

		return toReturn;
	}

	// TODO Look into more algorithmically efficient method of copying. 
	/**
	 * This method copies the list of Activities.
	 * @param activities : The HashMap to be copied.
	 * @return : The copy of the HashMap.
	 */
	public static HashMap<String, Activity[]> copyActivities(HashMap<String, Activity[]> activities) {
		HashMap<String, Activity[]> toReturn = new HashMap<String, Activity[]>();
		Activity[] copy = null;
		Activity[] toCopy = null;

		// Copying each entry in the map. s
		for(Entry<String, Activity[]> item : activities.entrySet()) {
			copy = new Activity[4];
			toCopy = item.getValue();

			// Copying activities in the array.
			for(int i = 0; i < toCopy.length; i++) {
				if(toCopy[i] != null) {
					Activity copied = new Activity(toCopy[i].getName(), 
							toCopy[i].getPeriod(), 
							toCopy[i].getMaxCampers(),
							toCopy[i].getIsVariable());
					copy[i] = copied;
				} else copy[i] = null;
			}

			toReturn.put(item.getKey(), copy);
		}

		return toReturn;
	}

	/**
	 * This method opens a connection to the user specified input file.
	 * @param inputFile
	 */
	public static void initializeInputFile(File spreadFile) {
		// Opening connection.
		try {
			filein = new FileInputStream(spreadFile);
			workbook = new XSSFWorkbook(filein);
		} catch (FileNotFoundException e) {
			String message = "I'm sorry. The file you have \n" +
					"selected cannot be found.\n" + "Please try selecting a new one.";
			JOptionPane.showMessageDialog(new JFrame(), message, "File Not Found",
					JOptionPane.ERROR_MESSAGE);
			spreadFile = SchedulerGUI.selectFile();
		} catch (IOException e) {
			String message = "I'm sorry. The file you have \n" +
					"selected cannot be opened.\n" + "Please make sure the file is closed and try again.";
			JOptionPane.showMessageDialog(new JFrame(), message, "IO Exception",
					JOptionPane.ERROR_MESSAGE);
			spreadFile = SchedulerGUI.selectFile();
		}
	}

	/**
	 * This method safely closes the file connections opened during the program.
	 */
	private static void cleanup() {

		if(filein != null) {

			try {
				filein.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(fileout != null) {

			try {
				fileout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Schedule generation finished.");
	}

	private static void sayFinished() {		
		DecimalFormat df = new DecimalFormat("#.###");
		
		String message =("In " + (int)finalData[2] + " iterations I was able to"
						+ " create a schedule with only\n " + (int)finalData[0] 
						+ " unscheduled campers and an optimized score of " + df.format(finalData[1]));
		JOptionPane.showMessageDialog(new JFrame(), message, "Finished Scheduling Campers",
				JOptionPane.INFORMATION_MESSAGE);
		//PLAIN_MESSAGE
	}

	/**
	 * This method reads in the Activity data from the inputted Excel File.
	 * @param actSheet : The sheet detailing the activities.
	 */
	private static void parseActivities(XSSFSheet actSheet){
		allActivitiesMaster = new HashMap<String, Activity[]>();
		failedActivitySchedule = new HashMap<String, Integer>();

		activitySpotCounter = new int[4];

		// the first row is a header, so start at the second one
		int rowIndex = 1;
		XSSFRow row;

		//as soon as we hit the first empty row, we break
		while ((row = actSheet.getRow(rowIndex++)) != null){
			int activityCount = 0;

			String actName;
			double maxCampers;
			boolean isVariable;

			try {
				actName = row.getCell(0).getStringCellValue().toLowerCase();
				maxCampers = row.getCell(1).getNumericCellValue();
				isVariable = row.getCell(6).toString().equals("Yes") ? true : false;
			} catch(NullPointerException e) {
				// There was some junk in the activities sheet. Let's ignore it.
				System.out.println("Found a junk line. Ignorning.");
				continue;
			}

			failedActivitySchedule.put(actName, 0);

			// The times when each activity will be offered. 
			Activity[] activityCatalog = new Activity[4];

			for(int i = 2; i < 6; i++) {
				// Activity exists during this period.
				if(row.getCell(i).getStringCellValue().toLowerCase().equals("yes")) {
					activityCatalog[i - 2] = new Activity(actName, i-2, maxCampers, isVariable);
					activityCount++;
					actPeriodCounter[i-2]++;
					activitySpotCounter[i-2] += maxCampers;
				}
				else
					activityCatalog[i - 2] = null;
			}

			// Number of times that activity occurs during the day. 
			for(Activity a : activityCatalog)
				if(a != null) a.setActivityCount(activityCount);

			allActivitiesMaster.put(actName, activityCatalog);
		}
	}

	private static void checkActivitySpots() {
		for(int i = 0; i < activitySpotCounter.length; i++) {
			if(activitySpotCounter[i]-1 < allCampersMaster.size()) {
				sayInvalidActivitySpots(i, allCampersMaster.size() - (activitySpotCounter[i] - 1));
				cleanup();
				System.exit(1);
			}
		}
	}

	private static void sayInvalidActivitySpots(int activity, int spots) {
		String message = "Currently there are too many campers and too few spots in activities.\n"
				+ "Please ensure that the number of spots in activities exceeds \n"
				+ "the number of campers total and try again.\n\n"
				+ "Specifically, there are " + spots + " too few spots in activity: " + (activity + 1);
		JOptionPane.showMessageDialog(new JFrame(), message, "File Not Found",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * This method reads in the camper data provided by the excel sheet. 
	 * @param camperSheet : The sheet leading to the camper data and choices.
	 */
	private static void parseCampers(XSSFSheet camperSheet) {
		allCampersMaster = new ArrayList<Camper>();

		// Row 0 is a header. 
		int rowIndex = 1;

		int idNumber = 100;

		// List terminates when two blank rows are encountered.
		boolean lastEmpty = false;

		ArrayList<Integer> blankChoices;

		XSSFRow row;
		outer:
			while(true) {

				row = camperSheet.getRow(rowIndex);

				String fname, lname;

				try {
					fname = row.getCell(2).toString();
					lname = row.getCell(1).toString();
				} catch(NullPointerException e) {
					fname = "";
					lname = "";
				}

				if(fname.equals("") && lname.equals("")) {
					if(lastEmpty) {
						// hit two blank rows consecutively: stop.
						break outer;
					} else {
						lastEmpty = true;
						rowIndex++;
						continue;
					}
				} else
					lastEmpty = false;

				String[] choices = new String[6];
				blankChoices = new ArrayList<Integer>();

				// Reading in camper activity choices. 
				for(int k = 0; k < 6; k++) {
					choices[k] = row.getCell(k+3).toString();
					if(choices[k].equals(""))
						blankChoices.add(k);
				}

				// TODO Add in emphasis checks. 
				boolean isEmph = false;
				if(choices[0].equals(choices[1])) isEmph = true;

				Camper c = new Camper(fname, lname, choices, idNumber, isEmph);
				idNumber++;

				// Checks if the camper has any choices that are blank.
				if(blankChoices.size() != 0) {
					fixBlankChoices(blankChoices, c);
				}

				allCampersMaster.add(c);

				rowIndex++;
			}
	}

	/**
	 * This method prompts the user to fix campers with blank activity choices.
	 * @param blanks : The spaces where the blanks exist.
	 * @param camper : The camper whose choices have the blanks.
	 */
	private static void fixBlankChoices(ArrayList<Integer> blanks, Camper camper) {
		for(int i = 0; i < blanks.size(); i++) {
			String s = (String)JOptionPane.showInputDialog(
					new JFrame(),
					"Current activities: \n" + camper.sayChoices() + 
					"\n\nEnter a new activity for them.",
					camper.getFirstName() + " " + camper.getLastName() +
					" has a blank activity choice",
					JOptionPane.WARNING_MESSAGE,
					null,
					null,
					"");

			camper.setChoice(blanks.get(i), s);
			changedChoices.add(camper.getID());

			// TODO need to add in some way to validate activity inputs here.
			// Two choices: One, wait till activities have been parsed and then
			// 				move blank activity logic to that point in time.
			//				Two, somehow compile a list of the activities before this.
		}
	}

	/**
	 * This method writes the schedule we've come up with to file. 
	 * @param campers : The campers we've stored.
	 * @param camperSheet : The sheet leading to where we got the camper data.
	 * @param activitiesSheet : The sheet leading to where we got the activities.
	 * @param activities : The activities we've stored. 
	 */
	private static void writeToFile(ArrayList<Camper> campers, XSSFSheet camperSheet, XSSFSheet activitiesSheet, HashMap<String, Activity[]> activities){
		try {
			fileout = new FileOutputStream(inputFile);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		XSSFRow row;
		XSSFCell cell;
		int rowCounter = 1;

		// Sorts campers by predetermined buddy number. 
		Camper.CamperComparator cc = new Camper.CamperComparator();
		campers.sort(cc);

		// Writes campers to file. 
		for(Camper c : campers) {
			if(camperSheet.getRow(rowCounter).getCell(0).toString().equals("")) {
				rowCounter++;
			}

			row = camperSheet.getRow(rowCounter);

			for(int k = 0; k < 4; k++) {
				cell = row.getCell(k+9);
				if(c.getActivity(k) == null) {
					cell.setCellValue("NULL");
					continue;
				}
				row.getCell(k+9).setCellValue(c.getActivity(k).getName());
			}

			// Checks if the camper's activity choices have been altered since start.
			if(changedChoices.size() != 0 && changedChoices.get(0) == c.getID()) {
				for(int i = 0; i < 6; i++) {
					row.getCell(i + 3).setCellValue(c.getChoice(i));
				}
				changedChoices.remove(0);
			}

			rowCounter++;
		}

		// Writes activities to file. 
		System.out.println("\nHere are the activities I'm writing to file:");
		for(int i = 0; i < activities.size(); i++) {
			row = activitiesSheet.getRow(i+1);
			Activity[] target = activities.get(row.getCell(0).toString());
			if(target == null) continue;
			System.out.println(row.getCell(0).toString());
			for(int j = 0; j < 4; j++) {
				if(target[j] != null) {
					row.getCell(j+2).setCellValue("Yes");
				} else {
					row.getCell(j+2).setCellValue("No");
				}
			}
		}
		System.out.println("\n");

		try {
			workbook.write(fileout);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

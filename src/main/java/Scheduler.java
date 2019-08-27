
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;

public class Scheduler {

	public static HashMap<String, Activity[]> allActivitiesMaster;
	public static ArrayList<Camper> allCampersMaster;

	public static int[] actPeriodCounter;
	private static int[] activitySpotCounter;

	public static HashMap<String, Integer> failedActivitySchedule;

	private static OutputStream fileout = null;
	private static FileInputStream filein = null;
	private static File spreadFile;

	private static XSSFWorkbook workbook = null;
	
	public static void main(String[] args){
		actPeriodCounter = new int[4];

		chooseInputFile();

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
		for(int i = 0; i < 7500; i++) {
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
				
				if(keepResults.problemCampers.size() > variableResults.problemCampers.size()) {
					System.out.println("Found better schedule: " + keepResults.problemCampers.size() + " to " + variableResults.problemCampers.size() +
							 " " + keepSchedule.getOptimizedScore() + "->" + variableSchedule.getOptimizedScore());
					keepSchedule = variableSchedule;
					keepResults = variableResults;
				} else if(keepResults.problemCampers.size() == variableResults.problemCampers.size() &&
						keepSchedule.getOptimizedScore() > variableSchedule.getOptimizedScore()) {
					System.out.println("Found better schedule: " + keepResults.problemCampers.size() + " to " + variableResults.problemCampers.size() +
							 " " + keepSchedule.getOptimizedScore() + "->" + variableSchedule.getOptimizedScore());
					keepSchedule = variableSchedule;
					keepResults = variableResults;
				} else continue;
			}

		}

		for(Map.Entry<String, Integer> item : failedActivitySchedule.entrySet()) {
			System.out.println(item.getKey() + ": " + item.getValue());
		}

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
	 * This method guides the user through selecting a file to read data from. 
	 */
	public static void chooseInputFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

		JDialog parent = new JDialog();
		int result = fileChooser.showOpenDialog(parent);

		if(result == JFileChooser.APPROVE_OPTION) {
			spreadFile = fileChooser.getSelectedFile();

			// Format check.
			if(!(spreadFile.getName().contains(".xlsx") ||
					spreadFile.getName().contains(".xlsm"))) {
				 String message = "I'm sorry. The file you have \n" +
						 "selected is an invalid file type.\n" + "Please try selecting a valid .xlsx or .xlsm file.";
							    JOptionPane.showMessageDialog(new JFrame(), message, "Invalid File Format",
							        JOptionPane.ERROR_MESSAGE);
							   chooseInputFile();
			}

			// Opening connection.
			try {
				filein = new FileInputStream(spreadFile);
				workbook = new XSSFWorkbook(filein);
			} catch (FileNotFoundException e) {
				 String message = "I'm sorry. The file you have \n" +
				 "selected cannot be found.\n" + "Please try selecting a new one.";
					    JOptionPane.showMessageDialog(new JFrame(), message, "File Not Found",
					        JOptionPane.ERROR_MESSAGE);
					   chooseInputFile();
			} catch (IOException e) {
				String message = "I'm sorry. The file you have \n" +
						 "selected cannot be opened.\n" + "Please make sure the file is closed and try again.";
							    JOptionPane.showMessageDialog(new JFrame(), message, "IO Exception",
							        JOptionPane.ERROR_MESSAGE);
							   chooseInputFile();
			}
		} else System.exit(0);
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

		System.out.println("Goodbye!");
		System.exit(0);
	}
	
	private static void sayFinished() {
        JOptionPane pane = new JOptionPane();
        JDialog dialog = pane.createDialog(pane, "Done");
        pane.setMessage("Finished scheduling campers");
        dialog.setVisible(true);
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
			System.out.println(activitySpotCounter[i]);
			if(activitySpotCounter[i]-1 < allCampersMaster.size()) {
				sayInvalidActivitySpots();
				cleanup();
				System.exit(1);
			}
		}
	}
	
	private static void sayInvalidActivitySpots() {
		String message = "Currently there are too many campers and too few spots in activities.\n"
				+ "Please ensure that the number of spots in activities exceeds \n"
				+ "the number of campers total and try again.";
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

		XSSFRow row;
		outer:
			while(true) {

				row = camperSheet.getRow(rowIndex);
				String fname = row.getCell(2).toString();
				String lname = row.getCell(1).toString();

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

				// Reading in camper activity choices. 
				for(int k = 0; k < 6; k++) {
					choices[k] = row.getCell(k+3).toString();
				}

				// TODO Add in emphasis checks. 
				boolean isEmph = false;
				if(choices[0].equals(choices[1])) isEmph = true;
				
				Camper c = new Camper(fname, lname, choices, idNumber, isEmph);
				idNumber++;

				allCampersMaster.add(c);

				rowIndex++;
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
			fileout = new FileOutputStream(spreadFile);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		XSSFRow row;
		XSSFCell cell;
		int blankCounter = 0;
		
		// Sorts campers by predetermined buddy number. 
		Camper.CamperComparator cc = new Camper.CamperComparator();
		campers.sort(cc);
		
		// Writes campers to file. 
		for(Camper c : campers) {
			row = camperSheet.getRow(c.getID()-(99 - blankCounter));
			
			if(row.getCell(0).toString().equals("")) {
				row = camperSheet.getRow(c.getID() - (99 - (blankCounter + 1)));
				blankCounter++;
			}
			
			for(int k = 0; k < 4; k++) {
				cell = row.getCell(k+9);
				if(c.getActivity(k) == null) {
					cell.setCellValue("NULL");
					continue;
				}
				row.getCell(k+9).setCellValue(c.getActivity(k).getName());
			}
		}
		
		// Writes activities to file. 
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
		
		try {
			workbook.write(fileout);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

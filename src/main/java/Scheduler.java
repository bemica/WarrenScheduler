
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.swing.*;

public class Scheduler {
  private static ArrayList<Camper> allCampers;
  private static ArrayList<Activity> allActs;

  public static void main(String[] args){
	  allActs = new ArrayList<Activity>();
	// 0: Open up all the spreadsheet things

	JFileChooser fileChooser = new JFileChooser();
	fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));


	//TODO: find a parent
	JDialog parent = new JDialog();
	int result = fileChooser.showOpenDialog(parent);

	if(result == JFileChooser.APPROVE_OPTION) {
		File spreadFile = fileChooser.getSelectedFile();



		InputStream spread = null;
		FileOutputStream os = null;
		try {
			//spread = new FileInputStream("C:\\Users\\Nathan\\IdeaProjects\\CampScheduler\\src\\main\\resources\\2018 1G Camper Activities (Grace Getchell's conflicted copy 2018-06-18).xls");
			spread = new FileInputStream(spreadFile);
			//os = new FileOutputStream(spreadFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("File name you entered is wrong");
		}

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet activities = null;
		HSSFSheet camperChoices = null;
		allCampers = null;
		try {
			workbook = new HSSFWorkbook(spread);
			activities = workbook.getSheetAt(0);
			camperChoices = workbook.getSheetAt(1);

			allCampers = buildAllCampers(camperChoices);

			ArrayList<Activity> firstPeriod = buildActivities(1, activities);
			ArrayList<Activity> secondPeriod = buildActivities(2, activities);
			ArrayList<Activity> thirdPeriod = buildActivities(3, activities);
			ArrayList<Activity> fourthPeriod = buildActivities(4, activities);

			System.out.println(firstPeriod);
			System.out.println(secondPeriod);
			System.out.println(thirdPeriod);
			System.out.println(fourthPeriod);


			allActs.addAll(firstPeriod);
			allActs.addAll(secondPeriod);
			allActs.addAll(thirdPeriod);
			allActs.addAll(fourthPeriod);
		} catch (IOException e) {
			e.printStackTrace();
		}


		// 1: extract data from spreadsheet


		// 2: place emph campers (treat emph as separate activity from norm)
		//    name emph acts "[act]emph1" and "[act]emph2" e.g. "sailemph1"
		for (Camper camper : allCampers) {
			try {
				camper.place(1, allActs);
				camper.place(2, allActs);

			} catch (TooManyCampersException e) {
//        System.out.println("Camper " + camper.firstName + " " + camper.lastName + " did not get their first or second choice");
				// TODO: something should happen here?
			} catch (NoMatchingActivitiesFoundException e) {
				e.printStackTrace();
			}
		}


		// 3: place other 1st Choices NEVERMIND
		// 4: place other 2nd Choices NEVERMIND done with emph
		// 5: place other choices in order
		//    ideas: randomize, prioritize low ratios, place everybody in 1st act

		for (int i = 3; i <= 6; i++) {
			for (Camper camper : allCampers) {
				if (!camper.scheduled()) {
					try {
						camper.place(i, allActs);
					} catch (TooManyCampersException e) {
//                  System.out.println("Camper " + camper.firstName + " " + camper.lastName + " did not get their " + i + "th choice");
					} catch (NoMatchingActivitiesFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// 6: print spreadsheet(s) of activities

		try {
			writeActivities(allCampers, camperChoices);

//			FileOutputStream os = new FileOutputStream("C:\\Users\\Nathan\\IdeaProjects\\CampScheduler\\src\\main\\resources\\2018 1G Camper Activities (Grace Getchell's conflicted copy 2018-06-18).xls");
			os = new FileOutputStream(spreadFile);
			workbook.write(os);
			workbook.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 7: profit!

		System.out.println(allCampers);

		// Activity act = new Activity("", 2, 4.2, 1);
		// Camper camper = new Camper("", "", new ArrayList<String>());
	}

  }

  /*
  Given a period (1 through 4) and a sheet of an excel file this method returns an ArrayList of the activities in that
  period
   */
  private static ArrayList<Activity> buildActivities(int period, HSSFSheet actSheet){
	ArrayList<Activity> acts = new ArrayList<Activity>();

	// the first row is a header, so start at the second one
	int rowIndex = 1;
	HSSFRow row;
	//as soon as we hit the first empty row, we break
	while ((row = actSheet.getRow(rowIndex++)) != null){
	  String actName = row.getCell(0).getStringCellValue().toLowerCase();
	  double maxCampers = row.getCell(1).getNumericCellValue();

	  //if the cell says "yes" then we add it to the list
	  HSSFCell cell = row.getCell(period + 1);
	  if (cell != null && cell.getStringCellValue().toLowerCase().equals("yes")) {
		Activity activity = new Activity(actName, period, maxCampers);
		acts.add(activity);
	  }
	}

	return acts;
  }

  // TODO
  private static ArrayList<Camper> buildAllCampers(HSSFSheet camperChoiceSheet){
	ArrayList<Camper> campers = new ArrayList<Camper>();

	HSSFRow row = camperChoiceSheet.getRow(1);
	int rowIndex = 1;
	while((row = camperChoiceSheet.getRow(rowIndex++)) != null){
	  String lastN = row.getCell(1).getStringCellValue();
	  String firstN = row.getCell(2).getStringCellValue();
	  if(lastN == null || lastN.equals("")){
		  break;
	  }

	  ArrayList<String> choices = new ArrayList<String>();
	  for (int i = 3; i < 9; i++){
	  	HSSFCell cell = row.getCell(i);
	  	if(cell == null){
	  		cell = row.createCell(i);
		}
		choices.add(cell.getStringCellValue().toLowerCase());
	  }

	  Camper camper = new Camper(firstN, lastN, choices, rowIndex - 1);
	  //System.out.println(camper);
	  campers.add(camper);
	}

	return campers;
  }

  private static void writeActivities(ArrayList<Camper> campers, HSSFSheet camperSheet){
      int rowIndex = 1;
      HSSFRow camperRow;

      for(Camper camper : campers){
          camperRow = camperSheet.getRow(camper.id);

          String first = camperRow.getCell(1).getStringCellValue();
          String last = camperRow.getCell(2).getStringCellValue();



          //Check to see if there is such a camper
          if(camper != null) {
              HSSFCell act1 = camperRow.getCell(9);
              HSSFCell act2 = camperRow.getCell(10);
              HSSFCell act3 = camperRow.getCell(11);
              HSSFCell act4 = camperRow.getCell(12);

              String act1Name = camper.getAct(1);
              String act2Name = camper.getAct(2);
              String act3Name = camper.getAct(3);
              String act4Name = camper.getAct(4);

              if (act1Name != null) {
                  act1.setCellValue(act1Name);
              }
              if (act2Name != null) {
                  act2.setCellValue(act2Name);
              }
              if (act3Name != null) {
                  act3.setCellValue(act3Name);
              }
              if (act4Name != null) {
                  act4.setCellValue(act4Name);
              }

              System.out.println("wrote " + camper + " to row " + (camper.id + 1));
          }
      }
  }

  private static Camper findCamper(ArrayList<Camper> allCampers, String first, String last){
      for (Camper camper : allCampers){
          if (first.toLowerCase().equals(camper.firstName.toLowerCase()) && last.toLowerCase().equals(camper.lastName.toLowerCase())){
              return camper;
          }
      }

      return null;
  }
}

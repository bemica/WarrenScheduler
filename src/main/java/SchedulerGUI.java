import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;

import javax.swing.*;

/**
 * This class provides the implementation for the GUI associated with this program.
 *
 */
public class SchedulerGUI extends JFrame {
	
	public int height;
	
	private JProgressBar jbar = null;
	private JTextField userText = null;
	
	private File inputFile = null;
	private int iterations = 8000;
	

	public void begin() {    
		// Creating instance of JFrame
		JFrame frame = new JFrame("YMCA Camp Warren Scheduler");

		// Setting frame based on screen size.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		height = (int) ((int)(screenSize.getHeight())*0.65);
		frame.setSize(height, (int)(height * 0.50));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Panel.
		JPanel panel = new JPanel(); 
		frame.add(panel);
		
		// Icon.
		Image icon = new ImageIcon("WarrenLogo.jpg").getImage();
		frame.setIconImage(icon);

		placeComponents(panel);

		frame.setVisible(true);
	}
	
	public int getProgressBarValue() {
		return jbar.getValue();
	}
	
	public void setProgressBarValue(int i) {
		jbar.setValue(i);
	}
	
	/**
	 * This method adds all our components to the frame.
	 * @param panel
	 */
	private void placeComponents(JPanel panel) {
		panel.setLayout(null);
		
		// TODO Fix this...
		ImageImplement image = new ImageImplement(new ImageIcon("WarrenLogo.jpg").getImage());
		panel.add(image);
		
		panel = createIterationInput(panel);
		panel = createFileInput(panel);
		
		panel = createExecuteButton(panel);
	}
	
	/**
	 * Adds the generate schedule button to the GUI and defines the logic attached to it.
	 * @param panel : Our main panel.
	 * @return : The panel with the button on it.
	 */
	private JPanel createExecuteButton(JPanel panel) {
		// Creating and positioning the button.
		JButton goCrazyButton = new JButton("Generate Schedule");
		goCrazyButton.setBounds(10, (int)(height - height * 0.67), height - 38, 25);
		goCrazyButton.setBackground(Color.lightGray);
		goCrazyButton.setForeground(Color.RED);
		
		jbar = new JProgressBar();
		jbar.setValue(0);
		jbar.setStringPainted(true);
		jbar.setForeground(Color.RED);
		jbar.setBounds(10, (int)(height - height * 0.74), height - 38, 25);
		
		panel.add(jbar);
		
		// What happens when the user clicks on the button.
		goCrazyButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent event) {
	        	 try {
	 	        	iterations = Integer.parseInt(userText.getText());
	 	        	
	 	        	// Creating the thread that will handle scheduling and starting it.
	 	        	Scheduler scheduler = new Scheduler( "Scheduler-Thread");
	 	        	scheduler.start();
	 	
	        	 } catch(NumberFormatException e) {
	        		 String message = "I'm sorry. You have entered something"
	        		 		+ "\n other than an integer into the iteraion field.";
	 				JOptionPane.showMessageDialog(new JFrame(), message, "Improper Iterations",
	 						JOptionPane.ERROR_MESSAGE);
	 				return;
	        	 }
	         }          
	      });
		
		
		panel.add(goCrazyButton);
		
		return panel;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	public File getInputFile() {
		return inputFile;
	}
	
	/**
	 * This method creates the elements necessary for the user to be able to select a file.
	 * @param panel
	 * @return
	 */
	private JPanel createFileInput(JPanel panel) {
		// File lable.
		JLabel file = new JLabel("File");
		file.setBounds((int)(height * 0.65), 0, 120, 25);
		panel.add(file);
		
		// Where our file name will be stored.
		JTextField fileChosen = new JTextField(20);
		fileChosen.setText("No file chosen yet...");
		fileChosen.setEditable(false);
		fileChosen.setForeground(Color.black);
		fileChosen.setBounds((int)(height * 0.65), 20, 130, 25);
		panel.add(fileChosen);

		// Button to choose the file.
		JButton chooseFileButton = new JButton("Choose File");
		chooseFileButton.setBounds((int)(height * 0.65), 60, 100, 25);
	
		// Listener so User can select file.
		chooseFileButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {
	        	inputFile = selectFile();
	            fileChosen.setText(inputFile.getName());
	         }          
	      });
		
		panel.add(chooseFileButton);
		
		return panel;
	}
	
	/**
	 * Method leads the user through the selection of a file. 
	 * @return : Returns the file the user has selected.
	 */
	public static File selectFile() {
		File toReturn = null;
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

		JDialog parent = new JDialog();
		int result = fileChooser.showOpenDialog(parent);

		if(result == JFileChooser.APPROVE_OPTION) {
			toReturn = fileChooser.getSelectedFile();

			// Format check.
			if(!(toReturn.getName().contains(".xlsx") ||
					toReturn.getName().contains(".xlsm"))) {
				String message = "I'm sorry. The file you have \n" +
						"selected is an invalid file type.\n" + "Please try selecting a valid .xlsx or .xlsm file.";
				JOptionPane.showMessageDialog(new JFrame(), message, "Invalid File Format",
						JOptionPane.ERROR_MESSAGE);
				return selectFile();
			}
	
		} else return null;
		
		return toReturn;
	}
	
	/**
	 * Creating the elements that go into letting the user decide how many iterations they desire.
	 * @param panel : Panel in question.
	 * @return : Panel with elements.
	 */
	private JPanel createIterationInput(JPanel panel) {
		JLabel userLabel = new JLabel("Number of Iterarions");
		userLabel.setBounds(110,0,180,25);
		panel.add(userLabel);

		userText = new JTextField(20);
		userText.setText("8000");
		userText.setToolTipText("Total execution time depends on processor speed.");
		userText.setBounds(110,20,230,25);
		panel.add(userText);
		
		JTextArea timeCalculation = new JTextArea("More iterations means higher accuracy "
				+ "\n"
				+ "but longer execution time.");
		
		timeCalculation.setLineWrap(true);
		timeCalculation.setBounds(110, 60, 230, 40);

		
		timeCalculation.setEditable(false);
		panel.add(timeCalculation);
		
		return panel;
	}
	
	/**
	 * This class provides the functionality for our icon to appear on the GUI.
	 */
	private class ImageImplement extends JPanel {

		private Image img;

		public ImageImplement(Image img) {
			this.img = img;
			Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);
			setSize(size);
			setLayout(null);
		}

		public void paintComponent(Graphics g) {
			g.drawImage(img, 0, 0, null);
		}

	}

}

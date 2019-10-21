<center>
	<img src="https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.1fTmJYEvdd8nf7Ufb5uG1AHaHa%26pid%3DApi&f=1" height=250>
	<h1> Camp Warren Scheduler</h1>
</center>
Table of contents

* [Welcome](#Welcome)
* [Getting Started](# "Getting Started")
* [Formatting Data](# "Formatting Data")
* [Troubleshooting](#Troubleshooting)
***
### Welcome
Hello there and welcome to the Camp Warren camper scheduler. 
This program exists to help you, the program coordinator, create schedules for camp in a fraction of the regular time. In the creation of this program we are hoping that you can spend more time writing the staff schedule and making the office run smoothly and less time figuring out how to fit kids into activities. Below you'll find hopefully all you'll need to know to get the program up and running but as always, if you have any questions or problems reach out to Cole Polyak at colepolyak@gmail.com or through Facebook messenger. 
Have a wonderful summer and good luck!

### Getting Started
To download the scheduler, download the jar file in this repository by first clicking on the file:
![GitHub Repo](https://i.imgur.com/B9pTTcW.jpg)
And then selecting the download option.
![Github Repo](https://i.imgur.com/E51cH9A.jpg)
After the download has completed, find the Jar file on your computer. Double click to run. A window will appear that looks like this: 

![GUI](https://i.imgur.com/oNglO1r.jpg)
Choose the file that contains the input data and if you so choose you can also alter the number of iterations the program will go through. Currently, 8,000 iterations takes roughly 2 minutes so if time is of no object, you can easily up that number. 

**IMPORTANT:** Ensure that the Excel data file is **closed** before running the program. In order for the program to read and write to the file, it must be closed.

After selecting your file and number of iterations, click the Generate Schedule button. As the program runs, the progress bar above the button will populate, indicating how close to being done the program is.

Once the program finishes, a dialog that looks like this will appear:

![Finished Dialog](https://i.imgur.com/tjPwvav.jpg)
The optimized score represents how "ideal" the schedule is. Basically, a better schedule will be closer to 1 and a worse schedule will be closer to 0. The number represents how many campers are getting choices higher up in their preferences (3&4 choices over 5&6).

You can exit out of the dialog and go look at the excel sheet as the data should have populated!

***
### Formatting Data
The program uses the XSSF library, meaning only Excel files from 2008 or newer can be used. In this repository you will also find the Excel sheet detailing the format the program expects campers in. 
![Excel Sheet](https://i.imgur.com/2OBX1id.jpg)
Each camper should appear in this format. 

Adding or altering activities is also simple. Activities are formatted as follows:
![Excel Sheet](https://i.imgur.com/A4XFCT1.jpg)Each column represents if the activity is offered that period. The final column, "Variable?", tells the program that which period that activity appears can be changed for the sake of finding a better schedule. 

**Emphasis** has to be handled slightly different currently. If you want an activity with only emphasis kids in that activity, you have to add the following to the activity sheet and the camper sheet:
![](https://i.imgur.com/3mo4xCl.jpg)
![](https://i.imgur.com/C4ETCXs.jpg)This way the program will schedule only emphasis kids in those activities. Make sure that emphasis activities aren't variable for things like Sailing emphasis. 
***
### Troubleshooting
If for some reason you get weird errors, try the following:
* Ensure there are no junk lines in the Excel file, including random space lines.
* Ensure that the campers activities are spelled correctly given the options available.
* Ensure that the sheet is **closed** so that the program can read and write to it. 
* Run the file in the command line using `java -jar WarrenScheduler.jar` to see the under the hood view.
* Shoot Cole an email at colepolyak@gmail.com




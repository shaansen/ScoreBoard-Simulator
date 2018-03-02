Author 	: Shantanu Sengupta
Date	: 05/09/2017

How to Run the Code: Using Eclipse IDE
1. Download the Archive File into a suitable location in the hard drive.
2. Open Eclipse. Go to File -> Import -> Archive File. Click on Next and Finished to complete loading the project into your workspace.
3. Update the inst.txt config.txt and data.txt as per testing requirements
4. Run the Main.java
5. Open the result.txt to view the results.


Settings to run the program with different input files : 
1. Open the Proiject in Eclipse
2. Go to Run -> RunConfiguration -> Arguments Tab -> Program Variables
3. Enter the path to the new input files in the following order
	inst.txt	data.txt	config.txt	result.txt
4. All the four files above must be in the project folder where the src and bin libraries are present.

SPECIAL NOTE : The program updates the inst.txt if the instructions contain a loop. Incase of re-running the code, please update the inst.txt to contain the original instructions file.
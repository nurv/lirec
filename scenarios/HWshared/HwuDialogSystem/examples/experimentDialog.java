import uk.ac.hw.lirec.dialogsystem.DialogInterface;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Moods;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Expression;
import java.util.HashMap;

/*
 * TODO list
 * 
 * How do we set participant ID so we can match logs to interviews etc?
 * Am I missing any screen specific stuff?
 * How are we specifying memory case, or not? Somewhere in the migrationData?
 */

/*utility stuff up first */

DialogInterface di;
HashMap migrationData = new HashMap();
String server;


speak(String speech) {
	di.speakText(speech);
}

giveDirections(String dir, String location, String direction) {
	System.out.println("DS: loc string: "+location+ "\n   dir: "+ direction+ "\n  string: "+dir);
	if (location.contains("2.22")) {
		speak("This is my favourite lab!");
	} else if (location.contains("Gnorth2") && direction.equals("R") && dir.contains("3 sets")) {
		speak("Go down this corridor, past the junction, into the next corridor.");
		return;
	} else if (location.contains("Gnorth2") && direction.equals("R") && dir.contains("2 sets")) {
		speak("Go down this corridor to the junction, then turn right");
		return;
	} else if (location.contains("Gliftarea") && direction.equals("D")) {
		speak("Follow this corridor, then turn right into the stairwell.");
		return;
	} else if (location.contains("1north3") && direction.equals("R") && dir.contains("3 sets of double doors then turn left")) {
		speak("Go straight on to the crush area, then turn left");
		return;
	}
	speak(dir);
}


String latest = "start";
//called when dialog/nav has failed
failure() {
	speak("Never mind");
	speak("Just head back to 1.54");
	di.getResponse("I'm back");
	
	if (latest.equals("start")) 
		speak("THIS ISN'T IMPLEMENTED YET.");
	else
		speak("Speak to Iain or Michael");	
}


/* Migration related code*/

migrateInDone() {
	//TODO condition on what's been migrated in somehow
	testInOut();
}
waitAndRequestMigration(String from) {
	server = from;
	di.blankScreen();
	Thread.sleep(3000);
	//di.getResponse("Ready");
	while(!di.inviteMigrate(from)) {
		System.out.println("ERROR MIGRATING");
		di.getResponse("Ready");
	}
}
waitAndMigrateOut(String to) {
	//di.getResponse("Ready");
	Thread.sleep(3000);
	int retries = 5;
	while(!di.migrateDataOut(to,migrationData) && retries > 0) {
		System.out.println("ERROR MIGRATING");
		retries--;
	}
}

/* now plans specific to the dialogue.*/

//EPISODE 1

episode1Screen() {
	speak("Hi there! So you've come for the treasure hunt, eh?");
	di.getResponse("Yes");
	speak("Great, let's first check we can communicate with each other");
	speak("Which of these colours do you like best?");
	String[] optionsColours = {"Red","Blue","Yellow","Green"};
	String colour = di.multipleChoiceQuestion(4,optionsColours);
	String emysColour = "purple";
	speak(colour+" is good, but my favourite is "+emysColour);
	speak("Ok, let's get this treasure hunt started");
	speak("There are 4 words that you need to find to complete the hunt");
	speak("I have the clues here that lead you to each of them.");
	speak("Which one do you want to try first?");
	String[] optionsClues = {"clue a","clue b","clue c","clue d"};
	String first_clue = di.multipleChoiceQuestion(4,optionsClues);
	speak("Good choice, the clue is \"hermit\"");
	//removed the option to choose another clue for now
	//as there isn't really a choice? Or do we want to allow
	//choice?
	speak("Hermit, remember that, it's important");
	speak("Now start the app, and press the ready button to get going");
	
	migrationData.put("first_clue",first_clue);
	migrationData.put("colourChoice",colour);
	migrationData.put("emysColour",emysColour);
	
}

//Episode 2

episode2PhoneMemory() {
	
}

episode2PhoneNoMemory() {
	
}
episode2PhoneCommon() {
	
}

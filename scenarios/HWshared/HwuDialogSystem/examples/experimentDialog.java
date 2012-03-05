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
 * Add some emotions/expressions in
 */

/*utility stuff up first */

DialogInterface di;
HashMap migrationData = new HashMap();
String server;//TODO SET THIS


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
	unBlankScreen
	
	//TODO choose which case here
	episode2PhoneMemory()
}
requestMigration(String from) {
	di.blankScreen();
	Thread.sleep(500);
	di.getResponse("Ready");
	while(!di.inviteMigrate(from)) {
		System.out.println("ERROR MIGRATING");
		di.getResponse("Ready");
	}
}
migrateOut(String to) {
	Thread.sleep(500);
	int retries = 5;
	while(!di.migrateDataOut(to,migrationData) && retries > 0) {
		System.out.println("ERROR MIGRATING");
		retries--;
	}
	//TODO need something to retry more maybe?
	di.blankScreen();
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
	//choice? Which episode is next will be affected
	speak("Hermit, remember that, it's important");
	speak("Now start the app, and press the ready button to get going");
	
	migrationData.put("first_clue",first_clue);
	migrationData.put("colourChoice",colour);
	migrationData.put("emysColour",emysColour);
	
}

/***********************************************/
//Episode 2

episode2PhoneMemory() {
	speak("Right, hermit, I think I know what that's about.");
	episode2PhoneCommon();
}
episode2PhoneNoMemory() {
	speak("Hello there, you're here to find a treasure, right?");
	speak("Can you let me know the clue please?");
	String userClue = di.getFreetext();
	while (!userClue.equalsIgnoreCase("hermit")) {
		speak("That doesn't make sense, try again:");
		userClue = di.getFreetext();
	}
	speak("Right, I think I know what that's about");
	episode2PhoneCommon();
}
episode2PhoneCommon() {
	speak("before we go, it's a bit dark in here, I don't like that.");
	speak("Can you set the backgrounf to my favourite colour?");
	String[] optionsColours = {"Red","Blue","Yellow","Green","Purple","Pink","Orange","What's your favourite?"};
	String colour = di.multipleChoiceQuestion(8,optionsColours);
	migrationData.put("phoneColourChoice","colour");
	if (colour.contains("favourite")) {
		speak("Ah, never mind, no time for this, time for treasure!");		
	} else {
		speak("Why did you choose that colour?");
		String responseWhy = di.getFreetext();
		speak("Hmm, I can't seem to set the colour, never mind."):
		speak("No time for this, time for treasure!");
		migrationData.put("firstWhy",responseWhy);
	}
	speak("So now I'll take you to the place where you'll find the hermit");
	di.getResponse("OK");
	//TODO need a different callback for non-memory case!
	di.startNav("1.54","1east1","episode2PhoneMemoryArrived()");
}
episode2ArrivedCommon() {
	Thread.sleep(3000);
	speak("Hermit's mentioned round here somewhere.");
	speak("On a poster about You Tunes I think.");
	Thread.sleep(3000);
	speak("Have you found the poster?");
	di.getResponse("Yes");
	speak("Great, so tell me, what is hermit?");
}
episode2PhoneMemoryArrived() {
	episode2ArrivedCommon();
	String clue1_answer = clue1_answer = di.getFreetext();;
	while (!clue1_answer.contains("rog")) { //rog to ignore case of F
		speak("Hmm, doesn't sound right to me");
		clue1_answer = di.getFreetext();
	}
	migrationData.put("clue1_answer",clue1_answer);
	ep2return();
}
episode2PhoneForgetArrived() {
	episode2ArrivedCommon();
	String[] optionsAnswer= {"A lonely man","the singer of a band","a frog","a crab", "a pizza"};
	String clue1_answer = di.multipleChoiceQuestion(5,optionsClues);
	migrationData.put("clue1_answer",clue1_answer);
	ep2return();
}

ep2return() {	
	speak("Ok, let's go back to the screen for another clue.");
	speak("I'll just let you find your way yourself?");
	di.getResponse("OK");
	waitForReturnToScreenEp2();
}


waitForReturnToScreenEp2() {
	speak("When you're back at the screen, let me know.");
	di.getResponse("We're there");
	migrateOut(server);
}

/***********************************************/
//Episode 3

episode3screenMemory() {
	//TODO whatever should happen to wait for a migration
	//should this be called AFTER the migrate out above?
	speak("Ok, we've got the first part of the answer.");
	speak("According to you, hermit is a "+ migrateData.get("clue1_answer");
	speak("We'll find out at the end if that's right.");
	speak("For now there are 3 more clues to work through.");
	speak("Which one do you want to try next?");
	
}



import uk.ac.hw.lirec.dialogsystem.DialogInterface;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Moods;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Expression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*
 * TODO list
 * 
 * How do we set participant ID so we can match logs to interviews etc?
 * Am I missing any screen specific stuff?
 * How are we specifying memory case, or not? Somewhere in the migrationData?
 * Add some emotions/expressions in
 * need logging
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
	
	if (latest.equals("episode2")) {
		migrationData.remove("phoneColourChoice");
		migrationData.remove("firstWhy");
		episode2startPhone();
	}else
		speak("Speak to Iain or Michael");	
}


/* Migration related code - phone side*/

migrateInDone() {
	di.unBlankScreen();
	
	if (migrationData.get("episode").equals("1"))
		episode2startPhone();
	
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
		Thread.sleep(1000);
	}
	if (retries == 0) {
		speak("Oh dear, something's gone wrong.");
		speak("Speak to Michael or Iain.");
	}
	//TODO need something to retry more maybe?
	di.blankScreen();
}

/* Migration related code - screen side*/

migrationOut()
{
	di.setEmysInvisible();
	di.migrateDataOut("target",migrationData);
}

migrationIn()
{
	di.setEmysVisible();
	if (migrationData.get("episode").equals("2"))
		episode3screen();
}


/* now plans specific to the dialogue.*/

//setup stuff

setup(String participantID, char track) {
	migrationData.clear();
	migrationData.put("participant",participantID);
	String memory = "1,2,3,4,5,6,7,8,";
	if (char == 'b') {
		memory = "TODO";
	} else if (char == 'c') {
		memory = "TODO";
	}
	
	
	//remember to end on a comma!
	//migrationData.put("memory","1,2,3,4,5,6,7,8,");//a number present means it should remember, missing means forget previous
	migrationData.put("memory",memory);
	/*
	 * What if we forget on 4 say, then need to remember from 2 in 8? shouldn't, it was
	 * forgotten in 4. Don't want to actually remove it from memory...
	 */
	episode1Screen();
}

boolean remembers(int episode, int currentEpisode) {
	String memory migrationData.get("memory");
	for (int i = (episode); i <= (currentEpisode);i++) {
		if (!memory.contains(i.toString() + ","))
			return false;
	}
	return true;
}

//EPISODE 1

episode1Screen() {
	migrationData.put("episode","1");
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

episode2startPhone() {
	if (remembers(1,2))
		episode2PhoneMemory();
	else
		episode2PhoneNoMemory();
}

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
	migrationData.put("episode","2");
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
	latest = "episode2";
	if (remembers(1,2))
		di.startNav("1.54","1east1","episode2PhoneMemoryArrived()");
	else
		di.startNav("1.54","1east1","episode2PhoneForgetArrived()");
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


episode3screen() {
	//TODO whatever should happen to wait for a migration
	//should this be called AFTER the migrate out above?
	migrationData.put("episode","3");
	if (remembers(2,3)) {
		speak("Ok, we've got the first part of the answer.");
		speak("According to you, hermit is "+ migrateData.get("clue1_answer");
		speak("We'll find out at the end if that's right.");
	}
	String first_clue = migrationDate.get("first_clue");
	if (remembers(1,3)) { //remembers which clue we picked before
		speak("For now there are 3 more clues to work through.");
		speak("Which one do you want to try next?");
		String[] optionsClues = {"clue a","clue b","clue c","clue d"};
		List<String> options = Arrays.asList("clue a","clue b","clue c","clue d");
		options.remove("first_clue");
		String second_clue = di.multipleChoiceQuestion(3,options.toArray());
		migrationData.put("second_clue",second_clue);
		
	} else { //don't remember the previous clue
		speak("Hi there, would you like to get a clue?");
		String[] optionsClues = {"clue a","clue b","clue c","clue d"};
		String second_clue = di.multipleChoiceQuestion(4,optionsClues);
		//TODO do we actually let them choose hermit?
		while (second_clue.equals(first_clue)) {
			speak("This clue is \"hermit\"");
			speak("Do you want this clue, or to choose again?");
			di.multipleChoiceQuestion(2,{"Choose again","keep it"});
			speak("Ok, here are the choices again:");
			second_clue = di.multipleChoiceQuestion(4,optionsClues);
		}
		migrationData.put("second_clue",second_clue);
		//TODO log this stuff
	}
	//reveal the clue now
	speak("OK, this clue is \"Angus\"."); //TODO or should it be submarine?
	speak("Remember that, it's important. Angus.");
	speak("Now start the app if you need to, and press the ready button to get going");
}



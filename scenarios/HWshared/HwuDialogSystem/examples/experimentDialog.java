import uk.ac.hw.lirec.dialogsystem.DialogInterface;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Moods;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Expression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;

/*
 * TODO list

 */

/*utility stuff up first */

DialogInterface di;
HashMap migrationData = new HashMap();
String server = "137.195.27.102";//102 is the big screen IP
long startTime;

speak(String speech) {
	di.speakText(speech);
}
boolean saidEntrance = false;

giveDirections(String dir, String location, String direction) {
	System.out.println("DS: loc string: "+location+ "\n   dir: "+ direction+ "\n  string: "+dir);
	if (location.contains("2.22")) {
		speak("This is my favourite lab!");
	} else if (location.contains("Gnorth2") && direction.equals("R") && dir.contains("3 sets")) {
		speak("Go down this corridor, past the junction, into the next corridor.");
		return;
	} else if (location.contains("Gnorth2") && direction.equals("R") && dir.contains("2 sets")) {
		speak("Go down this corridor to the junction, then turn right");
		saidEntrance = false;//bit of ahack putting this here
		return;
	} else if (location.contains("Gnorth2") ) {
		//comment about stairs
		if (saidEntrance) {
			speak(dir);
			return;
		} else if (migrationData.get("episode").equals("8") &&  remembers(4,8)) {
			speak("And we're back here again!");
			saidEntrance = true;
		}
		else {
			saidEntrance = true;
			speak("Have you been here before?");
			speak("Some people don't know there's an entrance at this end.");
		}
	} else if (location.contains("Gliftarea") && direction.equals("D")) {
		speak("Follow this corridor, then turn right into the stairwell.");
		return;
	} else if (location.contains("1north3") && direction.equals("R") && dir.contains("3 sets of double doors then turn left")) {
		speak("Go straight on to the crush area, then turn left");
		return;
	} else if (dir.contains("turn right along the corridor")) {
		Integer cur = Integer.parseInt(migrationData.get("episode"));
		if ( remembers(cur-2,cur)) {
			speak(dir);
			speak("Here we go again!");
			return;
		}
	}
	speak(dir);
}


String latest = "start";
//called when dialog/nav has failed
failure() {
	speak("Never mind");
	di.setMood(Moods.SADNESS);
	speak("Just head back to 1.54");
	di.getResponse("I'm back");
	di.setMood(Moods.NEUTRAL);
	di.showExpression(Expression.JOY);
	
	Integer fails = Integer.parseInt(migrationData.get(latest +"Failures"));
	fails++;
	migrationData.put(latest +"Failures",fails.toString());
	
	timeStamp("xxxfailure_"+latest+"_"+fails.toString());
	
	if (latest.equals("episode2")) {
		di.startNav("1.54","1east1","episode2PhoneArrived()");
	} else if (latest.equals("episode4")) {
		di.startNav("1.54","gwest3","episode4Arrived()");
	} else if (latest.equals("episode6")) {
		di.startNav("1.54","2north1","episode6Arrived()");
	} else if (latest.equals("episode8")) {
		di.startNav("1.54","gsouth1","episode8Arrived()");
	} else
		speak("Speak to Iain or Michael");	
}


/* Migration related code - phone side*/

migrateInDone() {
	di.unBlankScreen();
	
	if (migrationData.get("episode").equals("1"))
		episode2startPhone();
	else if  (migrationData.get("episode").equals("3"))
		episode4startPhone();
	else if  (migrationData.get("episode").equals("5"))
		episode6startPhone();
	else if  (migrationData.get("episode").equals("7"))
		episode8startPhone();
	
}

requestMigration() {
	di.blankScreen();
	Thread.sleep(500);
	di.getResponse("Ready");
	while(!di.inviteMigrate(server) && !di.isInterrupted()) {
		System.out.println("ERROR MIGRATING");
		di.getResponse("Ready");
	}
}

migrateOut() {
	if (di.isInterrupted())
		return;
	
	Thread.sleep(500);
	int retries = 5;
	boolean done = false;
	while (!done) {
		while(!di.migrateDataOut(server,migrationData) && retries > 0) {
			System.out.println("ERROR MIGRATING");
			retries--;
			Thread.sleep(950);
		}
		if (retries == 0){
			speak("Oh dear, I can't connect to the wifi");
			di.getResponse("Try again");
			outerLoop++;
			migrateOut();
		} else {
			done = true;
			requestMigration();	
		}
	}

	
}

/* Migration related code - screen side*/

migrationOut()
{
	di.setEmysInvisible();
	
	timeStamp("ts_ScreenEpisodeEnds_"+migrationData.get("episode"));
	
	di.migrateDataOut("target",migrationData);
}

migrationIn()
{
	timeStamp("ts_ScreenEpisodeStarts_"+migrationData.get("episode"));
	di.setEmysVisible();
	di.blockUntilUserPresent();
	if (migrationData.get("episode").equals("2"))
		episode3screen();
	else if (migrationData.get("episode").equals("4"))
		episode5screen();
	else if (migrationData.get("episode").equals("6"))
		episode7screen();
	else if (migrationData.get("episode").equals("8"))
		episode9screen();
}


/* now plans specific to the dialogue.*/

//setup stuff

setup(String participantID, char track) {
	migrationData.clear();
	migrationData.put("participant",participantID);
	migrationData.put("memoryTrack",track.toString());
	String memory = "2,3,4,5,6,7,8,";
	if (track == 'b') {
		memory = "9,";;
	} else if (track == 'c') {
		memory = "2,3,6,7,";
	}
	
	
	//remember to end on a comma!
	//migrationData.put("memory","1,2,3,4,5,6,7,8,");//a number present means it should remember, missing means forget previous
	migrationData.put("memory",memory);
	/*
	 * What if we forget on 4 say, then need to remember from 2 in 8? shouldn't, it was
	 * forgotten in 4. Don't want to actually remove it from memory...
	 */
	di.blockUntilUserPresent();
	startTime = System.currentTimeMillis();
	migrationData.put("startTimeMillis",startTime.toString());
	episode1Screen();
}

boolean remembers(int episode, int currentEpisode) {
	String memory = migrationData.get("memory");
	for (int i = (episode+1); i <= (currentEpisode);i++) {
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
	String[] optionsClues = {"clue b","clue a","clue d","clue c"};
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
	startTime = Long.parseLong(migrationData.get("startTimeMillis"));
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
	String userClue = di.getFreetext().trim();
	while (!userClue.equalsIgnoreCase("hermit")) {
		di.showExpression(Expression.SADNESS);
		speak("That doesn't make sense, try again:");
		userClue = di.getFreetext();
	}
	di.showExpression(Expression.JOY);
	speak("Right, I think I know what that's about");
	episode2PhoneCommon();
}
episode2PhoneCommon() {
	migrationData.put("episode","2");
	speak("before we go, it's a bit dark in here, I don't like that.");
	speak("Can you set the background to my favourite colour?");
	String[] optionsColours = {"What's your favourite?","Red","Blue","Yellow","Green","Purple","Pink","Orange"};
	String colour = di.multipleChoiceQuestion(8,optionsColours);
	migrationData.put("phoneColourChoice",colour);
	if (colour.contains("favourite")) {
		speak("Ah, never mind, no time for this, time for treasure!");		
		migrationData.put("firstWhy","asked for fav");
	} else {
		speak("Why did you choose that colour?");
		String responseWhy = di.getFreetext();
		di.showExpression(Expression.SADNESS);
		speak("Hmm, I can't seem to set the colour, never mind.");
		
		speak("No time for this, time for treasure!");
		migrationData.put("firstWhy",responseWhy);
	}
	speak("So, now I'll take you to the place where you'll find the hermit");
	di.getResponse("OK");
	latest = "episode2";
	migrationData.put(latest +"Failures","0");
	timeStamp("ts_PhoneNav_"+migrationData.get("episode"));
	di.startNav("1.54","1east1","episode2PhoneArrived()");
}
episode2PhoneArrived() {
	if (remembers(1,2))
		episode2PhoneMemoryArrived();
	else
		episode2PhoneForgetArrived();
}
episode2ArrivedCommon() {
	timeStamp("ts_PhoneNavArrived_"+migrationData.get("episode"));
	Thread.sleep(3000);
	speak("Hermit's mentioned round here somewhere.");
	speak("On a poster about You Tunes, I think.");
	Thread.sleep(3000);
	speak("Have you found the You Tunes poster?");
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
	String clue1_answer = di.multipleChoiceQuestion(5,optionsAnswer);

	migrationData.put("clue1_answer",clue1_answer);
	ep2return();
}

ep2return() {	
	speak("Ok, let's go back to the screen for another clue.");
	speak("I'll just let you find the way yourself?");
	di.getResponse("OK");
	waitForReturnToScreenEp2();
}


waitForReturnToScreenEp2() {
	timeStamp("ts_PhoneNavReturning_"+migrationData.get("episode"));
	speak("When you're back at the screen, let me know.");
	di.getResponse("We're there");
	migrateOut();
}

/***********************************************/
//Episode 3

episode3screen() {
	migrationData.put("episode","3");
	if (remembers(2,3)) {
		if (remembers(1,3))
			speak("Ok, we've got the first answer.");
		speak("According to you, hermit is "+ migrationData.get("clue1_answer"));
		speak("We'll find out at the end if that's right.");
	}
	String first_clue = migrationData.get("first_clue");
	if (remembers(1,3)) { //remembers which clue we picked before
		speak("For now there are 3 more clues to work through.");
		speak("Which one do you want to try next?");
		ArrayList options = new ArrayList(Arrays.asList(new String[]{"clue b","clue a","clue d","clue c"}));
		options.remove(first_clue);
		String second_clue = di.multipleChoiceQuestionList(3,options);
		migrationData.put("second_clue",second_clue);
		
	} else { //don't remember the previous clue
		speak("Do you have all 4 answers or do you need another clue?");
		String[] needClues = {"have all","need clue"};
		String answerOrClues = di.multipleChoiceQuestion(2,needClues);
		speak("Here are the clues. Please pick the next one.");		
		String[] optionsClues = {"clue b","clue a","clue d","clue c"};
		String second_clue = di.multipleChoiceQuestion(4,optionsClues);
		while (second_clue.equals(first_clue)) {
			speak("This clue is \"hermit\"");
			if (remembers(2,3))
			{
				speak("You've just done this one.");
			}
			speak("Do you want this clue, or to choose again?");
			di.multipleChoiceQuestion(2,new String[]{"choose again","keep it"});
			speak("Ok, here are the choices again:");
			second_clue = di.multipleChoiceQuestion(4,optionsClues);
		}
		migrationData.put("second_clue",second_clue);
		//TODO log this stuff
	}
	//reveal the clue now
	speak("OK, this clue is \"Angus\"."); 
	speak("Remember that, it's important. Angus.");
	speak("Now start the app if you need to, and press the ready button to get going");
}

/***********************************************/
//Episode 4
episode4startPhone() {
	migrationData.put("episode","4");
	if (remembers(3,4)) {
		speak("Okay, angus, great.");
		
	} else {
		speak("Hello there, you're here to find a treasure, right?");
		speak("Can you let me know the clue please?");
		String userClue = di.getFreetext().trim();
		while (!userClue.contains("ngus")) {
			speak("That doesn't make sense, try again:");
			userClue = di.getFreetext();
		}
		speak("Right, I think I know what that's about");
	}
	di.showExpression(Expression.JOY);
	speak("I'll take you to find Angus.");
	di.getResponse("OK");
	latest = "episode4";
	migrationData.put(latest +"Failures","0");
	timeStamp("ts_PhoneNav_"+migrationData.get("episode"));
	di.startNav("1.54","gwest3","episode4Arrived()");

}
episode4Arrived() {
	timeStamp("ts_PhoneNavArrived_"+migrationData.get("episode"));
	Thread.sleep(3000);
	speak("I've seen angus down here.");
	speak("It's written on a submarine.");
	Thread.sleep(3000);
	speak("Have you found the submarine?");
	di.getResponse("Yes");
	speak("Great, I think the question is, what colour's the submarine?");
	if (remembers(3,4)) {
		//freetext entry
		String clue2_answer =  di.getFreetext();;
		migrationData.put("clue2_answer",clue2_answer);
	} else {
		String[] optionsAnswer= {"Orange","Green","Purple","White", "Blue"};
		String clue2_answer = di.multipleChoiceQuestion(5,optionsAnswer);
		migrationData.put("clue2_answer",clue2_answer);
	}
	ep4return();
}

ep4return() {	
	speak("Ok, let's go back to the screen for another clue.");
	speak("I'll just let you find the way yourself?");
	di.getResponse("OK");
	
	// some chat about distance
	Thread.sleep(3000);
	speak("These corridors are a bit boring.");
	speak("Do you also find all this walking a bit tedious?");
	speak("You're only doing this once, I have to take lots of people around.");
	speak("Just with you I've walked around 300 metres.");
	speak("And we're not done yet.");
	
	waitForReturnToScreenEp2();
}

/***********************************************/
//Episode 5

episode5screen() {
	migrationData.put("episode","5");
	if (remembers(4,5)) {
		if (remembers(1,5))
			speak("Ok, we've got the second answer.");
		speak("According to you, Angus is "+ migrationData.get("clue2_answer"));
		speak("We'll find out at the end if that's right.");
	}
	String first_clue = migrationData.get("first_clue");
	String second_clue = migrationData.get("second_clue");
	if (remembers(3,5))  //remembers at least one clue we picked before
	{
		ArrayList options = new ArrayList(Arrays.asList(new String[]{"clue b","clue a","clue d","clue c"}));
		options.remove(second_clue);
		if (remembers(1,5))
		{	
			speak("You have 2 clues left to choose from.");
			options.remove(first_clue);
		} else
		{
			speak("Do you have all 4 answers or do you need another clue?");
			String[] needClues = {"have all","need clue"};
			String answerOrClues = di.multipleChoiceQuestion(2,needClues);
			speak("Ok. Let's give you the next clue");
		}
		speak("Which one do you want to try next?");
		String third_clue = di.multipleChoiceQuestionList(options.size(),options);
		while (third_clue.equals(first_clue))
		{
			speak("This clue is \"hermit\"");	
			if (remembers(2,5)) speak("You've done this one before.");
			speak("Do you want this clue, or to choose again?");
			di.multipleChoiceQuestion(2,new String[]{"choose again","keep it"});
			speak("Ok, here are the choices again:");
			third_clue = di.multipleChoiceQuestionList(options.size(),options);
		}
		migrationData.put("third_clue",third_clue);
	} else { //don't remember any previous clue
		speak("Do you have all 4 answers or do you need another clue?");
		String[] needClues = {"have all","need clue"};
		String answerOrClues = di.multipleChoiceQuestion(2,needClues);
		speak("Please choose your next clue.");
		String[] optionsClues = {"clue b","clue a","clue d","clue c"};
		String third_clue = di.multipleChoiceQuestion(4,optionsClues);
		while (third_clue.equals(first_clue) || third_clue.equals(second_clue)) {
			if (third_clue.equals(second_clue))
			{
				speak("This clue is \"Angus\"");
				if (remembers(4,5)) speak("You've just done this one.");
			} 
			else if (third_clue.equals(first_clue))
			{
				speak("This clue is \"hermit\"");	
				if (remembers(2,5)) speak("You've done this one before.");
			}
			speak("Do you want this clue, or to choose again?");
			di.multipleChoiceQuestion(2,new String[]{"choose again","keep it"});
			speak("Ok, here are the choices again:");
			third_clue = di.multipleChoiceQuestion(4,optionsClues);
		}
		migrationData.put("third_clue",third_clue);
		//TODO log this stuff
	}
	//reveal the clue now
	speak("OK, this clue is \"helmet\"."); 
	speak("Remember that, it's important. Helmet.");
	
	// reveal my home town
	speak("By the way, how are you doing so far,");
	speak("I'm from Cork in Ireland, I hope you're able to understand my accent ok?");
	String understand = di.multipleChoiceQuestion(2,new String[]{"it's fine","struggling a bit","you're subtitled"});
	migrationData.put("understand",understand);
	if (understand.equals("it's fine")) { di.showExpressionNoWait(Expression.JOY); speak("Good to hear that.");}
	else if (understand.equals("struggling a bit")) {di.showExpressionNoWait(Expression.SADNESS); speak("Sorry. But you can always read my subtitles you know.");}
	else {di.showExpressionNoWait(Expression.SURPRISE); speak("Bit of a joker, are you?");}
	
	speak("Good, back to business, as I've told you the clue is helmet.");
	speak("Now start the app if you need to, and press the ready button to get going");
}

/***********************************************/
//Episode 6 -- helmet
episode6startPhone() {
	migrationData.put("episode","6");
	if (remembers(5,6)) {
		speak("Hmmmm helmet, I know where to find one of those.");
	} else {
		speak("Hello there, you're here to find a treasure, right?");
		speak("Can you let me know the clue please?");
		String userClue = di.getFreetext().trim();
		while (!userClue.equalsIgnoreCase("helmet")) {
			speak("That doesn't make sense, try again:");
			userClue = di.getFreetext();
		}
		speak("Hmmmm helmet, I know where to find one of those.");
	}
	speak("Let's go find the helmet.");
	di.getResponse("OK");
	latest = "episode6";
	migrationData.put(latest +"Failures","0");
	timeStamp("ts_PhoneNav_"+migrationData.get("episode"));
	di.startNav("1.54","2north1","episode6Arrived()");

}
episode6Arrived() {
	timeStamp("ts_PhoneNavArrived_"+migrationData.get("episode"));
	Thread.sleep(3000);
	speak("I've seen a helmet in this corridor.");
	speak("It's on a poster somewhere.");
	Thread.sleep(3000);
	speak("Have you found the helmet?");
	di.getResponse("Yes");
	speak("Great, I think the question is, where would you need the helmet to live?");
	if (remembers(5,6)) {
		//freetext entry
		String clue3_answer =  di.getFreetext();;
		migrationData.put("clue3_answer",clue3_answer);
	} else {
		String[] optionsAnswer= {"Mars","Underwater","A Battle","Scotland"};
		String clue3_answer = di.multipleChoiceQuestion(4,optionsAnswer);
		migrationData.put("clue3_answer",clue3_answer);
	}
	ep6return();
}
ep6return() {	
	speak("Ok, let's go back to the screen for another clue.");
	speak("I'll just let you find the way yourself?");
	di.getResponse("OK");
	
	//chat time!
	speak("Did you know I'm part of the Lirec project?");
	speak("It's researching artificial companions, like myself! ");
	speak("There are ten different institutes involved");
	
	waitForReturnToScreenEp2();
}
/***********************************************/
//Episode 7

episode7screen() {
	migrationData.put("episode","7");
	if (remembers(6,7)) {
		if (remembers(1,7))
			speak("Ok, we've got the third answer.");
		speak("According to you, " + migrationData.get("clue3_answer") + " is where you need the helmet.");
		speak("We'll find out at the end if that's right.");
	}
	String first_clue = migrationData.get("first_clue");
	String second_clue = migrationData.get("second_clue");
	String third_clue = migrationData.get("third_clue");
	if (remembers(5,7)) //remembers at least one clue we picked before
	{
		ArrayList options = new ArrayList(Arrays.asList(new String[]{"clue b","clue a","clue d","clue c"}));
		options.remove(third_clue);
		if (remembers(3,7)) options.remove(second_clue);
		if (remembers(1,7)) options.remove(first_clue);
		String fourth_clue;
		if (options.size()==1)
		{
			speak("Here's the last clue for you.");
			fourth_clue = (String) options.get(0);
		}
		else
		{
			speak("Do you have all 4 answers or do you need another clue?");
			String[] needClues = {"have all","need clue"};
			String answerOrClues = di.multipleChoiceQuestion(2,needClues);			
			speak("Let's give you the next clue");
			speak("Which one do you want to try next?");
			fourth_clue = di.multipleChoiceQuestionList(options.size(),options);
			while (fourth_clue.equals(first_clue) || fourth_clue.equals(second_clue))
			{
				if (fourth_clue.equals(first_clue))
				{
					speak("This clue is \"hermit\"");	
					if (remembers(2,7)) speak("You've done this one before.");
				}
				else if (fourth_clue.equals(second_clue))
				{
					speak("This clue is \"Angus\"");	
					speak("Do you want this clue, or to choose again?");
				}
				di.multipleChoiceQuestion(2,new String[]{"choose again","keep it"});
				speak("Ok, here are the choices again:");
				fourth_clue = di.multipleChoiceQuestionList(options.size(),options);
			}
		}
		migrationData.put("fourth_clue",fourth_clue);
	} else { //don't remember any previous clue
		speak("Do you have all 4 answers or do you need another clue?");
		String[] needClues = {"have all","need clue"};
		String answerOrClues = di.multipleChoiceQuestion(2,needClues);			
		speak("Alright, please choose the next clue then.");
		String[] optionsClues = {"clue b","clue a","clue d","clue c"};
		String fourth_clue = di.multipleChoiceQuestion(4,optionsClues);
		while (fourth_clue.equals(first_clue) || fourth_clue.equals(second_clue) || fourth_clue.equals(third_clue)) {
			if (fourth_clue.equals(third_clue))
			{
				speak("The clue is \"helmet\"");
				if (remembers(6,7)) speak("You've just done this one.");
			} 
			else if (fourth_clue.equals(second_clue))
			{
				speak("The clue is \"Angus\"");
				if (remembers(4,7)) speak("You've done this one before.");
			}
			else if (fourth_clue.equals(first_clue))
			{
				speak("The clue is \"hermit\"");
				if (remembers(2,7)) speak("You've done this one before.");
			}
			speak("Do you want this clue, or to choose again?");
			di.multipleChoiceQuestion(2,new String[]{"choose again","keep it"});
			speak("Ok, here are the choices again:");
			fourth_clue = di.multipleChoiceQuestion(4,optionsClues);
		}
		migrationData.put("fourth_clue",fourth_clue);
		//TODO log this stuff
	}
	//reveal the clue now
	speak("OK, this clue is \"map\"."); 
	speak("Remember that, it's important. Map.");
	
	if (remembers(6,7)) {
		speak("Lirec is spread all over the map, as it were.");
		speak("the partners are in 6 different countries.");
	} else {
		speak("This reminds me, did you know I'm part of the Lirec project?");
		di.showExpressionNoWait(Expression.JOY);
		speak("It's researching artificial companions, like myself! ");
		speak("There are ten different institutes involved");
		speak("They are spread all over the map, as it were.");
	}
	di.showExpressionNoWait(Expression.JOY);
	speak("Anyway, I get distracted so easily.");
	speak("We've got a clue to hunt, map");
	speak("Now start the app if you need to, and press the ready button to get going");
}
/***********************************************/
//Episode 8 -- map
episode8startPhone() {
	migrationData.put("episode","8");
	if (remembers(7,8)) {
		speak("Right, map, I think I know what that's about.");
	} else {
		speak("Hello there, you're here to find a treasure, right?");
		speak("Can you let me know the clue please?");
		String userClue = di.getFreetext().trim();
		while (!userClue.equalsIgnoreCase("map")) {
			speak("That doesn't make sense, try again:");
			di.showExpression(Expression.SADNESS);
			userClue = di.getFreetext();
		}
		speak("Right, I think I know what that's about");
	}
	speak("So now I'll take you to find the map.");
	di.getResponse("OK");
	latest = "episode8";
	migrationData.put(latest +"Failures","0");
	timeStamp("ts_PhoneNav_"+migrationData.get("episode"));
	di.startNav("1.54","gsouth1","episode8Arrived()");

}
episode8Arrived() {
	timeStamp("ts_PhoneNavArrived_"+migrationData.get("episode"));
	Thread.sleep(16000);
	speak("I've seen a map down here.");
	speak("It's a map of an island.");
	Thread.sleep(3000);
	speak("Have you found the map?");
	di.getResponse("Yes");
	speak("Great, I think the question is, what island is it a map of?");
	if (remembers(7,8)) {
		//freetext entry
		String clue4_answer =  di.getFreetext();;
		migrationData.put("clue4_answer",clue4_answer);
	} else {
		String[] optionsAnswer= {"Japan","Mull","Lewis","Orkney", "Iceland"};
		String clue4_answer = di.multipleChoiceQuestion(5,optionsAnswer);
		migrationData.put("clue4_answer",clue4_answer);
	}
	ep8return();
}


ep8return() {	
	
	if (remembers(2,8))
	{
		speak("Ok, this was the last clue.");
		speak("Let's go back to the screen to finish");
		
	}
	else
		speak("Ok, let's go back to the screen for another clue.");

	speak("I'll just let you find the way yourself?");
	di.getResponse("OK");
	
	// 2nd recall test asking about agent's home town
	speak(migrationData.get("clue4_answer")+" is a pretty small island.");
	speak("Being Irish, I much prefer Ireland");
	speak("Where in Ireland do you think I'm from?");
	String[] optionsTowns = {"I have no idea.","Dublin","Cork","Limerick","Galway"};
	String town = di.multipleChoiceQuestion(5,optionsTowns);
	migrationData.put("townChoice",town);
	if (town.contains("idea")) {
		speak("Typical!");
		migrationData.put("secondWhy","have no idea");
	} else {
		speak("Why did you choose that town?");
		String responseWhy = di.getFreetext();
		speak("Oh, of course.");
		migrationData.put("secondWhy",responseWhy);
	}
	if (remembers(2,8))
	{
		speak("Nearly done, no more clues and walking about");
	}
	waitForReturnToScreenEp2();
}

/***********************************************/
//Episode 9

episode9screen() {
	migrationData.put("episode","9");
	String first_clue = migrationData.get("first_clue");
	String second_clue = migrationData.get("second_clue");
	String third_clue = migrationData.get("third_clue");
	String fourth_clue = migrationData.get("fourth_clue");
	String clue1_answer = migrationData.get("clue1_answer");
	String clue2_answer = migrationData.get("clue2_answer");
	String clue3_answer = migrationData.get("clue3_answer");
	String clue4_answer = migrationData.get("clue4_answer");

	if (remembers(8,9)) {
		if (remembers(1,9))
			speak("Ok, we've got the last answer.");
		speak("According to you, " + clue4_answer + " is the island on the map.");
		speak("We'll find out at the end if that's right.");
		di.showExpressionNoWait(Expression.JOY);
		speak("I still reckon Ireland is the best island though!");
	}
		
	if (!remembers(1,9))
	{
		speak("Do you have all 4 answers or do you need another clue?");
		String[] needClues = {"have all","need clue"};
		String answerOrClues = di.multipleChoiceQuestion(2,needClues);
	}
	
	di.showExpressionNoWait(Expression.JOY);
	speak("Ok, you've chased after all the clues. Let's see what you got right");
	int noCorrect = 0;

	
	if (remembers(2,9))
	{
		speak("First you went for hermit");	
	}
	else
	{
		speak("What is hermit?");
		String[] optionsAnswer= {"a lonely man","singer of a band","a frog","a crab"};
		clue1_answer = di.multipleChoiceQuestion(4,optionsAnswer);	
	}
	if (clue1_answer.contains("rog"))
	{
		di.showExpressionNoWait(Expression.JOY);
		speak("You gave the correct answer: frog"); 
		noCorrect ++;
	}
	else {
		speak("You didn't find the correct answer, it was frog.");
		di.showExpressionNoWait(Expression.SADNESS);
	}
	speak("Hermit the frog is a song title on the poster.");


	if (remembers(4,9))
	{
		speak("Next you went for Angus");	
	}
	else
	{
		speak("Moving on. What colour is Angus?");
		String[] optionsAnswer= {"Orange","Green","Purple","White"};
		clue2_answer = di.multipleChoiceQuestion(4,optionsAnswer);	
	}
	if (clue2_answer.contains("range"))
	{
		di.showExpressionNoWait(Expression.JOY);
		speak("You gave the correct answer: orange"); 
		noCorrect ++;
	}
	else {
		di.showExpressionNoWait(Expression.SADNESS);
		speak("You didn't find the correct answer, it was orange.");
	}
	speak("Angus is a submarine and his colour is orange.");
	
	
	if (remembers(6,9))
	{
		speak("Your third pick was helmet");	
	}
	else
	{
		speak("Next question. Where would you need the helmet?");
		String[] optionsAnswer= {"Mars","Underwater","A Battle","Scotland"};
		clue3_answer = di.multipleChoiceQuestion(4,optionsAnswer);	
	}
	if (clue3_answer.contains("ars"))
	{
		di.showExpressionNoWait(Expression.JOY);
		speak("You gave the correct answer: Mars"); 
		noCorrect ++;
	}
	else {
		di.showExpressionNoWait(Expression.SADNESS);
		speak("You didn't find the correct answer, it was Mars.");	
	}
	speak("You need a helmet on Mars.");	

	if (remembers(8,9))
	{
		speak("Finally you investigated the clue map");	
	}
	else
	{
		speak("Last one. Which island is on the map?");
		String[] optionsAnswer= {"Japan","Mull","Lewis","Orkney"};
		clue4_answer = di.multipleChoiceQuestion(4,optionsAnswer);	
	}
	if (clue4_answer.contains("ull"))
	{
		di.showExpressionNoWait(Expression.JOY);
		speak("You gave the correct answer: Mull"); 
		noCorrect ++;
	}
	else {
		di.showExpressionNoWait(Expression.SADNESS);
		speak("You didn't find the correct answer, it was Mull.");
	}
	speak("The island on the map is Mull.");
	
	// questions to check user's mental model and diversion questions
	speak("Nearly there. I have just 2 more questions for you.");
	speak("First, which clue do you think is the furthest away from here?");
	String[] optionsAnswer= {"Hermit","Angus","Helmet","Map"};
	answer_furthest = di.multipleChoiceQuestion(4,optionsAnswer);	
	migrationData.put("furthest",answer_furthest);
	speak("Ah, interesting you should say that.");

	speak("And finally, how many metres do you think I have travelled today?");	
	String[] optionsAnswer= {"327","0","2566","612"};
	answer_distance = di.multipleChoiceQuestion(4,optionsAnswer);	
	migrationData.put("DISTANCE",answer_distance);
	speak("Not everyone gets that right.");		
	
	// wrapping up
	speak("Right, back to the treasure.");
	migrationData.put("noCorrect",noCorrect.toString());
	speak("You have found " + noCorrect + " out of 4 answers.");
	if (noCorrect == 4)
	{
		di.showExpressionNoWait(Expression.JOY);
		speak("Well done. You've passed the test.");
		speak("The treasure is almost possibly yours. Please speak to the human.");		
	} else
	{
		speak("Sorry, you failed.");
		di.showExpressionNoWait(Expression.SADNESS);
		speak("Don't worry, the treasure was not that great anyway, don't be sad.");
		speak("Please speak to the human.");	
	}
	speak("Bye for now, I've enjoyed your company.");
	timeStamp("ts_EndOfExperiment");
	writeLog();
}

timeStamp(String key) {
	double timeSinceStart = ((System.currentTimeMillis() - startTime)/1000);
	migrationData.put(key,timeSinceStart.toString());
}

writeLog() {
	// SORT THIS INTO NEW DATA STRUCTURE
	TreeMap output = new TreeMap(migrationData);
	di.log("\n"+output.toString());
}



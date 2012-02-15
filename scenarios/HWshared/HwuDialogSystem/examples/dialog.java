import uk.ac.hw.lirec.dialogsystem.DialogInterface;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Moods;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Expression;
import java.util.HashMap;




/*
 * GENERAL HELP
 * Watch you don't miss a ; off the end of a line, can lead to silent failing to 
 * do anything!
 */


DialogInterface di;
HashMap migrationData = new HashMap();
String server = new String("192.168.1.21");

greetUser(String name) {
	di.speakText("Hello there, "+name);
}

saySomething(String speech) {
	di.speakText(speech);
}

giveDirections(String dir, String location) {
	if (location.contains("2.22")) {
		saySomething("Hey, this is my favourite lab!");
	}
	saySomething(dir);
}

//Script for demo video


startInteraction() {
	saySomething("Hello, are you Max? I'm waiting for him.");
	String[] options = {"Yes","No"};
	di.multipleChoiceQuestion(2,options);
	saySomething("Great, you're here to see Michael, do you know the way to the lab?");
	di.multipleChoiceQuestion(2,options);
	saySomething("OK, well I can tell you where to go or show you on your phone.")
	String[] options2 = {"Phone","Tell me"};
	di.multipleChoiceQuestion(2,options2);
	saySomething("Load the app and press ready when it's loaded.");
}

startPhoneInteraction() {
	di.blankScreen();
	di.getResponse("Ready");
	di.unBlankScreen();
	saySomething("Right, we're off to the lab.");
	di.startNav("Gnorth2","2.22");
}

/*
 * This shows up an issue in this "single episode" style, perhaps the answer is
 * to have blocking functions that return values? program/thread control might be a bit odd then...
 * 
 * need a policy and stick with it (or does it not matter?)
 * 
 * There's an issue here where getResponse was resulting in a call to evaluate something else
 * on this script, was calling the same interpreter, when it hadn't finished the previous evaluation...
 * ...etc. Seems to cause issues. Need to be careful to prevent this (or avoid with new interp instances
 * but then the issue is might end up with many cos of recursion!)
 * 
 * To make this work correctly, getResponse should:
 * 	not call the interpreter
 * 	return immediately, (as it's UI stuff).
 * 
 * THIS IS THE UNUSED APPROACH
 *
tellJoke() {
	System.out.println("tellJoke called");
	di.speakText("Here's a joke.");
	di.speakText("knock knock");
	di.getResponse("Who's there?","jokePart2()");
}

jokePart2() {
	di.speakText("Sherwood");
	di.getResponse("Sherwood who?","jokePart3()");
}

jokePart3() {
	di.speakText("sherwood like to meet you!");
}
*/

/*
 * There's a need to be careful with threading, e.g. the below might be acceptable if
 * this is run in a different thread to UI stuff
 * This shows how the timing could be played with.
 */

boolean wait = true;

blockingJoke() {
	di.speakText("knock knock");
	di.getResponse("Who's there?");
	Thread.sleep(3000);
	di.speakText("Sherwood");
	//just the first part to test this.
}




/*
	Here's a mk2 version, where the "actions" don't result in calls to the interpreter before
	they return - this is much safer, avoids the issue, but perhaps awkward?
	
	This relies on the getresponse calls blocking till the response is given, which they
	don't, as android's all about asynchronous dialog/ui in general.
*/
tellJokeBlocking() {
	di.speakText("knock knock");
	di.getResponse("Who's there?");
	jokePart2();
}

jokePart2() {
	di.speakText("Sherwood");
	di.getResponse("Sherwood who?");
	jokePart3();
}

jokePart3() {
	di.speakText("sherwood like to meet you!");
}

//of course this can be combined:

combinedJoke() {
	di.speakText("knock knock");
	di.getResponse("Who's there?");
	di.speakText("Sherwood");
	di.getResponse("Sherwood who?");
	di.speakText("sherwood like to meet you!");
}

//some waffle to test interruptions
waffle() {
	di.speakText("Here's a long sentence to test how this pans out with the subtitles.");
	di.speakText("more waffle waffle waffle waffle waffle.");
	di.speakText("And some more.");
}
test() {
	di.speakText("Time to navigate from 2.22 to 2.52");
	di.startNav("2.22","2.52");
}
test2() {
	di.speakText("First I'll blank the screen");
	di.blankScreen();
	Thread.sleep(2000);
	di.unBlankScreen();
	di.speakText("Now I'll test response:");
	di.getResponse("click me!");
	di.speakText("great, now a question.");
	di.speakText("Shall I be angered or surprised?");
	String[] options = {"Angered!","Surprised!"};
	String answer = di.multipleChoiceQuestion(2,options);
	if (answer.equals("Angered!")) {
		System.out.println("ANGER");
		di.showExpression(Expression.ANGER);
	} else {
		System.out.println("SURPRISE");
		di.showExpression(Expression.SURPRISE);
	}
	di.speakText("Now I'm going to set a mood - sadness");
	di.setMood(Moods.SADNESS);
	di.speakText("Time to navigate from 2.22 to 2.52");
	di.startNav("2.22","2.52");
}

testMigrate() {
	di.speakText("Stored name is: "+migrationData.get("name"));
	migrationData.put("result","some data");
	di.speakText("migrating out");
	di.migrateDataOut("somewhere",migrationData);
}

startScreenMigrateTest() {
	migrationData.put("migrationNum","0");
	di.speakText("Hello there");
	di.speakText("This is migration "+migrationData.get("migrationNum") );
	di.speakText("Press ready on the phone when you're ready to go.");
}

testInOut() {
	di.speakText("Hello there");
	di.speakText("This is migration "+migrationData.get("migrationNum") );
	Integer num = Integer.parseInt(migrationData.get("migrationNum"));
	num++;
	migrationData.put("migrationNum",num.toString());
	waitAndMigrateOut(server);
	waitAndRequestMigration(from);
}
testInOutScreen() {
	di.speakText("Hello there");
	di.speakText("This is migration "+migrationData.get("migrationNum") );
	Integer num = Integer.parseInt(migrationData.get("migrationNum"));
	num++;
	migrationData.put("migrationNum",num.toString());
	di.speakText("Press ready on the phone when you're ready to go.");
}

screenMigrateInDone() {
	testInOutScreen();
}

migrateInDone() {
	//TODO condition on what's been migrated in somehow
	testInOut();
}
waitAndRequestMigration(String from) {
	di.blankScreen();
	di.getResponse("Ready");
	while(!di.inviteMigrate(from)) {
		System.out.println("ERROR MIGRATING");
		di.getResponse("Ready");
	}
}
waitAndMigrateOut(String to) {
	di.getResponse("Ready");
	while(!di.migrateDataOut(to,migrationData)) {
		System.out.println("ERROR MIGRATING");
		di.getResponse("Ready");
	}
}

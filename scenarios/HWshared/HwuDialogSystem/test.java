import uk.ac.hw.lirec.dialogsystem.DialogInterface;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Moods;
import uk.ac.hw.lirec.dialogsystem.DialogInterface.Expression;





/*
 * GENERAL HELP
 * Watch you don't miss a ; off the end of a line, can lead to silent failing to 
 * do anything!
 */


DialogInterface di;


greetUser(String name) {
	di.speakText("Hello there, "+name);
}

saySomething(String speech) {
	di.speakText(speech);
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
	di.speakText("Cutting edge robot technology is being used to enhance the ancient tradition of whisky barrel cooperage. The hi-tech systems have been installed at a new custom-designed £10m cooperage near Alloa in Clackmannanshire.");
	di.speakText("more waffle waffle waffle waffle waffle.");
}

test() {
	di.speakText("First I'll test response:");
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
}

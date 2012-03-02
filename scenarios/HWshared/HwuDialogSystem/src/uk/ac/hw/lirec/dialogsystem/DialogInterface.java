package uk.ac.hw.lirec.dialogsystem;

import java.util.HashMap;

/**
 * @author iw24
 * The purpose of this class, and its subclasses, is to define the 
 * interface the dialog script has with the agent. So the methods here are
 * those provided for the dialog system to call.
 */
public abstract class DialogInterface {
	
	/**
	 * Decided that I'd limit the moods/EMOTIONS here rather than having free choice. Should
	 * help compatibility of scripts a bit.
	 *
	 */
	public enum  Moods { NEUTRAL, ANGER, JOY, SADNESS, SLEEP};
	/**
	 * this as basically momentary moods/expressions/emotions
	 */
	public enum Expression { ANGER, SURPRISE, JOY, FEAR, SADNESS };
	
	/**
	 *  Stop the current dialog.
	 */
	protected abstract void interruptDialog();
	/**
	 * This should reset the DI, ready to run a new event.
	 * 
	 * TODO is it implementation specific whether or not resetDi should be called after an
	 * interrupt? There's an issue with threading here:
	 * 	interrupt from another thread
	 * 	call reset - but the interrupt isn't done handling, so inconsistency?
	 * 
	 * could put the calls into a method in the dialog system itself, to enforce this?
	 * 	think this makes more sense? DONE THIS.
	 * 
	 */
	protected abstract void resetDi();
	
	/**
	 * @param text text for the agent to speak. 
	 */
	public abstract void speakText(String text);
	
	/**
	 * 
	 * Wait for a user's response
	 * 
	 * @param infoText text to display whilst awaiting confirmation/response
	 * 
	 */
	public abstract void getResponse(String infoText); 
	
	/**
	 * Ask the user a multiple choice question. This method allows for unlimited options
	 * but that might not sensible on some implementations! Again following maximally-general
	 * principle.
	 * 
	 * NOTE: this doesn't include the question, it's assumed it'll be spoken first.
	 * This will probably be sub-titled. Might want to provide a method to include
	 * it though - but it'd have no obligation to display it, so you'd need to speak
	 * anyhow...
	 * 
	 * @param numChoices how many choices are there.
	 * @param options what text should be shown for the options
	 * @return the string value for the choice (should be a member of options).
	 */
	public abstract String multipleChoiceQuestion(Integer numChoices, String[] options);
	
	/**
	 * A mood is a long-lasting expression the agent should wear.
	 * @param mood the mood to set.
	 */
	public abstract void setMood(Moods mood);
	
	/**
	 * @param expression An instantaneous expression to show, e.g. surprise, 
	 * should be blocking.
	 */
	public abstract void showExpression(Expression expression);
	
	/**
	 * This should handle any screen blanking etc etc required.
	 * @param migrateTo the IP/address (or some ID?) of the embodiment to migrate to
	 * @param dataToMigrate data to send out to migrate, just keys and values.
	 * @return if migration succeeded
	 */
	public abstract boolean migrateDataOut(String migrateTo, HashMap<String,String> dataToMigrate);
	
	/**
	 * This should handled whatever's required to migrate in - switching the screen on etc.
	 * @param migrateFrom where you want to invite migration from.
	 * @return true if migration is happening.
	 */
	public abstract boolean inviteMigrate(String migrateFrom);

}

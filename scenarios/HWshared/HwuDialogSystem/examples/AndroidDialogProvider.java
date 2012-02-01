package uk.ac.hw.lirec.dialogsystem;

import uk.ac.hw.lirec.emys3d.EmysModel.Emotion;

public interface AndroidDialogProvider {
	public void speakText(String text);
	public void confirmDialog(String infoText);
	public void dismissConfirmDialog();
	public void askMultiChoice(Integer numChoices, String[] options);
	public void dismissMultiDialog();
	public void setExpression (Emotion em);
	public Emotion getEmysEmotion();
}

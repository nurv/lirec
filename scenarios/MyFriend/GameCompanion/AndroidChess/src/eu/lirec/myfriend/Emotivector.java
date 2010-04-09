package eu.lirec.myfriend;

import java.util.ArrayList;
import java.util.List;

import org.kxml2.kdom.Element;

public class Emotivector {

	public enum Reactions {StrongerReward, ExpectedReward, WeakerReward, UnexpectedReward,
		Think, UnexpectedPunishment, WeakerPunishment, ExpectedPunishment, StrongerPunishment}
	
	public enum MoodModel {Dynamic, Happy, Neutral, Sad}
	
	private int error;
	private List<Integer> sensedValues;
	private int previousExpected;
	private int expected;
	private int valuesConsidered;
	private int threshold;
	private MoodModel currentModel;
	
	public Emotivector(){
		this.error = 3;
		this.sensedValues = new ArrayList<Integer>();
		this.previousExpected = 0;
		this.expected = 0;
		this.valuesConsidered = 2;
		this.threshold = 30;
		this.currentModel = MoodModel.Dynamic;
	}
	
	public void setMoodModel(MoodModel model){
		currentModel = model;
	}

	public double calculateMood(int heuristic){
		
		switch (currentModel) {
		case Happy:
			return 100;
		case Neutral:
			return 0;
		case Sad:
			return -100;

		case Dynamic:
		default:
			break;
		}
		
		double mood;
		if(heuristic == 0) {
			return 0;
		}
			
		if(heuristic > 0){
			mood = normalizeMood(heuristic);
		} else {
			mood = -normalizeMood(-heuristic);
		}
		
		mood = mood/2 * 100/2;
		
		return mood;
	}
	
	private double normalizeMood(int heuristic){
		return Math.log10(heuristic);
	}
	
	public void updateValues(int sensed) {
		this.sensedValues.add(sensed);

		previousExpected = expected;
		
		int s = 0;
		if (sensedValues.size() >= 2) {
			s = sensedValues.get(sensedValues.size() - 2);
		}
		expected = predict(sensedValues, /* sensed */s, previousExpected, valuesConsidered);

		if (expected > 9999) {
			expected = 9999;
		} else if (expected < -9999){
			expected = -9999;
		}
		
		//TODO calculate threshold
	}
	
	
	//moving averages algorithm
	protected int predict(List<Integer> sensedValues, int sensed, int previousExpected, int valuesConsidered){
		int expected = 0;
		float weight = 2/(1+valuesConsidered);
		
		if (sensedValues.size() <= valuesConsidered){
			int sum = 0;
			
			for (Integer value : sensedValues) {
				sum += value;
			}
			expected = sum / sensedValues.size();
			
		} else {
			expected = Math.round(sensed * weight + previousExpected * (1-weight));
		}
		
		return expected;
	}
	
	public Reactions getReaction(){
		int delta = expected - previousExpected;
		int sensed = sensedValues.get(sensedValues.size()-1);
		
		return calculateReaction(delta, expected, sensed, threshold);
	}
	
	private Reactions calculateReaction(int delta, int expected, int sensed, int threshold){
		
		Reactions reaction = Reactions.Think;
		
		if (-error < delta && delta < error){
			if (sensed > (expected + threshold)) {
				reaction = Reactions.UnexpectedReward;
			} else if (sensed < (expected - threshold)) {
				reaction = Reactions.UnexpectedPunishment;
			} else {
				reaction = Reactions.Think;
			}
			
		} else if (delta > 0) {
			if (sensed > (expected + threshold)) {
				reaction = Reactions.StrongerReward;
			}
			else if (sensed < (expected - threshold)) {
				reaction = Reactions.WeakerReward;
			} else {
				reaction = Reactions.ExpectedReward;
			}
			
		} else /* if (delta < 0)*/ {
			if (sensed > (expected + threshold)) {
				reaction = Reactions.WeakerPunishment;
			}
			else if (sensed < (expected - threshold)) {
				reaction = Reactions.StrongerPunishment;
			} else {
				reaction = Reactions.ExpectedPunishment;
			}
		}
		
		return reaction;
	}
}

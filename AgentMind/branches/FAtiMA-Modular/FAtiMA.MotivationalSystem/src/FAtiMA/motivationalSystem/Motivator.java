/*
 * Motivator.java - Represents a motivator based on the PSI model, 
 * which is an instance of MotivatorType
 */

package FAtiMA.motivationalSystem;

import java.io.Serializable;

import FAtiMA.Core.AgentSimulationTime;

/**
 *  Represents a motivator based on the PSI model, which is an instance of MotivatorType
 * 
 *  @author Meiyii Lim
 */

public class Motivator implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * Constant value that defines how fast should a drive decay over time.
	 */
	//public static final float MotivatorDecayFactor = 0.0025f;
	public static final float MotivatorDecayFactor = 0.001f;

	private final short _type;
	private float _decayFactor;	
	private float _weight;		// a factor of the personality threshold?
	private float _intensity;

	private float _intensityATt0; 
	private long _t0=0;
	
	/**
	 * Creates a new Motivator
	 * @param type - the type of built-in motivator
	 // * @param threshold - the min value for the motivator that the character wants to maintain
	 * @param decayFactor - the decay factor for the intensity of the motivator over time 
	 * @param weight - the weight of the motivator
	 * @param intensity- intensity of the built-in motivator, the higher the intensity 
	 * 					 the lower the need due to a smaller deviation from the threshold, 
	 * 					 this value will be pre-defined based on scenario	
	 */
	public Motivator(short type, float decayFactor, float weight, float intensity) {
		_type = type;
		_decayFactor = decayFactor;
		_weight = weight;
		_intensity = intensity;
		
		_t0 = AgentSimulationTime.GetInstance().Time();
		_intensityATt0 = _intensity;
	}
	
	/**
	 * Creates a new Motivator that consists in a copy of a given motivator
	 * @param mot - the motivator that will be copied into the new motivator
	 */
	public Motivator(Motivator mot) {
		_type = mot._type;
		_decayFactor = mot._decayFactor;
		_weight = mot._weight;
		_intensity = mot._intensity;
		
		_t0 = AgentSimulationTime.GetInstance().Time();
		_intensityATt0 = _intensity;
	}
	
	/**
	 * Gets the motivator's type
	 * @return a short representing the motivator type (enumerable)
	 * @see the enumerable MotivatorType
	 */
	public short GetType() {
		return _type;
	}
	
	/**
	 * Gets the motivator's weight
	 * @return a float value corresponding to the motivator's weight
	 */
	public float GetWeight() {
		return _weight;	
	}
	
	public void SetWeight(float weight)
	{
		_weight = (float) Math.pow(2, (weight/5)-1 );
	}

	/**
	 * Gets the motivator's intensity 
	 * @return a float value corresponding to the motivator's intensity
	 */
	public float GetIntensity() {
		return _intensity;	
	}
	
	/**
	 * Gets the motivator's need
	 * @return a float value corresponding to the motivator's intensity
	 * @deprecated use GetNeedUrgency() instead.
	 */
	public float GetNeed() {
		return (10 - _intensity);	
	}

	
	/**
	 * Gets the motivator's urgency
	 * discretizing the need intensity into diffent categories 
	 * (very urgent, urgent, not urgent, satisfied)
	 * @return a multiplier corresponding to the motivator's urgency 
	 */
	public float GetNeedUrgency() {
		
		return 10 - _intensity;
		/*if(_intensity < 2.5){ // VERY URGENT
			return 4;
		}
		if(_intensity >= 2.5 && _intensity < 5){ //URGENT
			return 3;
		}
		if(_intensity >= 5 && _intensity < 7.5){ //NOT URGENT
			return 2;
		}
		else{
			return 1; // NEED SATISFIED
		}*/		
	}
	
	/**
	 * Update the motivator's intensity 
	 * @return a float value corresponding to the difference between the current and the old value
	 */
	public float UpdateIntensity(float effect) {
		float oldIntensity = _intensity;
		
		_t0 = AgentSimulationTime.GetInstance().Time();
		//_intensity = Math.max(0, Math.min(10, _intensity + (_weight*K*effect)));
		_intensity = Math.max(0, Math.min(10, _intensity + effect));
		_intensityATt0 = _intensity;
		
		float gain =  _intensity - oldIntensity;
		
		return gain;
		
	}
	
	/**
	 * Set the motivator's intensity 
	 * @return a float value corresponding to the motivator's intensity
	 */
	public void SetIntensity(float intensity) {
		_t0 = AgentSimulationTime.GetInstance().Time();
		_intensity = intensity;
		
		_intensityATt0 = _intensity; 
	}
	
	/**
	 * Decays the motivator intensity according to the system's timer
	 * @return the intensity of the motivator after being decayed
	 */
	public void DecayMotivator() {
		long deltaT;
	
		deltaT = (AgentSimulationTime.GetInstance().Time() - _t0)/1000;
		_intensity = Math.max(0, _intensityATt0 * ((float) Math.exp(-MotivatorDecayFactor * _decayFactor * deltaT)));
	}
	
	/**
	 * Converts the Motivator to XMl
	 * @return a XML String that contains all information about the Motivator
	 */
	public String toXml() {
		return "<Motivator type=\"" + MotivatorType.GetName(_type) + 
				"\" decayFactor=\"" + _decayFactor + 
				"\" weight=\"" + _weight + 
				"\" intensity=\"" + _intensity + "\" />";
	}

	public float GetDecayFactor() {
		
		return _decayFactor;
	}

	public void SetDecayFactor(float newAffiliationDecayFactor) {
		this._decayFactor = newAffiliationDecayFactor;
		
	}
}

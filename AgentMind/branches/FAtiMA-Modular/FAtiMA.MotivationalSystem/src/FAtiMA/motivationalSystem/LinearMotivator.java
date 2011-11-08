package FAtiMA.motivationalSystem;

import FAtiMA.Core.AgentSimulationTime;


public class LinearMotivator extends Motivator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LinearMotivator(String name, float decayFactor, float weight, float intensity, boolean internalUpdate)
	{
		super(name,decayFactor,weight,intensity,internalUpdate);
	}
	
	protected LinearMotivator(LinearMotivator lm)
	{
		super(lm);
	}
	
	public float UpdateIntensity(float effect) {
		float oldIntensity = _intensity;
		
		_t0 = AgentSimulationTime.GetInstance().Time();
		//_intensity = Math.max(0, Math.min(10, _intensity + (_weight*K*effect)));
		_intensity =  _intensity + effect;
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
	
	public double evaluateNeedVariation(float deviation) 
	{
		return deviation;
	}
	
	public Object clone()
	{
		return new LinearMotivator(this);
	}
	
}

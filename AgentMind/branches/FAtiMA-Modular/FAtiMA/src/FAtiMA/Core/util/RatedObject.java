package FAtiMA.Core.util;

public class RatedObject<T> implements Comparable<RatedObject<T>> {
	
	private T _object;
	private float _rate;
	
	public RatedObject(T obj, float score)
	{
		this._object = obj;
		this._rate = score;
	}
	
	public T getObject()
	{
		return this._object;
	}
	
	public float getRate()
	{
		return this._rate;
	}
	
	public void setRate(float value)
	{
		this._rate = value;
	}

	@Override
	public int compareTo(RatedObject<T> o) {
		if(this._rate < o._rate)
		{
			return -1;
		}
		else if(this._rate == o._rate)
		{
			return 0;
		}
		else return 1;
	}
	
	public String toString()
	{
		return this._object.toString() + " : " + this._rate;
	}
}

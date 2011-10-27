/** 
 * MyPleoNeeds.java - Container for the companion needs: cleanliness, energy, petting, skills and
 * water.
 *  
 * Copyright (C) 2011 GAIPS/INESC-ID 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * Company: GAIPS/INESC-ID
 * Project: Pleo Scenario
 * @author: Paulo F. Gomes
 * Email to: pgomes@gaips.inesc-id.pt
 */

package eu.lirec.pleo;

public class MyPleoNeeds {
	public static final int CLEANLINESS_ID = 20480;
	public static final int ENERGY_ID = 6;
	public static final int PETTING_ID = 20481;
	public static final int SKILLS_ID = 20482;
	public static final int WATER_ID = 20483;
	public static final int NEED_END_ID = 34;
	
	private int _needCleanliness;
	private int _needEnergy;
	private int _needPetting;
	private int _needSkills;
	private int _needWater;
	
	public int getNeedCleanliness() {
		return _needCleanliness;
	}
	public void setNeedCleanliness(int needCleanliness) {
		_needCleanliness = needCleanliness;
	}
	public int getNeedEnergy() {
		return _needEnergy;
	}
	public void setNeedEnergy(int needEnergy) {
		_needEnergy = needEnergy;
	}
	public int getNeedPetting() {
		return _needPetting;
	}
	public void setNeedPetting(int needPetting) {
		_needPetting = needPetting;
	}
	public int getNeedSkills() {
		return _needSkills;
	}
	public void setNeedSkills(int needSkills) {
		_needSkills = needSkills;
	}
	public int getNeedWater() {
		return _needWater;
	}
	public void setNeedWater(int needWater) {
		_needWater = needWater;
	}
	@Override
	public String toString() {
		return "MyPleoNeeds [_needCleanliness=" + _needCleanliness
				+ ", _needEnergy=" + _needEnergy + ", _needPetting="
				+ _needPetting + ", _needSkills=" + _needSkills
				+ ", _needWater=" + _needWater + "]";
	}
}

/** 
 * MyPleoNeedsVerifiable.java - Container for the companion needs: cleanliness, energy, petting, skills and
 * water. Possible to verify if only part, or all needs have been set.
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

public class MyPleoNeedsVerifiable extends MyPleoNeeds {
	private boolean _needCleanlinessAvailable = false;
	private boolean _needEnergyAvailable = false;
	private boolean _needPettingAvailable = false;
	private boolean _needSkillsAvailable = false;
	private boolean _needWaterAvailable = false;
	
	private boolean _finishedLoading = false;
	
	@Override
	public void setNeedCleanliness(int needCleanliness) {
		_needCleanlinessAvailable = true;
		super.setNeedCleanliness(needCleanliness);
	}

	@Override
	public void setNeedEnergy(int needEnergy) {
		_needEnergyAvailable = true;
		super.setNeedEnergy(needEnergy);
	}

	@Override
	public void setNeedPetting(int needPetting) {
		_needPettingAvailable = true;
		super.setNeedPetting(needPetting);
	}

	@Override
	public void setNeedSkills(int needSkills) {
		_needSkillsAvailable = true;
		super.setNeedSkills(needSkills);
	}

	@Override
	public void setNeedWater(int needWater) {
		_needWaterAvailable = true;
		super.setNeedWater(needWater);
	}

	@Override
	public String toString() {
		return "[MyPleoNeeds]";
	}

	public boolean isNeedCleanlinessAvailable() {
		return _needCleanlinessAvailable;
	}

	public boolean isNeedEnergyAvailable() {
		return _needEnergyAvailable;
	}

	public boolean isNeedPettingAvailable() {
		return _needPettingAvailable;
	}

	public boolean isNeedSkillsAvailable() {
		return _needSkillsAvailable;
	}

	public boolean isNeedWaterAvailable() {
		return _needWaterAvailable;
	}
	
	public boolean areAllNeedsAvailable() {
		boolean allNeedsAvailable = _needCleanlinessAvailable && _needEnergyAvailable && _needPettingAvailable && _needSkillsAvailable && _needWaterAvailable;
		return allNeedsAvailable;
	}
	
	public boolean isFinishedLoading() {
		return _finishedLoading;
	}
	
	public void finishLoading(){
		_finishedLoading = true;
	}
}

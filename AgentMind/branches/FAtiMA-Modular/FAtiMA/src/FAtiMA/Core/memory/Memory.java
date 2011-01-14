/** 
 * Memory.java - Performs operations that involve data from different memories - currently
 * 				AM and STM
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: HWU
 * Project: LIREC
 * Created: 13/03/09 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 13/03/2009 - File created
 * 
 * **/

package FAtiMA.Core.memory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.io.OutputStreamWriter;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.semanticMemory.SemanticMemory;

/**
 * Performs operations that involve data from different memories - currently
 * AM and STM
 * 
 * @author Meiyii Lim
 */

public class Memory implements Serializable {

	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	private SemanticMemory _sm;
	private EpisodicMemory _em;
	private String _saveDirectory;
	private boolean _memoryLoaded;
	
	public Memory()
	{
		_sm = new SemanticMemory();
		_em = new EpisodicMemory();
		_saveDirectory = "";
		_memoryLoaded = false;
	}
	
	public SemanticMemory getSemanticMemory()
	{
		return _sm;
	}
	
	public EpisodicMemory getEpisodicMemory()
	{
		return _em;
	}
	
	public void setSaveDirectory(String saveDirectory)
	{
		_saveDirectory = saveDirectory;
	}
	
	public String getSaveDirectory()
	{
		return _saveDirectory;
	}
	
	public void setMemoryLoad(boolean memoryLoaded)
	{
		_memoryLoaded = memoryLoaded;
	}
	
	public boolean getMemoryLoad()
	{
		return _memoryLoaded;
	}
	
}

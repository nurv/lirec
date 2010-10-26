package FAtiMA.culture;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FAtiMA.exceptions.DuplicateSymbolTranslatorEntry;

public class SymbolTranslatorTest {
	
	@Before
	public void init() throws DuplicateSymbolTranslatorEntry  {
		SymbolTranslator.GetInstance();
		
		SymbolTranslator.GetInstance().addEntry("symbol-1","action-3");
		SymbolTranslator.GetInstance().addEntry("symbol-2","action-2");
		SymbolTranslator.GetInstance().addEntry("symbol-3","action-1");	
	}
	
	@After
	public void destroy()  {
		SymbolTranslator.GetInstance().clearAll();	
	}
	
	@Test(expected=DuplicateSymbolTranslatorEntry.class)
	public void testAddDuplicateSymbolEntry() throws DuplicateSymbolTranslatorEntry{
		SymbolTranslator.GetInstance().addEntry("symbol-1","action-1000");	
	}
	
	@Test(expected=DuplicateSymbolTranslatorEntry.class)
	public void testAddDuplicateActionEntry() throws DuplicateSymbolTranslatorEntry{
		SymbolTranslator.GetInstance().addEntry("symbol-1000","action-3");	
	}
	
	@Test
	public void testTranslateSymbolToAction(){
		assertEquals("action-3",SymbolTranslator.GetInstance().translateSymbolToAction("symbol-1"));
		assertEquals("action-2",SymbolTranslator.GetInstance().translateSymbolToAction("symbol-2"));
		assertEquals("action-1",SymbolTranslator.GetInstance().translateSymbolToAction("symbol-3"));
    }
	
	@Test
	public void testTranslateSymbolToActionNoSymbolFound(){
		assertEquals("symbol-1000",SymbolTranslator.GetInstance().translateSymbolToAction("symbol-1000"));
	}
	
	@Test
	public void testTranslationNoEntries(){
		SymbolTranslator.GetInstance().clearAll();	
		assertEquals("symbol-1000",SymbolTranslator.GetInstance().translateSymbolToAction("symbol-1000"));
	}
	
	
	@Test
	public void testTranslateActionToSymbolNoActionFound(){
		assertEquals("action-1000",SymbolTranslator.GetInstance().translateActionToSymbol("action-1000"));
	}

	
	@Test
	public void testTranslateActionToSymbol(){
		assertEquals("symbol-3",SymbolTranslator.GetInstance().translateActionToSymbol("action-1"));
		assertEquals("symbol-2",SymbolTranslator.GetInstance().translateActionToSymbol("action-2"));
		assertEquals("symbol-1",SymbolTranslator.GetInstance().translateActionToSymbol("action-3"));	
    }
	
	
	
	@Test
	public void testClearAll() throws DuplicateSymbolTranslatorEntry{
		SymbolTranslator.GetInstance().clearAll();
		SymbolTranslator.GetInstance().addEntry("symbol-1","action-1");
		assertEquals("symbol-1",SymbolTranslator.GetInstance().translateActionToSymbol("action-1"));
		SymbolTranslator.GetInstance().clearAll();
		assertEquals("action-1",SymbolTranslator.GetInstance().translateActionToSymbol("action-1"));
    }
	
}

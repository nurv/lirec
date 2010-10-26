package suites;

import junit.framework.JUnit4TestAdapter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import FAtiMA.culture.SymbolTranslatorTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SymbolTranslatorTest.class
        })
        
public class AllFAtiMATests {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(AllFAtiMATests.class);
    }
}

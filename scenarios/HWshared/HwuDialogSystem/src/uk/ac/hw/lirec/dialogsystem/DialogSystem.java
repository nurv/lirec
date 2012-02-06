package uk.ac.hw.lirec.dialogsystem;

import java.io.Reader;
import java.util.HashMap;

import bsh.EvalError;
import bsh.Interpreter;

public class DialogSystem {
	private Interpreter mScriptInterpreter = new Interpreter();
	private DialogInterface mDI;
	private boolean mInit = false;
	
	public DialogSystem(DialogInterface di) {
		mDI = di;
	}
	
	/**
	 * WARNING this is synchronized on the evaluation, so will interrupt then 
	 * wait for the current script to finish. So this could block - and is probably 
	 * called from the UI, so dangerous!
	 */
	public void interruptDialogEvent() {
		mDI.interruptDialog(); 
		//then wait for the evaluation to finish - we can use the lock
		//TODO WARNING not ideal, cos this might block the UI thread.
		synchronized(this) {
			mDI.resetDi();
		}
	}
	
	/**
	 * @param script a reader pointing to the file to load
	 * @return true is the DS is initialised with no errors, otherwise false and a stacktrace on stderr.
	 */
	public boolean initSystem(Reader script) {
		try {
			mScriptInterpreter.eval(script);
			mScriptInterpreter.set("di", mDI);
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		mInit = true;
		return true;
	}
	
	//this is kinda the maximally general approach, might not be the best. 
	//synchronized to make sure we're only in here once!
	//TODO Check, does this mean it might block badly? Fine, cos you should be using a 
	//thread to call this anyhow, right?
	public void evaluateEvent(String event) {
		//TODO more sensible error handling/reporting
		if (!mInit) 
			return;
		try {
			synchronized (this) {
				System.out.println("EVAL STARTING");
				mDI.resetDi(); //TODO only need to reset after interrupt
				mScriptInterpreter.eval(event);
				System.out.println("EVAL DONE");
			}
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param dataIn data to migrate in to the agent.
	 */
	public void migrateDataIn(HashMap<String,String> dataIn) {
		try {
			mScriptInterpreter.set("migrationData", dataIn);
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

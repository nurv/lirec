package FAtiMA.Core.util.parsers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xml.sax.Attributes;

public abstract class ReflectXMLHandler2 {
    // used for reflect
    Class<?>[] argTypes = {Attributes.class};
    Class<?>[] charArgTypes = {String.class};
    Class<? extends ReflectXMLHandler2> cl;

    public ReflectXMLHandler2() {
        cl = this.getClass();
    }

    public void callCharMethod(String methodName, String str) {
      Method meth = null;
      try {
        // Fetches the method
        meth = cl.getMethod(methodName, charArgTypes);
        Object args[] = {str};
        // invokes the method
        meth.invoke(this,args);
      }
      catch (java.lang.NoSuchMethodException e) {
        //System.err.println("Unable to handle message! No such method " + methodName + "(" + argTypes + ")");
      }
      catch(InvocationTargetException e) {
        e.printStackTrace();
      }
      catch(IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    public void callEndMethod(String methodName) {
      Method meth = null;
      try {
        // Fetches the method
        meth = cl.getMethod(methodName,(Class<?>[])null);
        meth.invoke(this,(Object[])null);
      }
      catch (java.lang.NoSuchMethodException e) {
        //System.err.println("Unable to handle message! No such method " + methodName + "(" + argTypes + ")");
      }
      catch(InvocationTargetException e) {
        e.printStackTrace();
      }
      catch(IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    public void callTagMethod(String methodName, Attributes attributes) {
      Method meth = null;
      try {
        // Fetches the method
        meth = cl.getMethod(methodName,argTypes);
        Object args[] = {attributes};
        meth.invoke(this,args);
      }
      catch (java.lang.NoSuchMethodException e) {
        //System.err.println("Unable to handle message! No such method " + methodName + "(" + argTypes + ")");
      }
      catch(InvocationTargetException e) {
        e.printStackTrace();
      }
      catch(IllegalAccessException e) {
        e.printStackTrace();
      }
    }
}
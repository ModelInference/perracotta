/*
 * MethodFilterPolicyInterface.java
 * Created on 2004-9-19
 */
package edu.virginia.cs.terracotta.CFG;

/**
 * @author Jinlin Yang
 */
public interface MethodFilterPolicyInterface {
    public boolean checkClass(String className);

    public boolean checkMethod(String methodName);
}
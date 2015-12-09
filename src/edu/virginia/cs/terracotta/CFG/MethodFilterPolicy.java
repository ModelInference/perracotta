/*
 * MethodFilterPolicy.java
 * Created on 2004-9-19
 */
package edu.virginia.cs.terracotta.CFG;

import java.util.Vector;
import java.util.regex.Pattern;

/**
 * @author Jinlin Yang
 */
public class MethodFilterPolicy implements MethodFilterPolicyInterface {

    protected Vector classIncludePatterns;

    protected Vector classExcludePatterns;

    protected Vector methodIncludePatterns;

    protected Vector methodExcludePatterns;

    /**
     *  
     */
    public MethodFilterPolicy(Vector classNamesInclude,
            Vector classNamesExclude, Vector methodNamesInclude,
            Vector methodNamesExclude) {
        classIncludePatterns = new Vector();
        methodIncludePatterns = new Vector();
        classExcludePatterns = new Vector();
        methodExcludePatterns = new Vector();
        addPattern(classNamesInclude, classIncludePatterns);
        addPattern(classNamesExclude, classExcludePatterns);
        addPattern(methodNamesInclude, methodIncludePatterns);
        addPattern(methodNamesExclude, methodExcludePatterns);
    }

    private void addPattern(Vector strs, Vector patterns) {
        if (patterns == null)
            patterns = new Vector();
        if (strs == null)
            return;
        for (int i = 0; i < strs.size(); i++) {
            patterns.add(Pattern.compile((String) strs.get(i)));
        }
    }

    /**
     * @see edu.virginia.cs.terracotta.terracotta.CFG.MethodFilterPolicyInterface#checkClass(java.lang.String)
     */
    public boolean checkClass(String className) {
        return (((classIncludePatterns.size() == 0) || check(className,
                classIncludePatterns)) && ((classExcludePatterns.size() == 0) || !check(
                className, classExcludePatterns)));
    }

    /**
     * @see edu.virginia.cs.terracotta.terracotta.CFG.MethodFilterPolicyInterface#checkMethod(java.lang.String)
     */
    public boolean checkMethod(String methodName) {
        return (((methodIncludePatterns.size() == 0) || check(methodName,
                methodIncludePatterns)) && ((methodExcludePatterns.size() == 0) || !check(
                methodName, methodExcludePatterns)));
    }

    private boolean check(String name, Vector patterns) {
        int i;
        for (i = 0; i < patterns.size(); i++) {
            if (((Pattern) patterns.get(i)).matcher(name).find())
                break;
        }
        if (i == (patterns.size()))
            return false;
        return true;
    }
}
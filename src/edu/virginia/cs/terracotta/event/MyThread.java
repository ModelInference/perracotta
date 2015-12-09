/*
 * MyThread.java
 * Created on 2004-9-27
 */
package edu.virginia.cs.terracotta.event;

/**
 * @author Jinlin Yang
 */
public class MyThread {
    private String name = null;

    private int priority = -1;

    private String group = null;
    
    /**
     * @param str
     *            a <code>String</code> in the same format as returned by the
     *            <code>toString()</code> method of <code>Thread</code>
     *            class, i.e. "Thread[name,priority,group]".
     */
    public MyThread(String str) {
        String core = str.substring(7, str.length() - 1);
        String[] parts = core.split(",");
        if (parts.length != 3) {
            System.out.println("Illegal argument str: " + str);
            System.exit(1);
        }
        name = parts[0];
        priority = Integer.valueOf(parts[1]).intValue();
        group = parts[2];
    }

    public String toString() {
        return "Thread[" + name + "," + priority + "," + group + "]";
    }

    public int hashCode() {
        return name.hashCode();
    }

    /**
     * It seems that there is no unique way to identify a thread through
     * interface of the Thread class, I just use thread name. But be warned that
     * two thread can have identical name.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof MyThread))
            return false;
        if (!this.name.equals(((MyThread) o).name))
            return false;
        return true;
    }
    
    
    /**
     * @return Returns the group.
     */
    public String getGroup() {
        return group;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @return Returns the priority.
     */
    public int getPriority() {
        return priority;
    }
}
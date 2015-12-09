/*
 * Event.java
 * Created on 2004-9-19
 */
package edu.virginia.cs.terracotta.event;

//import java.util.Hashtable;
//import java.util.LinkedList;

import edu.virginia.cs.terracotta.CFG.MyMethod;

/**
 * @author Jinlin Yang
 */
public class Event implements Comparable {
    private MyMethod method = null;

    private MyThread thread = null;

    private int freq = 0;

    private int traces = 0;

    public Event(String eventStr) {
        this.freq = 0;
        this.traces = 0;
        // Remove quotation marks at the very beginning and end
        if (eventStr.charAt(0) == '"')
            eventStr = eventStr.substring(1, eventStr.length());
        if (eventStr.charAt(eventStr.length() - 1) == '"')
            eventStr = eventStr.substring(0, eventStr.length() - 1);

        String[] parts = eventStr.split(":Thread");
        // parts[0] the old name
        method = new MyMethod(parts[0]);
        // parts[1] Thread
        if (parts.length == 2)
            thread = new MyThread("Thread" + parts[1]);
    }

    public Event(MyMethod method) {
        this.method = method;
        this.freq = method.getFreq();
        this.traces = method.getTraces();
    }

    /**
     * @return Returns the freq.
     */
    public int getFreq() {
        return freq;
    }

    /**
     * @return Returns the traces.
     */
    public int getTraces() {
        return traces;
    }

    public void increaseFreq() {
        freq++;
    }

    public void increaseTraces() {
        traces++;
    }

    /**
     * @return Returns the method.
     */
    public MyMethod getMethod() {
        return method;
    }

    /**
     * @return Returns the thread.
     */
    public MyThread getThread() {
        return thread;
    }

    public String toString() {
        if (thread == null)
            return "\"" + method + "\"";
        else
            return "\"" + method + ":" + thread + "\"";
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Event))
            return false;
        if (!(this.method.equals(((Event) obj).method)))
            return false;
        if ((this.thread != null) && (((Event) obj).thread != null)) {
            if (!this.thread.equals(((Event) obj).thread))
                return false;
        } else if ((this.thread == null) && (((Event) obj).thread != null))
            return false;
        else if ((this.thread != null) && (((Event) obj).thread == null))
            return false;

        return true;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (thread == null) ? method.hashCode() : method.hashCode()
                + thread.hashCode();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (!(o instanceof Event))
            throw new ClassCastException();
        if (this.freq > ((Event) o).freq)
            return 1;
        else if (this.freq < ((Event) o).freq)
            return -1;

        if (this.traces > ((Event) o).traces)
            return 1;
        else if (this.traces < ((Event) o).traces)
            return -1;

        return this.toString().compareTo(o.toString());
    }

//    public static void main(String[] args) {
//        Event a = new Event("\"a.aa()\"");
//        Event aa = new Event("\"a.aa()\"");
//        if (a.equals(aa))
//            System.out.println("equal");
//        Event b = new Event("\"b.bb()\"");
//
//        Hashtable table = new Hashtable();
//        LinkedList list = new LinkedList();
//        table.put(a, new Integer(0));
//        list.add(a);
//        if (table.containsKey(aa))
//            System.out.println("table contains a");
//        if (list.contains(aa))
//            System.out.println("list contains a");
//    }
}
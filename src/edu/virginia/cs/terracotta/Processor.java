/*
 * Processor.java
 * Created on 2004-10-7
 */
package edu.virginia.cs.terracotta;

/**
 * @author Jinlin Yang
 */
public interface Processor {

    public void process();
    
    public void processOpts(String[] args);
    
    public void usage();
    
}

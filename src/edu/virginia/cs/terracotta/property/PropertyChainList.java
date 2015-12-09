/*
 * PropertyChainList.java
 * Created on 2004-9-20
 */
package edu.virginia.cs.terracotta.property;

import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import edu.virginia.cs.terracotta.Synthesizer;

/**
 * @author Jinlin Yang
 */
public class PropertyChainList {
    private Vector list = null;

    public PropertyChainList() {
        list = new Vector();
    }

    public void add(PropertyChain pc) {
        list.add(pc);
    }

    public PropertyChain get(int i) {
        return (PropertyChain) list.get(i);
    }
    
    public void sort(){
        Collections.sort(list);
    }
    
    public int size(){
        return list.size();
    }
    
    public void print() throws IOException{
        for(int i=0; i<list.size(); i++){
            Synthesizer.LOG.writeBytes("Chain #" + i);
            Synthesizer.LOG.writeBytes("\n");
            ((PropertyChain)list.get(i)).print();
            Synthesizer.LOG.writeBytes("\n");
        }
    }
    
    public void print(String type) throws IOException{
        for(int i=0; i<list.size(); i++){
            Synthesizer.LOG.writeBytes("Chain #" + i);
            Synthesizer.LOG.writeBytes("\n");
            ((PropertyChain)list.get(i)).print(type);
        }
    }
}
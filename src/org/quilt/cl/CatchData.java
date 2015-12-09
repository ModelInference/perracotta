/* CatchData.java */
package org.quilt.cl;

import org.apache.bcel.generic.*;
import org.quilt.graph.Vertex;

/** 
 * Data structure describing an exception handler. 
 *
 * XXX There have been problems associated with generation of
 * exception handlers.  <code>tryEnd</code> should be used to get
 * a handle of the <b>last</b> instruction in the vertex.
 *
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class CatchData {

    /** First code vertex in try block. */
    public Vertex tryStart;
    /** Last code Vertex in try block. */
    public Vertex tryEnd;
    /** First code vertex in handler. */
    public Vertex handlerPC;
    /** Type of exception handled. */
    public ObjectType exception;
   
    /** The information needed to set up an exception handler. */

    public CatchData (Vertex start, Vertex end, Vertex handler, 
                                                    ObjectType exc) {
        tryStart  = start;
        tryEnd    = end;
        handlerPC = handler;
        exception = exc;
    }
}

    




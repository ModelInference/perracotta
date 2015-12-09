/* GraphBuildException.java */
package org.quilt.cl;

/**
 * An exception that occurs while building a control flow graph for
 * a method.
 *
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class GraphBuildException extends RuntimeException {

    public GraphBuildException() {
        super();
    }

    public GraphBuildException(String msg) {
        super(msg);
    }

    public GraphBuildException(String msg, Throwable cause) {
        super (msg, cause);
    }

    public GraphBuildException(Throwable cause) {
        super(cause);
    }
}

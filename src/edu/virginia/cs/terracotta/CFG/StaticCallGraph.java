/*
 * Created on 2004-8-15
 */
package edu.virginia.cs.terracotta.CFG;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.io.File;
import java.io.IOException;

import org.apache.bcel.classfile.ClassParser;
//import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.quilt.cl.ControlFlowGraph;

/**
 * @author Jinlin Yang
 *  
 */
public class StaticCallGraph {

    private Vector callGraph = new Vector();

    private HashMap method2ShortestDistance = new HashMap();

    private HashMap method2Pred = new HashMap();

    private MethodFilterPolicy callerPolicy = null;

    private MethodFilterPolicy calleePolicy = null;

    public static final int INF = Integer.MAX_VALUE;

    /**
     * The constructor.
     * 
     * @param filename:
     *            the path of a class file or a directory.
     */
    public StaticCallGraph(String filename, MethodFilterPolicy callerPolicy,
            MethodFilterPolicy calleePolicy) {
        this.callerPolicy = callerPolicy;
        this.calleePolicy = calleePolicy;
        process(filename);
    }

    /**
     * @deprecated This method, along with
     *             <code>{@link #getCallGraphSortedByPred()}</code> is for
     *             temporary testing purpose.
     * @return the call graph.
     */
    public Vector getCallGraph() {
        return callGraph;
    }

    /**
     * @deprecated This method, along with <code>{@link #getCallGraph()}</code>
     *             is for temporary testing purpose.
     * @return nodes of the call graph, sorted by their number of predecessors.
     */
    public Vector getCallGraphSortedByPred() {
        Vector sorted = new Vector();
        for (int i = 0; i < callGraph.size(); i++)
            sorted.add(callGraph.get(i));

        for (int i = 0; i < sorted.size(); i++) {
            int max = i;
            for (int j = i + 1; j < sorted.size(); j++)
                if (((MyMethod) sorted.get(max)).getPred().size() < ((MyMethod) sorted
                        .get(j)).getPred().size())
                    max = j;
            if (i != max) {
                MyMethod m_i = (MyMethod) sorted.get(i);
                MyMethod m_max = (MyMethod) sorted.get(max);
                sorted.set(max, m_i);
                sorted.set(i, m_max);
            }
        }
        return sorted;
    }

    /**
     * This method is called by the constructor
     * <code>{@link #StaticCallGraph(String)}</code>. It checks the type of
     * the filename and dispatch the control to the corresponding handle method.
     * If the filename is a file, then <code>{@link #handleFile(File)}</code>
     * will be called. If the filename is a directory, then
     * <code>{@link #handleDir(File)}</code> will be called.
     * 
     * @param filename:
     *            a path to a directory or a file
     */
    private void process(String filename) {
        File file = new File(filename);
        if (file.isDirectory())
            handleDir(file);
        else if (file.isFile())
            handleFile(file);
        else {
            System.err.println("Fatal error: " + filename
                    + " is neither a file nor a directory");
            System.exit(1);
        }
    }

    /**
     * Returns a <code>String</code> representation of the static call graph.
     * For each node (method) in the SCG, first print out the method's
     * signature, second print out this node's all succeeding nodes and all
     * preceeding nodes.
     * 
     * @author Jinlin Yang
     * @see MyMethod#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < callGraph.size(); i++) {
            MyMethod method = (MyMethod) callGraph.get(i);
            buf.append("############" + method + "\n");
            buf.append("\tcalling:\n");
            Vector post = method.getPost();
            for (int j = 0; j < post.size(); j++)
                buf.append("\t\t" + ((MyMethod) post.get(j)) + "\n");

            buf.append("\tcalled by:\n");
            Vector pred = method.getPred();
            for (int j = 0; j < pred.size(); j++)
                buf.append("\t\t" + ((MyMethod) pred.get(j)) + "\n");

            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * This method is called by <code>{@link #process(String)}</code> method.
     * It recursively finds class files in the directory tree and calls
     * <code>{@link #handleFile(File)}</code> for each of them.
     * 
     * @param file
     * @author Jinlin Yang
     * @see process(String)
     * @see handleFile(File)
     */
    private void handleDir(File file) {
        System.out.println("++++Entering directory: " + file.getAbsolutePath());
        File files[] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                handleDir(files[i]);
            else if (files[i].isFile()) {
                if (!files[i].getName().matches(".*\\.class")) {
                    System.out.println(files[i].getName()
                            + " is not a java class file");
                    continue;
                }
                handleFile(files[i]);
            }
        }
    }

    /**
     * This method is either called by method
     * <code>{@link process(String)}</code> or
     * <code>{@link handleDir(File)}</code>. It first parses the java class
     * file using the <a href="http://jakarta.apache.org/bcel/">BCEL </a>
     * library. Then for each method in that class file, it will call method
     * <code>{@link handleMethod(ClassGen, MethodGen)}</code>
     * 
     * @param file
     *            a java class file
     * @see handleMethod(ClassGen, MethodGen)
     */
    private void handleFile(File file) {
        // sanity check
        if (!file.getName().matches(".*\\.class")) {
            System.err.println(file.getName() + " is not a java class file");
            return;
        }
        System.out.println("####Begin processing file: "
                + file.getAbsolutePath());

        JavaClass javaClass = null;
        ClassParser classParser = null;

        try {
            classParser = new ClassParser(file.getAbsolutePath());
            javaClass = classParser.parse();
        } catch (IOException e) {
            System.err.println("Error during parsing " + file.getName());
            e.printStackTrace();
            return;
        }
        Method[] meths = javaClass.getMethods();
        String className = javaClass.getClassName();
        ConstantPoolGen cpGen = new ConstantPoolGen(javaClass.getConstantPool());
        ClassGen classGen = new ClassGen(javaClass);

        for (int i = 0; i < meths.length; i++) {
            MethodGen methodGen = new MethodGen(meths[i], className, cpGen);
            // Enforcing special policies
            if (!satisfySpecialPolicies(callerPolicy, methodGen))
                continue;
            handleMethod(classGen, methodGen);
        }
    }

    /**
     * This is one of the most important methods in this class. It first
     * constructs an intra-procedural control flow graph for the method of
     * concern. Then it finds all cut points in the CFG by calling
     * <code>{@link #findCutPoints(MyCFG)}</code>. Next, all method
     * invocation cut points are selected by calling
     * <code>{@link #findMethodInvocationCutPoints(Vector, MyCFG)}</code>.
     * After this, the inter-procedural call graph and inter-procedural control
     * flow graph are updated by calling
     * <code>{@link #updateCallGraph(Vector, MyCFG)}</code> and
     * <code>{@link #updateControlFlowGraph(Vector, MyCFG)}</code>
     * respectively.
     * 
     * @param classGen
     *            the ClassGen object that contains parsed class
     * @param methodGen
     *            the MethodGen object that contains the parsed method
     * @author Jinlin Yang
     */
    private void handleMethod(ClassGen classGen, MethodGen methodGen) {
        // FIXME It probably would be better move the following checkings to a
        // separate method and call that method before this method is called.
        // The reasons are: 1) these implements special policies which should
        // not be in this method in the first place. 2) these will also be
        // better abstracted if they are contained in a separate method, which
        // will facilitate maintanence later.

        // Ignore Abstract methods
        if (methodGen.isAbstract())
            return;
//        // Ignore non-public methods
//        if (!methodGen.isPublic())
//            return;

        System.out.println("----Begin processing method: " + methodGen);
        CFG t = new CFG();
        ControlFlowGraph cfg = null;
        MyCFG mycfg = null;
        try {
            cfg = t.makeGraph(classGen, methodGen);
        } catch (Exception e) {
            System.err.println("Error during processing this method");
            e.printStackTrace();
            return;
        }
        mycfg = new MyCFG(cfg, classGen, methodGen);

        Vector methodInvocationCutPoints = findMethodInvocationCutPoints(mycfg);

        updateCallGraph(methodInvocationCutPoints, mycfg);
        updateControlFlowGraph(methodInvocationCutPoints, mycfg);
        // Debugging

        //		if (methodInvocationCutPoints.size() > 0)
        //			System.out.println("Total " + methodInvocationCutPoints.size()
        //					+ " method invocation cut points in this method:");
        //		for (int k = 0; k < methodInvocationCutPoints.size(); k++) {
        //			MyVertex v = (MyVertex) methodInvocationCutPoints.get(k);
        //			ConstantPool cp = mycfg.getClassGen().getConstantPool()
        //					.getConstantPool();
        //			System.out.println(v.toString(cp));
        //		}
        //if (methodInvocationCutPoints.size() > 0)
        //	mycfg.toPS();
        //	System.out.println();

        // End Debugging
    }

    /**
     * This method finds all cut points in a CFG of a method. The algorithm
     * works as follows: for each node <code>V</code> in the CFG 1) that node
     * was removed from the CFG to produce CFG'; 2) if CFG' becomes unconnected,
     * then <code>V</code> is a cut point of the CFG. <br>
     * <br>
     * Whether a CFG is connected is decided using an adapted DFS algorithm,
     * whose complexity is <code>O(n^2)</code>, where <code>n</code> is the
     * number of nodes in a CFG. <br>
     * <br>
     * As a result the cut-point algorithm's complexity is <code>O(n^3)</code>.
     * It is not very efficient when <code>n</code> is big. But because an
     * intra-procedural CFG usually only has a small number of nodes (less than
     * 1000, most time only around 100), we have not had performance problem in
     * our experiment. <br>
     * <br>
     * 
     * @param mycfg
     *            an intra-procedural CFG of a method (should be connected)
     * @return a <code>Vector</code> of cut points
     * @author Jinlin Yang
     * @see MyCFG#isConnected()
     * @see #findMethodInvocationCutPoints(Vector, MyCFG)
     */
    private Vector findCutPoints(MyCFG mycfg) {
        Vector cutPoints = new Vector();
        /*
         * FIXME this is a temporary work-around of the problem I Suspect that
         * there is fault in the makeGraph method
         */
        MyVertex Exit = mycfg.getExit();
        if (Exit == null) {
            System.err.println(mycfg.getMethodGen().getMethod()
                    + " does not have an exit point. So by-pass");
            return cutPoints;
        }

        Vector vertices = mycfg.getVertices();
        for (int k = 0; k < vertices.size(); k++) {
            MyVertex vINmycfg = (MyVertex) vertices.get(k);

            MyCFG mycfg2 = new MyCFG(mycfg);
            MyVertex v = (MyVertex) mycfg2.getVertices().get(k);
            mycfg2.removeVertex(v);
            if (!mycfg2.isConnected())
                cutPoints.add(vINmycfg);
        }

        return cutPoints;
    }

    /**
     * This method first calls method <code>{@link #findCutPoints(MyCFG)}</code>
     * to find all cut points in a CFG. It then removes all non-method
     * invocation cut points.
     * 
     * @param mycfg
     *            an intra-procedural CFG of a method
     * @return a <code>Vector</code> of cut points which are also method
     *         invocation points.
     * @author Jinlin Yang
     * @see #findCutPoints(MyCFG)
     */
    private Vector findMethodInvocationCutPoints(MyCFG mycfg) {
        Vector allCutPoints = findCutPoints(mycfg);
        Vector methodInvocationCutPoints = new Vector();
        for (int k = 0; k < allCutPoints.size(); k++) {
            MyVertex v = (MyVertex) allCutPoints.get(k);

            // Bypass all non method invocation nodes
            if (!v.isInvoke())
                continue;

            if (v.isConstructor(mycfg.getClassGen().getConstantPool()))
                continue;

            /*
             * The following are specific filtering policies.
             */
            if (!satisfySpecialPolicies(calleePolicy, (InvokeInstruction) v
                    .getInst(), mycfg))
                continue;

            methodInvocationCutPoints.add(v);
        }
        return methodInvocationCutPoints;
    }

    /**
     * Checks whether a specific vertex <code>V</code> in the cfg satisfies a
     * set of special rules.
     * 
     * @param policy
     * @param v
     * @param mycfg
     * @return true if satisfied. false if not satisfied.
     */
    private boolean satisfySpecialPolicies(MethodFilterPolicy policy,
            InvokeInstruction invokeInst, MyCFG mycfg) {
        String className = invokeInst.getClassName(mycfg.getClassGen()
                .getConstantPool());
        String methodName = invokeInst.getMethodName(mycfg.getClassGen()
                .getConstantPool());
        if (!policy.checkClass(className))
            return false;
        if (!policy.checkMethod(methodName))
            return false;
        return true;
    }

    /**
     * Checks whether a method satisfies a set of special rules.
     * 
     * @param policy
     * @param m
     * @return
     */
    private boolean satisfySpecialPolicies(MethodFilterPolicy policy,
            MethodGen m) {
        String className = m.getClassName();
        String methodName = m.getName();
        if (!policy.checkClass(className))
            return false;
        if (!policy.checkMethod(methodName))
            return false;
        return true;
    }

    /**
     * This method updates the <code>{@link #callGraph}</code> field. For each
     * node <code>V</code> in the set of cut points, <code>V</code> is
     * inserted into the successor list of <code>M</code> and <code>M</code>
     * is inserted into the predecessor list of <code>V</code>, where
     * <code>M</code> is the node of the current method whose CFG is being
     * examined. <br>
     * <br>
     * For example: for a method <code>A</code> as following, <code>B</code>
     * and <code>C</code> will be added to the successor list of
     * <code>A</code>. And <code>A</code> will be added to the predecessor
     * list of <code>B</code> and <code>C</code>. Notice that since this
     * method focuses on the call relationship, it neither tries to add
     * <code>C</code> to <code>B</code>'s successor list, nor does it try
     * to add <code>B</code> to <code>C</code>'s predecessor list. This is
     * done by another method
     * <code>{@link #updateControlFlowGraph(Vector, MyCFG)}</code><br>
     * <br>
     * <code>void A(){</code><br>
     * <code>B();</code><br>
     * <code>C();</code><br>
     * <code>}</code><br>
     * 
     * @param cutPoints
     *            a set of method invocation cut points in a CFG
     * @param mycfg
     *            an intra-procedural CFG of a method
     * @see #updateControlFlowGraph(Vector, MyCFG)
     */
    private void updateCallGraph(Vector cutPoints, MyCFG mycfg) {
        MethodGen methodGen = mycfg.getMethodGen();
        ClassGen classGen = mycfg.getClassGen();
        ConstantPoolGen cpGen = classGen.getConstantPool();

        // One important trickness of this method is that only one MyMethod
        // object is created for each method, which is stored in the methods
        // vector and will be referenced to thereafter.
        // This is ensured by the 'equals' method of MyMethod class which only
        // compares two object's className, methodName, and signature.
        MyMethod sourceM = new MyMethod(methodGen);
        if (!callGraph.contains(sourceM))
            callGraph.add(sourceM);
        else
            // This method's MyMethod object has been created before, so we need
            // to set sourceM to that already created object
            sourceM = getMeth(sourceM);

        for (int i = 0; i < cutPoints.size(); i++) {
            MyVertex v = (MyVertex) cutPoints.get(i);
            if (!v.isInvoke())
                continue;

            // This follows a similar spirit as above. Namely a method is only
            // created once.
            MyMethod targetM = new MyMethod((InvokeInstruction) v.getInst(),
                    cpGen);
            if (!callGraph.contains(targetM))
                callGraph.add(targetM);
            else
                targetM = getMeth(targetM);

            // Insert links to source method
            sourceM.insert2post(targetM);
        }
    }

    /**
     * Similar to method <code>{@link #updateCallGraph(Vector, MyCFG)}</code>,
     * this method also updates the internal graph representation of the
     * methods' relationship (namely, <code>{@link #callGraph}</code>). The
     * difference is illustrated using the same example as following. <br>
     * <br>
     * 
     * For a method <code>A</code> as following, it adds <code>C</code> to
     * <code>B</code>'s successor list and <code>B</code> to <code>C</code>
     * 's predecessor list. However, it does not add <code>A</code> to the
     * predecessor list of <code>B</code> and <code>C</code>, nor does it
     * add <code>B</code> and <code>C</code> to the successor list of
     * <code>A</code>. These are done by another method
     * <code>{@link #updateCallGraph(Vector, MyCFG)}</code><br>
     * <br>
     * <code>void A(){</code><br>
     * <code>B();</code><br>
     * <code>C();</code><br>
     * <code>}</code><br>
     * 
     * @param cutPoints
     * @param mycfg
     * @author Jinlin Yang
     */
    private void updateControlFlowGraph(Vector cutPoints, MyCFG mycfg) {
        ClassGen classGen = mycfg.getClassGen();
        ConstantPoolGen cpGen = classGen.getConstantPool();

        int i = 0;
        while (i < cutPoints.size() - 1) {
            MyVertex source = (MyVertex) cutPoints.get(i);
            if (!source.isInvoke()) {
                i++;
                continue;
            }
            MyMethod sourceM = new MyMethod((InvokeInstruction) source
                    .getInst(), cpGen);
            if (!callGraph.contains(sourceM))
                callGraph.add(sourceM);
            else
                sourceM = getMeth(sourceM);

            MyVertex target = null;
            int j = i + 1;
            while (j < cutPoints.size()) {
                target = (MyVertex) cutPoints.get(j);
                if (target.isInvoke())
                    break;
                else
                    j++;
            }

            // We did not find next method invocation vertex. So exit the loop.
            if (j == cutPoints.size())
                break;

            MyMethod targetM = new MyMethod((InvokeInstruction) target
                    .getInst(), cpGen);
            if (!callGraph.contains(targetM))
                callGraph.add(targetM);
            else
                targetM = getMeth(targetM);

            // Insert links to source method
            sourceM.insert2post(targetM);

            i = j;
        }
    }

    /**
     * Compute the shortest distance between two nodes (methods) in the partial
     * inter-procedural graph.
     * 
     * @param from
     * @param to
     * @return the distance between <code>from</code> and <code>to</code>
     * @author Jinlin Yang
     */
    public int getShortestDistance(MyMethod from, MyMethod to) {
        int retval = -1;

        // First check whether these methods exist
        from = getMeth(from);
        to = getMeth(to);
        if ((from == null) || (to == null))
            return StaticCallGraph.INF;

        if (!(method2ShortestDistance.containsKey(from) && method2Pred
                .containsKey(from))) {
            // Create the shortest-paths-tree rooted at 'from' using the
            // Dijkstra's algorithm [Cormen pp595]
            Dijkstra(from);
        }

        HashMap SD = (HashMap) method2ShortestDistance.get(from);
        retval = ((Integer) SD.get(to)).intValue();
        return retval;
    }

    /**
     * This method implements the classical algorithm of Dijkstra for finding
     * the shortest path from a single source to each node in a directed graph.
     * [Cormen pp595]
     * 
     * @param s
     */
    private void Dijkstra(MyMethod src) {
        initializeSingleSource(src);
        HashSet S = new HashSet();
        HashSet Q = new HashSet(callGraph);
        while (!Q.isEmpty()) {
            MyMethod u = removeMinimum(Q, src);
            S.add(u);
            Vector post_u = u.getPost();
            for (int i = 0; i < post_u.size(); i++) {
                MyMethod v = (MyMethod) post_u.get(i);
                relax(u, v, src);
            }
        }
    }

    private void initializeSingleSource(MyMethod src) {
        HashMap SD = null;
        HashMap Pred = null;
        if (method2ShortestDistance.containsKey(src))
            SD = (HashMap) method2ShortestDistance.get(src);
        else {
            SD = new HashMap();
            method2ShortestDistance.put(src, SD);
        }

        if (method2Pred.containsKey(src))
            Pred = (HashMap) method2Pred.get(src);
        else {
            Pred = new HashMap();
            method2Pred.put(src, Pred);
        }

        for (int i = 0; i < callGraph.size(); i++) {
            SD.put(((MyMethod) callGraph.get(i)), new Integer(
                    StaticCallGraph.INF));
            Pred.put(((MyMethod) callGraph.get(i)), null);
        }

        SD.put(src, new Integer(0));
    }

    private MyMethod removeMinimum(HashSet Q, MyMethod src) {
        MyMethod min = null;
        HashMap SD = (HashMap) method2ShortestDistance.get(src);

        Iterator itr = Q.iterator();
        if (itr.hasNext())
            min = (MyMethod) itr.next();
        else
            return null;
        while (itr.hasNext()) {
            MyMethod i = (MyMethod) itr.next();
            int d_i = ((Integer) SD.get(i)).intValue();
            int d_min = ((Integer) SD.get(min)).intValue();
            if (d_min > d_i) {
                min = i;
            }
        }

        Q.remove(min);
        return min;
    }

    private void relax(MyMethod u, MyMethod v, MyMethod src) {
        HashMap SD = (HashMap) method2ShortestDistance.get(src);
        HashMap Pred = (HashMap) method2Pred.get(src);

        int d_v = ((Integer) SD.get(v)).intValue();
        int d_u = ((Integer) SD.get(u)).intValue();
        int u2v = 1;
        if (!u.getPost().contains(v))
            u2v = StaticCallGraph.INF;
        // If d_u is infinite, or u to v is infinite. We don't need to update
        // d_v
        if ((d_u == StaticCallGraph.INF) || (u2v == StaticCallGraph.INF))
            return;

        // d_u and u2v both are not infinite
        // If d_v is infinite, then we will update d_v
        if (d_v == StaticCallGraph.INF) {
            SD.put(v, new Integer(d_u + u2v));
            Pred.put(v, u);
        }
        // If d_v is not infinite, we will compare it with d_u + u2v to see if
        // we need update d_v
        else if (d_v > d_u + u2v) {
            SD.put(v, new Integer(d_u + u2v));
            Pred.put(v, u);
        }
    }

    private MyMethod getMeth(MyMethod a) {
        MyMethod retval = null;
        if (callGraph.contains(a)) {
            int index = callGraph.indexOf(a);
            retval = (MyMethod) callGraph.get(index);
        }

        return retval;
    }
}
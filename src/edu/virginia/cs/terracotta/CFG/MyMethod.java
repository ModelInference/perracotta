/*
 * Created on 2004-8-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.CFG;

import java.util.Vector;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

/**
 * @author jy6q
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MyMethod {
	private String className = "";

	private String methodName = "";

	private String signature = "";

	private Vector post = new Vector();

	private Vector pred = new Vector();

	private int freq = 0;

	private int traces = 0;

	public MyMethod() {
	}

	public MyMethod(InvokeInstruction inst, ConstantPoolGen cpGen) {
		className = inst.getClassName(cpGen);
		methodName = inst.getName(cpGen);
		signature = inst.getSignature(cpGen);
	}

	public MyMethod(MethodGen methodGen) {
		className = methodGen.getClassName();
		methodName = methodGen.getName();
		signature = methodGen.getSignature();
	}

	/**
	 * Assume the methodEvent has the following format
	 * "ClassName.MethodName(argumentstype)returntype" Parse methodEvent so that
	 * className = ClassName methodName = MethodName signature =
	 * (argumentstype)returntype
	 * 
	 * @param methodEvent
	 */

	public MyMethod(String methodEvent) {
		// Remove quotation marks if there are any at the beginning and end.
		if (methodEvent.charAt(0) == '"')
			methodEvent = methodEvent.substring(1, methodEvent.length());
		if (methodEvent.charAt(methodEvent.length() - 1) == '"')
			methodEvent = methodEvent.substring(0, methodEvent.length() - 1);

		int first_paren = methodEvent.indexOf('(');
		String[] parts = { methodEvent.substring(0, first_paren),
				methodEvent.substring(first_paren) };

		signature = parts[1];
		int dot = parts[0].lastIndexOf(".");
		if(dot < 0){
			System.err.println(methodEvent);
			System.exit(1);
		}
		className = parts[0].substring(0, dot);
		methodName = parts[0].substring(dot + 1, parts[0].length());
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

	public void increaseFreq(int i) {
		freq += i;
	}

	public void increaseTraces() {
		traces++;
	}

	public void increaseTraces(int i) {
		traces += i;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (className + methodName + signature).hashCode();
	}

	// This is tricky. We only consider className, methodName, and signature
	// when do the comparison. We ignore post, pred, post2count, and pred2count
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof MyMethod))
			return false;
		if (!(this.className.equals(((MyMethod) obj).className)))
			return false;
		if (!(this.methodName.equals(((MyMethod) obj).methodName)))
			return false;
		if (!(this.signature.equals(((MyMethod) obj).signature)))
			return false;
		return true;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	// TODO this is a makeshift
	//    public int hashCode() {
	//        return (className + methodName + signature).hashCode();
	//    }
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(className + ".");
		buf.append(methodName);
		buf.append(signature);
		return buf.toString();
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return Returns the methodName.
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName
	 *            The methodName to set.
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return Returns the signature.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * @param signature
	 *            The signature to set.
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * @return Returns the post.
	 */
	public Vector getPost() {
		return post;
	}

	/**
	 * @param post
	 *            The post to set.
	 */
	public void setPost(Vector post) {
		this.post = post;
	}

	/**
	 * @return Returns the pred.
	 */
	public Vector getPred() {
		return pred;
	}

	/**
	 * @param pred
	 *            The pred to set.
	 */
	public void setPred(Vector pred) {
		this.pred = pred;
	}

	/**
	 * Insert method m to this method's post/callee list and update the count of
	 * method m. It is very important to keep post and post2count consistent
	 * with each other. This method is not thread-safe.
	 * 
	 * @param m
	 */
	public void insert2post(MyMethod m) {
		/*
		 * Integer count = (Integer) post2count.remove(m); if (count == null) {
		 * post.add(m); post2count.put(m, new Integer(1)); } else
		 * post2count.put(m, new Integer(count.intValue() + 1));
		 * 
		 * m.insert2pred(this);
		 */
		if (!post.contains(m))
			post.add(m);
		m.insert2pred(this);
	}

	private void insert2pred(MyMethod m) {
		/*
		 * Integer count = (Integer) pred2count.remove(m); if (count == null) {
		 * pred.add(m); pred2count.put(m, new Integer(1)); } else
		 * pred2count.put(m, new Integer(count.intValue() + 1));
		 */
		if (!pred.contains(m))
			pred.add(m);
	}

//	public static void main(String args[]) {
//		MyMethod m1 = new MyMethod();
//		m1.setClassName("a");
//		m1.setMethodName("foo");
//		m1.setSignature("void");
//
//		MyMethod m2 = new MyMethod();
//		m2.setClassName("a");
//		m2.setMethodName("foo");
//		m2.setSignature("void");
//
//		m1.insert2post(m2);
//		m1.insert2post(m2);
//		//m2.getPost().add(m1);
//
//		MyMethod mm = new MyMethod(
//				"\"http.HttpContext.addHandler(ILhttp/HttpHandler)V\"");
//		/*
//		 * if (m1.getPost().contains(m2)) System.out.println("contains"); else
//		 * System.out.println("not contains");
//		 */
//	}
}
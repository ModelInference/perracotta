package edu.virginia.cs.terracotta.InferenceEnginePkg;
public interface EventHandler {
    public void enterMethod(int event);
    public void leaveMethod(int event);
    public void processTrace();
    public void processEnd();
}

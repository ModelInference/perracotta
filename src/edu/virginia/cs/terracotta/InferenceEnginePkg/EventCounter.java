package edu.virginia.cs.terracotta.InferenceEnginePkg;
public class EventCounter implements EventHandler {
    int event_count[];
    TraceReader tr;
    public EventCounter(TraceReader tr,int n_events) {
        this.tr = tr;
        event_count = new int[n_events];
    }
    public void enterMethod(int event) { 
        event_count[event]++;
    }
    public void leaveMethod(int event) { }
    public void processTrace() { }
    public void processEnd() { }
    public int getCount(int event) {
        return event_count[event];
    }
}

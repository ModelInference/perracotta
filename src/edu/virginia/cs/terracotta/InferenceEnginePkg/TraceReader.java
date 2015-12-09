package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.io.*;
import java.util.*;

import edu.virginia.cs.terracotta.InferenceEngine;
import edu.virginia.cs.terracotta.event.Event;

public class TraceReader {
	BufferedReader file;

	Vector events = new Vector();

	int numberOfTraces = 0;

	private int freqThreshold = 0;

	String delim = "----";

	public TraceReader(String filename, String delim) throws IOException {
		init(filename, delim);
	}

	public TraceReader(String filename) throws IOException {
		init(filename, "----");
	}

	public TraceReader(String filename, int freqThreshold) throws IOException {
		this.freqThreshold = freqThreshold;
		init(filename, "----");
	}

	private void init(String filename, String delim) throws IOException {
		this.delim = delim;
		this.file = new BufferedReader(new FileReader(filename));

		String line = null;
		HashMap event2lastTrace = new HashMap();
		Vector visited = new Vector();
		while ((line = file.readLine()) != null) {

			// filter out comments
			if (line.startsWith("//"))
				continue;

			// filter out empty lines
			if (line.matches("^$"))
				continue;

			if (line.startsWith(delim)) {
				numberOfTraces++;
			} else {
				int mode;
				String event_name;
				if (line.startsWith("Enter: ")) {
					event_name = line.substring(7);
					mode = 0;
				} else if (line.startsWith("Exit: ")) {
					event_name = line.substring(6);
					mode = 1;
				} else if (line.startsWith("Error: ")) {
					event_name = line.substring(7);
					mode = 2;
				} else {
					System.err.println("cannot parse: " + line);
					event_name = line;
					mode = 3;
				}
				Event event = new Event(event_name);
				if (!visited.contains(event)) {
					event.increaseFreq();
					event.increaseTraces();
					visited.add(event);
					event2lastTrace.put(event, new Integer(numberOfTraces));
				} else if (mode == 0) {
					int i = visited.indexOf(event);
					((Event) visited.get(i)).increaseFreq();
					if (((Integer) event2lastTrace.get(event)).intValue() != numberOfTraces) {
						((Event) visited.get(i)).increaseTraces();
						event2lastTrace.put(event, new Integer(numberOfTraces));
					}
				}
			}
		}
		file.close();
		this.file = new BufferedReader(new FileReader(filename));

		// Select those events whose frequency exceeds the threshold
		for (int i = 0; i < visited.size(); i++) {
			if (((Event) visited.get(i)).getFreq() > freqThreshold)
				events.add(visited.get(i));
		}
	}

	public void process(InferenceEngine engine) throws IOException {
		int count = 0;
		String line = null;
		boolean last_was_trace = false;

		while ((line = file.readLine()) != null) {

			// print out progress info
			count++;
			if ((count % 10000) == 0)
				//System.out.println(count);

			// filter out comments
			if (line.startsWith("//"))
				continue;

			// filter out empty lines
			if (line.matches("^$"))
				continue;

			if (line.startsWith(delim)) {
				engine.processTrace();
				last_was_trace = true;
			} else {
				String event_name;
				int mode;
				if (line.startsWith("Enter: ")) {
					event_name = line.substring(7);
					mode = 0;
				} else if (line.startsWith("Exit: ")) {
					event_name = line.substring(6);
					mode = 1;
				} else if (line.startsWith("Error: ")) {
					event_name = line.substring(7);
					mode = 2;
				} else {
					event_name = line;
					mode = 3;
				}
				int code = getEventCode(new Event(event_name));
				if (code != -1) {
					if (mode == 0) {
						engine.enterMethod(code);
					} else if (mode == 1) {
						engine.leaveMethod(code);
					}
				}
			}
		}

		file.close();
		if (!last_was_trace) {
			System.err.println("warning: last trace incomplete");
		}
		engine.processEnd();
	}

	public Event getEvent(int i) {
		return (Event) events.get(i);
	}

	private Vector splitIntoWords(String sentence) {
		Vector words = new Vector();
		String[] letters = sentence.split("");
		StringBuffer word = new StringBuffer();

		// the first letter might be an empty space

		int start;
		if (letters[0].equals(""))
			start = 1;
		else
			start = 0;

		for (int i = start; i < letters.length; i++) {
			if ((i != start) && (letters[i].matches("[A-Z]"))) {
				words.add(word.toString());
				word = new StringBuffer();
			}
			word.append(letters[i]);
		}
		words.add(word.toString());
		return words;
	}

	private double computeEditDistance(int i, int j) {
		String P = getEvent(i).toString();
		String S = getEvent(j).toString();

		// FIXME
		// This is a little hack for the windows trace for now
		Vector words_P = splitIntoWords(P.replaceAll("\"|dummy\\.|\\(\\)", ""));
		Vector words_S = splitIntoWords(S.replaceAll("\"|dummy\\.|\\(\\)", ""));

		if ((words_P.size() != words_S.size()) || (words_P.size() == 0))
			return -1.0;

		int count = 0;
		for (int k = 0; k < words_P.size(); k++)
			if (words_P.get(k).equals(words_S.get(k)))
				count++;

		if (count == words_P.size() - 1) {
			if (!((words_P.get(0).equals(words_S.get(0))) && (words_P
					.get(words_P.size() - 1).equals(words_S
					.get(words_S.size() - 1)))))
				return -1.0;
		}

		if (count == words_P.size() - 2) {
			if (!((words_P.get(0).equals(words_S.get(0))) || (words_P
					.get(words_P.size() - 1).equals(words_S
					.get(words_S.size() - 1)))))
				return -1.0;
		}

		return 1.0 * count / words_P.size();
	}

	public String getEventPair(int i, int j) {
		StringBuffer buf = new StringBuffer();
		buf.append(getEvent(i) + "->" + getEvent(j));
//		buf.append(" ed=" + computeEditDistance(i, j));
		if (InferenceEngine.printFreq) {
			buf.append(" pFreq=" + getEvent(i).getFreq() + " sFreq="
					+ getEvent(j).getFreq());
		}
		return buf.toString();
	}

	public int getEventCode(Event event) {
		return events.indexOf(event);
	}

	public int getNumberOfTraces() {
		return numberOfTraces;
	}

	public int getEventTypes() {
		return events.size();
	}
}
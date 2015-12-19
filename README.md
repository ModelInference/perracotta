# perracotta
This repo contains a working version of the Perracotta tool, developed at the University of Virginia

You can read more about the Perracotta tool [here](http://www.cs.virginia.edu/perracotta/).

###Building

We have provided an [Apache Ant](http://ant.apache.org/) buildfile to build Perracotta, along with the required libraries. 

Note that when running the ant command, many warnings will be outputted; Perracotta was built in the early to mid 2000s, and it appears that many Java practices from then now lead to warnings. 

###Running

Run Perracotta with the command

```
$ java -cp ./lib/java-getopt-1.0.14.jar:./bin edu.virginia.cs.terracotta.InferenceEngine [args]
```

where the required and optional arguments are listed below. 

```
Required arguments:
-i filename	The input trace file
Optional arguments:
-a double_number	turn on the probabilistic approximation mode
-d	turn on the detailed mode which prints out how much percent of the traces satisfy each of the eight patterns
-e	turn on the thread-aware mode
-f	print out the frequency of events (default is false)
-l int	set the minimum frequency of an event
-t	Enable measure of tree distance
-r	Enable measure of raw distance
-p	Enable measure of probability
-s patternCode	Enable single state machine mode
```

###Input Format

Perracotta's input is a single file containing multiple traces/executions, with each of these traces seperated by ----.

Each non-seperating line in the input file should be of the form

```
(Enter|Error|Exit): methodname(args)
```

and methodname must contain a period. 

###Output Format

If any instances of the 7 patterns Perracotta mines (Alternating, CauseFirst, EffectFirst, OneCause, OneEffect, MultiCause, MultiEffect), Perracotta will create a file which lists the found instances of the pattern. Seperate files are created for each pattern for which instances are found. Only the most restrictive pattern will be listed. 

The file extensions correspond to the pattern found. For instance, given an input file filename, if any Alternating patterns are found, they will be listed in filename.al. If any CauseFirst patterns are found, they will be listed in filename.cf; the extension for EffectFirst is .ef, .oc for OneCause, .oe for OneEffect, .mc for MultiCause and .me for MultiEffect. 

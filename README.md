# perracotta
This repo contains a working version of the Perracotta tool, developed at the University of Virginia

You can read more about the Perracotta tool [here](http://www.cs.virginia.edu/perracotta/).

###Building

###Running

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

#!/bin/bash
# first, remove all the javacc generated files
rm Comment*.java Java*.java NoWhite*.java Normaliser*.java cplusplus*.java NoComment*.java SimpleCharStream.java TokenMgrError.java Token.java ParseException.java
rm freetext/SentenceParser*.java freetext/NaturalParser*.java freetext/JavaCharStream.java freetext/TokenMgrError.java freetext/Token.java freetext/ParseException.java

# Before running javacc, make sure that the version of javacc in effect is
# between 2.1 and 3.2 inclusively. Newer versions will generate erroneous
# java files which will not compile.

# The code has been written to work with files generated with STATIC option
# set to *true* (the default) with the exception of SimpleCharStream.java
# which is required to be generated with STATIC option being *false*. One
# can either manually edit the file after it was generated or run the
# following commands in order.

# Run one file with STATIC option being false. After this, the boiler-plate
# files generated include SimpleCharStream.java, Token.java,
# TokenMgrError.java, and ParseException.java. Note that SimpleCharStream
# is now has STATIC option set to false which is what we want (the other
# boiler-plate files do not depend on the STATIC option so we don't have to
# worry about those); however, the custom files for Comment class generated
# by this step were also generated with STATIC option set to false which
# is not what we want. We will correct it in the next step.
javacc -STATIC=false Comment.jj

# Rerun the Comment.jj file with STATIC set to true (by default). Note that
# the boiler-plate files do not get overwritten at this step but the custom
# files do. So, after this step we have the correct SimpleCharStream.java 
# file (generated with -STATIC=false from previous step) and the correct
# custom files for Comment class (generated with -STATIC=true from this 
# step). This is the only non-standard procedure we need to follow. 
javacc Comment.jj

# The rest can just be generated in a standard and straightforward way.
javacc Java.jj
javacc NoComment.jj
javacc NoWhite.jj
javacc Normaliser.jj
javacc cplusplus.jj
cd freetext
javacc NaturalParser.jj
javacc SentenceParser.jj

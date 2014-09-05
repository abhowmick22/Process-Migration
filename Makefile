JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
	src/distsys/promigr/io/TransactionalFileInputStream.java \
	src/distsys/promigr/io/TransactionalFileOutputStream.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
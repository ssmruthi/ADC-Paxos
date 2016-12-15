JCC = javac

default: all RPCStub rmiregistry 

all:	
	$(JCC)	*.java

RPCStub: 
	rmic RPCServerAppImpl

clean: 
	$(RM) *.class

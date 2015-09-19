# Makefile for edu.washington.apl.aganse.RayDemo
# (note JAVABASEDIR and CODEDIR need to be changed for different platforms)

JAVABASEDIR = /Users/aganse/APLUW/src/java
CODEDIR = ${JAVABASEDIR}/edu/washington/apl/aganse/RayDemo

all: raydemo

raydemo: RayDemo.java Makefile
	javac -sourcepath ${JAVABASEDIR} -classpath ${CODEDIR}/classes \
              ${CODEDIR}/RayDemo.java -d ${CODEDIR}/classes
	cd ${CODEDIR}/classes; jar cmf ../mainclass.mf ../raydemo.jar *

doc: RayDemo.java
	javadoc -d doc -author -version RayDemo.java

srcjar:
	make clean
	jar cf raydemo.src.jar RayDemo.java Makefile runRayDemo.html \
		mainclass.mf classes README veldata.txt screenshot1.png \
		screenshot2.png

clean:
	\rm -rf ${CODEDIR}/doc
	\rm -rf ${CODEDIR}/classes/edu
	\rm -rf ${CODEDIR}/classes/ptolemyUpdates
	\rm -rf ${CODEDIR}/classes/*.class
	\rm -rf ${CODEDIR}/raydemo.jar

# Installing and running the "Village" model
This repository contains the "BeyondHooperville" version of the Village Ecodynamics Project agent based simulation, "Village". Village simulates human-environment interactions in an 1,817 km<sup>2</sup> portion of southwestern Colorado over from AD 600–1300. In the simulation, agents—representing ancestral Pueblo farm families—farm maize, hunt for deer, rabbits, and hares, raise turkeys, trade maize and protein, marry, have children, and in this latest form complex corporate groups that compete against one another. Village is written in Java, and is built using the [RePAST Java (v3.1)](http://repast.sourceforge.net/repast_3/index.html) toolkit.

Release version 3.0 of this code relates to *How to Make a Polity (in the central Mesa Verde region)*, a manuscript in press with *American Antiquity*. A full reference will be placed here upon publication.

## Compiling the Village source
Development of the Village simulation by VEP developers has taken place using the Eclipse IDE. Here, we provide instructions on compiling the Village code from the command prompt, or from within Eclipse. Both require an up-to-date version for the Java SDK.

### Java command line
The Village simulation requires Java SDK (Java SE 8), available [here](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Be sure to restart any browser you have open during installation. On OS X, the SDK should install to `/Library/Java/JavaVirtualMachines`. You can check your version of Java by opening up the Terminal (or "Command Prompt" in Windows) and typing `java -version`. The version listed should be `1.8.0_11` or higher.

To compile the Village code base, simply download this repository (perhaps using `git clone https://github.com/crowcanyon/vep_sim_beyondhooperville`), change into the `vep_sim_beyondhooperville` directory, and run the following:
```
mkdir bin
javac -cp src:lib/* -d bin $(find . -name "*.java")
```
This will create a new directory (`bin`) in the `vep_sim_beyondhooperville` directory, and compile all `*.java` files into Java `*.class` files using the `javac` command. The `-cp src:lib/*` option tells the compiler where to find other Java libraries referenced by the Village source.

#### Run a particular model
Once the Village source is compiled, you can run any of several version of the simulation, in either "observer" mode (i.e., with the RePAST graphical user interface) or in "batch" mode, which reads parameters from a text file and can be used to sweep over parameter values. There are two primary versions:
- AgentModelSwarm — the "classic" version of the Village model, conforming to the *Emergence and Collapse of Early Villages* edited volume (a.k.a., the VEP I final report) and Kyle Bocinsky and Stefani Crabtree's masters theses.
- BeyondHooperAgentModelSwarm — the latest version of the Village model, conforming to *How to make a polity*

For example, to run the observer version of the BeyondHooperAgentModelSwarm simulation, change into the `vep_sim_beyondhooperville` directory, and run the following (after compiling):
```
java -cp bin:lib/* com.mesaverde.groups.BeyondHooperObserverAgentModel
```
This uses the `java` command to run the `BeyondHooperObserverAgentModel` simulation, with the  `-cp bin:lib/*` option again telling the program where to find other Java libraries referenced by the Village code.

Alternatively, you can run the batch version:
```
java -cp bin:lib/* com.mesaverde.groups.BeyondHooperBatchAgentModel
```
which will begin running the code in the terminal.

In both cases, the program un-compresses the `VEPI_data.zip` file, which contains environmental data essential to running the Village simulation, and creates an `output` directory, where simulation results are stored.

## Installing, cloning, and running in Eclipse IDE

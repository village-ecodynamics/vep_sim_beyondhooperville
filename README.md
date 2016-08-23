# Installing and running the "Village" model
This repository contains the "BeyondHooperville" version of the Village Ecodynamics Project agent based simulation, "Village". Village simulates human-environment interactions in an 1,817 km<sup>2</sup> portion of southwestern Colorado from AD 600–1300. In the simulation agents—representing ancestral Pueblo farm families—grow maize, hunt for deer, rabbit, and hare, raise turkeys, trade maize and protein, marry, have children, and in this latest version form complex corporate groups that compete against one another for arable land. Village is written in Java, and is built using the [RePAST Java (v3.1)](http://repast.sourceforge.net/repast_3/index.html) toolkit.

** DRAFT Doxygen documentation of this code is available at []()**

Release version 3.0 of this code relates to *How to Make a Polity (in the central Mesa Verde region)*, a manuscript in press with *American Antiquity*. A full reference will be placed here upon publication.

Crabtree, Stefani A., R. Kyle Bocinsky, Paul L. Hooper, Susan C. Ryan, and Timothy A. Kohler
<br>2016 &emsp;&emsp; How to Make a Polity (in the central Mesa Verde region). *American Antiquity*.

This work was funded by the National Science Foundation under grant nos. [DEB-0816400](http://www.nsf.gov/awardsearch/showAward?AWD_ID=0816400), [BCS-0119981](http://www.nsf.gov/awardsearch/showAward?AWD_ID=0119981), and [DGE-1347973](http://www.nsf.gov/awardsearch/showAward?AWD_ID=1347973). Public release of the source code was made possible by GitHub and the [Research Institute at Crow Canyon Archaeological Center](http://www.crowcanyon.org/institute/).

### Compiling the Village source
Development of the Village simulation by VEP developers has taken place using the Eclipse IDE. Here, we provide instructions on compiling the Village code from the command prompt, or from within Eclipse. Both require an up-to-date version for the Java SDK and the Git versioning system. Git is installed by default on Mac OS X; download Git for Windows [here](https://git-scm.com/download/win).

#### Java command line
The Village simulation requires Java SDK (Java SE 8), available [here](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Be sure to restart any browser you have open during installation. On OS X, the SDK should install to `/Library/Java/JavaVirtualMachines`. You can check your version of Java by opening up the Terminal (or "Command Prompt" in Windows) and typing `java -version`. The version listed should be `1.8.0_11` or higher. (See below for alternate instructions on how to install Java on a Mac.)

To compile the Village code base, simply download this repository (perhaps using `git clone https://github.com/crowcanyon/vep_sim_beyondhooperville`), change into the `vep_sim_beyondhooperville` directory, and run the following (on Unix-alike OSs):
```
mkdir bin
javac -cp src:lib/* -d bin $(find . -name "*.java")
```

Or, run this on Windows (you may have to change your PATH environment variable to expose the `javac` command):
```
mkdir bin
dir /s /B *.java > sources.txt
javac -cp "./src;./lib/*;" -d bin @sources.txt
rm sources.txt
```

This will create a new directory (`bin`) in the `vep_sim_beyondhooperville` directory, and compile all `*.java` files into Java `*.class` files using the `javac` command. The `-cp src:lib/*` option tells the compiler where to find other Java libraries referenced by the Village source.

#### Run a particular model
Once the Village source is compiled, you can run any of several version of the simulation, in either "observer" mode (i.e., with the RePAST graphical user interface) or in "batch" mode, which reads parameters from a text file and can be used to sweep over parameter values. There are two primary versions:
- AgentModelSwarm — the "classic" version of the Village model, conforming to the *Emergence and Collapse of Early Villages* edited volume (a.k.a., the VEP I final report) and Kyle Bocinsky and Stefani Crabtree's masters theses.
- BeyondHooperAgentModelSwarm — the latest version of the Village model, conforming to *How to make a polity*

For example, to run the observer version of the BeyondHooperAgentModelSwarm simulation, change into the `vep_sim_beyondhooperville` directory, and run the following (after compiling, on Unix-alikes):
```
java -cp bin:lib/* com.mesaverde.groups.BeyondHooperObserverAgentModel
```
or this (on Windows):
```
java -cp "./bin;./lib/*;" com.mesaverde.groups.BeyondHooperObserverAgentModel
```
This uses the `java` command to run the `BeyondHooperObserverAgentModel` simulation, with the  `-cp bin:lib/*` option again telling the program where to find other Java libraries referenced by the Village code.

Alternatively, you can run the batch version:
```
java -cp bin:lib/* com.mesaverde.groups.BeyondHooperBatchAgentModel
```
which will begin running the code in the terminal.

In both cases, the program decompresses the `VEPI_data.zip` file, which contains environmental data essential to running the Village simulation, and creates an `output` directory, where simulation results are stored. Simulation results can then be analyzed after simulation completion (e.g., with the scripts that accompany *How to make a polity*).

### Installing, cloning, and running in Eclipse IDE

#### Downloading and installing Eclipse
We recommend downloading the latest version of the Eclipse IDE ("Neon" or newer), as all components required to run the Village simulation will already be installed. It is also imperative that you upgrade to the latest version because some earlier versions will not work with Java SE 8. Download Eclipse [here](https://www.eclipse.org/downloads/); you should download the version appropriate for your operating system. Most users running OS X on a relatively new computer will want the 64 Bit version.

Double click the installer, and select "Eclipse IDE for Java Developers" when prompted. Do *not* select "Eclipse IDE for Java EE Developers"! Install to a reasonable place on the hard drive—we suggest the `~/Applications` folder on a Mac. Start the Eclipse IDE by double-clicking the `Eclipse.app` (or `Eclipse.exe`) icon in the newly-created Eclipse directory. The Eclipse IDE should start. You will be prompted to supply a working directory. The default is fine here, unless you have been developing code in a working directory elsewhere.

Alternatively, on a Mac we suggest using Homebrew:
```
ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
brew cask install java eclipse-java
```
Select a working directory as above.

#### Cloning this repository
The first time you open Eclipse, a large welcome panel open. Close it by clicking the X nect to "Welcome" at the upper-left of the window. A Git repository explorer come installed by default with Eclipse. To access the Git Repositories control panel (called a “Perspective” in Eclipse), from the menu bar, select Window → Perspective → Open Perspective → Other. . . and select “Git”.

Developing in a Git repository should seem familiar to SVN users, though there are some subtle differences. Like SVN, Git repositories implement a two-tiered versioning system. A developer first “clones” a shared project or branch of a project, then makes changes to their local clone by “committing” them to that clone. The developer can at any time “pull” changes from the shared repository down to their local clone. Once their local version is ready to be shared, they can “push” their local changes to the shared repository, where they will supersede the version on the repository or be merged with changes other developers have pushed since creating their own clones. All users can clone this repository; only VEP developers have "push" permissions.

To clone this repository, select "Clone a Git repository" from the menu in the Git perspective; alternatively, select the small icon with a curved arrow in it. A new window will open. In the "URI:" field, type `https://github.com/crowcanyon/vep_sim_beyondhooperville`. The other fields should populate automatically. If you think you have push permissions for this repository, enter your credentials in the "Authentication" section. Click "Next". The next window should only have one option, "master", and it should be checked. Click "Next". Select a location for your Git clone to reside (or accept the default), and check the box next to "Import all existing Eclipse projects after clone finishes" in the Projects panel. Click "Finish".

In the upper-right section of the window, select the Java perspective (the little symbol with the "J"). You should see a fresh project named `vep_sim_beyondhooperville`.

#### Run a particular model
As above, you have the same options for running various versions of the Village simulation, except here Eclipse handles all of the compilation for you. To run a model, click the triangle next to the project to expand it, then right-click the "src" directory and select "Run As" → "Java Application". Eclipse will search the code for available versions; simply select the version you want to run and click "OK".

### Common Git tasks within Eclipse
As stated above, the Git versioning system is very similar to the Subversion system. A team developer “clones” a remote repository into a local Git repository, works in their local repository by “committing” changes to it, “pulls” remote changes and merges them into their local repository, and “pushes” their local changes onto the remote repository. Here, we’ll briefly overview how to accomplish each of these tasks from within the Java task view in Eclipse. It is assumed that you have already created a local clone of the Git repository and created a project in your workspace.

#### “Pulling” updates from the remote repository
“Pulling”—synonymous with “Updating” in Subversion—gets changes from a remote repository and adds them to your local repository. Any conflicts between changes you have made and those others have pushed to the remote repository will need to be dealt with when pulling.

To pull from the remote repository, simply right-click or CTRL-click the project name in the Java task view and select Team → Pull from the pop-up menu. A window will appear that will tell you the status of your pull request, and whether any changes have been made to your local clone.

#### “Committing” to your local repository
Unlike Subversion, “Committing” in Git refers to adding code changes to the local clone of your repository. Thus, you can track local versioning (and revert to them if need-be) during personal code development, and only subject your team members to your code once it is debugged; this is the appropriate way to go about developing code in a Git repository.

To commit to your local repository, simply right-click or CTRL-click the project name in the Java task view and select Team → Commit from the pop-up menu. A window will appear asking you to give some comments describing what you are committing. Finally, click “Commit” to commit your changes to the local repository. There is also an option to “Commit and Push” your changes to the remote repository. Please use it sparingly.

#### “Pushing” to the remote repository
Once the code in your local repository is stable and ready to be shared with your teammates, you may “push” it onto the remote Git repository.

To push your local changes to the remote repository, right-click or CTRL-click the project name in the Java task view and select Team → Push Upstream from the pop-up menu. Your changes will be pushed to the remote repository. Note that if you have not committed your changes to your local repository, none of the changes will be pushed to the remote repository.
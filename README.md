# P2P-system
A Java-based simulation of a Peer-to-Peer System, which uses Chord, a distributed lookup protocol. This simulation supports various operations implemented on top of Chord, including key lookup, processor addition into the network, processor removal from the network, and processor crashing on command.

### Installation

The source code can be downloaded via terminal or Command Prompt in Windows:
    
    git clone https://github.com/mmanka1/P2P-system.git
    

### Compilation

In the project directory, compile by typing the following in a terminal or Command Prompt:

    javac Chord.java
    
   
## Running 

### Configuration

To run, the program needs to initialize a network consisting of the processors and associated keys stored at these processors. The program requires two .txt files to read from, one file containing a list of processor identifiers, and another file containing a list of keys. The format of the lists in these files should be as follows:

#### Keys.txt

    <key1>,<key2>,<key3>
    
For example:

    1,2,3
    
#### Processors.txt

    <processorID1>,<processorID2>,<processorID3>
    
For example:

    1,2,3
    
Example [processor](https://github.com/mmanka1/P2P-system/blob/master/ProcessorList.txt) and [key](https://github.com/mmanka1/P2P-system/blob/master/KeyList.txt) list files have been provided for convenience.

*Note that at least three processors must be included
    
    
### Network Initialization
In the same directory where compiled and where the .txt files are located, first execute the following command to initialize the network:

    java Chord build <processorIDListFile.txt> <keyListFile.txt>

Once this command has been executed, a representation of the network is displayed.
    
### Commands to Interact with Network
Once the network has been initialized, the program will prompt for user command. The supported commands and associated arguments are as follows:

#### Key Lookup

    find,<processorID>,<key>
    
Where the processor ID specifies the processor to begin the search at.

#### Processor Join

    add,<processorId>
    
Where the processor ID specifies the processor to join the network.

#### Processor Leave

    end,<processorId>
    
Where the processor ID specifies the processor to be removed from the network.

#### Processor Crash

    crash,<processorId>
    
Where the processor ID specifies the processor to be crashed.

#### Exit Program
   
    quit


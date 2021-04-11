import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Chord {
    private static ChordNetwork p2pNetwork = null;
    public static void parseNetworkConfig(String command, String arg1, String arg2) throws IOException {
        if (command.equals("build")) {
            if (!Files.exists(Paths.get(arg1))){
                System.err.println("File does not exist: " + arg1);
                System.exit(0);
            }
            if (!Files.exists(Paths.get(arg2))){
                System.err.println("File does not exist: " + arg2);
                System.exit(0);
            }
            //Parse the processors from the processor id list
            String[] processorList = new String(Files.readAllBytes(Paths.get(arg1))).split(",");
            ArrayList<Integer> parsedProcessors = new ArrayList<>();
            for (String processor: processorList)
                parsedProcessors.add(Integer.parseInt(processor));
            
            if (parsedProcessors.size() <= 2){
                System.err.println("At least three processors must be initialized in the network");
                System.exit(0);
            }
            
            //Parse the keys from the key list
            String[] keyList = new String(Files.readAllBytes(Paths.get(arg2))).split(",");
            int[] parsedKeys = new int[keyList.length];
            for(int i = 0; i < keyList.length; i++) 
                parsedKeys[i] = Integer.parseInt(keyList[i]);

            //Initialize the Chord network
            p2pNetwork = new ChordNetwork(parsedProcessors, parsedProcessors.size());
            p2pNetwork.buildNetwork(parsedKeys);

            //Display network processors, their stored keys, and the connected edges as an adjacency list representation
            System.out.println(p2pNetwork.getNetwork().toString());

        } else {
            System.err.println("Usage for network initialization: java Chord build <processorIDListFile> <keyListFile>");
            System.exit(0);
        }
    }

    //arg1 is the processor id
    public static void parseCommand(String command, String arg1, String arg2) throws IOException {
        if (p2pNetwork != null){
            if (command.equals("find")) {
                System.out.println(p2pNetwork.findKey(Integer.parseInt(arg1), Integer.parseInt(arg2)));
            } else if (command.equals("add")) {
                ArrayList<Integer> keysTransferred = p2pNetwork.addProcessor(Integer.parseInt(arg1));
                if (keysTransferred != null)
                    System.out.println("KEYS TRANSFERRED: " + keysTransferred + "\n");
                else
                    System.out.println("Another processor already exists with the same id\n");
                //Display updated network processors, their stored keys, and the connected edges as an adjacency list representation
                System.out.println(p2pNetwork.getNetwork().toString());
            } else if (command.equals("end")) {
                p2pNetwork.endProcessor(Integer.parseInt(arg1), false);
                //Display updated network processors, their stored keys, and the connected edges as an adjacency list representation
                System.out.println(p2pNetwork.getNetwork().toString());
            } else if (command.equals("crash")) {
                p2pNetwork.crashProcessor(Integer.parseInt(arg1));
                //Display updated network processors, their stored keys, and the connected edges as an adjacency list representation
                System.out.println(p2pNetwork.getNetwork().toString());
            } else {
                System.err.println("Usage: java Chord <command>,<arguments>");
            }
        } else {
            System.err.println("Network not initialized correctly");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);
        try {
            if (args.length == 3) {
                parseNetworkConfig(args[0],args[1],args[2]);
            } else {
                System.err.println("Usage for network initialization: java Chord build <processorIDListFile> <keyListFile>");
                System.exit(0);
            }
            while(true) {   
                System.out.print("Enter command: find, add, end, crash, quit to exit: ");
                String[] input = userInput.nextLine().split(",");

                if (input[0].equals("quit")) {
                    userInput.close();
                    System.exit(0);
                } else {
                    if (input[0].equals("find")) {
                        parseCommand(input[0], input[1], input[2]);
                    } else if (input[0].equals("add")) {
                        parseCommand(input[0], input[1], "");
                    } else if (input[0].equals("end")) {
                        parseCommand(input[0], input[1], "");
                    } else if (input[0].equals("crash")) {
                        parseCommand(input[0], input[1], "");
                    } 
                } 
            }
        } catch(IOException error){
            System.err.println("Command Error");
        }
    }
}
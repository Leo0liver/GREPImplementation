import java.io.*;
import java.util.*;

public class REsearch {

    public static void main(String args[]){
        String FSMInput = "In.txt";
        File FSMFile = new File(FSMInput);
        String searchFileName = args[0];
        RandomAccessFile searchFile = null;
        try {
            searchFile = new RandomAccessFile(searchFileName, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        search(searchFile, FSMFile);
    }

    private static void search(RandomAccessFile searchFile, File FSMFile) {
        Deque<Integer> currentStates = new LinkedList<>();
        Deque<Integer> nextStates = new LinkedList<>();
        try {
            //Convert the FSM input file into 4 seperate arrays
            Scanner FSMSizeScanner = new Scanner(FSMFile);
            int FSMSize = 0;
            while(FSMSizeScanner.hasNextLine()){ //Get the number of lines (states) in the FSM input
                FSMSize ++;
                FSMSizeScanner.nextLine();
            }
            FSMSizeScanner.close();
            int[] state = new int[FSMSize];
            char[] ch = new char[FSMSize];
            int[] n1 = new int[FSMSize];
            int[] n2 = new int[FSMSize];
            Scanner FSMScanner = new Scanner(FSMFile);
            int i = 0;
            while(FSMScanner.hasNextLine()){
                String data = FSMScanner.nextLine();
                String parts[] = data.split(" ");
                state[i] = Integer.parseInt(parts[0]);
                ch[i] = parts[1].charAt(0);
                n1[i] = Integer.parseInt(parts[2]);
                n2[i] = Integer.parseInt(parts[3]);
                i++;
            }

            LinkedList<Long> lineIndexs = new LinkedList<>();
            LinkedList<String> foundLines = new LinkedList<>();
            long p = searchFile.getFilePointer();
            long m = searchFile.getFilePointer();
            while(searchFile.read() != -1){ //This loop increments through each symbol in the input txt file
                searchFile.seek(p);
                currentStates.add(0); //Add the start state to the list of current states
                if(searchFile.readByte() == 13){
                    lineIndexs.add(m);
                }
                boolean flag = true;
                while(flag == true) { //This loop tries to find potential matches
                    LinkedList<Integer> seenStates = new LinkedList<>();
                    searchFile.seek(m);
                    char currentChar = (char)searchFile.readByte();

                    while(currentStates.isEmpty() != true){
                        int s = currentStates.pop();

                        if(seenStates.contains(s) != true) { //If we havent already considered this state
                            if (n1[s] == -1 || n2[s] == -1) {
                                //We found a match print the entire line to output
                                long startLine = 0;
                                String line;
                                for (long l : lineIndexs) {
                                    if(m > l){
                                        startLine = l;
                                    }
                                }
                                searchFile.seek(startLine+2);
                                line = searchFile.readLine();
                                if(foundLines.contains(line) != true) { //We do not want to output a line that has already been output
                                    System.out.println(line);
                                    foundLines.add(line);
                                }
                                //System.out.println(searchFile.readLine());
                                flag = false;
                                break;
                            } else if (ch[s] == currentChar) { //If we have a match then add the next possible states
                                nextStates.add(n1[s]);
                                nextStates.add(n2[s]);
                            } else if (ch[s] == (char) 42) { //If the current state is a branching state add next states
                                currentStates.push(n1[s]);
                                currentStates.push(n2[s]);
                            }
                            seenStates.add(s);
                        }
                    }

                    if(nextStates.isEmpty() != true){
                        m++;
                        int x;
                        while(nextStates.isEmpty() != true){
                            x = nextStates.pop();
                            if(currentStates.contains(x) != true) {
                                currentStates.push(x);
                            }
                        }
                    }
                    else{
                        break;
                    }

                }
                p++;
                m = p;
                searchFile.seek(p);
            }
            System.out.println(lineIndexs.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

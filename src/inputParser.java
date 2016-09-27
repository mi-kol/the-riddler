/**
 * Created by start on 9/20/2016.
 */

import com.budhash.cliche.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class inputParser {

    static XStream xstream = new XStream(new DomDriver());
    public static String newFilePath;
    public static File file;
    public static Riddles riddles = new Riddles();
    public int relevantRiddle;


    public static void fileCreator() {
        // Creates the XML file that the rest of the program builds off of.
        xstream.alias("riddle", Riddle.class);
        Path currentRelativePath = Paths.get("");
        newFilePath = currentRelativePath.toAbsolutePath().toString() + "\\riddles.xml";
        file = new File(newFilePath);
        try {
            if (file.createNewFile()) {
                System.out.println("The XML file has been created!");
            } else {
                System.out.println("XML file exists!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // Controls command loop. Aliases riddles to riddles.class for easier use through xstream.
        fileCreator();
        xstream.alias("riddles", Riddles.class);
        xstream.alias("riddle", Riddle.class);
        try {
            FileInputStream fis = new FileInputStream("riddles.xml");
            if (fis.available() > 0) {
                xstream.fromXML(fis, riddles);
                riddles.stringification();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        ShellFactory.createConsoleShell("", "", new inputParser())
                .commandLoop();
    }

    @Command // Help
    public String help() {
        // Gives help
        return "Uses: help -- get help\n" +
                "      create  -- create a riddle and followp prompts\n" +
                "      select -- lists all riddles pulled from file. lets you select which one to interact with\n" +
                "      retrieve -- see all riddles\n" +
                "      question -- lists question for selected\n" +
                "      answer -- lists answer for selected\n" +
                "      wipe -- wipes XML file, probably don't do this\n" +
                "      solve -- attempt to solve the riddle. either use argument \"easy\" or \"hard\". If you can't figure it out and want to stop, type \"give up\"\n" +
                "      edit -- edit answers. use \"add\" to add answers, and \"remove\" or \"rm\" to delete them";

    }

    @Command // Select riddle
    public void select() {
        // Select the riddle that the user wants to work with.
        for (int i = 0; i < riddles.riddles.size(); i++) {
            String newString = "[" + i + "]\n" + riddles.riddles.get(i).stringification();
            System.out.println(newString);
        }
        Scanner in = new Scanner(System.in);
        System.out.println("Which riddle would you like to select?");
        String userInput = in.nextLine();
        if (Integer.parseInt(userInput) <= riddles.riddles.size() || Integer.parseInt(userInput) > -1) {
            relevantRiddle = Integer.parseInt(userInput);
        } else {
            System.out.println("That doesn't seem right. Try selecting a riddle shown on the screen.");
        }
        System.out.println("Alright.");
    }

    @Command // Get Question
    public void question() {
        // Prints the question.
        System.out.println(riddles.riddles.get(relevantRiddle).getQuestion());
    }

    @Command // Get Answer
    public void answer() {
        // Prints the answer.
        System.out.println(riddles.riddles.get(relevantRiddle).getAnswer());
    }

    @Command // Change Answer
    public void change() {
        // Change the answer index.
        for (int i = 0; i < riddles.riddles.get(relevantRiddle).possibleAnswers.size(); i++) {
            System.out.println("[" + i + "]" + " " + riddles.riddles.get(relevantRiddle).possibleAnswers.get(i));
        }
        Scanner in = new Scanner(System.in);
        System.out.println("What is your new answer? Answer in terms of index.");
        String userInput = in.nextLine();
        int userInt = Integer.parseInt(userInput);
        riddles.riddles.get(relevantRiddle).setAnswer(userInt);
        System.out.println(userInt + " is now the index. The answer is now " + riddles.riddles.get(relevantRiddle).getAnswer());

    }


    @Command // Instantiate new Riddle
    public void create() throws Exception {
        // Create a riddle
        System.out.println("Current riddles.");
        System.out.println(riddles.riddles.size());
        if (riddles.riddles.size() > 0) {
            retrieve();
        } else {
            System.out.println("None yet.");
        }
        Riddle riddle = new Riddle();
        Scanner in = new Scanner(System.in);
        System.out.println("Enter your riddle.");
        // First attribtue - riddle question
        String userInput = in.nextLine();
        riddle.setQuestion(userInput);
        in = new Scanner(System.in);
        System.out.println("How many possible answers should there be?");
        // Starts to generate the attribute - possible answers
        userInput = in.nextLine();
        if (Integer.parseInt(userInput) == (int) Integer.parseInt(userInput)) {
            int answersInt = Integer.parseInt(userInput);
            for (int i = 0; i < answersInt; i++) {
                in = new Scanner(System.in);
                System.out.println("[" + i + "] Possible answer:");
                riddle.possibleAnswers.add(in.nextLine());
            }
            // establishes possibleAnswers
            in = new Scanner(System.in);
            System.out.println("Which is the correct answer? Type the number, starting from 1.");
            for (int i = 0; i < riddle.possibleAnswers.size(); i++) {
                System.out.println("[" + i + "]" + " " + riddle.possibleAnswers.get(i));
            }
            // establishes answerID
            userInput = in.nextLine();
            if (Integer.parseInt(userInput) == (int) Integer.parseInt(userInput) && Integer.parseInt(userInput) <= riddle.possibleAnswers.size() && Integer.parseInt(userInput) > -1) {
                riddle.setAnswer(Integer.parseInt(userInput));
                riddles.addRiddle(riddle);
                if (file.length() == 0) {
                    String toXML = xstream.toXML(riddles);
                    PrintWriter pw = new PrintWriter("riddles.xml");
                    pw.println(toXML);
                    pw.flush();
                    pw.close();
                    // creates XML format
                } else {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document document = documentBuilder.parse("riddles.xml");
                    Element root = document.getDocumentElement();
                    Collection<Riddle> riddles = new ArrayList<Riddle>();
                    riddles.add(riddle);
                    for (int i = 0; i < riddles.size(); i++) {
                        Element newRiddles = document.createElement("riddles");

                        Element newRiddle = document.createElement("riddle");
                        newRiddles.appendChild(newRiddle);

                        Element question = document.createElement("question");
                        question.appendChild(document.createTextNode(riddle.getQuestion()));
                        newRiddle.appendChild(question);

                        Element possibleAnswers = document.createElement("possibleAnswers");
                        possibleAnswers.appendChild(document.createTextNode(Arrays.toString(riddle.possibleAnswers.toArray())));
                        newRiddle.appendChild(possibleAnswers);

                        Element answerID = document.createElement("answerID");
                        answerID.appendChild(document.createTextNode(Integer.toString(riddle.answerID)));
                        newRiddle.appendChild(answerID);

                        root.appendChild(newRiddle);

                    }

                    DOMSource source = new DOMSource(document);

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    StreamResult result = new StreamResult("riddles.xml");
                    transformer.transform(source, result);
                    // appends riddle nodes to xml
                }
                xstream.toXML(riddles, new FileWriter(newFilePath));
            } else {
                System.out.println("Try using an integer that is valid and matches a correct number.");
            }
        } else {
            System.out.println("Try entering an integer.");
        }
    }

    @Command // Retrieves XML file.
    public static void retrieve() {
        // Retrieves XML file. Prints it out through the "stringification" method.
        try {
            FileInputStream fis = new FileInputStream("riddles.xml");
            xstream.fromXML(fis, riddles);
            riddles.stringification();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Command // Wipes XML file.
    public void wipe() throws FileNotFoundException {
        // Wipes the XML file clean
        System.out.println("Current riddles.");
        retrieve();
        PrintWriter writer = new PrintWriter("riddles.xml");
        writer.print("");
        writer.close();
        System.out.println("Wiped.");
    }

    @Command // Solve
    public void solve(String mode) {
        // Attempts to solve the riddle, using the input that the user will later give and what difficulty the user wants (easy is multiple choice, hard is short answer)
        if (riddles.riddles.get(relevantRiddle).isValid()) {
            // checks if riddle is valid to be solved
            if (mode.toLowerCase().equals("hard")) {
                System.out.println(riddles.riddles.get(relevantRiddle).getQuestion());
                System.out.println("Answer here! Caps are irrelevant. Be sure to spell correctly!");
                Scanner in = new Scanner(System.in);
                String userAnswer = in.nextLine();
                if (userAnswer.toLowerCase().equals(riddles.riddles.get(relevantRiddle).possibleAnswers.get(riddles.riddles.get(relevantRiddle).answerID).toLowerCase())) {
                    // checks if riddle answer is what was answered
                    System.out.println("True!");
                } else {
                    System.out.println("False! Try again!");
                    solve(mode);
                }
            } else if (mode.toLowerCase().equals("easy")) {
                // multiple choice riddle solving
                System.out.println(riddles.riddles.get(relevantRiddle).getQuestion());
                System.out.println("Answer here! Enter the correct index!");
                for (int i = 0; i < riddles.riddles.get(relevantRiddle).possibleAnswers.size(); i++) {
                    System.out.println("[" + i + "]" + " " + riddles.riddles.get(relevantRiddle).possibleAnswers.get(i));
                }
                Scanner in = new Scanner(System.in);
                String userAnswer = in.nextLine();
                if (Integer.parseInt(userAnswer) == riddles.riddles.get(relevantRiddle).answerID) {
                    System.out.println("True");
                } else {
                    System.out.println("False! Try again!");
                    solve(mode);
                }
            } else if (mode.toLowerCase().equals("give up")) {
                System.out.println("Fair enough.");
            } else {
                System.out.println("This didn't work. Try typing 'easy' or 'hard' as your argument.");
            }
        } else {
            System.out.println("The riddle you requested to solve was not valid. Try adding some possible answers.");
        }
    }

    @Command // Edit answers
    public void edit(String mode) {
        // If the arg is remove or rm, tries to delete one of the possible answers. if the arg is add, it adds to the possible answers
        if (mode.toLowerCase().equals("remove") || mode.toLowerCase().equals("rm")) {
            System.out.println("Which answer would you like to remove?");
            for (int i = 0; i < riddles.riddles.get(relevantRiddle).possibleAnswers.size(); i++) {
                System.out.println("[" + i + "]" + " " + riddles.riddles.get(relevantRiddle).possibleAnswers.get(i));
            }
            // just prints the riddles with the index.
            Scanner in = new Scanner(System.in);
            if (Integer.parseInt(in.nextLine()) == (int) Integer.parseInt(in.nextLine()) && Integer.parseInt(in.nextLine()) <= riddles.riddles.get(relevantRiddle).possibleAnswers.size() && Integer.parseInt(in.nextLine()) > -1) {
                // checks to see that the integer is valid, and is an active possible answer.
                int userAnswer = Integer.parseInt(in.nextLine());
                riddles.riddles.get(relevantRiddle).rmAnswer(userAnswer);
                System.out.println("Removed. Here are the remaining answers.");
                for (int i = 0; i < riddles.riddles.get(relevantRiddle).possibleAnswers.size(); i++) {
                    System.out.println("[" + i + "]" + " " + riddles.riddles.get(relevantRiddle).possibleAnswers.get(i));
                }
                try {
                    refresh();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Try selecting an available answer index.");
                edit(mode);
            }
        } else if (mode.toLowerCase().equals("add")) {
            System.out.println("What would you like to add to the answers?");
            for (int i = 0; i < riddles.riddles.get(relevantRiddle).possibleAnswers.size(); i++) {
                System.out.println("[" + i + "]" + " " + riddles.riddles.get(relevantRiddle).possibleAnswers.get(i));
            }
            Scanner in = new Scanner(System.in);
            String userAnswer = in.nextLine();
            riddles.riddles.get(relevantRiddle).addAnswer(userAnswer);
            // just appends an answer to the array list
            for (int i = 0; i < riddles.riddles.get(relevantRiddle).possibleAnswers.size(); i++) {
                System.out.println("[" + i + "]" + " " + riddles.riddles.get(relevantRiddle).possibleAnswers.get(i));
            }
            try {
                refresh();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void refresh() throws FileNotFoundException {
        // refreshes xml file with current riddles data.
        PrintWriter pw = new PrintWriter("riddles.xml");
        pw.println();
        String toXML = xstream.toXML(riddles);
        pw.println(toXML);
        pw.flush();
        pw.close();
    }
}

    class Riddle {
        // Riddle class
        public String question;
        // question attribute
        ArrayList<String> possibleAnswers = new ArrayList<>();
        // possible answers attribute
        public int answerID;
        // answer id attribute
        public void setQuestion(String question) {
            // sets question attribute
            this.question = question;
            System.out.println("It worked.");
        }

        public String getQuestion() {
            // prints question
            return this.question;
        }

        public String getAnswer() {
            // prints answer string as well as index
            return this.possibleAnswers.get(this.answerID) + " : " + this.answerID;

        }

        public void setAnswer(int inputID) {
            // sets answer id
            answerID = inputID;
        }

        public void addAnswer(String newAnswer) {
            // adds answer to possible answers
            this.possibleAnswers.add(newAnswer);
        }

        public void rmAnswer(int answerID) {
            // removes answer from possible answers
            this.possibleAnswers.remove(answerID);
        }

        // this is just here to satisfy the needs of the assignment. i found that running through the solve function in inputParser class was faster to write (and looked cooler)
        public void answerQuestion(int answer) {
            if (this.answerID == answer) {
                System.out.println("You are correct!");
            } else {
                System.out.println("You are incorrect!");
            }
        }

        public Boolean isValid() {
            // checks if answer can be asked by making sure that there is enough possible answers
            if (this.possibleAnswers.size() > 0) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        public String stringification() {
            // prints out riddle in a human readable format
            return "Question : " + this.question +
                    "\nAnswers : " + this.possibleAnswers +
                    "\nCorrect Answer + Index: " + getAnswer();
        }
    }

    class Riddles {
        // manages all riddles. this is mainly for XML purposes
        public ArrayList<Riddle> riddles = new ArrayList<Riddle>();
        // array list of the riddle objects

        public void addRiddle(Riddle riddle) {
            // adds riddle to riddles
            riddles.add(riddle);
        }

        public void rmRiddle(Riddle riddle) {
            // removes riddle from riddles
            riddles.remove(riddle);
        }
        public void stringification() {
            // recursively prints human readable representations of the riddles
            for (int i = 0; i < riddles.size(); i++) {
                System.out.println("Question : " + this.riddles.get(i).getQuestion() +
                        "\nAnswers : " + this.riddles.get(i).possibleAnswers +
                        "\nCorrect Answer Index : " + this.riddles.get(i).answerID + "\n");
            }
        }
    }
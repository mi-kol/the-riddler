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

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

    @Command // Help - Not finished
    public String help() {
        return "Uses: help -- get help\n" +
                "      create  -- create a riddle and followp prompts\n" +
                "      select -- lists all riddles pulled from file. lets you select which one to interact with\n" +
                "      retrieve -- see all riddles\n" +
                "      question -- lists question for selected\n " +
                "     answer -- lists answer for selected\n" +
                "      wipe -- wipes XML file, probably don't do this\n" +
                "      solve -- attempt to solve the riddle. either use argument \"easy\" or \"hard\"\n";

    }

    @Command // Select riddle
    public void select() {
        for (int i = 0; i < riddles.riddles.size(); i++) {
            String newString = "[" + i + "]\n" + riddles.riddles.get(i).stringification();
            System.out.println(newString);
        }
        Scanner in = new Scanner(System.in);
        System.out.println("Which riddle would you like to select?");
        String userInput = in.nextLine();
        relevantRiddle = Integer.parseInt(userInput);
        System.out.println("Alright.");
    }

    @Command // Get Question
    public void question() {
        System.out.println(riddles.riddles.get(relevantRiddle).getQuestion());
    }

    @Command // Get Answer
    public void answer() {
        System.out.println(riddles.riddles.get(relevantRiddle).getAnswer());
    }

    @Command // Change Answer
    public void change() {
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
        String userInput = in.nextLine();
        riddle.setQuestion(userInput);
        in = new Scanner(System.in);
        System.out.println("How many possible answers should there be?");
        userInput = in.nextLine();
        int answersInt = Integer.parseInt(userInput);
        for (int i = 0; i < answersInt; i++) {
            in = new Scanner(System.in);
            System.out.println("[" + i + "] Possible answer:");
            riddle.possibleAnswers.add(in.nextLine());
        }
        in = new Scanner(System.in);
        System.out.println("Which is the correct answer? Type the number, starting from 1.");
        for (int i = 0; i < riddle.possibleAnswers.size(); i++) {
            System.out.println("[" + i + "]" + " " + riddle.possibleAnswers.get(i));
        }
        userInput = in.nextLine();
        riddle.setAnswer(Integer.parseInt(userInput));
        riddles.addRiddle(riddle);
        if (file.length() == 0) {
            String toXML = xstream.toXML(riddles);
            PrintWriter pw = new PrintWriter("riddles.xml");
            pw.println(toXML);
            pw.flush();
            pw.close();
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
        }
        xstream.toXML(riddles, new FileWriter(newFilePath));
        // addXMLNode();
    }

    @Command // Retrieves XML file.
    public static void retrieve() {
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
        System.out.println("Current riddles.");
        retrieve();
        PrintWriter writer = new PrintWriter("riddles.xml");
        writer.print("");
        writer.close();
        System.out.println("Wiped.");
    }

    @Command // Solve
    public void solve(String mode) {
        if (riddles.riddles.get(relevantRiddle).isValid()) {
            if (mode.toLowerCase().equals("hard")) {
                System.out.println(riddles.riddles.get(relevantRiddle).getQuestion());
                System.out.println("Answer here! Caps are irrelevant. Be sure to spell correctly!");
                Scanner in = new Scanner(System.in);
                String userAnswer = in.nextLine();
                if (userAnswer.toLowerCase().equals(riddles.riddles.get(relevantRiddle).possibleAnswers.get(riddles.riddles.get(relevantRiddle).answerID).toLowerCase())) {
                    System.out.println("True!");
                } else {
                    System.out.println("False! Try again!");
                    solve(mode);
                }
            } else if (mode.toLowerCase().equals("easy")) {
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
            } else {
                System.out.println("This didn't work. Try typing 'easy' or 'hard' as your argument.");
            }
        } else {
            System.out.println("The riddle you requested to solve was not valid. Try adding some possible answers.");
        }
    }

    @Command // Edit answers
    public void edit(String mode) {
        if (mode.toLowerCase().equals("remove") || mode.toLowerCase().equals("rm")) {
            System.out.println("Which answer would you like to remove?");
            for (int i = 0; i < riddles.riddles.get(relevantRiddle).possibleAnswers.size(); i++) {
                System.out.println("[" + i + "]" + " " + riddles.riddles.get(relevantRiddle).possibleAnswers.get(i));
            }
            Scanner in = new Scanner(System.in);
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
        } else if (mode.toLowerCase().equals("add")) {
            System.out.println("What would you like to add to the answers?");
            for (int i = 0; i < riddles.riddles.get(relevantRiddle).possibleAnswers.size(); i++) {
                System.out.println("[" + i + "]" + " " + riddles.riddles.get(relevantRiddle).possibleAnswers.get(i));
            }
            Scanner in = new Scanner(System.in);
            String userAnswer = in.nextLine();
            riddles.riddles.get(relevantRiddle).addAnswer(userAnswer);
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
        PrintWriter pw = new PrintWriter("riddles.xml");
        pw.println();
        String toXML = xstream.toXML(riddles);
        pw.println(toXML);
        pw.flush();
        pw.close();
    }
}

    class Riddle {
        public String question;
        ArrayList<String> possibleAnswers = new ArrayList<>();
        public int answerID;

        public void setQuestion(String question) {
            this.question = question;
            System.out.println("It worked.");
        }

        public String getQuestion() {
            return this.question;
        }

        public String getAnswer() {
            return this.possibleAnswers.get(this.answerID) + " : " + this.answerID;

        }

        public void setAnswer(int inputID) {
            answerID = inputID;
        }

        public void addAnswer(String newAnswer) {
            this.possibleAnswers.add(newAnswer);
        }

        public void rmAnswer(int answerID) {
            this.possibleAnswers.remove(answerID);
        }

        @Deprecated
        public void answerQuestion(int answer) {
            if (this.answerID == answer) {
                System.out.println("You are correct!");
            } else {
                System.out.println("You are incorrect!");
            }
        }

        public Boolean isValid() {
            if (this.possibleAnswers.size() > 0) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        public String stringification() {
            return "Question : " + this.question +
                    "\nAnswers : " + this.possibleAnswers +
                    "\nCorrect Answer + Index: " + getAnswer();
        }
    }

    class Riddles {
        public ArrayList<Riddle> riddles = new ArrayList<Riddle>();

        public void addRiddle(Riddle riddle) {
            riddles.add(riddle);
        }

        public void rmRiddle(Riddle riddle) {
            riddles.remove(riddle);
        }

        public ArrayList<Riddle> showRiddles() {
            return riddles;
        }

        public void stringification() {
            for (int i = 0; i < riddles.size(); i++) {
                System.out.println("Question : " + this.riddles.get(i).getQuestion() +
                        "\nAnswers : " + this.riddles.get(i).possibleAnswers +
                        "\nCorrect Answer Index : " + this.riddles.get(i).answerID + "\n");
            }
        }
    }




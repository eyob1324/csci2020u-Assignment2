package sample;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class controller {

    @FXML
    private Button Download;
    @FXML
    private Button Upload;
    @FXML
    private Button Update;
    @FXML
    private ListView client;
    @FXML
    private ListView Server;
    @FXML
    private javafx.scene.control.Label lblSystemMessage;
    @FXML
    private javafx.scene.control.Label text1;
    @FXML
    private javafx.scene.control.Label text2;
    @FXML
    private javafx.scene.control.Button Hide;



    protected List<String> ClientFiles = new ArrayList<>();
    protected List<String> ServerFiles = new ArrayList<>();
    protected ListProperty<String> ClientFileList = new SimpleListProperty<>();
    protected ListProperty<String> ServerFileList = new SimpleListProperty<>();
    final File Client_folder = new File("ClientFiles");
    private PrintWriter Out = null;
    private BufferedReader In = null;

    public void initialize() {
        text1.setFont(new Font("Cambria", 25));
        text2.setFont(new Font("Cambria", 25));
        String DataFromServer = "";
        String message1= "";
        String message2= "";


        /*
        * creates new socket to get all the files
        * from the the server file to get all the filenames to show on the UI
        * */
        try{
            Socket clientSocket = new Socket("localhost", 8080);
            Out = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()));
            In =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            message1 = In.readLine();
            message2 = In.readLine();
            Out.println("Dir");
        Out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DataFromServer = In.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] FromeServer = DataFromServer.split(" ");
        for (String file : FromeServer) {

            ServerFiles.add(file);

        }


        // takes the file folder and makes it a list that we can go through
        File[] listOfFilesClient = Client_folder.listFiles();
        //goes througgh the folder if in the folder of the object is a file then adds it to the ClienFiles List
        for (File file : listOfFilesClient) {
            if (file.isFile()) {
                ClientFiles.add(file.getName());
            }
        }

        Server.itemsProperty().bind(ServerFileList);
        client.itemsProperty().bind(ClientFileList);
        ClientFileList.set(FXCollections.observableArrayList(ClientFiles));
        ServerFileList.set(FXCollections.observableArrayList(ServerFiles));
    }

    public void helpLable(ActionEvent event){
        /*
         * Makes Hide button visible
         * And outputs help instruction
         */
        //Makes Hide button visible
        Hide.setVisible(true);
        lblSystemMessage.setText(
                "Help Menu:" +
                "\nUploaded:" +
                "\n 1.choose folder you would like to upload" +
                "\n  from client-side then press Upload"+
                "\nDownload:" +
                "\n 1.choose folder you would like to download" +
                "\n  from server-side Then press Download"
        );
        //Creates Hide button
        Hide.setText("Hide Text");
        Hide.setLayoutX(390);
        Hide.setLayoutY(845);
    }
    public void hideLable(ActionEvent event){
        /*
         * Hides The Label and Hide button ones run
         */
        lblSystemMessage.setText("");
        //hides Hide Button
        Hide.setVisible(false);
    }
    /*
    * btnOnPressdownload is used to get the file from the server and download it to the ClientFolder
    *
    *@pram ActionEvent actionEvent
    * when button is pressed it creates a new client socket and new output stream
    * It sends a command to the server "Download" and the filename that we want to get
    * once the server gets the command it starts sending the contents of the file and the function receives
    * it and creates a the file in The client file with the data gotten from the server.
    * */
    public void btnOnPressdownload(ActionEvent actionEvent) throws IOException {
        Socket clientSocket = null;
        PrintWriter OUT = null;
        String DataFromServer;
        String message1= "";
        String message2= "";

        try {
            clientSocket = new Socket("localhost",8080);
            OUT = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()));
            OUT.println("DownLoad"+" "+Server.getSelectionModel().getSelectedItems().get(0));
            In =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            message1 = In.readLine();
            message2 = In.readLine();
            OUT.flush();

      } catch (Exception e) {
          e.printStackTrace();
      }

        try{
            //data from server is read and the stored in DataFromServer
            DataFromServer = In.readLine();
            //path to were the new file is going to be created
            String path = "ClientFiles/" + Server.getSelectionModel().getSelectedItems().get(0);
            File IncomingFile= new File(path);
            //looks to see if the file already exists
            if(!IncomingFile.exists()) {
                IncomingFile.createNewFile();
            }
            FileWriter fw=new FileWriter(path);
            //writes to new file
            String [] Sentences =DataFromServer.split(" ");
            for(String Sentence:Sentences){
                fw.write(Sentence+" ");
            }
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //closes the socket
        clientSocket.close();
        //updates the list
        ListViewUpdate();
    }

    /*
    * btnOnPressUpload is used to upload Files from the ClientFiles to The ServerFiles
    *
    * It first creates a socket that connects to port 8080
    * and then sends a command to upload first the File name
    * and then after it sends the content of the file
    * */
    public void btnOnPressUpload(ActionEvent actionEvent) {
        Socket clientSocket = null;
        PrintWriter out = null;
        try{
            //creates socket
            clientSocket = new Socket("localhost",8080);
            out = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()));
            //sends upload command with the file name as second argument
            out.println("Upload"+" "+client.getSelectionModel().getSelectedItems().get(0));
            //takes client_folder and makes it a list of files;
            File[] listOfFilesClient = Client_folder.listFiles();
            //loops until the file selected is found
            for (File file : listOfFilesClient) {
                if (file.getName().equals(client.getSelectionModel().getSelectedItems().get(0)) ) {
                    //this is where the file content is sent through the socket
                    File FileReading = new File(String.valueOf(file.getAbsoluteFile()));
                    Scanner sc = new Scanner(file);
                    String FileSending = " ";
                    while (sc.hasNextLine()) {
                        FileSending +=sc.nextLine()+" ";
                    }
                    out.println(FileSending);
                }
            }
            out.flush();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //updates the listView
    ListViewUpdate();
    }


    public void exitProgram(ActionEvent event){
        /*
         * Function closes the system running
         */
        System.exit(0);
    }

    /*
    * ListViewUpdate is used to update what is seen on the Listveiw when new files are put in the clientfile
    *
    * It woks by first clearing the ArrayList ServerFiles and ClientFiles
    * Then it grabs the new Data and enteres them in their respective ArrayList
    * */
    public void ListViewUpdate() {
        ServerFiles.clear();
        ClientFiles.clear();

        String DataFromServer = "";
        String message1= "";
        String message2= "";

        try{
            Socket clientSocket = new Socket("localhost", 8080);
            Out = new PrintWriter(new BufferedOutputStream(clientSocket.getOutputStream()));
            In =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            message1 = In.readLine();
            message2 = In.readLine();
            Out.println("Dir");
            Out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DataFromServer = In.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] FromeServer = DataFromServer.split(" ");
        for (String file : FromeServer) {

            ServerFiles.add(file);

        }


        // takes the file folder and makes it a list that we can go through
        File[] listOfFilesClient = Client_folder.listFiles();
        for (File file : listOfFilesClient) {
            if (file.isFile()) {
                ClientFiles.add(file.getName());
            }
        }



        Server.itemsProperty().bind(ServerFileList);
        client.itemsProperty().bind(ClientFileList);
        ClientFileList.set(FXCollections.observableArrayList(ClientFiles));
        ServerFileList.set(FXCollections.observableArrayList(ServerFiles));
    }

    /*function that when pressed calls the ListViewUpdate
    * to update the ListView on the UI to the most uptodate version*/
    public void btnOnPressupdate(ActionEvent actionEvent) {
        ListViewUpdate();
    }
}

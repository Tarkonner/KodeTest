package com.example;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class DoctorsClient {
    private Socket socket; // Declare socket as an instance variable
    private DataOutputStream serverConnection; // Declare serverConnection as an instance variable
    private DataInputStream incomingMessages; // Declare incomingMessages as an instance variable
    private static boolean activeListenThread = true;
    private static Scanner scanner;

    private static String splitSymbol = "£";

    public DoctorsClient() {
      try {
          // Initialize socket, serverConnection, and incomingMessages here
          socket = new Socket("localhost", 4444);
          serverConnection = new DataOutputStream(socket.getOutputStream());
          incomingMessages = new DataInputStream(socket.getInputStream());
      } catch (IOException e) {
          e.printStackTrace();
      }
   }

   public static void main(String[] args) throws IOException {
      DoctorsClient client = new DoctorsClient();      

      // Start a new thread to handle incoming messages
      Thread messageHandlerThread = new Thread(() -> {
      try 
      {
         while (activeListenThread) { // Check the stop flag
            String serverMessage = client.incomingMessages.readUTF();
            System.out.println("Server: " + serverMessage);
            if ("exit".equals(serverMessage)) {
                break;
            }
        }
      } 
      catch (IOException e) 
      {
          e.printStackTrace();
      }
      });
      messageHandlerThread.start();
      ShowCommands();
      // Read input from the user
      scanner = new Scanner(System.in);    
         
      while (true) {
        String userInput = scanner.nextLine().toUpperCase();

        switch (userInput) {
            case "AD":
                String doctorDetails = AddDoctor();
                client.serverConnection.writeUTF(doctorDetails);
                break;
            case "AP":
                String pationDetails = AddPatient();
                client.serverConnection.writeUTF(pationDetails);
                break;
            case "FP":
                String IDs = FindPatientWithID();
                client.serverConnection.writeUTF(IDs);
            break;
            case "EXIT":
                client.serverConnection.writeUTF("exit");

                //Close open connections and threads
                scanner.close();
                client.socket.close();
                activeListenThread = false;
                break;
            default:
                System.out.println("Invalid command.");
        }
      }         
   }

   void ConnectToServer()
   {
      try {
         // Initialize socket, serverConnection, and incomingMessages here
         socket = new Socket("localhost", 4444);
         serverConnection = new DataOutputStream(socket.getOutputStream());
         incomingMessages = new DataInputStream(socket.getInputStream());
     } catch (IOException e) {
         e.printStackTrace();
     }
   }

   public static String AddDoctor() 
   {
        String[] values = new String[4];
    
        values[0] = "AD";
        System.out.print("Doctor's name: ");
        values[1] = scanner.nextLine();
        System.out.print("Doctor's ID: ");
        values[2] = scanner.nextLine();
        System.out.print("Doctor's department: ");
        values[3] = scanner.nextLine();
    
        String result = String.join(splitSymbol, values);
        System.out.println("Formatted doctor details: " + result);
    
        return result;
    }

   public static String AddPatient()
   {
        String[] values = new String[4];
        values[0] = "AP" + splitSymbol;
        System.out.print("Pations's name: ");
        values[1] = scanner.nextLine();
        System.out.print("Pations's social secruity number: ");
        values[2] = scanner.nextLine();
        System.out.print("Pations's department: ");
        values[3] = scanner.nextLine();

        //Get x doctors
        List<String> docsNames = new ArrayList<>();
        System.out.println("Doctor with patiens. Leave empty to exit");
        while(true)
        {
            String input = scanner.nextLine();
            if(input.isEmpty())
                break;
            else
                docsNames.add(input);
        }
        System.out.println("Send info");

        StringBuilder resultBuilder = new StringBuilder(values[0]);
        resultBuilder.append(values[1]).append(splitSymbol).append(values[2]).append(splitSymbol).append(values[3]);

        // Append doctor names to the result
        for (String docName : docsNames) {
            resultBuilder.append(splitSymbol).append(docName);
        }

        String result = resultBuilder.toString();
        System.out.println(result);
        return result;
   }
   public static String FindPatientWithID()
   {
        String[] values = new String[3];
        values[0] = "FPWI";
        System.out.print("Pations's social secruity number: ");
        values[1] = String.valueOf(scanner.nextLine());
        System.out.print("Doctor's ID: ");
        values[2] = String.valueOf(scanner.nextLine());

        String result = String.join(splitSymbol, values);
        return result;
   }

   static void ShowCommands()
   {
    System.out.println("help (Show commands)");
    System.out.println("AD (Add Doctor to server)");
    System.out.println("AP (Add Pation to server)");
    System.out.println("FP (Find patient with ID)");
    System.out.println("Enter your Command:");   
   }
}

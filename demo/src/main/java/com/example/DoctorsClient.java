package com.example;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class DoctorsClient {
    private Socket socket; // Declare socket as an instance variable
    private DataOutputStream serverConnection; // Declare serverConnection as an instance variable
    private DataInputStream incomingMessages; // Declare incomingMessages as an instance variable
    private static boolean activeListenThread = true;

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

      // Read input from the user
      Scanner scanner = new Scanner(System.in);    
      System.out.println("Enter your message:");      
      while (true) {
         String userMessage = scanner.nextLine();

         if(userMessage == "Exit")
            break;

         client.serverConnection.writeUTF(userMessage);
      }

      
      
      
      //Close server
      client.serverConnection.writeUTF("exit");
      
      //Close open connections and threads
      scanner.close();
      client.socket.close();
      activeListenThread = false;
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

   public void AddDoctor(Doctor inputDoctor)
   {
   }
   public void AddPatient(Admission inputPatient)
   {
   }
   public void FindPatient(int ID)
   {
   }
   public void FindPatientWithName(String PatientName)
   {
   }
}

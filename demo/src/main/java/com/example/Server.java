package com.example;

import java.beans.Statement;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.persistence.internal.databaseaccess.DatabaseAccessor;

public class Server 
{
   //Connection
    static Socket clientSocket;
    static DataInputStream ingoingToServer;
    static DataOutputStream outgoingToClient;

    //Database
    static String dataPath = "jdbc:sqlite:Data/Data.sqlite";
    static Connection dataCon;

    public static void main(String[] args) throws IOException {
        //Connect to Database
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            // Get the InputStream for the database file
            InputStream is = DatabaseAccessor.class.getResourceAsStream("/Data.sqlite3");
            // Use the InputStream to create a Connection
            dataCon = DriverManager.getConnection("jdbc:sqlite:" + is);
            System.out.println("Connected to the database successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }

        ServerSocket serverSocket = new ServerSocket(4444);
        System.out.println("Waiting for the client request");

        while (true) {
            clientSocket = serverSocket.accept();
            ingoingToServer = new DataInputStream(clientSocket.getInputStream());
            outgoingToClient = new DataOutputStream(clientSocket.getOutputStream());

            // Loop to continuously read messages from the client
            String message;

            //Treat input
            while ((message = ingoingToServer.readUTF())!= null) {
                message.toLowerCase();
                System.out.println("Message Received: " + message);

                //Commands
                switch (message) {
                    case "doctor":
                        AddDoctor("Emil", 1, "Bone");
                        break;
                    case "find doctor":
                        DoctorsDepartment("Emil");
                        break;
                    default:
                        outgoingToClient.writeUTF("No Command found");
                        break;
                }

                // Termination condition: if the client sends "exit"
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            
            serverSocket.close();
            System.out.println("Shutting down Socket server!!");
        }
    }    

    static void AddDoctor(String name, int ID, String department)
    {
        //Getting data
        try
        {

        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    static void DoctorsDepartment(String name)
    {
        String department = "";

        //Getting data
        try
        {

        }
        catch(Exception e)
        {
            System.out.println(e);
        }

        //Write to client
        if (outgoingToClient!= null) {
            try {
                outgoingToClient.writeUTF(department);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void AddPatient()
    {

    }
}

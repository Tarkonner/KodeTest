package com.example;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.persistence.internal.databaseaccess.DatabaseAccessor;

public class Server 
{
   //Connection
    static Socket clientSocket;
    static DataInputStream ingoingToServer;
    static DataOutputStream outgoingToClient;

    //Database
    static String dataPath = "jdbc:sqlite:demo\\Database.sqlite";
    static Connection dataCon;

    public static void main(String[] args) throws IOException {
        //Connect to Database
        try {
            Class.forName("org.sqlite.JDBC");
            dataCon = DriverManager.getConnection("jdbc:sqlite:database.db");
            MakeTable();
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
            String sql = "INSERT INTO doctors(name, id, department) VALUES(?,?,?)";
            PreparedStatement statement = dataCon.prepareStatement(sql);
            statement.setString(1, name);
            statement.setInt(2, ID);
            statement.setString(3, department);
            statement.executeUpdate();
            System.out.println("Doctor added successfully.");
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
            String sql = "SELECT department FROM doctors WHERE name=?";
            PreparedStatement statement = dataCon.prepareStatement(sql);
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                department = rs.getString("department");
            }
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

    static void MakeTable()
    {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS doctors(name TEXT PRIMARY KEY, id INTEGER, department TEXT)";
            java.sql.Statement stmt = dataCon.createStatement();
            stmt.execute(sql); 
            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

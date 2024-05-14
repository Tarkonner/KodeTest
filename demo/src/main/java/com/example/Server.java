package com.example;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.ResultSet;

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

            System.out.println("Connected to the database successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }

        //Setup data
        try
        {
            //Clear
            ClearTable("admissions");
            ClearTable("doctor");
            //Make
            MakeDoctorTable();
            MakeAdmissionTable();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }


        //Server setup
        ServerSocket serverSocket = new ServerSocket(4444);
        System.out.println("Waiting for the client request");

        while (true) {
            //Wait for a client to connect
            clientSocket = serverSocket.accept();
            ingoingToServer = new DataInputStream(clientSocket.getInputStream());
            outgoingToClient = new DataOutputStream(clientSocket.getOutputStream());
            
            //Treat input
            String message;
            while ((message = ingoingToServer.readUTF())!= null) {
                System.out.println("Message Received: " + message);

                //Commands
                switch (message) {
                    case "doctor":
                        AddDoctor("Emil", 1, "Bone");
                        break;
                    case "find doctor":
                        DoctorsDepartment("Emil");
                        break;
                    case "patient":
                        String[] docs = {"Emil"};
                        AddPatient("Bone", "Emma", 123, docs);
                        break;
                    default:
                        outgoingToClient.writeUTF("No Command found");
                        break;
                }

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            //Closeing done
            //Database
            try
            {
                ClearTable("doctor");
                ClearTable("admissions");
                dataCon.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            //Sockets
            serverSocket.close();
            System.out.println("Shutting down server");
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

    static void AddPatient(String department, String patiantName, int socialSecurityNumber, String[] assiantDoctors)
    {
        try
        {
            // Assuming you have a table structure that can accommodate the data
            String sql = "INSERT INTO admissions(department, patient_name, social_security_number, doctors_assiant) VALUES(?,?,?,?)";
            // Insert the medical journal data
            PreparedStatement pstmt = dataCon.prepareStatement(sql);
            pstmt.setString(1, department);
            pstmt.setString(2, patiantName);
            pstmt.setInt(3, socialSecurityNumber);
            
            // Join the list of doctors into a single string
            String doctorsAssiantString = String.join("", assiantDoctors);
            
            // Set the joined string as the fourth parameter in the SQL statement
            pstmt.setString(4, doctorsAssiantString);
            pstmt.executeUpdate();

            System.out.println("Data saved successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static void MakeDoctorTable()
    {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS doctors(name TEXT PRIMARY KEY, id INTEGER, department TEXT)";
            Statement stmt = dataCon.createStatement();
            stmt.execute(sql); 
            System.out.println("Doctor Table made.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static void ClearTable(String tableName)
    {
        try {
            String sql = "DELETE FROM " + tableName;
            Statement stmt = dataCon.createStatement();
            stmt.execute(sql);
            System.out.println("All rows deleted from table_name.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static void MakeAdmissionTable()
    {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS admissions(department TEXT, patient_name TEXT, social_security_number INTEGER, doctors_assiant TEXT);";
            Statement stmt = dataCon.createStatement();
            stmt.execute(sql); 
            System.out.println("Admission Table made.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

package com.example;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

    private static String splitSymbol = "£";

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

                String[] splitMessage = message.split(splitSymbol);

                //Commands
                switch (splitMessage[0].toLowerCase()) {
                    case "ad": //Add doctor
                        int ID = Integer.parseInt(splitMessage[2]);
                        AddDoctor(splitMessage[1], ID, splitMessage[3]);
                        break;
                    case "fd": //Find Doctor
                        DoctorsDepartment(splitMessage[1]);
                        break;
                    case "ap": //Add Patient
                        List<String> docsNames = new ArrayList<>();
                        //Add doctor
                        for(int i = 4; i < splitMessage.length; i++)
                        {
                            docsNames.add(splitMessage[i]);
                        }
                        int SCN = Integer.parseInt(splitMessage[2]);
                        AddPatient(splitMessage[1], SCN, splitMessage[3], docsNames);
                        break;
                    case "fpwi": //Find patient with ID
                        int scn = Integer.parseInt(splitMessage[1]);
                        int doctorID = Integer.parseInt(splitMessage[2]);
                        LookUpPatient(scn, doctorID);
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
//#region ADD
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



    static void AddPatient(String patiantName, int socialSecurityNumber, String department, List<String> assiantDoctors)
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
    //#endregion

    //#region Find things in database
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

    static void LookUpPatient(int socialSecurityNumber, int doctorsID)
    {
        String message = "";

        // Fetch the doctor's name
        String doctorName = ""; 
        String doctorsDepartment = "";
        try {
            //Get name
            String SQL_GetDoctorsName = "SELECT name, department FROM doctors WHERE ID=?";
            PreparedStatement statement = dataCon.prepareStatement(SQL_GetDoctorsName);
            statement.setInt(1, doctorsID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                doctorName = rs.getString("name");
                doctorsDepartment = rs.getString("department");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        //Check
        if(doctorName.isBlank())
        {
            message = "No doctor with ID";
        }
        else
        {
            //Check if the doctor can see journal
            try {
                //Get patient department
                String department = "";
                String SQL_GetPatientDepartment = "SELECT department FROM admissions WHERE social_security_number=? AND doctors_assiant=?";
                PreparedStatement deptStatement = dataCon.prepareStatement(SQL_GetPatientDepartment);
                ResultSet deptResult = deptStatement.executeQuery();
                if (deptResult.next())
                    department = deptResult.getString("department");

                if(department != doctorsDepartment)
                {
                    message = "Doctor not part of department of patient";
                }
                else
                {
                    //See if there is a doctor there match the info.
                    String SQL_CheckAssistantDoctor = "SELECT COUNT(*) FROM admissions WHERE social_security_number=? AND doctors_assiant=?";
                    PreparedStatement checkStatement = dataCon.prepareStatement(SQL_CheckAssistantDoctor);
                    checkStatement.setInt(1, socialSecurityNumber);
                    checkStatement.setString(2, doctorName); 
                    
                    
                    ResultSet countResult = checkStatement.executeQuery();
                    if (countResult.next()) {
                        int count = countResult.getInt(1);
                        if (count > 0) {
                            message = "An admission with social security number " + socialSecurityNumber + " has an assistant doctor with the name " + doctorName + ".";
                        } else {
                            message = doctorName + "has no patient with that security number";
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        //Send message to client
        if (outgoingToClient!= null) {
            try {
                outgoingToClient.writeUTF(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //#endregion

    //#region Tabels
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
    //#endregion
}

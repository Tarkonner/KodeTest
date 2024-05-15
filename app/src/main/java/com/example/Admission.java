package com.example;

import java.util.ArrayList;
import java.util.List;

public class Admission {
    public String department;
    public List<String> doctorsAssiant = new ArrayList<String> (); 
    public MedicalJournal medicalJournal;


    public Admission(String department,  String pationsName, int socialSecurityMumber, List<String> doctorsAssiant)
    {
        this.department = department;
        this.medicalJournal = new MedicalJournal(pationsName, socialSecurityMumber);
        this.doctorsAssiant = doctorsAssiant;
    }
}

package org.example;

import Service.JobOfferService;
import Service.CandidatureService;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        JobOfferService jobService = new JobOfferService();
        CandidatureService candidatureService = new CandidatureService();

        System.out.println("Login as:");
        System.out.println("1 - Recruiter");
        System.out.println("2 - Candidate");

        int role = sc.nextInt();
        sc.nextLine();

        /*if (role == 1) {
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Password: ");
            String pwd = sc.nextLine();

            try {
                Integer recruiterId = recruiterService.login(email, pwd);

                if (recruiterId == null) {
                    System.out.println("Login failed.");
                    return;
                }

                boolean running = true;
                while (running) {

                    System.out.println("\n1 - Add Job");
                    System.out.println("2 - Delete Job");
                    System.out.println("3 - Update Job");
                    System.out.println("4 - Show Jobs");
                    System.out.println("0 - Exit");

                    int choice = sc.nextInt();
                    sc.nextLine();

                    switch (choice) {

                        case 1:
                            System.out.print("Title: ");
                            String title = sc.nextLine();
                            System.out.print("Description: ");
                            String desc = sc.nextLine();
                            System.out.print("Skills: ");
                            String skills = sc.nextLine();

                            JobOffer job = new JobOffer(title, desc, skills, recruiterId);
                            jobService.addJobOffer(job);
                            System.out.println("Job added.");
                            break;

                        case 2:
                            System.out.print("Job ID: ");
                            int deleteId = sc.nextInt();
                            jobService.deleteJobOffer(deleteId);
                            System.out.println("Job deleted.");
                            break;

                        case 3:
                            System.out.print("Job ID: ");
                            int updateId = sc.nextInt();
                            sc.nextLine();

                            System.out.print("New Title: ");
                            String newTitle = sc.nextLine();
                            System.out.print("New Description: ");
                            String newDesc = sc.nextLine();
                            System.out.print("New Skills: ");
                            String newSkills = sc.nextLine();

                            JobOffer updated = new JobOffer();
                            updated.setId(updateId);
                            updated.setTitle(newTitle);
                            updated.setDescription(newDesc);
                            updated.setRequiredSkills(newSkills);

                            jobService.updateJobOffer(updated);
                            System.out.println("Job updated.");
                            break;

                        case 4:
                            for (JobOffer j : jobService.getJobOffers()) {
                                System.out.println(j);
                            }
                            break;

                        case 0:
                            running = false;
                            break;
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (role == 2) {
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Password: ");
            String pwd = sc.nextLine();

            try {
                Integer candidateId = candidatService.login(email, pwd);

                if (candidateId == null) {
                    System.out.println("Login failed.");
                    return;
                }

                boolean running = true;
                while (running) {

                    System.out.println("\n1 - Apply to Job");
                    System.out.println("2 - Delete Candidature");
                    System.out.println("3 - Update Status");
                    System.out.println("4 - Show Candidatures");
                    System.out.println("0 - Exit");

                    int choice = sc.nextInt();
                    sc.nextLine();

                    switch (choice) {

                        case 1:
                            for (JobOffer j : jobService.getJobOffers()) {
                                System.out.println(j);
                            }

                            System.out.print("Job ID: ");
                            int jobId = sc.nextInt();
                            sc.nextLine();

                            java.sql.Date date =
                                    new java.sql.Date(System.currentTimeMillis());

                            Candidature c =
                                    new Candidature(candidateId, jobId, date, "pending");

                            candidatureService.addCandidature(c);
                            System.out.println("Applied successfully.");
                            break;

                        case 2:
                            System.out.print("Candidature ID: ");
                            int deleteC = sc.nextInt();
                            candidatureService.deleteCandidature(deleteC);
                            System.out.println("Deleted.");
                            break;

                        case 3:
                            System.out.print("Candidature ID: ");
                            int updateC = sc.nextInt();
                            sc.nextLine();

                            System.out.print("New Status: ");
                            String status = sc.nextLine();

                            candidatureService.updateStatus(updateC, status);
                            System.out.println("Status updated.");
                            break;

                        case 4:
                            for (Candidature cand :
                                    candidatureService.getCandidatures()) {
                                System.out.println(cand);
                            }
                            break;

                        case 0:
                            running = false;
                            break;
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }*/
    }
}

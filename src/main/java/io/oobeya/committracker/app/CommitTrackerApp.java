package io.oobeya.committracker.app;

import io.oobeya.committracker.controller.CommitController;
import io.oobeya.committracker.service.*;

import java.util.InputMismatchException;
import java.util.Scanner;

public class CommitTrackerApp {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            int choice = getUserChoice(scanner);

            // Continue prompting until a valid choice is entered
            while (choice < 1 || choice > 4) {
                System.out.println("Geçersiz seçim! Lütfen 1-4 arasında bir değer giriniz.");
                choice = getUserChoice(scanner);
            }

            String owner = getInput(scanner, "Kullanıcı Adı (Owner) Giriniz: ");
            String repo = getInput(scanner, "Repo Adı Giriniz: ");
            String accessToken = getInput(scanner, "Access Token Giriniz (Opsiyonel): ");

            VCSService vcsService = getVCSService(choice, accessToken);
            if (vcsService == null) {
                System.out.println("Geçersiz seçim!");
                return;
            }

            CommitController commitController = new CommitController(vcsService);
            commitController.displayCommits(owner, repo);
        } catch (InputMismatchException e) {
            System.out.println("Lütfen geçerli bir sayı girin.");
        }
    }

    private static int getUserChoice(Scanner scanner) {
        int choice = -1;
        try {
            System.out.println("Veri çekmek istediğiniz platformu seçin:");
            System.out.println("1. GitHub");
            System.out.println("2. GitLab");
            System.out.println("3. Azure DevOps");
            System.out.println("4. Bitbucket");
            System.out.print("Seçiminiz (1-4): ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character
        } catch (InputMismatchException e) {
            System.out.println("Geçersiz giriş! Lütfen 1-4 arasında bir sayı girin.");
            scanner.nextLine(); // Consume invalid input
        }
        return choice;
    }

    private static String getInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static VCSService getVCSService(int choice, String accessToken) {
        switch (choice) {
            case 1:
                return new GitHubService(accessToken);
            case 2:
                return new GitLabService(accessToken);
            case 3:
                return new AzureDevOpsService(accessToken);
            case 4:
                return new BitbucketService(accessToken);
            default:
                return null;
        }
    }
}
package io.oobeya.committracker.app;

import io.oobeya.committracker.controller.CommitController;
import io.oobeya.committracker.service.*;

import java.util.Scanner;

public class CommitTrackerApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Kullanıcıdan VCS seçimi iste
        System.out.println("Veri çekmek istediğiniz platformu seçin:");
        System.out.println("1. GitHub");
        System.out.println("2. GitLab");
        System.out.println("3. Azure DevOps");
        System.out.println("4. Bitbucket");
        System.out.print("Seçiminiz (1-4): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Enter tuşu için

        System.out.print("Kullanıcı Adı (Owner) Giriniz: ");
        String owner = scanner.nextLine();

        System.out.print("Repo Adı Giriniz: ");
        String repo = scanner.nextLine();

        System.out.print("Access Token Giriniz (Opsiyonel): ");
        String accessToken = scanner.nextLine();

        VCSService vcsService;
        switch (choice) {
            case 1:
                vcsService = new GitHubService(accessToken);
                break;
            case 2:
                vcsService = new GitLabService(accessToken);
                break;
            case 3:
                vcsService = new AzureDevOpsService(accessToken);
                break;
            case 4:
                vcsService = new BitbucketService(accessToken);
                break;
            default:
                System.out.println("Geçersiz seçim!");
                return;
        }

        CommitController commitController = new CommitController(vcsService);
        commitController.displayCommits(owner, repo);
    }
}
package io.oobeya.committracker.app;

import io.oobeya.committracker.controller.CommitController;
import io.oobeya.committracker.dto.CommitResponse;
import io.oobeya.committracker.service.*;
import io.oobeya.committracker.service.github.GitHubCommitService;
import io.oobeya.committracker.service.github.GitHubIntegrationService;
import io.oobeya.committracker.service.github.parser.GitHubCommitParser;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.InputMismatchException;
import java.util.Scanner;

@SpringBootApplication
@ComponentScan(basePackages = "io.oobeya.committracker")
@EntityScan(basePackages = "io.oobeya.committracker.dto")
@EnableJpaRepositories(basePackages = "io.oobeya.committracker.repository")
public class CommitTrackerApp {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            int choice = getUserChoice(scanner);

            while (choice < 1 || choice > 4) {
                System.out.println("Geçersiz seçim! Lütfen 1-4 arasında bir değer giriniz.");
                choice = getUserChoice(scanner);
            }

            String owner = getInput(scanner, "Kullanıcı Adı (Owner) Giriniz: ");
            String repo = getInput(scanner, "Repo Adı Giriniz: ");
            String accessToken = getInput(scanner, "Access Token Giriniz (Opsiyonel): ");

            // Doğru CommitService nesnesi oluşturma
            CommitService commitService = getCommitService(choice, accessToken);
            if (commitService == null) {
                System.out.println("Geçersiz seçim!");
                return;
            }

            CommitController commitController = new CommitController((CommitService) commitService);

            // Tüm commitleri al ve her bir commit için detayları göster
            commitController.getCommits(owner, repo).forEach(commitResponse -> {
                System.out.println("========================================");
                System.out.println("Commit SHA: " + commitResponse.getSha());
                System.out.println("Author: " + commitResponse.getAuthor());
                System.out.println("Date: " + commitResponse.getDate());
                System.out.println("Message: " + commitResponse.getMessage());

                // Commit detaylarını al
                CommitResponse commitDetails = ((CommitService) commitService).getCommitDetails(owner, repo, commitResponse.getSha());

                if (commitDetails != null) {
                    if (!commitDetails.getFiles().isEmpty()) {
                        System.out.println("\nChanged Files:");
                        int fileIndex = 1;
                        for (var file : commitDetails.getFiles()) {
                            System.out.println(fileIndex++ + ". File: " + file.getFileName());
                            System.out.println("   - Added Lines: " + file.getAdditions());
                            System.out.println("   - Deleted Lines: " + file.getDeletions());
                        }
                    } else {
                        System.out.println("Bu committe değiştirilen dosya bulunamadı.");
                    }
                } else {
                    System.out.println("Geçersiz SHA veya commit detayları bulunamadı.");
                }

                System.out.println("----------------------------------------\n");
            });

        } catch (InputMismatchException e) {
            System.out.println("Lütfen geçerli bir sayı girin.");
        }
    }

    public static int getUserChoice(Scanner scanner) {
        int choice = -1;
        try {
            System.out.println("Veri çekmek istediğiniz platformu seçin:");
            System.out.println("1. GitHub");
            System.out.println("2. GitLab");
            System.out.println("3. Azure DevOps");
            System.out.println("4. Bitbucket");
            System.out.print("Seçiminiz (1-4): ");
            choice = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Geçersiz giriş! Lütfen 1-4 arasında bir sayı girin.");
            scanner.nextLine();
        }
        return choice;
    }

    public static String getInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static CommitService getCommitService(int choice, String accessToken) {
        VCSIntegrationService integrationService;
        CommitParserService parserService;

        switch (choice) {
            case 1: // GitHub
                integrationService = new GitHubIntegrationService(accessToken);
                parserService = new GitHubCommitParser();
                return new GitHubCommitService(integrationService, parserService);
            default:
                return null;
        }
    }
}
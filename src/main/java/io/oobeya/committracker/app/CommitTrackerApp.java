package io.oobeya.committracker.app;

import io.oobeya.committracker.controller.CommitController;

import java.util.Scanner;

public class CommitTrackerApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("GitHub Kullanıcı Adı Giriniz: ");
        String owner = scanner.nextLine();

        System.out.print("Repo Adı Giriniz: ");
        String repo = scanner.nextLine();

        System.out.print("Access Token Giriniz: ");
        String accessToken = scanner.nextLine();

        CommitController commitController = new CommitController(accessToken);
        commitController.displayCommits(owner, repo);
    }
}
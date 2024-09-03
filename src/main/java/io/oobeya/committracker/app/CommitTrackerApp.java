package io.oobeya.committracker.app;

import io.oobeya.committracker.controller.CommitController;

public class CommitTrackerApp {
    public static void main(String[] args) {
        CommitController commitController = new CommitController();
        commitController.displayCommits();
    }
}
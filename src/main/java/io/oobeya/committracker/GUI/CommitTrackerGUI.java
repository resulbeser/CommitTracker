package io.oobeya.committracker.GUI;

import io.oobeya.committracker.dto.CommitResponse;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import io.oobeya.committracker.controller.CommitController;
import io.oobeya.committracker.service.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CommitTrackerGUI extends Application {

    private TableView<CommitResponse> tableView;
    private TextField userTextField;
    private TextField repoTextField;
    private TextField tokenTextField;

    @Override
    public void start(Stage primaryStage) {
        // Logo yükle
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/CommitTrackerLogo.png")));
        logoView.setFitWidth(150);
        logoView.setPreserveRatio(true);

        // Tablo oluştur
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

// SHA sütunu
        TableColumn<CommitResponse, String> shaCol = new TableColumn<>("SHA");
        shaCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSha()));
        shaCol.setMinWidth(100);
        shaCol.setCellFactory(tc -> {
            TableCell<CommitResponse, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setAlignment(Pos.CENTER_LEFT);
                }
            };
            return cell;
        });

// Author sütunu
        TableColumn<CommitResponse, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        authorCol.setMinWidth(120);
        authorCol.setCellFactory(tc -> {
            TableCell<CommitResponse, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setAlignment(Pos.CENTER_LEFT);
                }
            };
            return cell;
        });

// Date sütunu
        TableColumn<CommitResponse, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDate()));
        dateCol.setMinWidth(130);
        dateCol.setCellFactory(tc -> {
            TableCell<CommitResponse, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setAlignment(Pos.CENTER_LEFT);
                }
            };
            return cell;
        });

// Message sütunu
        TableColumn<CommitResponse, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMessage()));
        messageCol.setMinWidth(250);
        messageCol.setCellFactory(tc -> {
            TableCell<CommitResponse, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setAlignment(Pos.CENTER_LEFT);
                }
            };
            return cell;
        });

// Sütunları tabloya ekle
        tableView.getColumns().addAll(shaCol, authorCol, dateCol, messageCol);
        tableView.setPrefHeight(250);

        // Platform seçimi
        Label platformLabel = new Label("Veri çekmek istediğiniz platformu seçin:");
        ComboBox<String> platformComboBox = new ComboBox<>();
        platformComboBox.getItems().addAll("GitHub", "GitLab", "Azure DevOps", "Bitbucket");

        // Giriş alanları
        Label userLabel = new Label("Kullanıcı Adı (Owner) Giriniz:");
        userTextField = new TextField();

        Label repoLabel = new Label("Repo Adı Giriniz:");
        repoTextField = new TextField();

        Label tokenLabel = new Label("Access Token Giriniz (Opsiyonel):");
        tokenTextField = new TextField();

        // Verileri Çek butonu
        Button fetchCommitsButton = new Button("Verileri Çek");
        fetchCommitsButton.setStyle("-fx-background-color: white; -fx-text-fill: #008080; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        fetchCommitsButton.setOnAction(e -> {
            String owner = userTextField.getText();
            String repo = repoTextField.getText();
            String accessToken = tokenTextField.getText();
            int choice = platformComboBox.getSelectionModel().getSelectedIndex() + 1;

            if (owner.isEmpty() || repo.isEmpty()) {
                showAlert("Hata", "Eksik Bilgi", "Kullanıcı adı ve repo adı boş bırakılamaz.");
                return;
            }

            if (choice < 1 || choice > 4) {
                showAlert("Hata", "Geçersiz Seçim", "Lütfen geçerli bir platform seçin.");
                return;
            }

            VCSService vcsService = getVCSService(choice, accessToken);
            if (vcsService == null) {
                showAlert("Hata", "Geçersiz Seçim", "Desteklenmeyen platform.");
                return;
            }

            CommitController commitController = new CommitController(vcsService);
            var commits = commitController.getCommits(owner, repo);

            if (commits == null || commits.isEmpty()) {
                tokenTextField.clear();
                tokenTextField.setPromptText("⚠ Access Token gerekli olabilir");
                tokenTextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-prompt-text-fill: red;");
                return;
            }

            tokenTextField.setStyle("");
            tokenTextField.setPromptText("Access Token Giriniz (Opsiyonel):");
            tableView.getItems().setAll(commits);
        });

        // Kaydet Butonu
        Button saveButton = new Button("Verileri Kaydet");
        saveButton.setStyle("-fx-background-color: white; -fx-text-fill: #008080; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        saveButton.setOnAction(e -> saveToFile());

        // Arayüz düzeni
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #71d9bb; -fx-padding: 20px;");
        layout.setAlignment(Pos.TOP_CENTER);

        layout.getChildren().addAll(
                logoView,
                platformLabel, platformComboBox,
                userLabel, userTextField,
                repoLabel, repoTextField,
                tokenLabel, tokenTextField,
                fetchCommitsButton, saveButton,
                tableView
        );

        Scene scene = new Scene(layout, 600, 650);
        primaryStage.setTitle("Git Repo Uygulaması");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void saveToFile() {
        try {
            String username = userTextField.getText().trim();
            String repoName = repoTextField.getText().trim();
            if (username.isEmpty() || repoName.isEmpty()) {
                showAlert("Hata", "Eksik Bilgi", "Kullanıcı adı veya repo adı boş olamaz.");
                return;
            }

            String fileName = username + repoName + ".txt";
            String userDesktop = System.getProperty("user.home") + "\\Desktop\\" + fileName;

            File file = new File(userDesktop);
            if (!file.exists()) file.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (CommitResponse commit : tableView.getItems()) {
                    writer.write("========================================\n");
                    writer.write("Commit SHA: " + commit.getSha() + "\n");
                    writer.write("Author: " + commit.getAuthor() + "\n");
                    writer.write("Date: " + commit.getDate() + "\n");
                    writer.write("Message: " + commit.getMessage() + "\n");

                    if (!commit.getFiles().isEmpty()) {
                        writer.write("\nChanged Files:\n");
                        int i = 1;
                        for (var fileChange : commit.getFiles()) {
                            writer.write(i++ + ". File: " + fileChange.getFileName() + "\n");
                            writer.write("   - Added Lines: " + fileChange.getAdditions() + "\n");
                            writer.write("   - Deleted Lines: " + fileChange.getDeletions() + "\n");
                        }
                    }
                    writer.write("----------------------------------------\n");
                }
            }

            System.out.println("Veri başarıyla kaydedildi: " + userDesktop);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Dosya kaydedilirken bir hata oluştu.");
        }
    }

    private void showAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VCSService getVCSService(int choice, String accessToken) {
        return switch (choice) {
            case 1 -> new GitHubService(accessToken);
            case 2 -> new GitLabService(accessToken);
            case 3 -> new AzureDevOpsService(accessToken);
            case 4 -> new BitbucketService(accessToken);
            default -> null;
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}

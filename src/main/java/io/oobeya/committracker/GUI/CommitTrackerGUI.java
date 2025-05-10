package io.oobeya.committracker.GUI;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import io.oobeya.committracker.controller.CommitController;
import io.oobeya.committracker.service.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CommitTrackerGUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Başlık
        Label titleLabel = new Label("CommitTracker");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Platform Seçimi
        Label platformLabel = new Label("Veri çekmek istediğiniz platformu seçin:");
        ComboBox<String> platformComboBox = new ComboBox<>();
        platformComboBox.getItems().addAll("GitHub", "GitLab", "Azure DevOps", "Bitbucket");

        // Kullanıcı Adı Girişi
        Label userLabel = new Label("Kullanıcı Adı (Owner) Giriniz:");
        TextField userTextField = new TextField();

        // Repo Adı Girişi
        Label repoLabel = new Label("Repo Adı Giriniz:");
        TextField repoTextField = new TextField();

        // Access Token Girişi (opsiyonel)
        Label tokenLabel = new Label("Access Token Giriniz (Opsiyonel):");
        TextField tokenTextField = new TextField();

        // Çıktıyı Görüntülemek İçin TextArea
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setStyle("-fx-background-radius: 5px; -fx-border-radius: 5px;");

        // Kaydetme Butonu
        Button saveButton = new Button("Verileri Kaydet");
        saveButton.setStyle("-fx-background-color: white; -fx-text-fill: #008080; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        saveButton.setOnAction(e -> saveToFile(outputArea.getText()));

        // Çalıştırma Butonu
        Button fetchCommitsButton = new Button("Verileri Çek");
        fetchCommitsButton.setStyle("-fx-background-color: white; -fx-text-fill: #008080; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        fetchCommitsButton.setOnAction(e -> {
            String owner = userTextField.getText();
            String repo = repoTextField.getText();
            String accessToken = tokenTextField.getText();
            int choice = platformComboBox.getSelectionModel().getSelectedIndex() + 1;

            if (repo.equals("private") && accessToken.isEmpty()) {
                showAlert("Hata", "Access Token Girmelisiniz", "Private repo için access token girmeniz gerekmektedir.");
                return;
            }

            if (choice < 1 || choice > 4) {
                outputArea.setText("Geçersiz seçim!");
                return;
            }

            VCSService vcsService = getVCSService(choice, accessToken);
            if (vcsService == null) {
                outputArea.setText("Geçersiz seçim!");
                return;
            }

            CommitController commitController = new CommitController(vcsService);
            StringBuilder output = new StringBuilder();

            commitController.getCommits(owner, repo).forEach(commitResponse -> {
                output.append("========================================\n");
                output.append("Commit SHA: ").append(commitResponse.getSha()).append("\n");
                output.append("Author: ").append(commitResponse.getAuthor()).append("\n");
                output.append("Date: ").append(commitResponse.getDate()).append("\n");
                output.append("Message: ").append(commitResponse.getMessage()).append("\n");

                if (!commitResponse.getFiles().isEmpty()) {
                    output.append("\nChanged Files:\n");
                    int fileIndex = 1;
                    for (var file : commitResponse.getFiles()) {
                        output.append(fileIndex++).append(". File: ").append(file.getFileName()).append("\n");
                        output.append("   - Added Lines: ").append(file.getAdditions()).append("\n");
                        output.append("   - Deleted Lines: ").append(file.getDeletions()).append("\n");
                    }
                }
                output.append("----------------------------------------\n");
            });

            outputArea.setText(output.toString());
        });

        // Görsel düzenlemeler
        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #71d9bb; -fx-padding: 20px;");
        layout.setAlignment(Pos.TOP_CENTER);

        layout.getChildren().addAll(
                titleLabel,
                platformLabel, platformComboBox,
                userLabel, userTextField,
                repoLabel, repoTextField,
                tokenLabel, tokenTextField,
                fetchCommitsButton, saveButton,
                outputArea
        );

        Scene scene = new Scene(layout, 550, 600);
        primaryStage.setTitle("Git Repo Uygulaması");
        primaryStage.setScene(scene);
        primaryStage.show();
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

    private void saveToFile(String content) {
        try {
            String userDesktop = System.getProperty("user.home") + "\\Desktop\\commit_data.txt";
            File file = new File(userDesktop);
            if (!file.exists()) file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
            }
            System.out.println("Veri başarıyla kaydedildi: " + userDesktop);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Dosya kaydedilirken bir hata oluştu.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

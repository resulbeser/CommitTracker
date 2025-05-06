package io.oobeya.committracker.GUI;

import javafx.application.Application;
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

        // Kaydetme Butonu
        Button saveButton = new Button("Verileri Kaydet");
        saveButton.setOnAction(e -> {
            String content = outputArea.getText();
            saveToFile(content); // Dosyaya kaydetme fonksiyonunu çağırıyoruz
        });

        // Çalıştırma Butonu
        Button fetchCommitsButton = new Button("Verileri Çek");
        fetchCommitsButton.setOnAction(e -> {
            String owner = userTextField.getText();
            String repo = repoTextField.getText();
            String accessToken = tokenTextField.getText();
            int choice = platformComboBox.getSelectionModel().getSelectedIndex() + 1;

            // Private repo ise ve token yoksa uyarı
            if (repo.equals("private") && accessToken.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hata");
                alert.setHeaderText("Access Token Girmelisiniz");
                alert.setContentText("Private repo için access token girmeniz gerekmektedir.");
                alert.showAndWait();
                return;
            }

            // Platform ve Seçim Kontrolü
            if (choice < 1 || choice > 4) {
                outputArea.setText("Geçersiz seçim!");
                return;
            }

            // VCS Servisini Al
            VCSService vcsService = getVCSService(choice, accessToken);
            if (vcsService == null) {
                outputArea.setText("Geçersiz seçim!");
                return;
            }

            // Commit Verilerini Çekme
            CommitController commitController = new CommitController(vcsService);
            StringBuilder output = new StringBuilder();

            commitController.getCommits(owner, repo).forEach(commitResponse -> {
                output.append("========================================\n");
                output.append("Commit SHA: " + commitResponse.getSha() + "\n");
                output.append("Author: " + commitResponse.getAuthor() + "\n");
                output.append("Date: " + commitResponse.getDate() + "\n");
                output.append("Message: " + commitResponse.getMessage() + "\n");

                if (!commitResponse.getFiles().isEmpty()) {
                    output.append("\nChanged Files:\n");
                    int fileIndex = 1;
                    for (var file : commitResponse.getFiles()) {
                        output.append(fileIndex++ + ". File: " + file.getFileName() + "\n");
                        output.append("   - Added Lines: " + file.getAdditions() + "\n");
                        output.append("   - Deleted Lines: " + file.getDeletions() + "\n");
                    }
                }
                output.append("----------------------------------------\n");
            });

            // Sonuçları TextArea'ya Göster
            outputArea.setText(output.toString());
        });

        // Layout (GUI Bileşenlerini Yerleştir)
        VBox layout = new VBox(10);
        layout.getChildren().addAll(platformLabel, platformComboBox, userLabel, userTextField, repoLabel, repoTextField,
                tokenLabel, tokenTextField, fetchCommitsButton, saveButton, outputArea);

        // Scene (GUI'nin Temel Yapısı)
        Scene scene = new Scene(layout, 500, 400);
        primaryStage.setTitle("Git Repo Uygulaması");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // VCSService'e Bağlanma
    private VCSService getVCSService(int choice, String accessToken) {
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

    // Dosya Kaydetme İşlemi
    private void saveToFile(String content) {
        try {
            String userDesktop = System.getProperty("user.home") + "\\Desktop\\commit_data.txt"; // Masaüstü yolu
            File file = new File(userDesktop);

            if (!file.exists()) {
                file.createNewFile(); // Eğer dosya yoksa oluştur
            }

            // Veriyi dosyaya yazma
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content); // TextArea'daki metni yazıyoruz
            }
            System.out.println("Veri başarıyla kaydedildi: " + userDesktop);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Dosya kaydedilirken bir hata oluştu.");
        }
    }

    // JavaFX Uygulamasını Başlatma
    public static void main(String[] args) {
        launch(args);
    }
}

package committracker.GUI;

import committracker.dto.CommitResponse;
import committracker.controller.CommitController;
import committracker.service.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.util.List;

public class CommitTrackerGUI extends Application {

    private ComboBox<String> platformComboBox;
    private TextField ownerField;
    private TextField repoField;
    private TextField tokenField;
    private TextArea resultArea;
    private Button fetchButton;
    private ProgressIndicator progressIndicator;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Commit Tracker - Git Platform Manager");

        // Pencere ikonu ayarla (varsa)
        try {
            Image icon = new Image(getClass().getResourceAsStream("/CommitTrackerLogo.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Pencere ikonu yüklenemedi");
        }

        // Ana layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Üst kısım - Şeffaf logo ve başlık
        VBox headerSection = createHeaderSection();
        root.setTop(headerSection);

        // Orta kısım - İki sütunlu SplitPane
        SplitPane mainContent = createMainContent();
        root.setCenter(mainContent);

        // Scene oluştur ve CSS yükle
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private VBox createHeaderSection() {
        VBox headerBox = new VBox();
        headerBox.getStyleClass().add("header-section");

        // Logo - şeffaf arkaplan
        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/CommitTrackerLogo.png")));
            logoView.setFitHeight(100);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            logoView.getStyleClass().add("logo");

            // Logo hover efekti
            logoView.setOnMouseEntered(e -> {
                logoView.setScaleX(1.1);
                logoView.setScaleY(1.1);
            });
            logoView.setOnMouseExited(e -> {
                logoView.setScaleX(1.0);
                logoView.setScaleY(1.0);
            });

            headerBox.getChildren().add(logoView);
        } catch (Exception e) {
            // Logo yoksa text başlık
            Label titleLabel = new Label("🚀 COMMIT TRACKER");
            titleLabel.getStyleClass().add("app-title");
            headerBox.getChildren().add(titleLabel);
        }

        Label subtitleLabel = new Label("🔍 Git Platform Commit Manager");
        subtitleLabel.getStyleClass().add("app-subtitle");

        Label versionLabel = new Label("v2.0 - Modern Edition");
        versionLabel.getStyleClass().add("app-version");

        headerBox.getChildren().addAll(subtitleLabel, versionLabel);
        return headerBox;
    }

    private SplitPane createMainContent() {
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("split-pane");

        // Sol panel - Form alanı
        VBox leftPanel = createLeftPanel();

        // Sağ panel - Sonuçlar alanı
        VBox rightPanel = createRightPanel();

        splitPane.getItems().addAll(leftPanel, rightPanel);

        // Sol panelin sabit genişliği
        splitPane.setDividerPositions(0.35);
        SplitPane.setResizableWithParent(leftPanel, false);

        return splitPane;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        leftPanel.getStyleClass().add("left-panel");

        // Platform seçimi
        VBox platformSection = createFormSection("🌐 Platform Seçin:");
        platformComboBox = new ComboBox<>();
        platformComboBox.getStyleClass().add("modern-combo-box");
        platformComboBox.getItems().addAll("GitHub", "GitLab", "Azure DevOps", "Bitbucket");
        platformComboBox.setValue("GitHub");
        platformSection.getChildren().add(platformComboBox);

        // Owner field
        VBox ownerSection = createFormSection("👤 Kullanıcı Adı (Owner):");
        ownerField = new TextField();
        ownerField.getStyleClass().add("modern-text-field");
        ownerField.setPromptText("Örn: microsoft");
        ownerSection.getChildren().add(ownerField);

        // Repo field
        VBox repoSection = createFormSection("📁 Repository Adı:");
        repoField = new TextField();
        repoField.getStyleClass().add("modern-text-field");
        repoField.setPromptText("Örn: vscode");
        repoSection.getChildren().add(repoField);

        // Token field
        VBox tokenSection = createFormSection("🔑 Access Token (Opsiyonel):");
        tokenField = new TextField();
        tokenField.getStyleClass().add("modern-text-field");
        tokenField.setPromptText("Private repository erişimi için token");
        tokenSection.getChildren().add(tokenField);

        // Fetch button
        fetchButton = new Button("🚀 Commit'leri Getir");
        fetchButton.getStyleClass().add("primary-button");
        fetchButton.setOnAction(e -> fetchCommits());

        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.getStyleClass().add("modern-progress");
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(40, 40);

        leftPanel.getChildren().addAll(
            platformSection, ownerSection, repoSection, tokenSection,
            fetchButton, progressIndicator
        );

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.getStyleClass().add("right-panel");

        // Başlık
        Label resultLabel = new Label("📊 Commit Sonuçları");
        resultLabel.getStyleClass().add("results-title");

        // Sonuç alanı - ScrollPane içinde
        resultArea = new TextArea();
        resultArea.getStyleClass().add("results-text-area");
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setText("🎯 Commit bilgilerini görüntülemek için:\n\n" +
                          "1️⃣ Sol panelden bir platform seçin (GitHub, GitLab, vb.)\n" +
                          "2️⃣ Kullanıcı adını girin (örn: microsoft)\n" +
                          "3️⃣ Repository adını girin (örn: vscode)\n" +
                          "4️⃣ Private repo ise Access Token girin\n" +
                          "5️⃣ '🚀 Commit'leri Getir' butonuna tıklayın\n\n" +
                          "💡 İpucu: Private repository'ler için mutlaka Access Token gereklidir!");

        ScrollPane scrollPane = new ScrollPane(resultArea);
        scrollPane.getStyleClass().add("results-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // VBox constraints - sağ panel expand olsun
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        rightPanel.getChildren().addAll(resultLabel, scrollPane);
        return rightPanel;
    }

    private VBox createFormSection(String labelText) {
        VBox section = new VBox();
        section.getStyleClass().add("form-section");

        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        section.getChildren().add(label);
        return section;
    }

    private void fetchCommits() {
        String owner = ownerField.getText().trim();
        String repo = repoField.getText().trim();
        String token = tokenField.getText().trim();
        String platform = platformComboBox.getValue();

        if (owner.isEmpty() || repo.isEmpty()) {
            showAlert("Hata", "Lütfen kullanıcı adı ve repository adını girin!");
            return;
        }

        // UI'yi disable et
        fetchButton.setDisable(true);
        progressIndicator.setVisible(true);
        resultArea.setText("🔄 Commit bilgileri yükleniyor...\n\nLütfen bekleyin...");

        // Background task oluştur
        Task<List<CommitResponse>> task = new Task<List<CommitResponse>>() {
            @Override
            protected List<CommitResponse> call() throws Exception {
                VCSService vcsService = getVCSService(platform, token);
                if (vcsService == null) {
                    throw new Exception("Geçersiz platform seçimi!");
                }

                // CommitController kullanarak doğru dönüşümü yap
                CommitController controller = new CommitController(vcsService);
                return controller.getCommits(owner, repo);
            }
        };

        task.setOnSucceeded(e -> {
            List<CommitResponse> commits = task.getValue();
            if (commits.isEmpty()) {
                // Private repository kontrolü - token yoksa ve commits boşsa
                if (token.isEmpty()) {
                    showPrivateRepoAlert("Bu repository private olabilir ve Access Token gerektirebilir.\n\n" +
                        "Lütfen geçerli bir Access Token girin veya repository'nin public olduğundan emin olun.");
                    resultArea.setText("🔒 PRIVATE REPOSITORY DETECTED\n\n" +
                        "Toplam 0 commit bulundu - Bu repository private olabilir!\n\n" +
                        "💡 Access Token nasıl alınır:\n" +
                        getTokenInstructions(platform));
                } else {
                    showAlert("Bilgi", "Bu repository'de commit bulunamadı.\n\n" +
                        "Olası nedenler:\n" +
                        "• Repository gerçekten boş olabilir\n" +
                        "• Repository adı veya kullanıcı adı hatalı olabilir\n" +
                        "• Access Token'ın yetkileri yetersiz olabilir");
                    resultArea.setText("❌ Bu repository'de erişilebilir commit bulunamadı.\n\n" +
                        "🔍 Repository adı ve kullanıcı adının doğru olduğundan emin olun.\n" +
                        "🔑 Access Token'ın repo erişim yetkisi olduğundan emin olun.");
                }
            } else {
                displayCommits(commits);
            }
            fetchButton.setDisable(false);
            progressIndicator.setVisible(false);
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            System.err.println("Error fetching commits: " + exception.getMessage());
            exception.printStackTrace();

            // Hata mesajına göre private repository tespiti
            String errorMessage = exception.getMessage().toLowerCase();
            if (errorMessage.contains("401") || errorMessage.contains("unauthorized") ||
                errorMessage.contains("403") || errorMessage.contains("forbidden") ||
                errorMessage.contains("not found") && token.isEmpty()) {

                showPrivateRepoAlert("Bu repository'ye erişim reddedildi. Repository private olabilir.\n\n" +
                    "Lütfen geçerli bir Access Token girin.");
                resultArea.setText("🔒 PRIVATE REPOSITORY DETECTED\n\n" +
                    "Erişim reddedildi - Repository private olabilir!\n\n" +
                    "💡 Access Token nasıl alınır:\n" +
                    getTokenInstructions(platform));
            } else {
                showAlert("Hata", "Commit bilgileri alınırken hata oluştu: " + exception.getMessage());
                resultArea.setText("❌ Hata oluştu: " + exception.getMessage());
            }
            fetchButton.setDisable(false);
            progressIndicator.setVisible(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void displayCommits(List<CommitResponse> commits) {
        StringBuilder result = new StringBuilder();

        result.append("🎉 Toplam ").append(commits.size()).append(" commit bulundu!\n");
        result.append("=".repeat(80)).append("\n\n");

        for (int i = 0; i < commits.size(); i++) {
            CommitResponse commit = commits.get(i);
            result.append("📝 Commit #").append(i + 1).append("\n");
            result.append("🔑 SHA: ").append(commit.getSha()).append("\n");
            result.append("👤 Author: ").append(commit.getAuthor()).append("\n");
            result.append("📅 Date: ").append(commit.getDate()).append("\n");
            result.append("💬 Message: ").append(commit.getMessage()).append("\n");

            if (!commit.getFiles().isEmpty()) {
                result.append("📁 Changed Files (").append(commit.getFiles().size()).append("):\n");
                int fileIndex = 1;
                for (var file : commit.getFiles()) {
                    result.append("   ").append(fileIndex++).append(". ").append(file.getFileName()).append("\n");
                    result.append("      ➕ Added: ").append(file.getAdditions()).append(" lines\n");
                    result.append("      ➖ Deleted: ").append(file.getDeletions()).append(" lines\n");
                }
            }
            result.append("-".repeat(80)).append("\n\n");
        }

        Platform.runLater(() -> resultArea.setText(result.toString()));
    }

    private VCSService getVCSService(String platform, String accessToken) {
        switch (platform) {
            case "GitHub":
                return new GitHubService(accessToken.isEmpty() ? null : accessToken);
            case "GitLab":
                return new GitLabService(accessToken.isEmpty() ? null : accessToken);
            case "Azure DevOps":
                return new AzureDevOpsService(accessToken.isEmpty() ? null : accessToken);
            case "Bitbucket":
                return new BitbucketService(accessToken.isEmpty() ? null : accessToken);
            default:
                return null;
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showPrivateRepoAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("🔒 Private Repository Detected");
            alert.setHeaderText("Access Token Gerekli");
            alert.setContentText(message);

            // Alert'i genişlet
            alert.getDialogPane().setPrefWidth(500);
            alert.getDialogPane().setPrefHeight(300);

            alert.showAndWait();
        });
    }

    private String getTokenInstructions(String platform) {
        switch (platform) {
            case "GitHub":
                return "GitHub Personal Access Token:\n" +
                    "1. GitHub → Settings → Developer settings → Personal access tokens\n" +
                    "2. 'Generate new token' butonuna tıklayın\n" +
                    "3. 'repo' yetkisini seçin\n" +
                    "4. Token'ı kopyalayıp yukarıdaki alana yapıştırın";
            case "GitLab":
                return "GitLab Personal Access Token:\n" +
                    "1. GitLab → User Settings → Access Tokens\n" +
                    "2. 'Add new token' butonuna tıklayın\n" +
                    "3. 'read_repository' yetkisini seçin\n" +
                    "4. Token'ı kopyalayıp yukarıdaki alana yapıştırın";
            case "Azure DevOps":
                return "Azure DevOps Personal Access Token:\n" +
                    "1. Azure DevOps → User settings → Personal access tokens\n" +
                    "2. 'New Token' butonuna tıklayın\n" +
                    "3. 'Code (read)' yetkisini seçin\n" +
                    "4. Token'ı kopyalayıp yukarıdaki alana yapıştırın";
            case "Bitbucket":
                return "Bitbucket App Password:\n" +
                    "1. Bitbucket → Personal settings → App passwords\n" +
                    "2. 'Create app password' butonuna tıklayın\n" +
                    "3. 'Repositories (Read)' yetkisini seçin\n" +
                    "4. Password'ı kopyalayıp yukarıdaki alana yapıştırın";
            default:
                return "Platform için token talimatları bulunamadı.";
        }
    }

    // Custom exception class for private repository detection
    private static class PrivateRepositoryException extends Exception {
        public PrivateRepositoryException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

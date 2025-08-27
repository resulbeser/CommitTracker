package committracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.awt.Desktop;
import java.net.URI;

/**
 * Web Demo Application - Portfolio için web arayüzü
 * JavaFX uygulamasından bağımsız çalışır
 */
@SpringBootApplication
public class WebDemoApplication {

    public static void main(String[] args) {
        System.out.println("🚀 CommitTracker Web Demo başlatılıyor...");
        System.out.println("📱 Web arayüzü: http://localhost:9090");

        SpringApplication.run(WebDemoApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        try {
            String url = "http://localhost:9090";
            System.out.println("🌐 Browser açılıyor: " + url);

            // Windows'ta varsayılan browser'ı aç
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Alternatif yöntem - cmd ile açma
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            System.err.println("❌ Browser açılamadı: " + e.getMessage());
            System.out.println("📱 Manuel olarak şu adresi açın: http://localhost:9090");
        }
    }
}

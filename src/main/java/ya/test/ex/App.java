package ya.test.ex;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

import java.io.InputStream;
import java.util.Scanner;


public class App {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(8080);

        configureStaticFiles(app);
        setupRoutes(app);
    }

    private static void configureStaticFiles(Javalin app) {
        app.config.addStaticFiles("/static/css", Location.CLASSPATH);
        app.config.addStaticFiles("/templates/html", Location.CLASSPATH);
        app.config.addStaticFiles("/result", Location.CLASSPATH);
    }

    private static void setupRoutes(Javalin app) {
        app.get("/result", ctx -> serveHtmlFile(ctx, "result.html"));
        app.get("/", ctx -> serveHtmlFile(ctx, "index.html"));

        app.post("/submit", ctx -> {
            String cidrString = ctx.formParam("cidr");
            int threads = Integer.parseInt(ctx.formParam("thread"));
            IPAddressRange ipAddressRange = new IPAddressRange(cidrString);
            SSLScanner sslScanner = new SSLScanner(ipAddressRange.calculateIPRange(), threads);
            sslScanner.start();
            ctx.redirect("/result");
        });
    }

    private static void serveHtmlFile(Context ctx, String fileName) {
        String htmlContent = loadResourceFile("/templates/html/" + fileName);
        ctx.html(htmlContent);
    }

    private static String loadResourceFile(String filePath) {
        try (InputStream inputStream = App.class.getResourceAsStream(filePath);
             Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

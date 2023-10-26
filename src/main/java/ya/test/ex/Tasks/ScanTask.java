package ya.test.ex.Tasks;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import ya.test.ex.SSLScanner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ScanTask implements Runnable {
    static Logger logger = Logger.getLogger(String.valueOf(ScanTask.class));
    private String ipAddress;
    private CloseableHttpClient httpClient;
    private ArrayList<String> stringsDomain;

    public ScanTask(String ipAddress, CloseableHttpClient httpClient, ArrayList<String> stringsDomain) {
        this.ipAddress = ipAddress;
        this.httpClient = httpClient;
        this.stringsDomain = stringsDomain;
    }

    @Override
    public void run() {
        try {
            X509Certificate[] certificates = fetchCertificates(httpClient, ipAddress);

            for (X509Certificate certificate : certificates) {
                String subjectDN = certificate.getSubjectX500Principal().getName();
                Pattern pattern = Pattern.compile("CN=([^,]*)");
                Matcher matcher = pattern.matcher(subjectDN);

                while (matcher.find()) {
                    String domain = matcher.group(1);
                    stringsDomain.add("Ip:"+ipAddress+" Domain:"+domain);
                    logger.info("Found domain: " + domain);
                }
            }
        } catch (java.net.ConnectException e) {
            // Handle connection timeout gracefully
            logger.warning("Connection timed out for IP address: " + ipAddress);
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
    private static X509Certificate[] fetchCertificates(CloseableHttpClient httpClient, String url) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();
        X509Certificate[] certificates;
        try {
            SSLSocketFactory factory = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket(url, 443);
            socket.startHandshake();
            certificates = (X509Certificate[]) socket.getSession().getPeerCertificates();
            socket.close();

        } finally {
            httpClient.close();

        }
        return certificates;
    }
}

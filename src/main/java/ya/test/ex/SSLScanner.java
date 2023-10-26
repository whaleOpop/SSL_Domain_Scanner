package ya.test.ex;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;

import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import ya.test.ex.Tasks.ScanTask;


import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSLScanner {

    private String ipAddresses[];
    private Integer Thread;
    static Logger logger = Logger.getLogger(String.valueOf(SSLScanner.class));
    public SSLScanner(String[] ipAddresses, Integer thread) {
        this.ipAddresses = ipAddresses;
        Thread = thread;
    }

    public void start() {
        CloseableHttpClient httpClient = null;

        try {
            ArrayList<String> stringsDomain = new ArrayList<>();
            httpClient = createHttpClient();

            ExecutorService executorService = Executors.newFixedThreadPool(Thread);
            List<Future<?>> futures = new ArrayList<>();

            for (String ipAddress : ipAddresses) {
                futures.add(executorService.submit(new ScanTask(ipAddress, httpClient, stringsDomain)));
            }

            executorService.shutdown();

            // Дождитесь завершения всех задач.
            for (Future<?> future : futures) {
                future.get();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(App.class.getResource("/result/domains.txt").getPath()))) {
                for (String item : stringsDomain) {
                    writer.write(item);
                    writer.newLine(); // Add a newline character to separate items
                }
                logger.info("Data has been saved to " + "domains.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static CloseableHttpClient createHttpClient() throws Exception {
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                SSLContextBuilder.create()
                        .loadTrustMaterial(new TrustSelfSignedStrategy())
                        .build(),
                NoopHostnameVerifier.INSTANCE);


        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)  // 5 seconds
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }

    private static String fetchHttpsContent(CloseableHttpClient httpClient, String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }


}


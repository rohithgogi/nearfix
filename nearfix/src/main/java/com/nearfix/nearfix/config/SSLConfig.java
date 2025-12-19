package com.nearfix.nearfix.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class SSLConfig {

    @PostConstruct  // This runs BEFORE ApplicationReadyEvent
    public void disableSSLVerification() {
        try {
            // Create a trust manager that accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // Accept all
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // Accept all
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting hostname verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting hostname verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            System.out.println("========================================");
            System.out.println("⚠️  SSL VERIFICATION DISABLED");
            System.out.println("⚠️  Corporate proxy bypass enabled");
            System.out.println("⚠️  FOR DEVELOPMENT USE ONLY");
            System.out.println("========================================");

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            System.err.println("CRITICAL: Failed to disable SSL verification!");
            e.printStackTrace();
            throw new RuntimeException("Failed to configure SSL for corporate proxy", e);
        }
    }
}
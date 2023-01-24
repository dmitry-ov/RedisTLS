package org.example;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class MockTrustManager implements X509TrustManager {

    public MockTrustManager() {
    }

    public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
    }

    public void checkServerTrusted(X509Certificate[] arg0, String arg1)  {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}


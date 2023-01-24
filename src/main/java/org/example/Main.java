package org.example;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
         String filePath = "pathTo/certificate.p12";
         String password = "passwordCertificate";

        Optional<SSLSocketFactory> sslSocketFactoryOptional = getSslSocketFactory(filePath, password);
        SSLSocketFactory sslSocketFactory = sslSocketFactoryOptional
                .orElseThrow(() -> new RuntimeException("Can't create SSLSocketFactory"));

        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();

        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128); // max total size
        poolConfig.setMaxIdle(128); // max idle size
        poolConfig.setMinIdle(16); // min idle size
        poolConfig.setMaxWaitMillis(0L); // client wait time for connection
        poolConfig.setTestOnBorrow(false); // Send PING command before borrow from pool. if invalid - remove connection
        poolConfig.setTestOnReturn(false); // Send PING on return to pool
        poolConfig.setTestWhileIdle(true); // test idle connections
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis()); // min idle time of resource. When reached - resource is evicted
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis()); // idle resource detection cycle interval
        poolConfig.setNumTestsPerEvictionRun(3); // number of connections to be tested per cycle (-1 = all)
        poolConfig.setBlockWhenExhausted(true); // if exhausted and no capacity for new connections - block or throw

        //with ssl config jedis pool
        boolean sslEnamble = true;
        JedisPool pool = new JedisPool(
                genericObjectPoolConfig,
                "localhost",
                6379,
                sslEnamble,
                sslSocketFactory,
                new SSLParameters(),
                NoopHostnameVerifier.INSTANCE);

        Jedis jedis = pool.getResource();
        System.out.println(jedis.ping());
        System.out.println(jedis.select(0));
        jedis.close();

    }

    private static Optional<SSLSocketFactory> getSslSocketFactory(String filePath, String password) {
        try {
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(Files.newInputStream(Paths.get(filePath)), password.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
            factory.init(clientStore, password.toCharArray());
            sslContext.init(factory.getKeyManagers(), new TrustManager[]{new MockTrustManager()}, new SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return Optional.of(sslSocketFactory);

        } catch (UnrecoverableKeyException | CertificateException | KeyStoreException | IOException
                 | NoSuchAlgorithmException | KeyManagementException ex) {
            return Optional.empty();
        }
    }
}
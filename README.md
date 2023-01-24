# RedisTLS

Java example work with Redis throught TLS.
Certificate in p12 keyStore.

Run:
1. Download Redis redis-6.2.10 or higher
2. Run ``utils/gen-test-certs.sh`` for generate TLS certificates. You will find it in ``/tests/tls``
3. Make client p12 keyStore (remember the certificate password)
```
openssl pkcs12 -export -in redis.crt -certfile ca.crt -inkey redis.key -out certificate_for_red.p12
```
4. Run redis server
```
redis-server \
    --tls-port 6379 --port 0 \
    --tls-cert-file /pathToRedis/tests/tls/server.crt \
    --tls-key-file /pathToRedis/tests/tls/server.key \
    --tls-ca-cert-file /pathToRedis/tests/tls/ca.crt \
    --tls-auth-clients no
```
5. Run java app with correct path to your client certificate

Application makes 2 requests to Redis: ``ping`` and ``select 0``

Correct application out
```
PONG
OK
```




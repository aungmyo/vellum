# SSL connection test using openssl #

## public trusted cert ##

One can get remote server cert to import as follows, without having to ask them for it.

```
openssl s_client -connect 192.17.16.15:443
```

Cut and paste the PEM text eg. into <tt>trusted_server_cert.pem</tt>, and that cert can then be imported in to keystore.

```
-----BEGIN CERTIFICATE-----
MIIC+jCCArigAwIBAgIET5gTFzALBgcqhkjOOAQDBQAwYDELMAkGA1UEBhMCS1kx
...
MCwCFGQTkWoQIzQ5o9UIhd/zcLABIYplAhQcQEVbK6juAZQI81up/3BYJN8hdA==
-----END CERTIFICATE-----
```

```
keytool -keystore trusted_certs.jks -storetype JKS -alias other_server -importcert -file trusted_server_cert
```

## private key ##

In order to test a java keystore keys and certs for client auth SSL connection, we will export the private key to PKCS12 and convert that to PEM,
in order to using openssl to test the connection.

**Export the private key from keystore to a .p12 file format using keytool -importkeystore -deststoretype pcks12**

```
keytool -importkeystore -destkeystore mykey.p12 -deststoretype pkcs12 -deststorepass mypass -srckeystore mystore.jks -alias mykey
```

**Convert .p12 to .pem using openssl**

```
openssl pkcs12 -in mykey.p12 > mykey.private.pem
```

This is private key so it'll have "this is private key so it'll have <tt>"ENCRYPTED PRIVATE KEY"</tt> as well as its public cert as <tt>"CERTIFICATE"</tt>.

**Export trusted remote server cert to PEM**

```
keytool -keystore mystore.jks -list -alias mykey.jks -rfc
```

**Test connection using opensl**

```
openssl s_client -connect 192.17.16.15:443 -CAfile trusted_server_cert.pem -cert mykey.private.pem
```

Finally, we can troubleshoot e.g. check what cert they identify themselves as to you, and what their trusted certs are.

```
...
Certificate chain
 0 s:/C=ZA/ST=South Africa/L=Cape Town/O=Some Org/OU=Some Org Unit/CN=Some Name
   i:/C=ZA/ST=South Africa/L=Cape Town/O=Some Org/OU=Some Org Unit/CN=Some Name
---
Server certificate
-----BEGIN CERTIFICATE-----
MIIC+jCCArigAwIBAgIET5gTFzALBgcqhkjOOAQDBQAwYDELMAkGA1UEBhMCS1kx
...
MCwCFGQTkWoQIzQ5o9UIhd/zcLABIYplAhQcQEVbK6juAZQI81up/3BYJN8hdA==
-----END CERTIFICATE-----
subject=/C=ZA/ST=South Africa/L=Cape Town/O=Some Org/OU=Some Org Unit/CN=Some Name
issuer=/C=ZA/ST=South Africa/L=Cape Town/O=Some Org/OU=Some Org Unit/CN=Some Name
---
Acceptable client certificate CA names
/C=South Africa/ST=WP/L=Cape Town/O=My Org/OU=My Org Unit/CN=evanx.info
...
    Verify return code: 0 (ok)
```
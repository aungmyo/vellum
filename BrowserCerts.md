# Browser certs #

#### Certificate-based authentication for Tomcat ####

http://emo.sourceforge.net/cert-login-howto.html

To use passwordless login capabilities via certificate authentication using SSL requires setting up the Public Key Infrastructure (PKI) to provide this capability.

This guide will walk you through the steps of setting up your own PKI as well as how to set up Tomcat to handle Certificate-based authentication.

#### HowtoForge MySQL openssl guide ####

The following article includes creating self-signed server and client certs using openssl, albeit for the purpose of an MySQL SSL connection:

http://www.howtoforge.com/how-to-set-up-mysql-database-replication-with-ssl-encryption-on-debian-squeeze

#### OpenVPM openssl tool ####

OpenVPN comes with simple tool that uses openssl for generating self-signed server cert and client certs:

http://openvpn.net/index.php/open-source/documentation/miscellaneous/77-rsa-key-management.html

#### Chromium on Linux ####

http://code.google.com/p/chromium/wiki/LinuxCertManagement


#### Examples ####

Create private key and corresponding public certificate, and combine into PKCS12 format to import into browser
```
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout privateKey.key -out certificate.crt
  openssl pkcs12 -export -out certificate.pfx -inkey privateKey.key -in certificate.crt -certfile CACert.crt
```

Create CA cert
```
  openssl genrsa 2048 > ca-key.pem # server key   
  openssl req -new -x509 -nodes -days 1000 -key ca-key.pem > ca-cert.pem
```

Create server cert
```
  openssl req -newkey rsa:2048 -days 1000 -nodes -keyout server-key.pem > server-req.pem 
  openssl x509 -req -in server-req.pem -days 1000 -CA ca-cert.pem -CAkey ca-key.pem -set_serial 01 > server-cert.pem
```

Create client cert
```
  openssl req -newkey rsa:2048 -days 1000 -nodes -keyout client-key.pem > client-req.pem
  openssl x509 -req -in client-req.pem -days 1000 -CA ca-cert.pem -CAkey ca-key.pem -set_serial 01 > client-cert.pem
```

Generate a key pair (private key and public certificate) in pem format
```
  openssl req -new -nodes -x509 -out client_public.pem -keyout client_private.pem -days 7300
```

One can specify the subject on the command-line as follows.
```
   -subj "/C=ZA/ST=Western Cape/L=Cape Town/CN=MyCompany"
```

Covert to DER format
```
  openssl pkcs8 -topk8 -nocrypt -in client_private.pem -inform PEM -out client_private.der -outform DER
  openssl x509 -in testssl_client_public.pem -inform PEM -out client_public.der -outform DER
```

Test connection
```
  $ openssl
  OpenSSL> s_client -connect localhost:9040 -key client_private.pem -cert client_public.pem -CAfile server_public.pem
```

JDK 1.6 one can effectively "export" private by <tt>-importkeystore</tt> into PCKS12 "keystore"
```
  keytool -importkeystore -srckeystore ~/.keystore -destkeystore my.p12 -deststoretype PKCS12-srcalias mykey
```

Use openssl to convert the key to PEM (which produces a des3 encrypted key):
```
  openssl pkcs12 -in my.p12 -out my.pem -nocerts
```

No DES encryption:
```
  openssl pkcs12 -in my.p12 -out my-nodes.pem -nocerts -nodes
```

Convert both private key and cert to PEM:
```
  openssl pkcs12 -in my.p12 -out my.pem
```
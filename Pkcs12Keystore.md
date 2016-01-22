http://stackoverflow.com/questions/8759956/failed-to-create-service-exception-javax-xml-ws-webserviceexception

http://www.crsr.net/Notes/SSL.html

```
openssl pkcs12 -export -in cert.pem -inkey key.pem -out store.p12
```

```
<Connector port="8443" ... scheme="https" secure="true" 
     keystoreFile="/path/to/store.p12"  
     keystorePass="..." keystoreType="PKCS12" sslProtocol="TLS" />
```

To extract the content of the cert in the PKCS#12 file:

```
openssl pkcs12 -in store.p12 -nokeys -clcerts | openssl x509 -text -noout
```

To check the certificate the server is actually using:

```
echo "" | openssl s_client -showcerts -connect hostname_or_ip_address:port
```

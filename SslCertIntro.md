A quick write up i did, which might be useful to some your guys as well...

# SSL Certs

An SSL certificate is a digitally-signed document that, for starters, contains some bits of info as follows

1. an identity's info (e.g CN=biz.co.za etc)

2. this identity's public key

3. an expiry date

This document is then digitally signed by a CA, using their private key. That is, a digital signature is appended to the document.

If self-signed, it means it is signed using the server's own private key.

# Public key cryptography

Now to delve into the technical how-to...

First we understand "public key cryptography," that data encrypted with the public key can only be decrypted using the corresponding private key; and data encrypted with the private key can only be decrypted using the corresponding public key.

The application of this public key cryptography is:

1. A private key is used to sign, and decrypt.

2. The public key is used to verify, and encrypt.

where to "sign" means to encrypt a hash (e.g. MD5 or SHA) of a message or document, using one's private key.

So, anyone and everyone with the public key, can:

1. verify a digital signature produced by the private key holder.

2. send data securely to the private key holder.

# Creating an SSL cert

The first step in creating a cert, is generating the private/public key pair. We use "keytool -genkey" to create a keystore with a private/public key pair.

We then produce a CSR using "keytool -certreq" and providing identity info such as CN=biz.co.za etc, as well an an expiry date.

This CSR includes the unsigned cert document with our identity info (CN etc), plus our public key.

Before signing the cert document, the CA might alter some info, notably the expiry date, e.g. to one year.

The CA signs this document and sends it back to us, as a signed digital certificate. They have added a digital signature, which is the hash of the document encrypted using their private key.

We import this certificate (with identity info, public key, expiry date, and now the CA's digital signature as well) into our keystore using "keytool -importcert."

# SSL for website browsing

Now when a browser hits our site:

1. The browser (client) reads the cert presented by the server
2. It checks that the URL entered by the user to hit this server matches the server's cert eg. "www.biz.co.za"
3. It verifies the server cert, using the CA's public key, from its CA cert database
4. It can send data securely to the server's public key (as contained in the cert) e.g. to establish an SSL session.

Remember that data encrypted with the public key can only be successfully decrypted by the private key holder.  Therefore the browser can securely offer a shared secret session key to the server, using the server's public key, for the purposes of encrypting messages for the SSL session, using symmetric encryption e.g. AES.

# Application SSL connections

Now consider the case of an SSL connection that is not between a browser and a website, but between a client and server application.
In this case, the URL check is not performed because that is browser specific.

1. The client reads the cert presented by the server
2. It verifies the server cert, using the CA's public key, from its CA cert database
3. It can send data securely to the server's public key (as contained in the cert) e.g. to establish an SSL session.

The client still verifies the server cert using it's "CA cert" database, or as called in Java SSL, a "truststore."

Server certs usually have a CN which is a domain name, e.g. "CN=**.biz.co.za" is a wild-card domain name.**

Client certs on the other hand, have some "client ID" in the CN field to uniquely identify the client, e.g. an email address (which makes sense for an individual), an EAN number, or some other alphanumeric client ID.

# Client Auth

If "client auth" is enabled, then the server will request the connecting client's cert, and verify this against it's trusted "CA" certs. Sometimes we load the individual POS certs as trusted "CA" certs into the "truststore," which is fine and well, but that is another story.

--

the above from reading, http://security.stackexchange.com/questions/12103/explanation-of-ssl-handshake-with-certificate

as per http://tools.ietf.org/html/rfc4346#appendix-B

"public key cryptography: A class of cryptographic techniques employing two-key ciphers. Messages encrypted with the public key can only be decrypted with the associated private key. Conversely, messages signed with the private key can be verified with the public key."

--

http://en.wikipedia.org/wiki/Public_key_certificate

In cryptography, a public key certificate (also known as a digital certificate or identity certificate) is an electronic document that uses a digital signature to bind a public key with an identity — information such as the name of a person or an organization, their address, and so forth. The certificate can be used to verify that a public key belongs to an individual.

--

http://publib.boulder.ibm.com/infocenter/tivihelp/v5r1/topic/com.ibm.itim_a.infocenter.doc/ins_ldp_4602.htm#ToC_45

Public key encryption requires that a public key and a private key be generated for an application. Data encrypted with the public key can only be decrypted using the corresponding private key. Data encrypted with the private key can only be decrypted using the corresponding public key.


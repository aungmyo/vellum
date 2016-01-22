i done prototype with browser certs ! first https to enroll, using Mozilla Persona to supply verified email address - and the server generates p12 browser cert (signed by server cert) - with CN is your email (which is all one has - at least with Google login you can get real name as well)

then you can access "secure site" which is another webserver with client auth enabled

and this second HTTPS server has a dynamic trust store - which only trusts its own server cert - and then also checks that browser cert is in the database and enabled - altho given that can only be signed by the server cert, we really only have to check that that CN is enabled - not blacklisted, effectively

the slight "problem" in general with this approach, is that one needs 2x HTTPS servers which would mean any site wanting this browser cert security would need 2x VMs - unless you wanted to use a 2nd https port - which you don't want to do in general because of firewalls etc - one can only assume they allow default https port

and while a nice topic and demo for a blog article, i'm not sure this browser cert buys much compared to using google oauth (or mozilla persona) where you are sure the person at the other side is indeed that email ?!

still it's cool in that its really easy browser cert access - which isn't usually done because its overly technical for people to create key, generate CSR, and import signed cert, and produce a p12 to import - altho having said that, i believe IIS does make it very simple as well with their cert tools

so this way the server gives you a p12 which then you click on that download and the browser imports it, and then you can access the secure site WITHOUT the usual cookies and sign-on measures etc - just like an ssh key (where your browser is your ssh agent :)

```
public class CrocTrustManager implements X509TrustManager {
...
    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
        String dname = certs[0].getSubjectDN().getName();
        logger.info("checkClientTrusted " + dname);
        if (dname.equals(app.getServerCert().getSubjectDN().getName())) {
            return;
        }
        try {
            String cname = CrocSecurity.getCommonName(dname);
            Cert cert = app.getStorage().getCertStorage().findName(cname);
            if (cert == null) {
                logger.info("cert not found", cname);
                throw new RuntimeException(dname);                
            }
            logger.info("cert", cert.getSubject());
            if (!cert.isEnabled()) {
                throw new RuntimeException(dname);
            }
        } catch (SQLException e) {
            logger.warn(e, dname);
        }
    }
}
```

```
public class GenKeyP12Handler implements HttpHandler {
    ...
    private void handle() throws Exception {
        AdminUser user = app.getUser(httpExchangeInfo, true);
        char[] password = httpExchangeInfo.getParameterMap().getString("password").toCharArray();
        if (password.length < 8) {
            throw new EnumException(CrocExceptionType.PASSWORD_TOO_SHORT);
        }
        GeneratedRsaKeyPair keyPair = new GeneratedRsaKeyPair();
        keyPair.generate(user.formatSubject(), new Date(), 999);
        String alias = app.getServerKeyAlias();
        X509Certificate serverCert = app.getServerCert();
        keyPair.sign(DefaultKeyStores.getPrivateKey(alias), serverCert);
        user.setCert(keyPair.getCert());
        storage.getUserStorage().updateCert(user);
        storage.getCertStorage().save(keyPair.getCert(), user.getEmail());
        PKCS12KeyStore p12 = new PKCS12KeyStore();
        X509Certificate[] chain = new X509Certificate[] {keyPair.getCert(), serverCert};
        p12.engineSetKeyEntry(user.getUserName(), keyPair.getPrivateKey(), password, chain);
        httpExchangeInfo.sendResponseFile("application/x-pkcs12", "croc-client.p12");
        p12.engineStore(httpExchangeInfo.getPrintStream(), password);
    }    
}
```



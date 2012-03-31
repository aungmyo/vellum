/*
 * Copyright Evan Summers
 * 
 */
package vellum.provider;

import com.sun.crypto.provider.AESCipher;
import java.io.File;
import java.io.FileInputStream;
import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import sun.security.tools.KeyTool;
import vellum.logger.Logr;
import vellum.logger.LogrFactory;
import vellum.util.Base64;
import vellum.util.Bytes;
import vellum.util.Streams;

/**
 *
 * @author evan
 */
public class VTest implements Runnable {
    Logr logger = LogrFactory.getLogger(getClass());    

    VTestProperties properties = new VTestProperties(); 
    SecureRandom sr = new SecureRandom();
    
    VProviderConfig providerConfig = new VProviderConfig();
    VProviderContext providerContext = VProviderContext.instance;
    VProvider provider = new VProvider();
    
    VCipherConfig cipherConfig = new VCipherConfig();
    VCipherProperties cipherProperties = new VCipherProperties();
    VCipherContext cipherContext = new VCipherContext();
    VCipherServer server = new VCipherServer();

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    private void setProperties() {
        providerConfig.keyStore = properties.providerKeyStoreFile;
        providerConfig.trustStore = properties.providerTrustStoreFile;
        cipherConfig.keyStore = properties.cipherPrivateKeyStoreFile;        
        cipherConfig.trustKeyStore = properties.cipherTrustKeyStoreFile;
        cipherConfig.secretKeyStore = properties.cipherSecretKeyStoreFile;
        cipherProperties.keyStorePassword = properties.keyStorePass.toCharArray();
        cipherProperties.trustKeyStorePassword = properties.trustKeyStorePass.toCharArray();
        cipherProperties.privateKeyPassword = properties.privateKeyPass.toCharArray();
        cipherProperties.secretKeyStorePassword = properties.secretKeyStorePass.toCharArray();
        cipherProperties.secretKeyPassword = properties.secretKeyPass.toCharArray();
    }
    
    private void deleteKeyStores() {
        new File(providerConfig.keyStore).delete();
        new File(providerConfig.trustStore).delete();
        new File(cipherConfig.keyStore).delete();
        new File(cipherConfig.trustKeyStore).delete();
        new File(cipherConfig.secretKeyStore).delete();        
    }

    private void testProvider() throws Exception {
        KeyTool.main(buildKeyToolGenKeyPairArgs(providerConfig.keyStore, providerConfig.keyAlias, "provider"));
        KeyTool.main(buildKeyToolExportCertArgs(providerConfig.keyStore, providerConfig.keyAlias, properties.providerCert));
        KeyTool.main(buildKeyToolGenKeyPairArgs(cipherConfig.keyStore, cipherConfig.privateAlias, "cipher"));
        KeyTool.main(buildKeyToolExportCertArgs(cipherConfig.keyStore, cipherConfig.privateAlias, properties.cipherCert));
        KeyTool.main(buildKeyToolImportCertArgs(cipherConfig.trustKeyStore, cipherConfig.trustAlias, properties.providerCert));
        KeyTool.main(buildKeyToolImportCertArgs(providerConfig.trustStore, providerConfig.trustAlias, properties.cipherCert));
        KeyTool.main(buildKeyToolListArgs(cipherConfig.keyStore));
        KeyTool.main(buildKeyToolListArgs(cipherConfig.trustKeyStore));
        KeyTool.main(buildKeyToolListArgs(providerConfig.keyStore));
        KeyTool.main(buildKeyToolListArgs(providerConfig.trustStore));
        Key key = generateKey();
        if (false) {
            importKey(cipherConfig.secretKeyStore, cipherConfig.secretAlias, key);
        }
        cipherContext.config(cipherConfig, cipherProperties);
        server.config(cipherContext);
        listProviders();
        if (false) {
            Cipher cipher = Cipher.getInstance("AES", provider);
            logger.info(cipher.getProvider().getClass());
            cipher.init(0, (Key) null);
        }
        loadKey(cipherConfig.keyStore, cipherConfig.privateAlias);
        server.start();
        providerContext.config(providerConfig, 
                properties.keyStorePass.toCharArray(), properties.privateKeyPass.toCharArray(),
                properties.keyStorePass.toCharArray());
        providerContext.init();        
        testCipher();
        server.close();
    }
    
    private void testCipher() throws Exception {
        VCipherSpi cipher = new VCipherSpi();
        cipher.engineInit(Cipher.ENCRYPT_MODE, null, sr);
        String datum = "12345678901234567890";
        logger.info(datum);
        byte[] bytes = datum.getBytes();
        byte[] encryptedBytes = cipher.engineDoFinal(bytes, 0, bytes.length);
        byte[] iv = cipher.engineGetIV();
        logger.info(encryptedBytes.length);
        cipher.engineInit(Cipher.DECRYPT_MODE, null, new IvParameterSpec(iv), sr);
        byte[] decryptedBytes = cipher.engineDoFinal(encryptedBytes, 0, bytes.length);
        logger.info(new String(decryptedBytes));
    }
    
    private void testSecretKey() throws Exception {
        Security.addProvider(provider);
        KeyTool.main(buildKeyToolGenSecKeyArgs(cipherConfig.secretKeyStore, cipherConfig.secretAlias));
        KeyTool.main(buildKeyToolListArgs(cipherConfig.secretKeyStore));
        Key key = loadKey(cipherConfig.secretKeyStore, cipherConfig.secretAlias);
        Cipher aesEncryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesEncryptCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = aesEncryptCipher.getIV();
        logger.info(Bytes.formatHex(iv));
        Cipher aesDecryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ips = new IvParameterSpec(iv);
        aesDecryptCipher.init(Cipher.DECRYPT_MODE, key, ips);
        String message = "0123456789";
        byte[] encryptedBytes = aesEncryptCipher.doFinal(message.getBytes());
        byte[] decryptedBytes = aesDecryptCipher.doFinal(encryptedBytes);
        logger.info(Bytes.formatHex(encryptedBytes));
        logger.info(new String(decryptedBytes));        
        logger.info(Bytes.formatHex(aesEncryptCipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV()));
    }
    
    public void process() throws Exception {
        setProperties();
        deleteKeyStores();
        testSecretKey();
        if (true) {
            testProvider();
        }
    }
        
    private Key generateKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");        
        generator.init(256, sr);
        SecretKey key = generator.generateKey();
        logger.info(key.getAlgorithm(), key.getFormat(), key.getEncoded().length, Base64.encode(key.getEncoded()));
        return key;
    }

    private String[] buildKeyToolGenSecKeyArgs(String secretKeyStoreFile, String secretKeyAlias) {
        return new String[] {
            "-genseckey", 
            "-keyalg", properties.secretKeyAlg, 
            "-keysize", Integer.toString(properties.secretKeySize), 
            "-keystore", secretKeyStoreFile, 
            "-storetype", properties.keyStoreType, 
            "-storepass", properties.secretKeyStorePass,
            "-alias", secretKeyAlias, 
            "-keypass", properties.secretKeyPass
        };
    }
    
    private String[] buildKeyToolGenKeyPairArgs(String keyStoreFile, String keyAlias, String cn) {
        return new String[] {
            "-genkeypair", 
            "-keyalg", properties.keyAlg, 
            "-keystore", keyStoreFile, 
            "-storetype", properties.keyStoreType, 
            "-storepass", properties.keyStorePass,
            "-alias", keyAlias, 
            "-keypass", properties.privateKeyPass, 
            "-dname", String.format("CN=%s, OU=Development, O=venigmasecured.com, L=Cape Town, S=WP, C=za", cn)
        };
    }

    private String[] buildKeyToolExportCertArgs(String keyStore, String alias, String certFile) {
        return new String[] {
            "-export", 
            "-keystore", keyStore, 
            "-storetype", properties.keyStoreType, 
            "-storepass", properties.keyStorePass,
            "-alias", alias, 
            "-keypass", properties.privateKeyPass, 
            "-file", certFile, 
        };
    }
    
    private String[] buildKeyToolImportCertArgs(String trustStore, String trustAlias, String certFile) {
        return new String[] {
            "-import", 
            "-noprompt",
            "-keystore", trustStore, 
            "-storetype", properties.keyStoreType, 
            "-storepass", properties.trustKeyStorePass,
            "-alias", trustAlias,
            "-file", certFile,
        };
    }

    private String[] buildKeyToolListArgs(String keyStore) throws Exception {
        return new String[] {
            "-list", 
            "-keystore", keyStore, 
            "-storetype", properties.keyStoreType, 
            "-storepass", properties.keyStorePass
        };
    }

    private Key loadKey(String keyStoreFile, String keyAlias) throws Exception {
        File file = new File(keyStoreFile);
        KeyStore keyStore = KeyStore.getInstance("JCEKS", "VProvider");
        keyStore.load(new FileInputStream(file), properties.keyStorePass.toCharArray());
        logger.info("loadKey", keyStore.getType(), keyStore.getProvider().getName());
        Key key = keyStore.getKey(keyAlias, properties.privateKeyPass.toCharArray());
        logger.info(key.getAlgorithm(), key.getFormat());
        return key;
    }

    private void importKey(String cipherKeyStore, String secretAlias, Key key) throws Exception {
        logger.info("importKey", key.getAlgorithm(), key.getFormat());
        File file = new File(cipherKeyStore);
        KeyStore keyStore = KeyStore.getInstance("JCEKS", "VProvider");
        keyStore.load(new FileInputStream(file), properties.keyStorePass.toCharArray());
        logger.info("Cipher KeyStore provider", keyStore.getProvider().getName());
    }

    
    private void listProviders() {
        for (Provider provider : Security.getProviders()) {
            logger.info(provider.getName());
        }
    }
    
    public static void main(String[] args) {
        VTest instance = new VTest() ;
        try {
            if (true) {
                new Thread(instance).start();
                Thread.sleep(2000);
                Streams.close(instance.server);
                Thread.sleep(1000);
                System.exit(0);
            } else {
                instance.process();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            Streams.close(instance.server);
        }
    }

    
}

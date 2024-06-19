package com.autologin;

import net.fabricmc.loader.api.FabricLoader;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

import static com.autologin.AutoLoginClient.LOGGER;


public class ConfigA {
//    private String fileName;
    private static final String AES = "AES";
    private static final String AES_CBC_PADDING = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;

    private Path folderPath;
    public ConfigA(){
        folderPath = FabricLoader.getInstance().getConfigDir().resolve("AutoLogin");
    }
    public ConfigA(String folderName,boolean separator,String... configs) {
        folderPath = FabricLoader.getInstance().getConfigDir().resolve(folderName);
        for (int i = 1; i < configs.length; i++) {
            createConfig(configs[i]);
        }
    }
    public static String encryptString(String input) {
        int key=0457454;
        try {
            String stringKey = String.valueOf(key);
            String formattedKey = String.format("%16s", stringKey).replace(' ', '0');
            SecretKeySpec secretKeySpec = new SecretKeySpec(formattedKey.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(input.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String decryptString(String encryptedString) {
        int key=0457454;
        try {
            String stringKey = String.valueOf(key);
            String formattedKey = String.format("%16s", stringKey).replace(' ', '0');
            SecretKeySpec secretKeySpec = new SecretKeySpec(formattedKey.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedString));
            return new String(decryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void makeOld(String fileName) {
        try {
            Path filePath = folderPath.resolve(fileName + ".properties");
            Path oldFilePath = folderPath.resolve(fileName + ".properties.old");
            if (Files.exists(filePath)) {
                Files.move(filePath, oldFilePath);
                LOGGER.info("Config file renamed to: " + oldFilePath);
            } else {
                LOGGER.warn("Config file does not exist: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    public void makeOld() {
//        try {
//            Path filePath = folderPath.resolve(fileName + ".properties");
//            Path oldFilePath = folderPath.resolve(fileName + ".properties.old");
//            if (Files.exists(filePath)) {
//                Files.move(filePath, oldFilePath);
//                LOGGER.info("Config file renamed to: " + oldFilePath);
//            } else {
//                LOGGER.warn("Config file does not exist: " + filePath);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    public void createConfig(String fileName) {
        try {
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName + ".properties");
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getValue(String fileName, String key) {
        Properties properties = new Properties();
        Path filePath = folderPath.resolve(fileName + ".properties");
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            properties.load(reader);
            return properties.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, String> getAllValues(String fileName) {
        Properties properties = new Properties();
        Path filePath = folderPath.resolve(fileName + ".properties");
        Map<String, String> values = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            properties.load(reader);
            for (String key : properties.stringPropertyNames()) {
                values.put(key, decryptString(properties.getProperty(key)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }

    public void addValue(String fileName, String key, String value) {
        if (getValue(fileName, key) == null) {
            try {
                Files.createDirectories(folderPath);
                Path filePath = folderPath.resolve(fileName + ".properties");
                Properties properties = new Properties();
                if (Files.exists(filePath)) {
                    try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                        properties.load(reader);
                    }
                }
                properties.setProperty(key, value);
                try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE)) {
                    properties.store(writer, null);
                }
            } catch (IOException e) {}
        }
    }

    public void writeNewPasswords(Map<String, String> passwords) {
        this.createConfig("NewData");
        for(Map.Entry<String, String> entry : passwords.entrySet()){
            this.addValue("NewData",entry.getKey(),entry.getValue());
        }
    }

    private SecretKey getSecretKey(String keyString) throws Exception {
        byte[] keyBytes = new byte[KEY_SIZE / 8];
        byte[] providedKeyBytes = keyString.getBytes("UTF-8");
        System.arraycopy(providedKeyBytes, 0, keyBytes, 0, Math.min(providedKeyBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, AES);
    }
    public String encryptPassword(String plainText) {
        try {
            SecretKey secretKey = this.getSecretKey(this.getKey());
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decryptPassword(String encryptedText) {
        try {
            SecretKey secretKey = this.getSecretKey(this.getKey());
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void createHiddenFile(String fileName) {
        try {
            Path filePath = folderPath.resolve(fileName+".properties");
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                Files.setAttribute(filePath, "dos:hidden", true);
            }
        } catch (FileAlreadyExistsException e) {} catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateRandomKey() {
        int length = 16;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }

    private String getKey() {
        try {
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve("key.properties");
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line.startsWith("key" + "=")) {
                    return line.substring("key".length() + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> readConfigFile(Path filePath) {
        Map<String, String> configMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    configMap.put(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configMap;
    }
    public void setValue(String fileName, String key, String value) {
        try {
            Files.createDirectories(this.folderPath);
            Path filePath = folderPath.resolve(fileName + ".properties");
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith(key + "=")) {
                    lines.set(i, key + "=" + value);
                    break;
                }
            }
            Files.write(filePath, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isEmpty(String fileName) {
        try {
            Path filePath = folderPath.resolve(fileName + ".properties");

            if (Files.exists(filePath)) {
                List<String> lines = Files.readAllLines(filePath);
                return lines.isEmpty();
            } else {
                return true;
            }
        } catch (IOException e) {
            return true;
        }
    }
    public void clearFile(String fileName) {
        Path filePath = folderPath.resolve(fileName + ".properties");
        try {
            Files.createDirectories(folderPath);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.autologin;

import net.fabricmc.loader.api.FabricLoader;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.autologin.AutoLoginClient.LOGGER;


public class ConfigA {
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
    public void createConfig(String fileName) {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path folderPath = configDir.resolve("AutoLogin");
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName + ".properties");
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                LOGGER.info("Config file created: " + filePath);
            } else {
                LOGGER.info("Config file already exists: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addString(String fileName, String key, String value) {
        if(getString(fileName,key)==null) {
            try {
                Path configDir = FabricLoader.getInstance().getConfigDir();
                Path folderPath = configDir.resolve("AutoLogin");
                Files.createDirectories(folderPath);
                Path filePath = folderPath.resolve(fileName + ".properties");
                try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    writer.write(key + "=" + encryptString(value));
                    writer.newLine();
                }
                saveConfig(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void saveConfig(Path filePath) {
        try {
            Files.setAttribute(filePath, "dos:hidden", true);
            LOGGER.info("Configs saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getString(String fileName, String key) {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path folderPath = configDir.resolve("AutoLogin");
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName + ".properties");
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line.startsWith(key + "=")&&key!=null) {
                    return decryptString(line.substring(key.length() + 1));
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
                // Split each line by '=' to get the key and value
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
    public static void setValue(String fileName, String key, String value) {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path folderPath = configDir.resolve("AutoLogin");
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(fileName + ".properties");
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith(key + "=")) {
                    lines.set(i, key + "=" + encryptString(value));
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
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path folderPath = configDir.resolve("AutoLogin");
            Path filePath = folderPath.resolve(fileName + ".properties");

            if (Files.exists(filePath)) {
                List<String> lines = Files.readAllLines(filePath);
                return lines.isEmpty();
            } else {
                LOGGER.info("Config file does not exist: " + filePath);
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("Error while checking config file: " + e.getMessage());
            return true;
        }
    }
}

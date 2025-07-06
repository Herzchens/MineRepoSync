package me.Herzchen.minereposync.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void createBackup(File file) throws IOException {
        if (!file.exists()) return;

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File backupDir = new File(file.getParentFile(), "backups");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw new IOException("Failed to create backup directory");
        }

        File backup = new File(backupDir, file.getName() + "." + timestamp + ".bak");
        copyFile(file, backup);
    }

    public static List<String> listFilesRecursive(File dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir.toPath())) {
            return walk.filter(Files::isRegularFile)
                    .map(path -> dir.toPath().relativize(path).toString())
                    .collect(Collectors.toList());
        }
    }

    public static String calculateHash(File file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hashBytes = md.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(file.lastModified());
        }
    }
}
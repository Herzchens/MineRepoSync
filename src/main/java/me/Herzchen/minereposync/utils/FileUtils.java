package me.Herzchen.minereposync.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    public static void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void createBackup(File file) throws IOException {
        if (!file.exists()) return;

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File backupDir = new File(file.getParentFile(), "backups");
        if (!backupDir.exists()) backupDir.mkdirs();

        File backup = new File(backupDir, file.getName() + "." + timestamp + ".bak");
        copyFile(file, backup);
    }
}
package com.gzy.pestdetectionsystem.utils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class StoragePathResolver {

    private StoragePathResolver() {
    }

    public static Path resolveProjectPath(String configuredPath, Class<?> anchorClass) {
        Path path = Paths.get(configuredPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return resolveBaseDir(anchorClass).resolve(path).normalize();
    }

    private static Path resolveBaseDir(Class<?> anchorClass) {
        try {
            URI location = anchorClass.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path sourcePath = Paths.get(location).toAbsolutePath().normalize();
            if (sourcePath.endsWith(Paths.get("target", "classes"))) {
                return sourcePath.getParent().getParent();
            }
            return sourcePath.toFile().isFile() ? sourcePath.getParent() : sourcePath;
        } catch (Exception e) {
            return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        }
    }
}

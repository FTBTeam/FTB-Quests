package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.FTBQuests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUtils {
    public static void tryRename(Path oldPath, Path newPath) {
        if (!oldPath.equals(newPath)) {
            try {
                Files.move(oldPath, newPath);
                FTBQuests.LOGGER.info("migrated {} to {}", oldPath, newPath);
            } catch (IOException e) {
                FTBQuests.LOGGER.error("can't migrate {} to {}: {}", oldPath, newPath, e.getMessage());
            }
        }
    }

    public static void sortPathsByModificationTime(String what, List<Path> paths) {
        paths.sort((p1, p2) -> {
            try {
                return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
            } catch (IOException e) {
                FTBQuests.LOGGER.error("exception caught while sorting files for {}: {}", what, e.getMessage());
                return 0;
            }
        });
    }
}

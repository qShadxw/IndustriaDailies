package uk.co.tmdavies.motdoftheday.utils;

import uk.co.tmdavies.motdoftheday.MOTDoftheDay;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;

public class ConfigWatcher {

    private final String path;
    private static final long DEBOUNCE_MS = 200;
    private volatile long lastModified = 0;

    public ConfigWatcher(String path) {
        this.path = path;
    }

    public void watchFile() {
        MOTDoftheDay.LOGGER.info("Starting file watcher");

        Path watcherPath = Paths.get(path);
        WatchService watchService;

        try {
            watchService = FileSystems.getDefault().newWatchService();

            watcherPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException exception) {
            MOTDoftheDay.LOGGER.error("Failed to create or register watch service.");
            exception.printStackTrace();

            return;
        }

        AtomicLong lastModified = new AtomicLong(0);
        Thread watcherThread = new Thread(() -> watchLoop(watchService, lastModified));

        watcherThread.setDaemon(true);
        watcherThread.setName("ConfigFile-Watcher");
        watcherThread.start();
    }

    private void watchLoop(WatchService watchService, AtomicLong lastModified) {
        while (true) {
            try {
                WatchKey key = watchService.take();

                key.pollEvents().stream()
                        .map(WatchEvent::context)
                        .map(Object::toString)
                        .filter("config.json"::equals)
                        .findFirst()
                        .ifPresent(s -> {
                            long now = System.currentTimeMillis();
                            if (now - lastModified.get() >= 200L) {
                                lastModified.set(now);
                                configFileChanged();
                            }
                        });

                if (!key.reset()) {
                    MOTDoftheDay.LOGGER.warn("Watch key no longer valid. Exiting watcher thread.");
                    break;
                }
            } catch (InterruptedException exception) {
                MOTDoftheDay.LOGGER.error("File watcher thread interrupted.");
                exception.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void configFileChanged() {
        long now = System.currentTimeMillis();

        if (now - lastModified < DEBOUNCE_MS) {
            return;
        }

        lastModified = now;

        MOTDoftheDay.LOGGER.info("Config File Modified");
        MOTDoftheDay.configFile.loadConfig();
    }

}

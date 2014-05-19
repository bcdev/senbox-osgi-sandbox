package org.teo.cli;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectoryWatcher {

    private final WatchService watchService;
    private final Map<Path, WatchTask> watchTasks;
    private final List<Listener> listeners;

    public DirectoryWatcher() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        watchTasks = new HashMap<>();
        listeners = new ArrayList<>();
    }

    public void addDirectory(Path dir) throws IOException {

        WatchKey watchKey = dir.register(watchService,
                                         StandardWatchEventKinds.ENTRY_CREATE,
                                         StandardWatchEventKinds.ENTRY_DELETE,
                                         StandardWatchEventKinds.ENTRY_MODIFY);

        WatchTask watchTask = new WatchTask(dir, watchKey);
        watchTasks.put(dir, watchTask);

    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void start() {
        for (Path dir : watchTasks.keySet()) {
            WatchTask watchTask = watchTasks.get(dir);
            new Thread(watchTask, "Watcher for " + dir).start();
        }
    }

    public void stop() {
        for (WatchTask watchTask : watchTasks.values()) {
            watchTask.stop();
        }
    }

    public interface Listener {
        void entryCreated(Path dir, Path child);

        void entryDeleted(Path dir, Path child);

        void entryModified(Path dir, Path child);
    }

    class WatchTask implements Runnable {
        final Path dir;
        final WatchKey watchKey;
        boolean stopped;

        WatchTask(Path dir, WatchKey watchKey) {
            this.dir = dir;
            this.watchKey = watchKey;
        }

        void stop() {
            stopped = true;
        }

        @Override
        public void run() {
            stopped = false;

            while (!stopped) {

                // wait for key to be signaled
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException x) {
                    return;
                }

                if (stopped) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path child = ev.context();

                    for (Listener listener : listeners) {
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            listener.entryCreated(dir, child);
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            listener.entryModified(dir, child);
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            listener.entryDeleted(dir, child);
                        }
                    }
                }

                if (!key.reset()) {
                    return;
                }
            }
        }
    }
}



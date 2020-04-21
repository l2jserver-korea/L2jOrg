package org.l2j.gameserver.taskmanager;

import org.l2j.commons.threading.ThreadPool;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.settings.GeneralSettings;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.l2j.commons.configuration.Configurator.getSettings;
import static org.l2j.commons.util.Util.falseIfNullOrElse;

/**
 * @author JoeAlisson
 */
public class SaveTaskManager {

    private final Map<Player, Long> playerSaveStamp = Collections.synchronizedMap(new WeakHashMap<>());
    private ScheduledFuture<?> scheduledTask;

    private SaveTaskManager() {
    }

    public void registerPlayer(Player player) {
        var scheduleTime = getSettings(GeneralSettings.class).autoSavePlayerTime();
        if(playerSaveStamp.isEmpty() && (isNull(scheduledTask) || scheduledTask.isDone())) {
            scheduledTask = ThreadPool.scheduleAtFixedDelay(this::saveTask, scheduleTime, scheduleTime, TimeUnit.MINUTES);
        }
        playerSaveStamp.put(player, nextSave(scheduleTime));
    }

    protected long nextSave(int scheduleTime) {
        return System.currentTimeMillis() + Duration.ofMinutes(scheduleTime).toMillis();
    }

    private void saveTask() {
        final var now = System.currentTimeMillis();
        final var nextSave = nextSave(getSettings(GeneralSettings.class).autoSavePlayerTime());

        synchronized (playerSaveStamp) {
            playerSaveStamp.entrySet().stream()
                    .filter(entry -> falseIfNullOrElse(entry, e -> e.getValue() < now))
                    .forEach(entry -> save(nextSave, entry));
        }
    }

    private void save(long nextSave, Map.Entry<Player, Long> entry) {
        entry.getKey().storeMe();
        entry.setValue(nextSave);
    }

    public void remove(Player player) {
        playerSaveStamp.remove(player);
        if(playerSaveStamp.isEmpty() && nonNull(scheduledTask) && !scheduledTask.isDone()) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public static SaveTaskManager getInstance() {
        return Singleton.INSTANCE;
    }

    private static final class Singleton {
        private static final SaveTaskManager INSTANCE = new SaveTaskManager();
    }
}
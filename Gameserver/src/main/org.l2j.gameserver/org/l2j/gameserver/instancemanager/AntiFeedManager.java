package org.l2j.gameserver.instancemanager;

import org.l2j.gameserver.Config;
import org.l2j.gameserver.model.L2World;
import org.l2j.gameserver.model.actor.L2Character;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.network.L2GameClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class AntiFeedManager {
    public static final int GAME_ID = 0;
    public static final int OLYMPIAD_ID = 1;
    public static final int TVT_ID = 2;
    public static final int L2EVENT_ID = 3;

    private final Map<Integer, Long> _lastDeathTimes = new ConcurrentHashMap<>();
    private final Map<Integer, Map<Integer, AtomicInteger>> _eventIPs = new ConcurrentHashMap<>();

    protected AntiFeedManager() {
    }

    public static AntiFeedManager getInstance() {
        return SingletonHolder._instance;
    }

    /**
     * Set time of the last player's death to current
     *
     * @param objectId Player's objectId
     */
    public void setLastDeathTime(int objectId) {
        _lastDeathTimes.put(objectId, System.currentTimeMillis());
    }

    /**
     * Check if current kill should be counted as non-feeded.
     *
     * @param attacker Attacker character
     * @param target   Target character
     * @return True if kill is non-feeded.
     */
    public boolean check(L2Character attacker, L2Character target) {
        if (!Config.ANTIFEED_ENABLE) {
            return true;
        }

        if (target == null) {
            return false;
        }

        final L2PcInstance targetPlayer = target.getActingPlayer();
        if (targetPlayer == null) {
            return false;
        }

        // Players in offline mode should't be valid targets.
        if (targetPlayer.getClient().isDetached()) {
            return false;
        }

        if ((Config.ANTIFEED_INTERVAL > 0) && _lastDeathTimes.containsKey(targetPlayer.getObjectId())) {
            if ((System.currentTimeMillis() - _lastDeathTimes.get(targetPlayer.getObjectId())) < Config.ANTIFEED_INTERVAL) {
                return false;
            }
        }

        if (Config.ANTIFEED_DUALBOX && (attacker != null)) {
            final L2PcInstance attackerPlayer = attacker.getActingPlayer();
            if (attackerPlayer == null) {
                return false;
            }

            final L2GameClient targetClient = targetPlayer.getClient();
            final L2GameClient attackerClient = attackerPlayer.getClient();
            if ((targetClient == null) || (attackerClient == null) || targetClient.isDetached() || attackerClient.isDetached()) {
                // unable to check ip address
                return !Config.ANTIFEED_DISCONNECTED_AS_DUALBOX;
            }

            return !targetClient.getHostAddress().equals(attackerClient.getHostAddress());
        }

        return true;
    }

    /**
     * Clears all timestamps
     */
    public void clear() {
        _lastDeathTimes.clear();
    }

    /**
     * Register new event for dualbox check. Should be called only once.
     *
     * @param eventId
     */
    public void registerEvent(int eventId) {
        _eventIPs.putIfAbsent(eventId, new ConcurrentHashMap<>());
    }

    /**
     * @param eventId
     * @param player
     * @param max
     * @return If number of all simultaneous connections from player's IP address lower than max then increment connection count and return true.<br>
     * False if number of all simultaneous connections from player's IP address higher than max.
     */
    public boolean tryAddPlayer(int eventId, L2PcInstance player, int max) {
        return tryAddClient(eventId, player.getClient(), max);
    }

    /**
     * @param eventId
     * @param client
     * @param max
     * @return If number of all simultaneous connections from player's IP address lower than max then increment connection count and return true.<br>
     * False if number of all simultaneous connections from player's IP address higher than max.
     */
    public boolean tryAddClient(int eventId, L2GameClient client, int max) {
        if (client == null) {
            return false; // unable to determine IP address
        }

        final Map<Integer, AtomicInteger> event = _eventIPs.get(eventId);
        if (event == null) {
            return false; // no such event registered
        }

        final Integer addrHash = client.getHostAddress().hashCode();
        final AtomicInteger connectionCount = event.computeIfAbsent(addrHash, k -> new AtomicInteger());

        if (!Config.DUALBOX_COUNT_OFFLINE_TRADERS) {
            final String address = client.getHostAddress();
            for (L2PcInstance player : L2World.getInstance().getPlayers()) {
                if (((player.getClient() == null) || player.getClient().isDetached()) && player.getIPAddress().equals(address)) {
                    connectionCount.decrementAndGet();
                }
            }
        }

        if ((connectionCount.get() + 1) <= (max + Config.DUALBOX_CHECK_WHITELIST.getOrDefault(addrHash, 0))) {
            connectionCount.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Decreasing number of active connection from player's IP address
     *
     * @param eventId
     * @param player
     * @return true if success and false if any problem detected.
     */
    public boolean removePlayer(int eventId, L2PcInstance player) {
        return removeClient(eventId, player.getClient());
    }

    /**
     * Decreasing number of active connection from player's IP address
     *
     * @param eventId
     * @param client
     * @return true if success and false if any problem detected.
     */
    public boolean removeClient(int eventId, L2GameClient client) {
        if (client == null) {
            return false; // unable to determine IP address
        }

        final Map<Integer, AtomicInteger> event = _eventIPs.get(eventId);
        if (event == null) {
            return false; // no such event registered
        }

        final Integer addrHash = client.getHostAddress().hashCode();

        return event.computeIfPresent(addrHash, (k, v) ->
        {
            if ((v == null) || (v.decrementAndGet() == 0)) {
                return null;
            }
            return v;
        }) != null;
    }

    /**
     * Remove player connection IP address from all registered events lists.
     *
     * @param client
     */
    public void onDisconnect(L2GameClient client) {
        if ((client == null) || (client.getHostAddress() == null) || (client.getActiveChar() == null)) {
            return;
        }

        _eventIPs.forEach((k, v) ->
        {
            removeClient(k, client);
        });
    }

    /**
     * Clear all entries for this eventId.
     *
     * @param eventId
     */
    public void clear(int eventId) {
        final Map<Integer, AtomicInteger> event = _eventIPs.get(eventId);
        if (event != null) {
            event.clear();
        }
    }

    /**
     * @param player
     * @param max
     * @return maximum number of allowed connections (whitelist + max)
     */
    public int getLimit(L2PcInstance player, int max) {
        return getLimit(player.getClient(), max);
    }

    /**
     * @param client
     * @param max
     * @return maximum number of allowed connections (whitelist + max)
     */
    public int getLimit(L2GameClient client, int max) {
        if (client == null) {
            return max;
        }

        final Integer addrHash = client.getHostAddress().hashCode();
        int limit = max;
        if (Config.DUALBOX_CHECK_WHITELIST.containsKey(addrHash)) {
            limit += Config.DUALBOX_CHECK_WHITELIST.get(addrHash);
        }
        return limit;
    }

    private static class SingletonHolder {
        protected static final AntiFeedManager _instance = new AntiFeedManager();
    }
}
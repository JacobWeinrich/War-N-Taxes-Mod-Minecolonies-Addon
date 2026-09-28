package net.machiavelli.minecolonytax.integration;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Reflection-based compatibility shim for EconomyPlus (jakeseconomyplus).
 * Allows War-N-Taxes to interact with EconomyPlus without creating a compile-time dependency
 * or crashing if EconomyPlus is not installed.
 */
public class EconomyPlusCompat {
    private static final Logger LOGGER = LogManager.getLogger(EconomyPlusCompat.class);
    public static final String MOD_ID = "jakeseconomyplus";

    private static boolean initialized = false;
    private static boolean available = false;

    private static Class<?> playerDataManagerClass;
    private static Class<?> playerDataClass;
    private static Method loadOrCreateMethod;
    private static Method saveMethod;
    private static Method getBalanceMethod;
    private static Method setBalanceMethod;
    private static Method addBalanceMethod;
    private static Method subtractBalanceMethod;

    public static synchronized boolean isAvailable() {
        if (!initialized) {
            initialized = true;
            if (ModList.get().isLoaded(MOD_ID)) {
                try {
                    playerDataManagerClass = Class.forName("com.weinrichdevelopment.jakeseconomyplus.PlayerDataManager");
                    playerDataClass = Class.forName("com.weinrichdevelopment.jakeseconomyplus.PlayerData");

                    loadOrCreateMethod = playerDataManagerClass.getMethod("loadOrCreate", MinecraftServer.class, UUID.class, String.class);
                    saveMethod = playerDataManagerClass.getMethod("save", MinecraftServer.class, playerDataClass);

                    getBalanceMethod = playerDataClass.getMethod("getBalance");
                    setBalanceMethod = playerDataClass.getMethod("setBalance", double.class);
                    addBalanceMethod = playerDataClass.getMethod("addBalance", double.class);
                    subtractBalanceMethod = playerDataClass.getMethod("subtractBalance", double.class);

                    available = true;
                    LOGGER.info("Successfully hooked into EconomyPlus (jakeseconomyplus)");
                } catch (Exception e) {
                    LOGGER.error("Failed to initialize EconomyPlus integration despite mod being loaded", e);
                    available = false;
                }
            } else {
                available = false;
            }
        }
        return available;
    }

    private static Object getPlayerData(ServerPlayer player) {
        if (!isAvailable() || player == null) return null;
        try {
            MinecraftServer server = player.getServer();
            if (server == null) {
                server = ServerLifecycleHooks.getCurrentServer();
            }
            if (server == null) return null;
            return loadOrCreateMethod.invoke(null, server, player.getUUID(), player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("Error loading PlayerData from EconomyPlus for player " + player.getName().getString(), e);
            return null;
        }
    }

    private static void savePlayerData(ServerPlayer player, Object playerData) {
        if (!isAvailable() || player == null || playerData == null) return;
        try {
            MinecraftServer server = player.getServer();
            if (server == null) {
                server = ServerLifecycleHooks.getCurrentServer();
            }
            if (server == null) return;
            saveMethod.invoke(null, server, playerData);
        } catch (Exception e) {
            LOGGER.error("Error saving PlayerData to EconomyPlus for player " + player.getName().getString(), e);
        }
    }

    public static long getMoney(ServerPlayer player) {
        Object data = getPlayerData(player);
        if (data == null) return 0L;
        try {
            double balance = (Double) getBalanceMethod.invoke(data);
            return (long) Math.floor(balance);
        } catch (Exception e) {
            LOGGER.error("Error getting balance from EconomyPlus", e);
            return 0L;
        }
    }

    public static boolean setMoney(ServerPlayer player, long amount) {
        Object data = getPlayerData(player);
        if (data == null) return false;
        try {
            setBalanceMethod.invoke(data, (double) amount);
            savePlayerData(player, data);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error setting balance in EconomyPlus", e);
            return false;
        }
    }

    public static boolean addMoney(ServerPlayer player, long amount) {
        Object data = getPlayerData(player);
        if (data == null) return false;
        try {
            addBalanceMethod.invoke(data, (double) amount);
            savePlayerData(player, data);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error adding balance in EconomyPlus", e);
            return false;
        }
    }

    public static boolean removeMoney(ServerPlayer player, long amount) {
        Object data = getPlayerData(player);
        if (data == null) return false;
        try {
            double current = (Double) getBalanceMethod.invoke(data);
            if (current < amount) return false;
            subtractBalanceMethod.invoke(data, (double) amount);
            savePlayerData(player, data);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error removing balance from EconomyPlus", e);
            return false;
        }
    }

    public static boolean transferMoney(ServerPlayer from, ServerPlayer to, long amount) {
        if (removeMoney(from, amount)) {
            if (addMoney(to, amount)) {
                return true;
            } else {
                // Rollback
                addMoney(from, amount);
                return false;
            }
        }
        return false;
    }
}

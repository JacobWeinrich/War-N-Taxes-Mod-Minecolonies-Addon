package net.machiavelli.minecolonytax.integration;

import net.minecraft.server.level.ServerPlayer;

/**
 * Compatibility wrapper delegating to {@link EconomyIntegration}.
 * Kept for backward compatibility with existing callers.
 */
public final class SDMShopIntegration {

    private SDMShopIntegration() {}

    /** @see EconomyIntegration#isAvailable() */
    public static boolean isAvailable() {
        return EconomyIntegration.isAvailable();
    }

    /** @see EconomyIntegration#getMoney(ServerPlayer) */
    public static long getMoney(ServerPlayer player) {
        return EconomyIntegration.getMoney(player);
    }

    /** @see EconomyIntegration#setMoney(ServerPlayer, long) */
    public static boolean setMoney(ServerPlayer player, long amount) {
        return EconomyIntegration.setMoney(player, amount);
    }

    /** @see EconomyIntegration#addMoney(ServerPlayer, long) */
    public static boolean addMoney(ServerPlayer player, long amount) {
        return EconomyIntegration.addMoney(player, amount);
    }

    /** @see EconomyIntegration#removeMoney(ServerPlayer, long) */
    public static boolean removeMoney(ServerPlayer player, long amount) {
        return EconomyIntegration.removeMoney(player, amount);
    }

    /** @see EconomyIntegration#deductPlayerBalance(ServerPlayer, int) */
    public static int deductPlayerBalance(ServerPlayer player, int amount) {
        return EconomyIntegration.deductPlayerBalance(player, amount);
    }
}

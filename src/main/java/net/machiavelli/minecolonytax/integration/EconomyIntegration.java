package net.machiavelli.minecolonytax.integration;

import net.machiavelli.minecolonytax.TaxConfig;
import net.minecraft.server.level.ServerPlayer;

/**
 * Central economy provider router for War-N-Taxes.
 * Routes currency requests dynamically to EconomyPlus, SDMShop, or Item currency
 * based on configuration and mod availability.
 */
public final class EconomyIntegration {

    private EconomyIntegration() {}

    /**
     * Returns whether a digital economy provider (EconomyPlus or SDMShop) is available and enabled.
     */
    public static boolean isAvailable() {
        TaxConfig.EconomyProvider provider = TaxConfig.getEconomyProvider();
        if (provider == TaxConfig.EconomyProvider.ITEM) {
            return false;
        }
        if (provider == TaxConfig.EconomyProvider.ECONOMY_PLUS) {
            return EconomyPlusCompat.isAvailable();
        }
        if (provider == TaxConfig.EconomyProvider.SDM_SHOP) {
            return SDMShopCompat.isAvailable();
        }
        // AUTO mode: prefers EconomyPlus, falls back to SDMShop
        return EconomyPlusCompat.isAvailable() || SDMShopCompat.isAvailable();
    }

    /**
     * Gets the display name of the currently active economy provider.
     */
    public static String getActiveProviderName() {
        TaxConfig.EconomyProvider provider = TaxConfig.getEconomyProvider();
        if (provider == TaxConfig.EconomyProvider.ECONOMY_PLUS && EconomyPlusCompat.isAvailable()) {
            return "EconomyPlus";
        }
        if (provider == TaxConfig.EconomyProvider.SDM_SHOP && SDMShopCompat.isAvailable()) {
            return "SDMShop";
        }
        if (provider == TaxConfig.EconomyProvider.AUTO) {
            if (EconomyPlusCompat.isAvailable()) return "EconomyPlus";
            if (SDMShopCompat.isAvailable()) return "SDMShop";
        }
        return "None";
    }

    public static long getMoney(ServerPlayer player) {
        if (isEconomyPlusActive()) {
            return EconomyPlusCompat.getMoney(player);
        }
        if (isSDMShopActive()) {
            return SDMShopCompat.getMoney(player);
        }
        return 0L;
    }

    public static boolean setMoney(ServerPlayer player, long amount) {
        if (isEconomyPlusActive()) {
            return EconomyPlusCompat.setMoney(player, amount);
        }
        if (isSDMShopActive()) {
            return SDMShopCompat.setMoney(player, amount);
        }
        return false;
    }

    public static boolean addMoney(ServerPlayer player, long amount) {
        if (isEconomyPlusActive()) {
            return EconomyPlusCompat.addMoney(player, amount);
        }
        if (isSDMShopActive()) {
            return SDMShopCompat.addMoney(player, amount);
        }
        return false;
    }

    public static boolean removeMoney(ServerPlayer player, long amount) {
        if (isEconomyPlusActive()) {
            return EconomyPlusCompat.removeMoney(player, amount);
        }
        if (isSDMShopActive()) {
            return SDMShopCompat.removeMoney(player, amount);
        }
        return false;
    }

    public static int deductPlayerBalance(ServerPlayer player, int amount) {
        if (amount <= 0) return 0;
        return removeMoney(player, amount) ? amount : 0;
    }

    public static boolean transferMoney(ServerPlayer from, ServerPlayer to, long amount) {
        if (isEconomyPlusActive()) {
            return EconomyPlusCompat.transferMoney(from, to, amount);
        }
        if (isSDMShopActive()) {
            return SDMShopCompat.transferMoney(from, to, amount);
        }
        return false;
    }

    private static boolean isEconomyPlusActive() {
        TaxConfig.EconomyProvider provider = TaxConfig.getEconomyProvider();
        if (provider == TaxConfig.EconomyProvider.ITEM) return false;
        if (provider == TaxConfig.EconomyProvider.ECONOMY_PLUS) return EconomyPlusCompat.isAvailable();
        if (provider == TaxConfig.EconomyProvider.AUTO) {
            return EconomyPlusCompat.isAvailable();
        }
        return false;
    }

    private static boolean isSDMShopActive() {
        TaxConfig.EconomyProvider provider = TaxConfig.getEconomyProvider();
        if (provider == TaxConfig.EconomyProvider.ITEM) return false;
        if (provider == TaxConfig.EconomyProvider.SDM_SHOP) return SDMShopCompat.isAvailable();
        if (provider == TaxConfig.EconomyProvider.AUTO) {
            return !EconomyPlusCompat.isAvailable() && SDMShopCompat.isAvailable();
        }
        return false;
    }
}

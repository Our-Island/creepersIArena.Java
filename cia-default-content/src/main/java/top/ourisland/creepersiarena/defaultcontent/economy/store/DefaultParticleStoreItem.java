package top.ourisland.creepersiarena.defaultcontent.economy.store;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.economy.*;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmetic;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticService;
import top.ourisland.creepersiarena.api.economy.store.*;
import top.ourisland.creepersiarena.core.economy.store.StorePurchaseRepository;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;
import top.ourisland.creepersiarena.defaultcontent.economy.DefaultCurrencies;
import top.ourisland.creepersiarena.defaultcontent.economy.cosmetic.particle.DefaultParticleCosmetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultParticleStoreItem implements IStoreItem {

    private final StoreItemId id;
    private final CosmeticId cosmeticId;
    private final ICosmetic cosmetic;
    private final CurrencyCost price;
    private final IWalletService wallet;
    private final ICosmeticService cosmetics;
    private final ICurrencyRegistry currencies;
    private final IAbilityGate abilities;
    private final StorePurchaseRepository purchases;
    private final boolean free;

    public DefaultParticleStoreItem(
            StoreItemId id,
            CosmeticId cosmeticId,
            ICosmetic cosmetic,
            CurrencyCost price,
            IWalletService wallet,
            ICosmeticService cosmetics,
            ICurrencyRegistry currencies,
            IAbilityGate abilities,
            StorePurchaseRepository purchases,
            boolean free
    ) {
        this.id = id;
        this.cosmeticId = cosmeticId;
        this.cosmetic = cosmetic;
        this.price = price == null ? CurrencyCost.free() : price;
        this.wallet = wallet;
        this.cosmetics = cosmetics;
        this.currencies = currencies;
        this.abilities = abilities;
        this.purchases = purchases;
        this.free = free;
    }

    @Override
    public StoreItemId id() {
        return id;
    }

    @Override
    public ItemStack icon(StoreRenderContext context) {
        var icon = cosmetic.icon();
        var meta = icon.getItemMeta();
        if (meta != null) {
            meta.displayName(cosmetic.displayName());
            meta.lore(lore(context));
            icon.setItemMeta(meta);
        }
        return icon;
    }

    @Override
    public StoreItemState state(StoreRenderContext context) {
        if (context == null || context.player() == null) return StoreItemState.DISABLED;

        var player = context.player();
        if (!enabled(player)) return StoreItemState.DISABLED;
        if (!wallet.loaded(player.getUniqueId()) || !cosmetics.loaded(player.getUniqueId())) {
            return StoreItemState.DISABLED;
        }

        var selected = cosmetics.selected(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL);

        if (free) {
            return selected == null
                    ? StoreItemState.SELECTED
                    : StoreItemState.FREE;
        }
        if (cosmeticId.equals(selected)) {
            return StoreItemState.SELECTED;
        }
        if (cosmetics.isUnlocked(player.getUniqueId(), cosmeticId)) {
            return StoreItemState.OWNED;
        }

        return wallet.canAfford(player.getUniqueId(), price)
                ? StoreItemState.PURCHASABLE
                : StoreItemState.LOCKED;
    }

    @Override
    public StoreClickResult click(StoreClickContext context) {
        if (context == null || context.player() == null) return StoreClickResult.noOp();

        var player = context.player();
        if (!enabled(player)) {
            return StoreClickResult.disabled(Component.text("粒子商店或粒子外观已禁用。"));
        }
        if (!wallet.loaded(player.getUniqueId()) || !cosmetics.loaded(player.getUniqueId())) {
            return StoreClickResult.disabled(Component.text("玩家数据仍在加载中，请稍后再试。"));
        }

        if (free || DefaultParticleCosmetics.NONE.equals(cosmeticId)) {
            cosmetics.clearSelection(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL);
            playSelect(player);
            return StoreClickResult.selected(Component.text("成功选择了 ").append(cosmetic.displayName()));
        }

        if (cosmetics.isUnlocked(player.getUniqueId(), cosmeticId)) {
            cosmetics.select(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL, cosmeticId);
            playSelect(player);
            return StoreClickResult.selected(Component.text("成功选择了 ").append(cosmetic.displayName()));
        }

        var result = wallet.withdraw(
                player.getUniqueId(),
                price,
                WalletChangeReason.purchase(id.asString())
        );

        if (result.disabled()) {
            return StoreClickResult.disabled(Component.text("货币系统已禁用。"));
        }

        if (!result.success()) {
            playFail(player);
            return StoreClickResult.notEnoughCurrency(missingMessage(result.missingAmounts(), player));
        }

        cosmetics.unlock(player.getUniqueId(), cosmeticId);
        cosmetics.select(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL, cosmeticId);

        playPurchase(player);
        if (purchases != null) {
            purchases.recordPurchaseAsync(player.getUniqueId(), DefaultParticleStore.STORE_ID, id, null, "PURCHASED");
        }

        player.getServer().broadcast(Component.text(player.getName() + " 购买了 ").append(cosmetic.displayName()));

        return StoreClickResult.purchased(Component.text("再次点击选择 ").append(cosmetic.displayName()));
    }

    private void playSelect(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F);
    }

    private void playFail(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0F, 1.0F);
    }

    private Component missingMessage(
            Map<CurrencyId, Long> missing,
            Player player
    ) {
        if (missing == null || missing.isEmpty()) return Component.text("余额不足。");
        var out = Component.text("余额不足: ");

        boolean first = true;
        for (var entry : missing.entrySet()) {
            if (!first) out = out.append(Component.text(", "));
            first = false;
            out = out.append(currencyName(entry.getKey())).append(Component.text(" 缺少 " + entry.getValue()));
        }
        return out;
    }

    private void playPurchase(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
    }

    private Component currencyName(CurrencyId id) {
        var currency = currencies.currency(id);
        return currency == null ? Component.text(plainName(id)) : currency.displayName();
    }

    private List<Component> lore(StoreRenderContext context) {
        var out = new ArrayList<Component>();
        var state = state(context);

        out.add(Component.text("状态: " + stateText(state)));

        if (!price.freeCost()) {
            out.add(Component.text("价格: " + priceText()));
        } else {
            out.add(Component.text("价格: 免费"));
        }

        out.add(Component.text(actionText(state)));

        return out;
    }

    private String stateText(StoreItemState state) {
        return switch (state) {
            case FREE -> "免费";
            case LOCKED -> "未拥有";
            case PURCHASABLE -> "可购买";
            case OWNED -> "已拥有";
            case SELECTED -> "已选择";
            case DISABLED -> "已禁用";
            case HIDDEN -> "隐藏";
        };
    }

    private String priceText() {
        var parts = price.amounts().stream()
                .map(amount -> amount.amount() + " " + plainName(amount.currencyId()))
                .collect(Collectors.toCollection(ArrayList::new));
        return String.join(" + ", parts);
    }

    private String actionText(StoreItemState state) {
        return switch (state) {
            case FREE, OWNED -> "左键选择";
            case PURCHASABLE, LOCKED -> "左键购买";
            case SELECTED -> "当前正在使用";
            default -> "不可用";
        };
    }

    private boolean enabled(Player player) {
        return abilities != null
                && abilities.isEnabled(DefaultContentAbilities.PARTICLE_STORE, player, "particle_store_item")
                && abilities.isEnabled(DefaultContentAbilities.PARTICLE_COSMETICS, player, "particle_store_item");
    }

    private String plainName(CurrencyId id) {
        if (DefaultCurrencies.GUNPOWDER.equals(id)) return "火药";
        if (DefaultCurrencies.TNT.equals(id)) return "TNT";
        return id.asString();
    }

}

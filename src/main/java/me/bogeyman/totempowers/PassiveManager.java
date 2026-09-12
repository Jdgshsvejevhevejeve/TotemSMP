package me.bogeyman.totempowers;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

public final class PassiveManager {
    private final TotemPowers plugin;
    private final Map<UUID, Map<PotionEffectType, PotionEffect>> previous = new HashMap<>();
    private final Map<UUID, Map<String, Long>> proc = new HashMap<>();
    private final Map<UUID, AttributeModifier> knockbackMods = new HashMap<>();
    private final BukkitTask task;

    public PassiveManager(TotemPowers p) {
        plugin = p;
        task = new org.bukkit.scheduler.BukkitRunnable() { public void run() { tick(); } }.runTaskTimer(p, 0L, 5L);
    }

    private void tick() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            TotemType t = plugin.getTotems().get(p);
            boolean eq = t != null && plugin.getItems().isEquipped(p, t);
            if (!eq) { clear(p); continue; }
            applyCore(p, t);
            applyAttribute(p, t);
            switch (t) {
                case ENDER -> sense(p, 12, false);
                case WARDEN -> sense(p, 12, true);
                default -> { }
            }
            if (t == TotemType.WITHER) aura(p, 3, PotionEffectType.WITHER, 0, 40, "wither");
            if (t == TotemType.FROST) aura(p, 4, PotionEffectType.SLOWNESS, 0, 30, "frost");
        }
    }

    private void applyCore(Player p, TotemType t) {
        switch (t) {
            case ENDER -> effect(p, PotionEffectType.SPEED, 0, 10);
            case WARDEN -> effect(p, PotionEffectType.NIGHT_VISION, 0, 20);
            case WITHER -> effect(p, PotionEffectType.RESISTANCE, 0, 10);
            case BLAZE, MAGMA -> effect(p, PotionEffectType.FIRE_RESISTANCE, 0, 20);
            case STORM -> effect(p, PotionEffectType.SPEED, 1, 10);
            case TIDE -> {
                effect(p, PotionEffectType.WATER_BREATHING, 0, 20);
                if (p.isInWaterOrBubbleColumn()) effect(p, PotionEffectType.DOLPHINS_GRACE, 0, 10);
            }
            case SHADOW -> {
                effect(p, PotionEffectType.SPEED, 0, 10);
                if (p.isSneaking()) effect(p, PotionEffectType.INVISIBILITY, 0, 6);
            }
            case DRAGON -> effect(p, PotionEffectType.RESISTANCE, 0, 10);
            case SOUL -> effect(p, PotionEffectType.REGENERATION, 0, 10);
            case FROST -> { }
        }
        if (t == TotemType.MAGMA && isInLava(p)) effect(p, PotionEffectType.SPEED, 0, 10);
    }

    private boolean isInLava(Player p) { return p.isInLava() || p.getLocation().getBlock().isLiquid() && p.getLocation().getBlock().getType() == Material.LAVA; }

    private void applyAttribute(Player p, TotemType t) {
        int percent = (t == TotemType.WARDEN || t == TotemType.TIDE || t == TotemType.DRAGON) ? 15 : 0;
        if (percent == 0 || (t == TotemType.TIDE && !p.isInWaterOrBubbleColumn())) { removeAttribute(p); return; }
        Attribute attr = Attribute.KNOCKBACK_RESISTANCE;
        if (p.getAttribute(attr) == null) return;
        AttributeModifier old = knockbackMods.get(p.getUniqueId());
        if (old == null) {
            AttributeModifier mod = new AttributeModifier(new NamespacedKey(plugin, "kb_" + t.key()), percent / 100.0, AttributeModifier.Operation.ADD_NUMBER);
            p.getAttribute(attr).addModifier(mod); knockbackMods.put(p.getUniqueId(), mod);
        }
    }

    private void removeAttribute(Player p) {
        AttributeModifier m = knockbackMods.remove(p.getUniqueId());
        if (m != null && p.getAttribute(Attribute.KNOCKBACK_RESISTANCE) != null) p.getAttribute(Attribute.KNOCKBACK_RESISTANCE).removeModifier(m);
    }

    private void sense(Player p, double radius, boolean warden) {
        for (LivingEntity e : AbilityUtils.nearbyLiving(p.getLocation(), radius, p)) {
            if (e instanceof ArmorStand) continue;
            if (e instanceof Player target && target.isSneaking()) continue;
            if (e.getVelocity().lengthSquared() < 0.0025) continue;
            e.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 8, 0, true, false, false));
            if (warden) AbilityUtils.burst(e.getLocation(), Particle.SCULK_SOUL, 3, .15);
        }
    }

    private void aura(Player p, double r, PotionEffectType type, int amp, int dur, String key) {
        for (LivingEntity e : AbilityUtils.nearbyLiving(p.getLocation(), r, p)) {
            if (e instanceof Player target && target.getUniqueId().equals(p.getUniqueId())) continue;
            UUID id = e.getUniqueId();
            if (onCooldown(id, key, 900)) continue;
            e.addPotionEffect(new PotionEffect(type, dur, amp, true, false, true));
        }
    }

    private boolean onCooldown(UUID id, String key, long millis) {
        long now = System.currentTimeMillis();
        Map<String, Long> m = proc.computeIfAbsent(id, k -> new HashMap<>());
        Long until = m.get(key);
        if (until != null && until > now) return true;
        m.put(key, now + millis); return false;
    }

    private void effect(Player p, PotionEffectType type, int amp, int dur) {
        Map<PotionEffectType, PotionEffect> m = previous.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>());
        if (!m.containsKey(type)) m.put(type, p.getPotionEffect(type));
        p.addPotionEffect(new PotionEffect(type, dur, amp, true, false, true));
    }

    public void clear(Player p) {
        removeAttribute(p);
        proc.remove(p.getUniqueId());
        Map<PotionEffectType, PotionEffect> m = previous.remove(p.getUniqueId());
        if (m != null) for (var e : m.entrySet()) {
            p.removePotionEffect(e.getKey());
            if (e.getValue() != null) p.addPotionEffect(e.getValue());
        }
        // Remove only the temporary glow generated by sense after it expires naturally.
        // Shadow invisibility is also removed here when the Totem is no longer equipped.
        if (plugin.getSkills() != null) plugin.getSkills().cancelVeil(p);
    }

    public void clearAll() {
        if (task != null) task.cancel();
        for (Player p : plugin.getServer().getOnlinePlayers()) clear(p);
        previous.clear(); proc.clear(); knockbackMods.clear();
    }
}

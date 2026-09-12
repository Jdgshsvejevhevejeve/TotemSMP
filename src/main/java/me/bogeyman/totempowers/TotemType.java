package me.bogeyman.totempowers;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import java.util.*;

public enum TotemType {
    ENDER("🟣 ENDER TOTEM", NamedTextColor.LIGHT_PURPLE, "ender", "Ender Step", "Void Sense", "Void Blink", "Ender Rift", "Teleport up to 12 blocks", "Create a damaging Ender Rift", 12, 28),
    WARDEN("🟢 WARDEN TOTEM", NamedTextColor.DARK_GREEN, "warden", "Sculk Sense", "Warden's Resolve", "Sonic Boom", "Dark Pulse", "Warden sonic attack up to 18 blocks", "Damage and darken nearby enemies", 18, 30),
    WITHER("⚫ WITHER TOTEM", NamedTextColor.DARK_GRAY, "wither", "Witherborn", "Decay Aura", "Wither Blast", "Wither Nova", "Fire a Wither-style blast", "Damage and wither nearby enemies", 16, 35),
    BLAZE("🔥 BLAZE TOTEM", NamedTextColor.GOLD, "blaze", "Flameborn", "Burning Touch", "Flame Burst", "Inferno Ring", "Fire three flame projectiles", "Create a damaging fire-energy ring", 12, 30),
    STORM("⚡ STORM TOTEM", NamedTextColor.BLUE, "storm", "Stormcharged", "Static Guard", "Lightning Strike", "Thunder Dash", "Call lightning on a target", "Dash forward with a storm shockwave", 15, 25),
    FROST("❄ FROST TOTEM", NamedTextColor.AQUA, "frost", "Frozen Heart", "Frost Aura", "Frost Bolt", "Absolute Zero", "Launch an ice attack", "Freeze and slow nearby enemies briefly", 10, 32),
    TIDE("🌊 TIDE TOTEM", NamedTextColor.DARK_AQUA, "tide", "Ocean's Blessing", "Tidal Armor", "Tidal Spear", "Tidal Wave", "Launch a water spear", "Send a damaging water wave", 12, 28),
    SHADOW("🌑 SHADOW TOTEM", NamedTextColor.DARK_PURPLE, "shadow", "Shadowstep", "Umbral Protection", "Shadow Veil", "Shadow Strike", "Become invisible briefly", "Teleport behind a target and strike", 25, 22),
    DRAGON("🐉 DRAGON TOTEM", NamedTextColor.LIGHT_PURPLE, "dragon", "Dragon's Might", "Dragon's Fury", "Dragon Breath", "Dragon Dive", "Release a dragon breath cone", "Launch forward and create a shockwave", 25, 40),
    SOUL("💀 SOUL TOTEM", NamedTextColor.BLUE, "soul", "Soulbound", "Soul Guardian", "Soul Flame", "Soul Rebirth", "Launch a soul-flame attack", "Heal and regenerate", 14, 45),
    MAGMA("🌋 MAGMA TOTEM", NamedTextColor.RED, "magma", "Magma Core", "Molten Movement", "Magma Burst", "Magma Eruption", "Launch five magma projectiles", "Create a damaging magma eruption", 12, 32);

    private final String display; private final NamedTextColor color; private final String key;
    private final String p1,p2,a1,a2,d1,d2; private final int cd1,cd2;
    TotemType(String display, NamedTextColor color, String key, String p1,String p2,String a1,String a2,String d1,String d2,int cd1,int cd2){
        this.display=display;this.color=color;this.key=key;this.p1=p1;this.p2=p2;this.a1=a1;this.a2=a2;this.d1=d1;this.d2=d2;this.cd1=cd1;this.cd2=cd2;
    }
    public String display(){return display;} public NamedTextColor color(){return color;} public String key(){return key;}
    public String p1(){return p1;} public String p2(){return p2;} public String a1(){return a1;} public String a2(){return a2;}
    public String d1(){return d1;} public String d2(){return d2;} public int cd1(){return cd1;} public int cd2(){return cd2;}
    public static Optional<TotemType> parse(String s){try{return Optional.of(valueOf(s.toUpperCase(Locale.ROOT)));}catch(Exception e){return Optional.empty();}}
    public static List<TotemType> enabled(org.bukkit.configuration.ConfigurationSection root){
        List<TotemType> out=new ArrayList<>();
        for(TotemType t:values()) if(root.getBoolean(t.name()+".enabled",true)) out.add(t);
        return out;
    }
}

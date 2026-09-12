package me.bogeyman.totempowers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;

public final class TotemItemManager {
    private final TotemPowers plugin; private final NamespacedKey typeKey;
    public TotemItemManager(TotemPowers plugin){this.plugin=plugin;typeKey=new NamespacedKey(plugin,"totem_type");}
    public ItemStack create(TotemType t){
        ItemStack item=new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta m=item.getItemMeta();
        m.displayName(Component.text(t.display(),t.color()));
        List<Component> lore=new ArrayList<>();
        lore.add(Component.text("✦ PASSIVE I",NamedTextColor.GRAY)); lore.add(Component.text(t.p1(),NamedTextColor.WHITE));
        lore.add(Component.text("✦ PASSIVE II",NamedTextColor.GRAY)); lore.add(Component.text(t.p2(),NamedTextColor.WHITE));
        lore.add(Component.text("⚡ ACTIVE I",NamedTextColor.GRAY)); lore.add(Component.text(t.a1(),NamedTextColor.WHITE)); lore.add(Component.text(t.d1(),NamedTextColor.GRAY)); lore.add(Component.text("Cooldown: "+cd1(t)+"s",NamedTextColor.YELLOW));
        lore.add(Component.text("⚡ ACTIVE II",NamedTextColor.GRAY)); lore.add(Component.text(t.a2(),NamedTextColor.WHITE)); lore.add(Component.text(t.d2(),NamedTextColor.GRAY)); lore.add(Component.text("Cooldown: "+cd2(t)+"s",NamedTextColor.YELLOW));
        lore.add(Component.text("Right Click → "+t.a1(),NamedTextColor.AQUA));
        lore.add(Component.text("Shift + Right Click → "+t.a2(),NamedTextColor.AQUA));
        m.lore(lore);
        m.getPersistentDataContainer().set(typeKey,PersistentDataType.STRING,t.name());
        try { m.setItemModel(new NamespacedKey(plugin,"totem/"+t.key())); } catch (Throwable ex) { plugin.getLogger().warning("Failed to set item model for "+t.name()+": "+ex.getMessage()); }
        item.setItemMeta(m); return item;
    }
    private int cd1(TotemType t){return plugin.getConfig().getInt("totems."+t.name()+".cooldown1",t.cd1());}
    private int cd2(TotemType t){return plugin.getConfig().getInt("totems."+t.name()+".cooldown2",t.cd2());}
    public TotemType identify(ItemStack item){
        if(item==null||!item.hasItemMeta())return null;
        String s=item.getItemMeta().getPersistentDataContainer().get(typeKey,PersistentDataType.STRING);
        return s==null?null:TotemType.parse(s).orElse(null);
    }
    public boolean isEquipped(org.bukkit.entity.Player p, TotemType t){
        return identify(p.getInventory().getItemInMainHand())==t || identify(p.getInventory().getItemInOffHand())==t;
    }
}

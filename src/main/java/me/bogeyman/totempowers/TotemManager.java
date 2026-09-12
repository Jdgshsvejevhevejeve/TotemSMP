package me.bogeyman.totempowers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class TotemManager {
    private final TotemPowers plugin;
    private final NamespacedKey key;
    private final Map<UUID,TotemType> cache=new HashMap<>();
    public TotemManager(TotemPowers plugin){this.plugin=plugin;key=new NamespacedKey(plugin,"assigned_totem");}
    public TotemType get(Player p){
        TotemType t=cache.get(p.getUniqueId()); if(t!=null)return t;
        String s=p.getPersistentDataContainer().get(key,PersistentDataType.STRING);
        if(s==null)return null;
        try{t=TotemType.valueOf(s);cache.put(p.getUniqueId(),t);return t;}catch(Exception e){return null;}
    }
    public void assign(Player p, TotemType t){cache.put(p.getUniqueId(),t);p.getPersistentDataContainer().set(key,PersistentDataType.STRING,t.name());}
    public void remove(Player p){cache.remove(p.getUniqueId());p.getPersistentDataContainer().remove(key);}
    public TotemType randomEnabled(){
        List<TotemType> list=TotemType.enabled(plugin.getConfig().getConfigurationSection("totems"));
        return list.isEmpty()?null:list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
    public void clearCache(UUID id){cache.remove(id);}
}

package me.bogeyman.totempowers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*; import org.bukkit.entity.Player; import org.bukkit.scheduler.BukkitTask;
import java.util.*;
public final class RollManager {
 private final TotemPowers plugin; private final Map<UUID,BukkitTask> rolling=new HashMap<>();
 public RollManager(TotemPowers p){plugin=p;}
 public void start(Player p){
  UUID id=p.getUniqueId(); if(rolling.containsKey(id)||plugin.getTotems().get(p)!=null)return;
  TotemType chosen=plugin.getTotems().randomEnabled(); if(chosen==null){p.sendMessage(Component.text("No Totems are enabled in config.yml.",NamedTextColor.RED));return;}
  if(!plugin.getConfig().getBoolean("roll.enabled",true)){finish(p,chosen);return;}
  int total=Math.max(1,plugin.getConfig().getInt("roll.duration-seconds",5))*20;
  BukkitTask task=new org.bukkit.scheduler.BukkitRunnable(){int tick=0,index=0; public void run(){
   if(!p.isOnline()||p.isDead()){cancel(p.getUniqueId());cancel();return;} tick++;
   int interval=Math.max(2,(int)(2+(tick/(double)total)*10));
   if(tick%interval==0){List<TotemType> enabled=TotemType.enabled(plugin.getConfig().getConfigurationSection("totems"));if(!enabled.isEmpty()){TotemType shown=enabled.get(index++%enabled.size());p.sendActionBar(Component.text("✦ TOTEM ROLL ✦  ",NamedTextColor.LIGHT_PURPLE).append(Component.text(shown.display(),shown.color())));p.getWorld().spawnParticle(Particle.PORTAL,p.getLocation().add(0,1,0),8,.4,.7,.4,.1);p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HAT,.7f,1f+tick/(float)total);}}
   if(tick>=total){finish(p,chosen);cancel(p.getUniqueId());cancel();}
  }}.runTaskTimer(plugin,0L,1L); rolling.put(id,task);
 }
 private void finish(Player p,TotemType chosen){plugin.getTotems().assign(p,chosen);p.sendActionBar(Component.text("✦ YOU GOT ✦  ",NamedTextColor.GOLD).append(Component.text(chosen.display(),chosen.color())));p.sendMessage(Component.text("✦ YOU GOT ✦",NamedTextColor.GOLD));p.sendMessage(Component.text(chosen.display(),chosen.color()));p.playSound(p.getLocation(),Sound.UI_TOAST_CHALLENGE_COMPLETE,1f,1.1f);p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,p.getLocation().add(0,1,0),80,.7,.9,.7,.2);p.getInventory().addItem(plugin.getItems().create(chosen));}
 public void cancel(UUID id){BukkitTask task=rolling.remove(id);if(task!=null)task.cancel();}
 public void cancelAll(){for(BukkitTask t:rolling.values())t.cancel();rolling.clear();}
}

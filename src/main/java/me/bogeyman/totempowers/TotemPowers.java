package me.bogeyman.totempowers;
import me.bogeyman.totempowers.commands.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class TotemPowers extends JavaPlugin {
 private TotemManager totems; private TotemItemManager items; private RollManager rolls; private SkillManager skills; private PassiveManager passives;
 @Override public void onEnable(){
  saveDefaultConfig();
  totems=new TotemManager(this);items=new TotemItemManager(this);rolls=new RollManager(this);skills=new SkillManager(this);passives=new PassiveManager(this);
  getServer().getPluginManager().registerEvents(new TotemListener(this),this);
  var cmd=getCommand("totem"); if(cmd!=null){cmd.setExecutor(new TotemCommand(this));cmd.setTabCompleter(new TotemTabCompleter());}
  getLogger().info("TotemPowers enabled with "+TotemType.values().length+" Totems.");
 }
 @Override public void onDisable(){if(rolls!=null)rolls.cancelAll();if(passives!=null)passives.clearAll();if(skills!=null)skills.shutdown();getLogger().info("TotemPowers disabled.");}
 public TotemManager getTotems(){return totems;} public TotemItemManager getItems(){return items;} public RollManager getRolls(){return rolls;} public SkillManager getSkills(){return skills;}
}

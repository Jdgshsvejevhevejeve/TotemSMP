package me.bogeyman.totempowers.commands;
import me.bogeyman.totempowers.*; import net.kyori.adventure.text.Component; import net.kyori.adventure.text.format.NamedTextColor; import org.bukkit.*; import org.bukkit.command.*; import org.bukkit.entity.Player;
public final class TotemCommand implements CommandExecutor {
 private final TotemPowers plugin; public TotemCommand(TotemPowers p){plugin=p;}
 public boolean onCommand(CommandSender s,Command c,String l,String[] a){
  if(a.length==0||a[0].equalsIgnoreCase("info")){Player p=a.length>1?Bukkit.getPlayerExact(a[1]):(s instanceof Player pl?pl:null);if(p==null){s.sendMessage("Player not found.");return true;}TotemType t=plugin.getTotems().get(p);s.sendMessage(Component.text(p.getName()+" → "+(t==null?"None":t.display()),NamedTextColor.LIGHT_PURPLE));return true;}
  if(!s.hasPermission("totempowers.admin")){s.sendMessage(Component.text("No permission.",NamedTextColor.RED));return true;}
  switch(a[0].toLowerCase()){
   case "reroll" -> {Player p=player(a,1);if(p==null)return true;TotemType t=plugin.getTotems().randomEnabled();if(t==null){s.sendMessage("No enabled Totems.");return true;}plugin.getTotems().assign(p,t);s.sendMessage("Rerolled "+p.getName()+" → "+t.name());}
   case "give" -> {Player p=player(a,1);if(p==null||a.length<3)return true;TotemType t=TotemType.parse(a[2]).orElse(null);if(t==null){s.sendMessage("Unknown Totem.");return true;}p.getInventory().addItem(plugin.getItems().create(t));}
   case "remove" -> {Player p=player(a,1);if(p!=null)plugin.getTotems().remove(p);}
   case "restore" -> {Player p=player(a,1);if(p!=null){TotemType t=plugin.getTotems().get(p);if(t!=null)p.getInventory().addItem(plugin.getItems().create(t));}}
   case "reload" -> {plugin.reloadConfig();s.sendMessage("TotemPowers config reloaded.");}
   default -> s.sendMessage("/totem info | reroll <player> | give <player> <totem> | remove <player> | restore <player> | reload");
  } return true;
 }
 private Player player(String[] a,int i){return a.length>i?Bukkit.getPlayerExact(a[i]):null;}
}

package me.bogeyman.totempowers.commands;
import me.bogeyman.totempowers.TotemType; import org.bukkit.*; import org.bukkit.command.*; import java.util.*;
public final class TotemTabCompleter implements TabCompleter{
 public List<String> onTabComplete(CommandSender s,Command c,String l,String[] a){
  if(a.length==1)return List.of("info","reroll","give","remove","restore","reload").stream().filter(x->x.startsWith(a[0].toLowerCase())).toList();
  if(a.length==2&&(a[0].equalsIgnoreCase("reroll")||a[0].equalsIgnoreCase("give")||a[0].equalsIgnoreCase("remove")||a[0].equalsIgnoreCase("restore")))return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
  if(a.length==3&&a[0].equalsIgnoreCase("give"))return Arrays.stream(TotemType.values()).map(Enum::name).toList();
  return List.of();
 }
}

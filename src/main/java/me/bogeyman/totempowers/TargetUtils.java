package me.bogeyman.totempowers;
import org.bukkit.entity.*; import org.bukkit.util.Vector;
public final class TargetUtils {
 private TargetUtils(){}
 public static Vector direction(Player p){return p.getEyeLocation().getDirection().clone().normalize();}
}

package me.bogeyman.totempowers;
import org.bukkit.entity.*; import java.util.*;
public final class DamageUtils {
 private DamageUtils(){}
 public static void damage(LivingEntity source,LivingEntity target,double amount){if(target.isDead()||target.equals(source))return; target.damage(amount,source);}
 public static void area(LivingEntity source,org.bukkit.Location center,double radius,double damage){
  for(LivingEntity e:AbilityUtils.nearbyLiving(center,radius,source))damage(source,e,damage);
 }
}

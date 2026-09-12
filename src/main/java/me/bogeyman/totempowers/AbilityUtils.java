package me.bogeyman.totempowers;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.util.Vector; import java.util.*;
public final class AbilityUtils {
 private AbilityUtils(){}
 public static Location safeTeleport(Location start, Vector dir, double max){
  World w=start.getWorld(); Location last=start.clone(); Vector v=dir.clone().normalize().multiply(.5);
  int steps=(int)(max*2);
  for(int i=0;i<steps;i++){Location n=last.clone().add(v); if(n.getY()<w.getMinHeight()+1||n.getY()>w.getMaxHeight()-2)break;
   if(!n.getBlock().isPassable()||!n.clone().add(0,1,0).getBlock().isPassable())break;
   if(n.getBlock().isLiquid())break; last=n;}
  return last;
 }
 public static void burst(Location l, Particle p,int count,double spread){l.getWorld().spawnParticle(p,l,count,spread,spread,spread,.08);}
 public static void sound(Location l,Sound s){l.getWorld().playSound(l,s,1f,1f);}
 public static List<LivingEntity> nearbyLiving(Location l,double r,Entity except){
  List<LivingEntity> out=new ArrayList<>(); for(Entity e:l.getWorld().getNearbyEntities(l,r,r,r)) if(e instanceof LivingEntity le&&e!=except&&!le.isDead())out.add(le); return out;
 }
 public static LivingEntity target(Player p,double range){
  return p.getTargetEntity((int)Math.ceil(range)) instanceof LivingEntity le && !le.equals(p)?le:null;
 }
 public static Location ray(Player p,double range){return p.getEyeLocation().clone().add(p.getEyeLocation().getDirection().normalize().multiply(range));}
}

package me.bogeyman.totempowers;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.event.*; import org.bukkit.event.block.Action; import org.bukkit.event.entity.*; import org.bukkit.event.player.*; import org.bukkit.event.inventory.*; import org.bukkit.event.entity.EntityTargetEvent; import org.bukkit.potion.*; import java.util.*; import java.util.concurrent.ThreadLocalRandom;
public final class TotemListener implements Listener {
 private final TotemPowers plugin; private final Set<UUID> soulCd=new HashSet<>(); private final Set<UUID> blazeCd=new HashSet<>(); private final Set<UUID> dragonCd=new HashSet<>();
 public TotemListener(TotemPowers p){plugin=p;}
 @EventHandler public void join(PlayerJoinEvent e){Player p=e.getPlayer();if(plugin.getTotems().get(p)==null)plugin.getRolls().start(p);}
 @EventHandler public void quit(PlayerQuitEvent e){UUID id=e.getPlayer().getUniqueId();plugin.getRolls().cancel(id);plugin.getSkills().clear(id);soulCd.remove(id);blazeCd.remove(id);dragonCd.remove(id);plugin.getTotems().clearCache(id);}
 @EventHandler public void interact(PlayerInteractEvent e){if(e.getAction()!=Action.RIGHT_CLICK_AIR&&e.getAction()!=Action.RIGHT_CLICK_BLOCK)return;Player p=e.getPlayer();TotemType t=plugin.getItems().identify(e.getItem());if(t==null||plugin.getTotems().get(p)!=t||!plugin.getItems().isEquipped(p,t))return;e.setCancelled(true);plugin.getSkills().use(p,p.isSneaking());}
 @EventHandler public void damage(EntityDamageEvent e){
  if(!(e.getEntity() instanceof Player p))return; TotemType t=plugin.getTotems().get(p); if(t==null||!plugin.getItems().isEquipped(p,t))return;
  if(t==TotemType.ENDER&&e.getCause()==EntityDamageEvent.DamageCause.FALL)e.setDamage(e.getDamage()*.6);
  if(t==TotemType.TIDE&&e.getCause()==EntityDamageEvent.DamageCause.DROWNING)e.setDamage(e.getDamage()*.5);
  if(t==TotemType.WITHER&&e.getCause()==EntityDamageEvent.DamageCause.WITHER)e.setCancelled(true);
  if(t==TotemType.SOUL&&p.getHealth()-e.getFinalDamage()<=4&&!soulCd.contains(p.getUniqueId())){p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,60,1,true,false,true));soulCd.add(p.getUniqueId());plugin.getServer().getScheduler().runTaskLater(plugin,()->soulCd.remove(p.getUniqueId()),900L);}
 }
 @EventHandler public void byEntity(EntityDamageByEntityEvent e){
  if(plugin.getSkills().handleProjectileDamage(e))return;
  plugin.getSkills().damageEvent(e);
  if(e.getEntity() instanceof Player victim){TotemType t=plugin.getTotems().get(victim);if(t!=null&&plugin.getItems().isEquipped(victim,t)&&t==TotemType.WARDEN&&e.getDamager() instanceof LivingEntity attacker&&attacker.getLocation().distanceSquared(victim.getLocation())<=25)e.setDamage(e.getDamage()*.8);}
  if(e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity target){TotemType t=plugin.getTotems().get(p);if(t!=null&&plugin.getItems().isEquipped(p,t)){
   if(t==TotemType.BLAZE&&!blazeCd.contains(p.getUniqueId())&&ThreadLocalRandom.current().nextDouble()<.15){target.setFireTicks(Math.max(target.getFireTicks(),20));AbilityUtils.burst(target.getLocation(),Particle.FLAME,10,.2);blazeCd.add(p.getUniqueId());plugin.getServer().getScheduler().runTaskLater(plugin,()->blazeCd.remove(p.getUniqueId()),10L);}
   if(t==TotemType.DRAGON&&!dragonCd.contains(p.getUniqueId())&&ThreadLocalRandom.current().nextDouble()<.08){AbilityUtils.burst(target.getLocation(),Particle.DRAGON_BREATH,25,.5);dragonCd.add(p.getUniqueId());plugin.getServer().getScheduler().runTaskLater(plugin,()->dragonCd.remove(p.getUniqueId()),10L);}
  }}
  if(e.getEntity() instanceof Player p){TotemType t=plugin.getTotems().get(p);if(t==TotemType.STORM&&plugin.getItems().isEquipped(p,t)&&e.getDamager() instanceof LivingEntity attacker&&ThreadLocalRandom.current().nextDouble()<.10)AbilityUtils.burst(attacker.getLocation(),Particle.ELECTRIC_SPARK,25,.4);}
 }
 @EventHandler public void potion(EntityPotionEffectEvent e){if(!(e.getEntity() instanceof Player p))return;TotemType t=plugin.getTotems().get(p);if(t==TotemType.FROST&&e.getNewEffect()!=null&&e.getNewEffect().getType()==PotionEffectType.SLOWNESS&&plugin.getItems().isEquipped(p,t)&&e.getNewEffect().getAmplifier()<=0)e.setCancelled(true);}
 @EventHandler public void entityTarget(EntityTargetEvent e){
  if(!(e.getTarget() instanceof Player p))return;
  TotemType t=plugin.getTotems().get(p);
  if(t==TotemType.SHADOW&&plugin.getItems().isEquipped(p,t)&&e.getEntity() instanceof Monster && ThreadLocalRandom.current().nextDouble()<0.50)e.setCancelled(true);
 }
 @EventHandler public void projectileHit(ProjectileHitEvent e){plugin.getSkills().handleProjectileHit(e);}

}

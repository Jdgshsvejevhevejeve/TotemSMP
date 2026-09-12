package me.bogeyman.totempowers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.*;

public final class SkillManager {
    private final TotemPowers plugin;
    private final CooldownManager cd = new CooldownManager();
    private final Set<UUID> veil = new HashSet<>();
    private final Set<BukkitTask> tasks = new HashSet<>();
    private final Set<UUID> charging = new HashSet<>();
    private final NamespacedKey projectileKey;
    private final NamespacedKey projectileDamageKey;
    private final NamespacedKey projectileFireKey;

    public SkillManager(TotemPowers p) {
        plugin = p;
        projectileKey = new NamespacedKey(p, "ability_projectile");
        projectileDamageKey = new NamespacedKey(p, "ability_damage");
        projectileFireKey = new NamespacedKey(p, "ability_fire_seconds");
    }

    public void clear(UUID id) { cd.clear(id); veil.remove(id); charging.remove(id); }
    public void shutdown() { for (BukkitTask t : new HashSet<>(tasks)) t.cancel(); tasks.clear(); cd.clearAll(); veil.clear(); charging.clear(); }

    public void use(Player p, boolean second) {
        if (!plugin.getConfig().getBoolean("skills.enabled", true)) return;
        TotemType t = plugin.getTotems().get(p);
        if (t == null || !plugin.getItems().isEquipped(p, t)) {
            p.sendActionBar(Component.text("Equip your assigned Totem in your hand.", NamedTextColor.RED)); return;
        }
        String key = t.name() + "_" + (second ? 2 : 1);
        double left = cd.remaining(p.getUniqueId(), key);
        if (left > 0) {
            p.sendActionBar(Component.text("⚠ Ability on cooldown: " + String.format(Locale.US, "%.1f", left) + "s", NamedTextColor.RED)); return;
        }
        int secs = second ? plugin.getConfig().getInt("totems." + t.name() + ".cooldown2", t.cd2()) : plugin.getConfig().getInt("totems." + t.name() + ".cooldown1", t.cd1());
        boolean ok = second ? skill2(p, t) : skill1(p, t);
        if (ok) {
            cd.set(p.getUniqueId(), key, secs);
            p.sendActionBar(Component.text("⚡ " + (second ? t.a2() : t.a1()).toUpperCase(Locale.ROOT) + " ACTIVATED", t.color()));
        }
    }

    private boolean skill1(Player p, TotemType t) {
        switch (t) {
            case ENDER -> { Location dest = AbilityUtils.safeTeleport(p.getLocation(), TargetUtils.direction(p), 12); if (dest.distanceSquared(p.getLocation()) < .01) return false; p.teleport(dest); p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,40,1,true,false,true)); burst(dest, Particle.PORTAL,60,.6); sound(dest,Sound.ENTITY_ENDERMAN_TELEPORT); return true; }
            case WARDEN -> { LivingEntity e=AbilityUtils.target(p,18); if(e==null)return false; sonicDamage(p,e,6); burst(e.getLocation(),Particle.SONIC_BOOM,1,0); sound(e.getLocation(),Sound.ENTITY_WARDEN_SONIC_BOOM); return true; }
            case WITHER -> { Location l=AbilityUtils.ray(p,20); burst(l,Particle.SMOKE,50,.8); for(LivingEntity e:AbilityUtils.nearbyLiving(l,5,p)) { DamageUtils.damage(p,e,8); e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,80,0)); } return true; }
            case BLAZE -> { launchFireballs(p,3,.12,4,"BLAZE_FLAME"); return true; }
            case STORM -> { LivingEntity e=AbilityUtils.target(p,20); Location l=e!=null?e.getLocation():AbilityUtils.ray(p,20); p.getWorld().strikeLightningEffect(l); if(e!=null)DamageUtils.damage(p,e,10); return true; }
            case FROST -> { LivingEntity e=AbilityUtils.target(p,18); if(e==null)return false; DamageUtils.damage(p,e,6); e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,60,3)); burst(e.getLocation(),Particle.SNOWFLAKE,50,.6); return true; }
            case TIDE -> { launchProjectile(p, "TIDE_SPEAR", 8, 0, false, 18); return true; }
            case SHADOW -> { veil.add(p.getUniqueId()); p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,120,0,true,false,true)); burst(p.getLocation(),Particle.SMOKE,35,.5); return true; }
            case DRAGON -> { Set<UUID> hit=new HashSet<>(); Vector d=TargetUtils.direction(p); Location o=p.getEyeLocation(); for(int i=1;i<=10;i++){Location l=o.clone().add(d.clone().multiply(i)); burst(l,Particle.DRAGON_BREATH,12,.5); for(LivingEntity e:AbilityUtils.nearbyLiving(l,1.5,p)) if(hit.add(e.getUniqueId())) DamageUtils.damage(p,e,10);} sound(p.getLocation(),Sound.ENTITY_ENDER_DRAGON_GROWL); return true; }
            case SOUL -> { launchProjectile(p,"SOUL_FLAME",8,2,true,18); return true; }
            case MAGMA -> { launchFireballs(p,5,.08,3,"MAGMA_BURST"); return true; }
        }
        return false;
    }

    private boolean skill2(Player p, TotemType t) {
        switch(t) {
            case ENDER -> { if(!charging.add(p.getUniqueId())) return false; Location l=AbilityUtils.ray(p,16); burst(p.getLocation(),Particle.PORTAL,35,.5); sound(p.getLocation(),Sound.BLOCK_PORTAL_AMBIENT); BukkitTask[] h=new BukkitTask[1]; h[0]=plugin.getServer().getScheduler().runTaskLater(plugin,()->{ charging.remove(p.getUniqueId()); tasks.remove(h[0]); if(!p.isOnline()||p.isDead()||!plugin.getItems().isEquipped(p,TotemType.ENDER)) return; Location hit=AbilityUtils.ray(p,16); burst(hit,Particle.PORTAL,100,2); sound(hit,Sound.BLOCK_PORTAL_TRAVEL); for(LivingEntity e:AbilityUtils.nearbyLiving(hit,4,p)){ pull(hit,e,.8); DamageUtils.damage(p,e,8); } },30L); tasks.add(h[0]); return true; }
            case WARDEN -> { Set<UUID> hit=new HashSet<>(); for(LivingEntity e:AbilityUtils.nearbyLiving(p.getLocation(),6,p)) if(hit.add(e.getUniqueId())){sonicDamage(p,e,4); e.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,100,0)); e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,60,1));} burst(p.getLocation(),Particle.SCULK_SOUL,100,2); return true; }
            case WITHER -> { Set<UUID> hit=new HashSet<>(); for(LivingEntity e:AbilityUtils.nearbyLiving(p.getLocation(),5,p)) if(hit.add(e.getUniqueId())){DamageUtils.damage(p,e,10); e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,120,0)); pushAway(p,e,.9,.45);} burst(p.getLocation(),Particle.EXPLOSION,20,1); return true; }
            case BLAZE -> { startInfernoRing(p); return true; }
            case STORM -> { Location old=p.getLocation().clone(); Location d=AbilityUtils.safeTeleport(old,TargetUtils.direction(p),10); if(d.distanceSquared(old)<.01)return false; p.teleport(d); p.getWorld().strikeLightningEffect(old); p.getWorld().strikeLightningEffect(d); for(LivingEntity e:AbilityUtils.nearbyLiving(d,3,p)){DamageUtils.damage(p,e,6);pushForward(p,e,.8,.4);} return true; }
            case FROST -> { Set<UUID> hit=new HashSet<>(); for(LivingEntity e:AbilityUtils.nearbyLiving(p.getLocation(),6,p)) if(hit.add(e.getUniqueId())){DamageUtils.damage(p,e,6);e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,80,4));e.setFreezeTicks(Math.max(e.getFreezeTicks(),20));} burst(p.getLocation(),Particle.SNOWFLAKE,120,2); return true; }
            case TIDE -> { Vector d=TargetUtils.direction(p); Set<UUID> hit=new HashSet<>(); for(int i=1;i<=12;i++){Location l=p.getLocation().clone().add(d.clone().multiply(i)); burst(l,Particle.BUBBLE,18,.7); for(LivingEntity e:AbilityUtils.nearbyLiving(l,1.5,p)) if(hit.add(e.getUniqueId())){DamageUtils.damage(p,e,8);e.setVelocity(d.clone().multiply(1.2).setY(.3));}} return true; }
            case SHADOW -> { LivingEntity e=AbilityUtils.target(p,12); if(e==null)return false; Location behind=safeBehind(e); if(behind==null)return false; p.teleport(behind); DamageUtils.damage(p,e,10); e.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,40,0)); burst(behind,Particle.SMOKE,50,.6); return true; }
            case DRAGON -> { Vector d=TargetUtils.direction(p); p.setVelocity(d.clone().multiply(1.4).setY(.8)); BukkitTask[] holder=new BukkitTask[1]; holder[0]=plugin.getServer().getScheduler().runTaskTimer(plugin,()->{ if(!p.isOnline()||p.isDead()||!plugin.getItems().isEquipped(p,TotemType.DRAGON)){holder[0].cancel();tasks.remove(holder[0]);return;} if(p.isOnGround()){Set<UUID> hit=new HashSet<>();for(LivingEntity e:AbilityUtils.nearbyLiving(p.getLocation(),5,p))if(hit.add(e.getUniqueId())){DamageUtils.damage(p,e,12);pushAway(p,e,1.2,.6);}burst(p.getLocation(),Particle.DRAGON_BREATH,120,2);sound(p.getLocation(),Sound.ENTITY_ENDER_DRAGON_GROWL);holder[0].cancel();tasks.remove(holder[0]);}},2L,1L); tasks.add(holder[0]); return true; }
            case SOUL -> { double hp=Math.min(p.getMaxHealth(),p.getHealth()+8); p.setHealth(hp); p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,80,1,true,false,true)); burst(p.getLocation(),Particle.SOUL,80,1); return true; }
            case MAGMA -> { Set<UUID> hit=new HashSet<>(); for(LivingEntity e:AbilityUtils.nearbyLiving(p.getLocation(),6,p))if(hit.add(e.getUniqueId())){DamageUtils.damage(p,e,10);e.setFireTicks(60);pushAway(p,e,1,.5);} burst(p.getLocation(),Particle.LAVA,100,2); sound(p.getLocation(),Sound.BLOCK_LAVA_POP); return true; }
        }
        return false;
    }

    private void startInfernoRing(Player p){
        UUID owner=p.getUniqueId();
        BukkitTask[] h=new BukkitTask[1];
        h[0]=plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable(){
            int ticks=0;
            final Map<UUID,Long> lastHit=new HashMap<>();
            @Override public void run(){
                if(!p.isOnline()||p.isDead()||!plugin.getItems().isEquipped(p,TotemType.BLAZE)||ticks>=80){ h[0].cancel(); tasks.remove(h[0]); return; }
                Location c=p.getLocation();
                for(int i=0;i<24;i++){ double a=(Math.PI*2*i)/24.0; Location l=c.clone().add(Math.cos(a)*5,0.15,Math.sin(a)*5); burst(l,Particle.FLAME,3,.12); }
                for(LivingEntity e:AbilityUtils.nearbyLiving(c,5,p)){
                    if(e.getLocation().distanceSquared(c)>30.25) continue;
                    long now=System.currentTimeMillis(), until=lastHit.getOrDefault(e.getUniqueId(),0L);
                    if(until>now) continue;
                    lastHit.put(e.getUniqueId(),now+900L); DamageUtils.damage(p,e,2); e.setFireTicks(Math.max(e.getFireTicks(),20));
                }
                ticks+=5;
            }
        },0L,5L);
        tasks.add(h[0]);
    }

    private void launchFireballs(Player p,int count,double spread,double damage,String id){
        for(int i=0;i<count;i++){SmallFireball f=p.launchProjectile(SmallFireball.class);Vector d=TargetUtils.direction(p).clone().rotateAroundY((i-(count-1)/2.0)*spread);f.setDirection(d);f.setIsIncendiary(false);f.setYield(0); markProjectile(f,id,damage, id.equals("BLAZE_FLAME")?1:0);}
    }
    private void launchProjectile(Player p,String id,double damage,int fireSeconds,boolean soul,double range){
        Snowball s=p.launchProjectile(Snowball.class); s.setItem(new ItemStack(soul?Material.SOUL_SAND:Material.PRISMARINE_SHARD)); markProjectile(s,id,damage,fireSeconds); s.setVelocity(TargetUtils.direction(p).multiply(Math.min(2.2,1.4))); 
    }
    private void markProjectile(Entity e,String id,double damage,int fireSeconds){
        e.getPersistentDataContainer().set(projectileKey,PersistentDataType.STRING,id);
        e.getPersistentDataContainer().set(projectileDamageKey,PersistentDataType.DOUBLE,damage);
        e.getPersistentDataContainer().set(projectileFireKey,PersistentDataType.INTEGER,fireSeconds);
    }
    public NamespacedKey projectileKey(){ return projectileKey; }

    public void handleProjectileHit(ProjectileHitEvent event){
        Projectile pr=event.getEntity(); String id=pr.getPersistentDataContainer().get(projectileKey,PersistentDataType.STRING); if(id==null)return;
        if(event.getHitEntity() instanceof LivingEntity target && pr.getShooter() instanceof Player owner){
            double damage=pr.getPersistentDataContainer().getOrDefault(projectileDamageKey,PersistentDataType.DOUBLE,0.0);
            DamageUtils.damage(owner,target,damage);
            int fire=pr.getPersistentDataContainer().getOrDefault(projectileFireKey,PersistentDataType.INTEGER,0);
            if(fire>0)target.setFireTicks(Math.max(target.getFireTicks(),fire*20));
        }
        pr.remove();
    }

    public boolean handleProjectileDamage(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof Projectile pr))return false;
        String id=pr.getPersistentDataContainer().get(projectileKey,PersistentDataType.STRING); if(id==null)return false;
        event.setCancelled(true);
        if(!(pr.getShooter() instanceof Player owner))return true;
        if(!(event.getEntity() instanceof LivingEntity target))return true;
        double damage=pr.getPersistentDataContainer().getOrDefault(projectileDamageKey,PersistentDataType.DOUBLE,0.0);
        DamageUtils.damage(owner,target,damage);
        int fire=pr.getPersistentDataContainer().getOrDefault(projectileFireKey,PersistentDataType.INTEGER,0);
        if(fire>0)target.setFireTicks(Math.max(target.getFireTicks(),fire*20));
        pr.remove(); return true;
    }

    private Location safeBehind(LivingEntity e){
        Vector back=e.getLocation().getDirection().clone().setY(0).normalize().multiply(-1.5); if(back.lengthSquared()<.01)back=new Vector(0,0,-1.5); Location l=e.getLocation().clone().add(back); if(!isSafe(l)) return null; return l;
    }
    private boolean isSafe(Location l){return l.getWorld()!=null&&!l.getBlock().isLiquid()&&l.getBlock().isPassable()&&l.clone().add(0,1,0).getBlock().isPassable();}
    private void pull(Location c,LivingEntity e,double power){Vector v=c.toVector().subtract(e.getLocation().toVector());if(v.lengthSquared()<.0001)return;e.setVelocity(v.normalize().multiply(power));}
    private void pushAway(LivingEntity source,LivingEntity e,double power,double y){Vector v=e.getLocation().toVector().subtract(source.getLocation().toVector());if(v.lengthSquared()<.0001)v=new Vector(0,0,1);e.setVelocity(v.normalize().multiply(power).setY(y));}
    private void pushForward(Player p,LivingEntity e,double power,double y){e.setVelocity(TargetUtils.direction(p).multiply(power).setY(y));}
    private void sonicDamage(Player source,LivingEntity target,double hearts){
        // Half normal damage and half direct health damage: this gives a partial armor bypass while Resistance still affects the normal half.
        double total=hearts*2.0; double normal=total*0.5; double direct=total-normal;
        target.damage(normal,source);
        double resistance=1.0; PotionEffect r=target.getPotionEffect(PotionEffectType.RESISTANCE); if(r!=null) resistance=Math.max(0,1.0-0.2*(r.getAmplifier()+1));
        double finalDirect=direct*resistance; target.setHealth(Math.max(0,target.getHealth()-finalDirect));
    }
    private void burst(Location l,Particle p,int count,double spread){AbilityUtils.burst(l,p,count,spread);}
    private void sound(Location l,Sound s){AbilityUtils.sound(l,s);}
    public void cancelVeil(Player p){if(veil.remove(p.getUniqueId()))p.removePotionEffect(PotionEffectType.INVISIBILITY);}
    public void damageEvent(EntityDamageByEntityEvent e){if(e.getEntity() instanceof Player p&&veil.contains(p.getUniqueId()))cancelVeil(p);}
}

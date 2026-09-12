package me.bogeyman.totempowers;
import java.util.*;
public final class CooldownManager {
 private final Map<UUID,Map<String,Long>> data=new HashMap<>();
 public void set(UUID id,String skill,double seconds){data.computeIfAbsent(id,k->new HashMap<>()).put(skill,System.nanoTime()+(long)(seconds*1_000_000_000L));}
 public double remaining(UUID id,String skill){Long t=data.getOrDefault(id,Collections.emptyMap()).get(skill);if(t==null)return 0;return Math.max(0,(t-System.nanoTime())/1_000_000_000.0);}
 public void clear(UUID id){data.remove(id);}
 public void clearAll(){data.clear();}
}

package org.github.kovalski.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.github.kovalski.HorseManager;
import org.github.kovalski.TwoPlayerHorseRiding;
import org.github.kovalski.event.HorseDismountEvent;
import org.github.kovalski.event.HorseMountEvent;
import org.github.kovalski.stand.StandMoveController;
import org.github.kovalski.stand.StandMoveHandler;
import org.github.kovalski.task.HorseBreed;

public class HorseListener implements Listener {

    private static final TwoPlayerHorseRiding instance = TwoPlayerHorseRiding.getInstance();
    private final HorseManager horseManager = instance.getHorseManager();

    @EventHandler
    public void onHorseTookDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)){
            return;
        }
        if (instance.getYamlConfig().getBoolean("auto_horse_feed")){
            LivingEntity horse = (LivingEntity) event.getEntity();
            if (horseManager.isTwoPlayerAllowedEntity(horse)) {
                double damage = event.getDamage();
                double health = horse.getHealth();
                if (damage >= health) {
                    Player passanger = horseManager.getRider(horse);
                    if (passanger == null){
                        return;
                    }
                    event.setCancelled(true);
                    new HorseBreed(passanger, horse, new ItemStack(Material.HAY_BLOCK), event.getDamage());
                }
            }
        }
    }

    @EventHandler
    public void onMount(HorseMountEvent event){
        LivingEntity horse = event.getHorse();
        if (horseManager.isTwoPlayerAllowedEntity(horse)){
            if (event.getRider() instanceof Player){
                StandMoveHandler.addStandMove((Player) event.getRider(), horse, horse.getType());
            }
        }
    }

    @EventHandler
    public void onDismount(HorseDismountEvent event){
        LivingEntity horse = event.getHorse();
        Location location = event.getDismountLocation();
        if (horseManager.isTwoPlayerAllowedEntity(horse)){
            switch (event.getDismountType()){
                case TELEPORT:
                    StandMoveHandler.teleportIfExist(horse, location);
                case DISMOUNT:
                    StandMoveHandler.removeIfExist(horse);
                case DEATH:
                    StandMoveHandler.removeIfExist(horse);
            }
        }
        if (horse instanceof ArmorStand){
            if (event.getRider() instanceof LivingEntity){
                for (StandMoveController standMoveController : StandMoveHandler.horseStandMoveList){
                    if (standMoveController.getStand().equals(horse)){
                        standMoveController.getStandMove().sendVisiblePacket();
                        return;
                    }
                }
            }
        }
    }
}
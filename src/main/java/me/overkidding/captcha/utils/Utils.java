package me.overkidding.captcha.utils;


import me.overkidding.captcha.SimpleCaptcha;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Utils {

    private static final List<Material> invalid = Arrays.asList(
            Material.SIGN_POST, Material.WALL_SIGN,
            Material.LAVA, Material.STATIONARY_LAVA,
            Material.CAKE_BLOCK,
            Material.PISTON_MOVING_PIECE, Material.PISTON_EXTENSION,
            Material.WATER, Material.STATIONARY_WATER,
            Material.WOODEN_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.IRON_DOOR, Material.ACACIA_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR, Material.IRON_DOOR_BLOCK,
            Material.DAYLIGHT_DETECTOR_INVERTED, Material.DAYLIGHT_DETECTOR,
            Material.BED_BLOCK, Material.BED,
            Material.STANDING_BANNER, Material.WALL_BANNER,
            Material.BREWING_STAND, Material.CAULDRON);

    public static List<Material> getRandomMaterials(Material chosen){
        List<Material> materialList = new ArrayList<>();

        while(materialList.size() < 27){
            Material random = getRandomMaterial();
            while (materialList.contains(random)){
                random = getRandomMaterial();
            }
            materialList.add(random);
        }

        if(!materialList.contains(chosen)){
            int randomNumber = new Random().nextInt(materialList.size() - 1);
            materialList.set(randomNumber, chosen);
        }

        System.out.println(materialList.size());

        Collections.shuffle(materialList);

        return materialList;
    }

    public static Material getRandomMaterial(){
        List<Material> materialList = new ArrayList<>();
        for(Material material : Material.values()){
            if(material != Material.AIR && !material.isOccluding() && !material.isTransparent() && !invalid.contains(material)){
                materialList.add(material);
            }
        }
        return materialList.get(new Random().nextInt(materialList.size() - 1));
    }

    public static void openInventory(Player player, Material item){

        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory inventory = Bukkit.createInventory(player, 27, ChatColor.DARK_RED + "Choose: " + item.name());
                int x = 0;
                List<Material> materials = Utils.getRandomMaterials(item);
                for(int j = 0; j < 27; j++){
                    Material material = materials.get(j);
                    ItemStack stack = new ItemStack(material);
                    ItemMeta meta = stack.getItemMeta();
                    if(material == item){
                        meta.setDisplayName(ChatColor.GREEN.toString() + ChatColor.BOLD + material.name());
                    }else{
                        meta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + material.name());
                    }
                    stack.setItemMeta(meta);

                    inventory.setItem(j, stack);
                    //System.out.println(++x + ":" + material.name());
                }

                player.openInventory(inventory);
            }
        }.runTaskLaterAsynchronously(SimpleCaptcha.getInstance(), 4L);

    }
}

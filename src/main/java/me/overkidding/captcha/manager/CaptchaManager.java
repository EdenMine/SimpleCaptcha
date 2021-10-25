package me.overkidding.captcha.manager;

import me.overkidding.captcha.SimpleCaptcha;
import me.overkidding.captcha.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CaptchaManager implements Listener {

    private final SimpleCaptcha plugin = SimpleCaptcha.getInstance();
    private final Map<UUID, Material> players = new HashMap<>();
    private final Map<UUID, Integer> tries = new HashMap<>();
    private final Map<UUID, Long> sessions = new HashMap<>();

    public CaptchaManager(){
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        long session = sessions.getOrDefault(player.getUniqueId(), -1L);

        if(session == -1){
            Material item = Utils.getRandomMaterial();
            players.put(player.getUniqueId(), item);

            Utils.openInventory(player, item);
        }else{
            long untilExpires = session - System.currentTimeMillis();
            if(untilExpires <= 0){
                sessions.remove(player.getUniqueId());

                Material item = Utils.getRandomMaterial();
                players.put(player.getUniqueId(), item);

                Utils.openInventory(player, item);
            }else{
                player.sendMessage(ChatColor.GREEN + "You have " + Utils.formatDuration(untilExpires) + " until next verification.");
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getClickedInventory() == null) return;
        if(event.getCurrentItem() == null) return;
        if(event.getWhoClicked() == null) return;
        if(!event.getClickedInventory().getTitle().contains("Choose:")) return;

        Player player = (Player) event.getWhoClicked();

        if(players.containsKey(player.getUniqueId())){
            Material item = players.get(player.getUniqueId());
            if(event.getCurrentItem().getType() != item){
                int currentTry = tries.getOrDefault(player.getUniqueId(), 1);
                if(currentTry == 3){
                    player.kickPlayer(ChatColor.RED + "Wrong captcha!");
                }else{
                    tries.put(player.getUniqueId(), currentTry + 1);
                    player.sendMessage(ChatColor.RED + "Wrong captcha item! (" + event.getCurrentItem().getType().name() + ")");
                    player.sendMessage(ChatColor.YELLOW + "You have " + (3 - currentTry) + " tries left.");
                    player.closeInventory();
                }
            }else{
                player.sendMessage(ChatColor.GREEN + "Successfully verified!");
                player.sendMessage(ChatColor.DARK_GREEN + "You have 15 minutes until next verification.");
                players.remove(player.getUniqueId());
                tries.remove(player.getUniqueId());
                sessions.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15));
                player.closeInventory();
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if(players.containsKey(player.getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();

        if(players.containsKey(player.getUniqueId())){
            Material item = players.get(player.getUniqueId());
            Utils.openInventory(player, item);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        players.remove(event.getPlayer().getUniqueId());
        tries.remove(event.getPlayer().getUniqueId());
    }


}

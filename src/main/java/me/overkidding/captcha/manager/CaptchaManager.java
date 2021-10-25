package me.overkidding.captcha.manager;

import lombok.Getter;
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

@Getter
public class CaptchaManager implements Listener {

    private final SimpleCaptcha plugin = SimpleCaptcha.getInstance();
    private final Map<UUID, Material> players = new HashMap<>();
    private final Map<UUID, Integer> tries = new HashMap<>();
    private final Map<UUID, Long> sessions = new HashMap<>();

    /**
     *
     * Configuration variables
     *
     * inventorySize - Max Captcha Inventory Size
     * itemDisplayName - Item Display Name %item_name% to display Material Enum Name
     * itemColor_Correct - Color of the correct item in the gui
     * itemColor_Wrong - Color of the wrong item in the gui
     * title - Title of the Inventory
     *
     * maxTries - Max Tries that a player can do until he get kicked.
     * sessionMinutes - Time for the player that don't need to verify him self.
     *
     **/
    private final int inventorySize;
    private final String itemDisplayName, itemColor_Correct, itemColor_Wrong, title;

    private final String verifiedMessage, sessionMessage;
    private final String wrongKickMessage, wrongMessage;

    private final int maxTries;
    private final int sessionMinutes;

    public CaptchaManager(){
        String path = "captcha.";

        inventorySize = plugin.getConfig().getInt(path + "gui.size");
        itemDisplayName = plugin.getConfig().getString(path + "gui.item_display_name");
        itemColor_Correct = plugin.getConfig().getString(path + "gui.correct_item_display_color");
        itemColor_Wrong = plugin.getConfig().getString(path + "gui.wrong_item_display_color");
        title = plugin.getConfig().getString(path + "gui.title");

        verifiedMessage = plugin.getConfig().getString(path + "messages.verified");
        sessionMessage = plugin.getConfig().getString(path + "messages.session-message");

        wrongKickMessage = plugin.getConfig().getString(path + "messages.wrong.kick");
        wrongMessage = plugin.getConfig().getString(path + "messages.wrong.message");

        maxTries = plugin.getConfig().getInt("captcha.tries");
        sessionMinutes = plugin.getConfig().getInt("captcha.session");

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        long session = sessions.getOrDefault(player.getUniqueId(), -1L);

        if(session == -1){
            Material item = Utils.getRandomMaterial();
            players.put(player.getUniqueId(), item);

            Utils.openInventory(this, player, item);
        }else{
            long untilExpires = session - System.currentTimeMillis();
            if(untilExpires <= 0){
                sessions.remove(player.getUniqueId());

                Material item = Utils.getRandomMaterial();
                players.put(player.getUniqueId(), item);

                Utils.openInventory(this, player, item);
            }else{
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', sessionMessage.replace("%time%", Utils.formatDuration(untilExpires))));
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getClickedInventory() == null) return;
        if(event.getCurrentItem() == null) return;
        if(event.getWhoClicked() == null) return;
        if(!event.getClickedInventory().getTitle().contains(ChatColor.translateAlternateColorCodes('&', title.replace("%item%", "")))) return;

        Player player = (Player) event.getWhoClicked();

        if(players.containsKey(player.getUniqueId())){
            Material item = players.get(player.getUniqueId());
            if(event.getCurrentItem().getType() != item){
                int currentTry = tries.getOrDefault(player.getUniqueId(), 1);
                if(currentTry == maxTries){
                    player.kickPlayer(ChatColor.translateAlternateColorCodes('&', wrongKickMessage));
                }else{
                    tries.put(player.getUniqueId(), currentTry + 1);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', wrongMessage.replace("%tries%", (maxTries - currentTry) + "")));
                    player.closeInventory();
                }
            }else{
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', verifiedMessage));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', sessionMessage.replace("%time%", "15 minutes")));
                players.remove(player.getUniqueId());
                tries.remove(player.getUniqueId());
                sessions.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(sessionMinutes));
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
            Utils.openInventory(this, player, item);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        players.remove(event.getPlayer().getUniqueId());
        tries.remove(event.getPlayer().getUniqueId());
    }


}

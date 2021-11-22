package me.overkidding.captcha.manager;

import fr.xephi.authme.events.LoginEvent;
import me.overkidding.captcha.SimpleCaptcha;
import me.overkidding.captcha.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuthMeListener implements Listener {

    @EventHandler
    public void onAuthMeLogin(LoginEvent event){
        Player player = event.getPlayer();

        long session = SimpleCaptcha.getInstance().getCaptchaManager().getSessions().getOrDefault(player.getUniqueId(), -1L);

        if(session == -1){
            Material item = Utils.getRandomMaterial();
            SimpleCaptcha.getInstance().getCaptchaManager().getPlayers().put(player.getUniqueId(), item);

            Utils.openInventory(SimpleCaptcha.getInstance().getCaptchaManager(), player, item);
        }else{
            long untilExpires = session - System.currentTimeMillis();
            if(untilExpires <= 0){
                SimpleCaptcha.getInstance().getCaptchaManager().getSessions().remove(player.getUniqueId());

                Material item = Utils.getRandomMaterial();
                SimpleCaptcha.getInstance().getCaptchaManager().getPlayers().put(player.getUniqueId(), item);

                Utils.openInventory(SimpleCaptcha.getInstance().getCaptchaManager(), player, item);
            }else{
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', SimpleCaptcha.getInstance().getCaptchaManager().getSessionMessage().replace("%time%", Utils.formatDuration(untilExpires))));
            }
        }
    }
}

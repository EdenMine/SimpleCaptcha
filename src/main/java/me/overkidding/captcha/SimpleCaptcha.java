package me.overkidding.captcha;

import lombok.Getter;
import me.overkidding.captcha.manager.CaptchaManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class SimpleCaptcha extends JavaPlugin {

    @Getter private static SimpleCaptcha instance;

    private CaptchaManager captchaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        this.captchaManager = new CaptchaManager();

        sendConsoleLogs(
                ChatColor.GRAY + StringUtils.repeat("-", 25),
                ChatColor.DARK_GREEN + "SimpleCaptcha is now enabled.",
                ChatColor.GREEN + "Thank you for using our plugin.",
                ChatColor.YELLOW + "This plugin was made by overkidding for mc.edenmine.eu",
                ChatColor.GRAY + StringUtils.repeat("-", 25));
    }

    @Override
    public void onDisable() {
        sendConsoleLogs(
                ChatColor.GRAY + StringUtils.repeat("-", 25),
                ChatColor.RED + "SimpleCaptcha is now disabled.",
                ChatColor.GREEN + "Thank you for using our plugin.",
                ChatColor.YELLOW + "This plugin was made by overkidding for mc.edenmine.eu",
                ChatColor.GRAY + StringUtils.repeat("-", 25));
    }

    private void sendConsoleLogs(String... messages){
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(messages);
    }
}

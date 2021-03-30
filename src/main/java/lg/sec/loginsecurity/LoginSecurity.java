package lg.sec.loginsecurity;



import lg.sec.loginsecurity.commands.CommandsManager;
import lg.sec.loginsecurity.database.SQLManager;
import lg.sec.loginsecurity.listeners.ListenersManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class LoginSecurity extends JavaPlugin {

    public final Logger logger =  Logger.getLogger("");
    public static final String ANSI_RESET = "\u001b[0m";
    public static final String ANSI_RED = "\u001b[31m";
    public static final String ANSI_CYAN = "\u001b[36m";
    public static final String ANSI_BRIGHT_CYAN = "\u001b[36;1m";
    public static final String ANSI_GREEN = "\u001b[32m";

    @Getter
    private static LoginSecurity instance;

    @Getter
    private ListenersManager listenersManager;

    @Getter
    private SQLManager sqlManager;

    private String getMinecraftVersion(Server server) {
        String version = server.getVersion();
        int start = version.indexOf("MC: ") + 4;
        int end = version.length() - 1;
        return version.substring(start, end);
    }

    private String getPluginVersion() {
        PluginDescriptionFile pdf = this.getDescription();
        return pdf.getVersion();
    }

    public static String convertColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public void onEnable() {
        instance = this;
        sqlManager = new SQLManager();
        sqlManager.init();
        new CommandsManager().init();
        this.listenersManager.init();
        enablingMessage();
    }

    @Override
    public void onDisable() {
        disablingMessage();
    }


    private void enablingMessage() {
        logger.info(" ");
        logger.info(ANSI_BRIGHT_CYAN + "            `7MMF'      " + ANSI_CYAN + "`7MM\"\"\"Mq." + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM        " + ANSI_CYAN + "  MM   `MM." + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM        " + ANSI_CYAN + "  MM   ,M9" + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM        " + ANSI_CYAN + "  MMmmdM9" + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM      , " + ANSI_CYAN + "  MM" + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM     ,M " + ANSI_CYAN + "  MM" + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "            .JMMmmmmMMM" + ANSI_CYAN + " .JMML." + ANSI_RESET);
        logger.info(" ");
        logger.info("         LoginSecurity v"+ getPluginVersion());
        logger.info("         Running on Spigot - " + getMinecraftVersion(Bukkit.getServer()));
        logger.info("         Made by DevieTeam");
        logger.info(" ");
        logger.info("         Action: " + ANSI_GREEN +"Plugin Enabled!" + ANSI_RESET);
        logger.info(" ");
    }

    private void disablingMessage() {
        logger.info(" ");
        logger.info(ANSI_BRIGHT_CYAN + "            `7MMF'      " + ANSI_CYAN + "`7MM\"\"\"Mq." + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM        " + ANSI_CYAN + "  MM   `MM." + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM        " + ANSI_CYAN + "  MM   ,M9" + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM        " + ANSI_CYAN + "  MMmmdM9" + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM      , " + ANSI_CYAN + "  MM" + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "              MM     ,M " + ANSI_CYAN + "  MM" + ANSI_RESET);
        logger.info(ANSI_BRIGHT_CYAN + "            .JMMmmmmMMM" + ANSI_CYAN + " .JMML." + ANSI_RESET);
        logger.info(" ");
        logger.info("         LoginSecurity v"+ getPluginVersion());
        logger.info("         Running on Spigot - " + getMinecraftVersion(Bukkit.getServer()));
        logger.info("         Made by DevieTeam");
        logger.info(" ");
        logger.info("         Action: " + ANSI_RED + "Disabling...." + ANSI_RESET);
        logger.info(" ");
    }
}

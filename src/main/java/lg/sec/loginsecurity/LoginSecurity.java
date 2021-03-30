package lg.sec.loginsecurity;



import lg.sec.loginsecurity.commands.CommandsManager;
import lg.sec.loginsecurity.database.SQLManager;
import lombok.Getter;
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
    public static final String ANSI_GREEN = "\u001b[32m";
    public static final String ANSI_BRIGHT_GREEN = "\u001b[32;1m";

    @Getter
    private static LoginSecurity instance;

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
        enablingMessage();
    }

    @Override
    public void onDisable() {
        disablingMessage();
    }


    private void enablingMessage() {

    }

    private void disablingMessage() {

    }
}

package lg.sec.loginsecurity.commands;

import lg.sec.loginsecurity.LoginSecurity;
import org.bukkit.command.Command;

public class CommandsManager {

    public void init() {

    }

    public static String getDescription(String label, Command command) {
        String[] strings = command.getUsage().split(" ", 2);
        String usage = strings.length < 2 ? "" :  " " + command.getUsage().split(" ", 2)[1];
        return LoginSecurity.convertColors("&2&lUse &7=> &2&l/" + label + "&7" + usage);
    }
}

package lg.sec.loginprivacy.commands;

import lg.sec.loginprivacy.LoginPrivacy;
import lombok.Getter;
import org.bukkit.command.Command;

public class CommandsManager {

    @Getter
    private LoginPrivacy loginPrivacy;

    public static String getDescription(String label, Command command) {
        String[] strings = command.getUsage().split(" ", 2);
        String usage = strings.length < 2 ? "" : " " + command.getUsage().split(" ", 2)[1];
        return LoginPrivacy.convertColors("&2&lUse &7=> &2&l/" + label + "&7" + usage);
    }

    public void init() {
        this.loginPrivacy = LoginPrivacy.getInstance();
        this.loginPrivacy.getCommand("login").setExecutor(new LoginCommand(this.loginPrivacy));
        this.loginPrivacy.getCommand("register").setExecutor(new RegisterCommand(this.loginPrivacy));
    }
}

package lg.sec.loginprivacy.commands;

import lg.sec.loginprivacy.LoginPrivacy;
import lg.sec.loginprivacy.listeners.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {

    private final LoginPrivacy loginPrivacy;
    public LoginCommand(LoginPrivacy plugin) {
        this.loginPrivacy = plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(LoginPrivacy.convertColors("&cOnly player can execute this command"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(CommandsManager.getDescription(label, command));
        }
        if (args.length > 0) {
            Player player = (Player) sender;
            Bukkit.getPluginManager().callEvent(new LoginEvent(player.getUniqueId(), args[0]));
            return true;
        }
        return true;
    }
}

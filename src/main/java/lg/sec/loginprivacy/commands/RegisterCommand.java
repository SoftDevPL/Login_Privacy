package lg.sec.loginprivacy.commands;

import lg.sec.loginprivacy.LoginPrivacy;
import lg.sec.loginprivacy.listeners.events.RegisterEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {

    private final LoginPrivacy loginPrivacy;

    public RegisterCommand(LoginPrivacy plugin) {
        this.loginPrivacy = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LoginPrivacy.convertColors("&cOnly player can execute this command"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(CommandsManager.getDescription(label, command));
        }
        if (args.length == 2) {
            Player player = (Player) sender;
            if (args[0].equals(args[1])) {
                Bukkit.getPluginManager().callEvent(new RegisterEvent(player, args[0], command));
            } else {
                sender.sendMessage(LoginPrivacy.convertColors("&cPasswords must match"));
            }
            return true;
        }
        return true;
    }
}

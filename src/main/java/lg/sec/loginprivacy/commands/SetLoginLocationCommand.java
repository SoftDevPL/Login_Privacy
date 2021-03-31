package lg.sec.loginprivacy.commands;

import lg.sec.loginprivacy.LoginPrivacy;
import lg.sec.loginprivacy.listeners.events.SetLoginLocationEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLoginLocationCommand implements CommandExecutor {

    private final LoginPrivacy loginPrivacy;

    public SetLoginLocationCommand(LoginPrivacy loginPrivacy) {
        this.loginPrivacy = loginPrivacy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LoginPrivacy.convertColors("&cOnly player can execute this command"));
            return true;
        }
        Location location = LoginPrivacy.getInstance().getSqlManager().getDatabase().getLoginLocation(((Player) sender).getLocation().getWorld().getUID());
        Bukkit.getPluginManager().callEvent(new SetLoginLocationEvent(((Player) sender).getPlayer(), location, ((Player) sender).getLocation()));
        sender.sendMessage(LoginPrivacy.convertColors("&aLogin location set"));
        return true;
    }
}

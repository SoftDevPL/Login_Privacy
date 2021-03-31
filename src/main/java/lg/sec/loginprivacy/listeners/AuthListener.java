package lg.sec.loginprivacy.listeners;

import lg.sec.loginprivacy.LoginPrivacy;
import lg.sec.loginprivacy.database.Database;
import lg.sec.loginprivacy.listeners.events.LoginEvent;
import lg.sec.loginprivacy.listeners.events.RegisterEvent;
import lg.sec.loginprivacy.listeners.hashingUtils.PasswordHarsher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthListener implements Listener {

    private LoginPrivacy loginPrivacy;
    private List<UUID> loggedPlayers = new ArrayList<>();
    private Database database;

    public void init() {
        this.loginPrivacy = LoginPrivacy.getInstance();
        this.database = this.loginPrivacy.getSqlManager().getDatabase();
        this.loginPrivacy.getServer().getPluginManager().registerEvents(this, this.loginPrivacy);
        this.loggedPlayers = this.database.getAllPlayersFromSession();
    }

    private boolean login(UUID uuid, String rawPassword) {
        String hashedPassword = this.database.getPlayerHashedPasswordByUUID(uuid);
        if (hashedPassword == null) {
            return false;
        }
        return new PasswordHarsher().matches(rawPassword, hashedPassword);
    }

    @EventHandler
    private void onLogin(LoginEvent event) {
        event.getPlayer().sendMessage("Checking....");
        if (login(event.getUuid(), event.getPassword())) {
            if (!database.playerIsInSession(event.getPlayer().getUniqueId())) {
                this.database.addPlayerToSession(event.getUuid());
                event.getPlayer().sendMessage("you logged in");
            } else {
                event.getPlayer().sendMessage("you already logged in");
            }
            this.loggedPlayers.clear();
            this.loggedPlayers = this.database.getAllPlayersFromSession();
        } else {
            event.getPlayer().sendMessage("Wrong Password");
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        if (database.playerIsInSession(event.getPlayer().getUniqueId())) {
            this.database.removePlayerFromSessionByUUID(event.getPlayer().getUniqueId());
            this.loggedPlayers.clear();
            this.loggedPlayers = this.database.getAllPlayersFromSession();
        }
    }

    @EventHandler
    private void onRegister(RegisterEvent event) {
        event.getPlayer().sendMessage("Checking....");
        String hashedPassword = new PasswordHarsher().encode(event.getMatchedPassword());
        if (!database.playerIsRegistered(event.getUuid())) {
            this.database.registerPlayerInDatabase(event.getUuid(), hashedPassword);
            event.getPlayer().sendMessage("You successfully registered");
        } else {
            event.getPlayer().sendMessage("You are already registered");
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
          event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void EntityDamageEvent(EntityDamageEvent event) {
        if (!loggedPlayers.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

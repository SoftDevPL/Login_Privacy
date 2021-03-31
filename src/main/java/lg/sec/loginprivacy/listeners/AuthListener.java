package lg.sec.loginprivacy.listeners;

import lg.sec.loginprivacy.LoginPrivacy;
import lg.sec.loginprivacy.database.Database;
import lg.sec.loginprivacy.listeners.events.LoginEvent;
import lg.sec.loginprivacy.listeners.events.RegisterEvent;
import lg.sec.loginprivacy.listeners.hashingUtils.PasswordHarsher;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

import java.util.*;

public class AuthListener implements Listener {

    private final Map<UUID, Integer> scheduledTimers = new HashMap<>();
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
    private void onJoin(PlayerJoinEvent event) {
        boolean isRegistered = this.database.playerIsRegistered(event.getPlayer().getUniqueId());
        if (this.database.playerIsInSession(event.getPlayer().getUniqueId())) {
            scheduledTimers.remove(event.getPlayer().getUniqueId());
            loggedPlayers.remove(event.getPlayer().getUniqueId());
        }
        int id = LoginPrivacy.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(this.loginPrivacy, () -> {
            if (isRegistered) {
                event.getPlayer().sendMessage("You need to login /login [password]");
            } else {
                event.getPlayer().sendMessage("You need to register first /register [password] [password]");
            }

        }, 0, 50);
        scheduledTimers.put(event.getPlayer().getUniqueId(), id);
    }

    @EventHandler
    private void onLogin(LoginEvent event) {
        event.getPlayer().sendMessage(LoginPrivacy.convertColors("&eChecking...."));
        if (login(event.getPlayer().getUniqueId(), event.getPassword())) {
            if (!database.playerIsInSession(event.getPlayer().getUniqueId())) {
                this.database.addPlayerToSession(event.getPlayer().getUniqueId());
                LoginPrivacy.getInstance().getServer().getScheduler().cancelTask(scheduledTimers.get(event.getPlayer().getUniqueId()));
                scheduledTimers.remove(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(LoginPrivacy.convertColors("&ayou logged in"));
            } else {
                event.getPlayer().sendMessage(LoginPrivacy.convertColors("&eyou already logged in"));
            }
            this.loggedPlayers.clear();
            this.loggedPlayers = this.database.getAllPlayersFromSession();
        } else {
            event.getPlayer().sendMessage(LoginPrivacy.convertColors("&cWrong Password"));
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
        event.getPlayer().sendMessage(LoginPrivacy.convertColors("&eChecking...."));
        String hashedPassword = new PasswordHarsher().encode(event.getMatchedPassword());
        if (!database.playerIsRegistered(event.getPlayer().getUniqueId())) {
            this.database.registerPlayerInDatabase(event.getPlayer().getUniqueId(), hashedPassword);
            this.database.addPlayerToSession(event.getPlayer().getUniqueId());
            LoginPrivacy.getInstance().getServer().getScheduler().cancelTask(scheduledTimers.get(event.getPlayer().getUniqueId()));
            scheduledTimers.remove(event.getPlayer().getUniqueId());
            this.loggedPlayers.clear();
            this.loggedPlayers = this.database.getAllPlayersFromSession();
            event.getPlayer().sendMessage(LoginPrivacy.convertColors("&aYou successfully registered"));
        } else {
            event.getPlayer().sendMessage(LoginPrivacy.convertColors("&eYou are already registered"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onMove(PlayerMoveEvent event) {
        if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent event) {
        if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void chatEvent(AsyncPlayerChatEvent event) {
        if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void interactEvent(PlayerInteractEvent event) {
        if (!loggedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void damageEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && !loggedPlayers.contains(event.getDamager().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

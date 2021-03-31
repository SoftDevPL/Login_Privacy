package lg.sec.loginprivacy.listeners.events;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;


public class LoginEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    @Getter
    private final Command command;
    @Getter
    private final Player player;
    @Getter
    private final String password;

    public LoginEvent(Player player, String password, Command command) {
        this.command = command;
        this.password = password;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

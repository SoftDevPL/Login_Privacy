package lg.sec.loginprivacy.listeners.events;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}

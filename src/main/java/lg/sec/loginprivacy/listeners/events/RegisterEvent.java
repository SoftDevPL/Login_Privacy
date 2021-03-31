package lg.sec.loginprivacy.listeners.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class RegisterEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    @Getter
    private final UUID uuid;
    @Getter
    private final Player player;
    @Getter
    private final String matchedPassword;

    public RegisterEvent(Player player, UUID uuid, String matchedPassword) {
        this.uuid = uuid;
        this.matchedPassword = matchedPassword;
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

package lg.sec.loginprivacy.listeners.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SetLoginLocationEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private boolean cancel;
    private final Location previousLocation;
    private Location newLocation;

    public SetLoginLocationEvent(Player who, Location previousLocation, Location newLocation) {
        super(who);
        this.previousLocation = previousLocation;
        this.newLocation = newLocation;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public Location getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(Location newHub) {
        this.newLocation = newHub;
    }

    public Location getPreviousLocation() {
        return previousLocation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}

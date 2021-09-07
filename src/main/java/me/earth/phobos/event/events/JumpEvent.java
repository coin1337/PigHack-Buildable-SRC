package me.earth.phobos.event.events;


import me.earth.phobos.event.EventStage;

public class JumpEvent extends EventStage {

    private Location location;

    public JumpEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
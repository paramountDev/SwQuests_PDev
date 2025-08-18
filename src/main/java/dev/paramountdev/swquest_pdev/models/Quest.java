package dev.paramountdev.swquest_pdev.models;

import org.bukkit.entity.Player;

import java.util.List;

public class Quest {
    private final String id, name, description;
    private final List<Requirement> requirements;

    public Quest(String id, String name, String description,
                 List<Requirement> requirements) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requirements = requirements;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<Requirement> getRequirements() { return requirements; }

    public boolean canBeCompletedBy(Player player) {
        return requirements.stream().allMatch(req -> req.isMetBy(player));
    }

    public void complete(Player player) {
        requirements.forEach(req -> req.consumeFrom(player));
    }
}
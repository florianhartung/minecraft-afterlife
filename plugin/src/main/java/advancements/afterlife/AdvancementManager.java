package advancements.afterlife;

import advancements.ParentCompletedRequirement;
import advancements.afterlife.advancements.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

public class AdvancementManager {

    private static final List<AdvancementListener> advancementListeners = new ArrayList<>();

    static {
        advancementListeners.add(new FoundConstructAdvancement());
        advancementListeners.add(new TrackerAdvancement());
        advancementListeners.add(new TurtleKillerAdvancement());
        advancementListeners.add(new CoalceptionAdvancement());
        advancementListeners.add(new BerryBushAdvancement());
    }

    public static void init(Plugin plugin) {
        advancementListeners.forEach(listener -> listener.init(plugin));

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        advancementListeners.forEach(advancementListener -> pluginManager.registerEvents(advancementListener, plugin));
        pluginManager.registerEvents(new ParentCompletedRequirement(), plugin);
    }
}

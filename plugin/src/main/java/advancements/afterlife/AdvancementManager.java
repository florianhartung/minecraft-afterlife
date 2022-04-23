package advancements.afterlife;

import advancements.ParentCompletedRequirement;
import advancements.afterlife.advancements.FoundConstructAdvancement;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

public class AdvancementManager {

    private static final List<AdvancementListener> advancementListeners = new ArrayList<>();

    static {
        advancementListeners.add(new FoundConstructAdvancement());
    }

    public static void init(Plugin plugin) {
        advancementListeners.forEach(listener -> listener.init(plugin));

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        advancementListeners.forEach(advancementListener -> pluginManager.registerEvents(advancementListener, plugin));
        pluginManager.registerEvents(new ParentCompletedRequirement(), plugin);
    }
}

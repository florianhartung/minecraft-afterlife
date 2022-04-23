package advancements;

import advancements.cancelable.AdvancementCompletedEvent;
import config.Config;
import config.ConfigType;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_18_R1.advancement.CraftAdvancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Optional;

import static main.Util.unsafeListCast;


public class ParentCompletedRequirement implements Listener {

    private final List<String> affectedAdvancementNamespaces;

    public ParentCompletedRequirement() {
        affectedAdvancementNamespaces = unsafeListCast(Config.get(ConfigType.DEFAULT).getList("parent-required-advancement-namespaces"));
    }

    @EventHandler
    public void onAdvancement(AdvancementCompletedEvent e) {
        String advancementKeyNamespace = e.getAdvancement().getKey().getNamespace();
        if (!affectedAdvancementNamespaces.contains(advancementKeyNamespace)) {
            return;
        }

        getParentAdvancement(e.getAdvancement())
                .map(e.getPlayer()::getAdvancementProgress)
                .filter(progress -> !progress.isDone())
                .ifPresent(ignored -> e.setCancelled(true));
    }

    private Optional<Advancement> getParentAdvancement(Advancement child) {
        return Optional.ofNullable(((CraftAdvancement) child))
                .map(CraftAdvancement::getHandle)
                .map(net.minecraft.advancements.Advancement::b)
                .map(advancement -> advancement.bukkit);
    }
}

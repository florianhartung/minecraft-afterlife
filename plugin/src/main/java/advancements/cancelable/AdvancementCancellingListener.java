package advancements.cancelable;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.craftbukkit.v1_19_R1.advancement.CraftAdvancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Optional;

import static main.ChatHelper.send;

public class AdvancementCancellingListener implements Listener {

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        Player player = e.getPlayer();
        Advancement advancement = e.getAdvancement();


        AdvancementCompletedEvent newEvent = new AdvancementCompletedEvent(player, advancement);

        Bukkit.getServer().getPluginManager().callEvent(newEvent);

        if (newEvent.isCancelled()) {
            AdvancementProgress progress = player.getAdvancementProgress(advancement);

            progress.getAwardedCriteria()
                    .forEach(progress::revokeCriteria);
        } else {
            broadcastAdvancementMessage(advancement, player);
        }
    }

    private void broadcastAdvancementMessage(Advancement advancement, Player player) {

        Component advancementDetails = ((CraftAdvancement) advancement).getHandle().getChatComponent();


        Optional<String> optionalAdvancementType = getAdvancementType(advancement);
        if (optionalAdvancementType.isEmpty()) {
            return;
        }
        String messageKey = "chat.type.advancement." + optionalAdvancementType.get();


        Component msg = Component.translatable(messageKey, player.getDisplayName(), advancementDetails);

        Bukkit.getOnlinePlayers()
                .forEach(p -> send(p, msg));
    }

    /**
     * @return an empty optional if the advancement is a recipe, else one of the following advancement types: task, challenge, goal
     */
    private Optional<String> getAdvancementType(Advancement advancement) {
        return Optional.ofNullable((CraftAdvancement) advancement)
                .map(CraftAdvancement::getHandle)
                .filter(adv -> adv.getParent() != null) // Ensure message is not broadcasted for root advancements
                .map(net.minecraft.advancements.Advancement::getDisplay)
                .map(DisplayInfo::getFrame)
                .map(FrameType::getName);
    }
}

package main;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Util {
    @SuppressWarnings("unchecked")
    public static <T> List<T> unsafeListCast(List<?> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>();
        list.forEach(x -> result.add((T) x));
        return result;
    }

    public static boolean isTheSameLocation(Location l1, Location l2) {
        if (l1 == null || l2 == null) {
            return false;
        }

        Vector direction = new Vector(0, 0, 0);
        return l1.clone().setDirection(direction).equals(l2.clone().setDirection(direction));
    }

    public static double healthPercentage(Player player) {
        return player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    public static boolean isNighttime(long time) {
        return time < 23658 && time > 12542;
    }
}

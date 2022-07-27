package skill.injection;

import hud.HudManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SkillInjector {

    private final Plugin plugin;
    private final FileConfiguration skillsConfiguration;
    private Collection<MinecraftSkill> skillInstances;

    public SkillInjector(Plugin plugin, FileConfiguration skillsConfiguration) {
        this.plugin = plugin;
        this.skillsConfiguration = skillsConfiguration;
    }

    public void inject(Object instance) {
        injectConfiguration(instance);
        injectPlugin(instance);
        injectTimer(instance);
        injectSkillInstances(instance);
    }

    private void injectSkillInstances(Object instance) {
        Class<?> clazz = instance.getClass();

        List<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            InjectSkill injectSkill = field.getAnnotation(InjectSkill.class);
            if (injectSkill != null) {
                Class<?> skillClazz = field.getType();
                Optional<?> optionalMinecraftSkillInstance = skillInstances.stream().filter(minecraftSkill -> minecraftSkill.getClass().equals(skillClazz)).findFirst();
                optionalMinecraftSkillInstance.ifPresentOrElse(minecraftSkillInstance -> {
                    try {
                        field.setAccessible(true);
                        field.set(instance, minecraftSkillInstance);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }, () -> System.err.println(clazz.getName() + ": Annotation @InjectSkill can only be used on fields of a type that is a registered MinecraftSkill!"));
            }
        }

    }

    private void injectConfiguration(Object instance) {
        Class<?> clazz = instance.getClass();

        Configurable configurable = clazz.getAnnotation(Configurable.class);
        if (configurable == null) {
            return;
        }
        String sectionPath = configurable.value();
        ConfigurationSection skillConfigSection = skillsConfiguration.getConfigurationSection(sectionPath);
        if (skillConfigSection == null) {
            throw new NullPointerException("Could not find configuration section \"" + sectionPath + "\"");
        }

        List<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            ConfigValue annotation = field.getAnnotation(ConfigValue.class);
            if (annotation == null) {
                continue;
            }

            String valuePath = annotation.value();
            String mapper = annotation.mapper();

            Object value = skillConfigSection.get(valuePath);

            if (value == null) {
                throw new NullPointerException("Could not find value of configuration \"" + valuePath + "\" in section \"" + sectionPath + "\"");
            }

            Method mapperMethod = null;
            Object mappedValue;
            try {
                for (Method method : clazz.getDeclaredMethods()) {
                    List<Class<?>> parameterTypes = List.of(method.getParameterTypes());
                    if (parameterTypes.size() == 1) {
                        if (parameterTypes.get(0).isAssignableFrom(value.getClass()) && method.getName().equals(mapper)) {
                            mapperMethod = method;
                        }
                    }
                }
                if (mapperMethod == null) {
                    mappedValue = value;
                } else {
                    mapperMethod.setAccessible(true);
                    mappedValue = mapperMethod.invoke(instance, value);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                return;
            }

            try {
                field.setAccessible(true);
                field.set(instance, mappedValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void injectPlugin(Object instance) {
        Class<?> clazz = instance.getClass();

        List<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            InjectPlugin injectPlugin = field.getAnnotation(InjectPlugin.class);
            if (injectPlugin != null) {
                if (field.getType() == Plugin.class) {
                    field.setAccessible(true);
                    try {
                        field.set(instance, plugin);
                        String postInjectMethodName = injectPlugin.postInject();
                        if (postInjectMethodName != null && postInjectMethodName.length() > 0) {
                            try {
                                for (Method method : clazz.getDeclaredMethods()) {
                                    List<Class<?>> parameterTypes = List.of(method.getParameterTypes());
                                    if (method.getName().equals(postInjectMethodName) && parameterTypes.size() == 0) {
                                        method.setAccessible(true);
                                        method.invoke(instance);
                                        return;
                                    }
                                }
                                System.err.println(clazz.getName() + ": Could not find the specified postInject method for @InjectPlugin annotation!");
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                e.printStackTrace();
                                return;
                            }
                        }

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println(clazz.getName() + ": Annotation @InjectPlugin can only be used on fields of type Plugin!");
                }
            }
        }
    }

    private void injectTimer(Object instance) {
        Class<?> clazz = instance.getClass();

        List<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            InjectTimer injectTimer = field.getAnnotation(InjectTimer.class);
            if (injectTimer == null) {
                continue;
            }

            if (field.getType() != MinecraftSkillTimer.class) {
                System.err.println(clazz.getName() + ": Annotation @InjectTimer can only be used on fields of type MinecraftSkillTimer!");
                return;
            }

            MinecraftSkillTimer timer = new MinecraftSkillTimer(plugin);
            int duration = injectTimer.duration();
            if (!injectTimer.durationField().isEmpty()) {
                if (duration > 0) {
                    System.err.println(clazz.getName() + ": Only one duration source can be set for Annotation @InjectTimer");
                    return;
                }
                try {
                    Field durationField = clazz.getDeclaredField(injectTimer.durationField());
                    durationField.setAccessible(true);
                    duration = (int) durationField.get(instance);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (duration > 0) {
                timer.setDurationInTicks(duration);
            }

            if (!injectTimer.onTimerFinished().isEmpty()) {
                try {
                    Method onTimerFinishedMethod = clazz.getDeclaredMethod(injectTimer.onTimerFinished(), Player.class);
                    onTimerFinishedMethod.setAccessible(true);
                    timer.setOnTimerFinished(player -> {
                        try {
                            onTimerFinishedMethod.invoke(instance, player);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (injectTimer.hudEntry() != HudManager.HudEntry.NONE) {
                timer.setHudEntry(injectTimer.hudEntry());
            }

            field.setAccessible(true);
            try {
                field.set(instance, timer);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

        if (clazz.getSuperclass() != null) {
            fields.addAll(getAllFields(clazz.getSuperclass()));
        }

        return fields;
    }

    public void setSkillInstances(Collection<MinecraftSkill> skillInstances) {
        this.skillInstances = skillInstances;
    }

    public Collection<MinecraftSkill> getSkillInstances() {
        return skillInstances;
    }
}

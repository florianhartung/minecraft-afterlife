package skill.injection;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillInjector {

    private final Plugin plugin;
    private final FileConfiguration skillsConfiguration;

    public SkillInjector(Plugin plugin, FileConfiguration skillsConfiguration) {
        this.plugin = plugin;
        this.skillsConfiguration = skillsConfiguration;
    }

    public void inject(Object instance) {
        injectConfiguration(instance);
        injectPlugin(instance);
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

        List<Field> allFields = getAllFields(new ArrayList<>(), clazz);
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

        List<Field> allFields = getAllFields(new ArrayList<>(), clazz);
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

    public static List<Field> getAllFields(List<Field> fields, Class<?> clazz) {
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        if (clazz.getSuperclass() != null) {
            getAllFields(fields, clazz.getSuperclass());
        }

        return fields;
    }
}

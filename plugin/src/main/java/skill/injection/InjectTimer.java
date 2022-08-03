package skill.injection;

import hud.HudManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface InjectTimer {
    int duration() default 0;

    String durationField() default "";

    String onTimerFinished() default "";

    HudManager.HudEntry hudEntry() default HudManager.HudEntry.NONE;
}

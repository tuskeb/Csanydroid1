package hu.csany_zeg.one.csanydroid1.core;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BattleState {
	int value();
    byte next() default Battle.STATE_AUTO;
}

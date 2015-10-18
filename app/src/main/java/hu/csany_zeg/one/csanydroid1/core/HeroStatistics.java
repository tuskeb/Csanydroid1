package hu.csany_zeg.one.csanydroid1.core;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HeroStatistics {
	String value();
}

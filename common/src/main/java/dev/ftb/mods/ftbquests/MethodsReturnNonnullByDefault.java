package dev.ftb.mods.ftbquests;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Nonnull
@TypeQualifierDefault(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodsReturnNonnullByDefault {
}

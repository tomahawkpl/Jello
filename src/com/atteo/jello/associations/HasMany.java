package com.atteo.jello.associations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.atteo.jello.Storable;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface HasMany {
	public boolean dependent() default false;

	public String foreignField();

	public Class<? extends Storable> klass();
}

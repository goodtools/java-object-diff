/*
 * Copyright 2014 Daniel Bechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.danielbechler.diff.introspection;

import de.danielbechler.diff.access.PropertyAwareAccessor;
import de.danielbechler.util.Assert;
import de.danielbechler.util.Exceptions;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Resolves the accessors of a given type by using the standard Java {@link Introspector}.
 *
 * @author Daniel Bechler
 */
public class StandardIntrospector implements de.danielbechler.diff.introspection.Introspector
{
	public Iterable<PropertyAwareAccessor> introspect(final Class<?> type)
	{
		Assert.notNull(type, "type");
		try
		{
			return internalIntrospect(type);
		}
		catch (final IntrospectionException e)
		{
			throw Exceptions.escalate(e);
		}
	}

	private Iterable<PropertyAwareAccessor> internalIntrospect(final Class<?> type) throws IntrospectionException
	{
		final PropertyDescriptor[] descriptors = getBeanInfo(type).getPropertyDescriptors();
		final Collection<PropertyAwareAccessor> accessors = new ArrayList<PropertyAwareAccessor>(descriptors.length);
		for (final PropertyDescriptor descriptor : descriptors)
		{
			if (shouldSkip(descriptor))
			{
				continue;
			}
			final String propertyName = descriptor.getName();
			final Method readMethod = descriptor.getReadMethod();
			final Method writeMethod = descriptor.getWriteMethod();
			accessors.add(new PropertyAccessor(propertyName, readMethod, writeMethod));
		}
		return accessors;
	}

	protected BeanInfo getBeanInfo(final Class<?> type) throws IntrospectionException
	{
		return Introspector.getBeanInfo(type);
	}

	private static boolean shouldSkip(final PropertyDescriptor descriptor)
	{
		if (descriptor.getName().equals("class")) // Java & Groovy
		{
			return true;
		}
		if (descriptor.getName().equals("metaClass")) // Groovy
		{
			return true;
		}
		if (descriptor.getReadMethod() == null)
		{
			return true;
		}
		return false;
	}
}

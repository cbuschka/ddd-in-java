package dddwj.ddd.internal;

import dddwj.ddd.CommandHandler;
import dddwj.ddd.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AggregateIntrospector
{
	public <T> AggregateMetadata<T> reflect(Class<T> aggregateDataType)
	{
		Map<Class<?>, CommandHandlerAdapter> commandHandlers = new HashMap<>();
		Map<Class<?>, EventHandlerAdapter> eventHandlers = new HashMap<>();
		Map<String, Class<?>> eventClassByType = new HashMap<>();
		Map<Class<?>, String> typeByEventClass = new HashMap<>();
		for (Method m : ReflectionUtils.getAllDeclaredMethods(aggregateDataType))
		{
			if (m.getAnnotation(CommandHandler.class) != null)
			{
				Class<?>[] parameterTypes = m.getParameterTypes();
				if (parameterTypes.length != 1 || !List.class.isAssignableFrom(m.getReturnType()))
				{
					throw new IllegalArgumentException("Command handler " + m + " must have single command argument and return list of events.");
				}

				m.setAccessible(true);
				commandHandlers.put(parameterTypes[0], new CommandHandlerAdapter(m));
			}

			if (m.getAnnotation(EventHandler.class) != null)
			{
				Class<?>[] parameterTypes = m.getParameterTypes();
				if (parameterTypes.length != 1 || !Void.TYPE.equals(m.getReturnType()))
				{
					throw new IllegalArgumentException("Event handler " + m + " must have single event argument and return void.");
				}

				Class<?> eventClass = parameterTypes[0];
				String eventType = localName(eventClass.getName());
				eventClassByType.put(eventType, eventClass);
				typeByEventClass.put(eventClass, eventType);

				m.setAccessible(true);
				eventHandlers.put(parameterTypes[0], new EventHandlerAdapter(m));
			}
		}

		return new AggregateMetadata<>(localName(aggregateDataType.getName()), aggregateDataType, eventHandlers, commandHandlers, eventClassByType, typeByEventClass);
	}

	private static String localName(String s)
	{
		int lastDotPos = s.lastIndexOf('.');
		if (lastDotPos != -1)
		{
			s = s.substring(lastDotPos + 1);
		}
		return s;
	}

}

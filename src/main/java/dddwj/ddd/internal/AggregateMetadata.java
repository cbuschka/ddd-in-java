package dddwj.ddd.internal;

import java.util.Map;
import java.util.NoSuchElementException;

public class AggregateMetadata<T>
{
	private String aggregateName;
	private Class<T> rootType;
	private final Map<Class<?>, EventHandlerAdapter> eventHandlers;
	private final Map<Class<?>, CommandHandlerAdapter> commandHandlers;
	private Map<String, Class<?>> eventClassByType;
	private Map<Class<?>, String> typeByEventClass;

	public AggregateMetadata(String aggregateName,
							 Class<T> rootType,
							 Map<Class<?>, EventHandlerAdapter> eventHandlers,
							 Map<Class<?>, CommandHandlerAdapter> commandHandlers,
							 Map<String, Class<?>> eventClassByType,
							 Map<Class<?>, String> typeByEventClass)
	{
		this.aggregateName = aggregateName;
		this.rootType = rootType;
		this.eventHandlers = eventHandlers;
		this.commandHandlers = commandHandlers;
		this.eventClassByType = eventClassByType;
		this.typeByEventClass = typeByEventClass;
	}

	public Class<T> getRootType()
	{
		return rootType;
	}

	public CommandHandlerAdapter getCommandHandlerFor(Class<?> commandClass)
	{
		CommandHandlerAdapter commandHandlerAdapter = this.commandHandlers.get(commandClass);
		if (commandHandlerAdapter == null)
		{
			throw new NoSuchElementException("No command handler for " + commandClass.getName() + " in " + this.rootType.getName() + ".");
		}

		return commandHandlerAdapter;
	}

	public Class<?> getEventClassByType(String type)
	{
		Class<?> eventClass = this.eventClassByType.get(type);
		if (eventClass == null)
		{
			throw new NoSuchElementException("No class for event type " + type + ".");
		}
		return eventClass;
	}

	public EventHandlerAdapter getEventHandlerFor(Class<?> eventClass)
	{
		EventHandlerAdapter eventHandlerAdapter = this.eventHandlers.get(eventClass);
		if (eventHandlerAdapter == null)
		{
			throw new NoSuchElementException("No event handler for " + eventClass.getName() + " in " + this.rootType.getName() + ".");
		}
		return eventHandlerAdapter;
	}

	public String getTypeByEventClass(Class<?> eventClass)
	{
		String type = this.typeByEventClass.get(eventClass);
		if (type == null)
		{
			throw new NoSuchElementException("No type for event class " + eventClass.getName() + ".");
		}
		return type;
	}

	public T create()
	{
		try
		{
			return this.rootType.newInstance();
		}
		catch (ReflectiveOperationException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public String getAggregateName()
	{
		return this.aggregateName;
	}
}

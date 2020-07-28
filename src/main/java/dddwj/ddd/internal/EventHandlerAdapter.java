package dddwj.ddd.internal;

import java.lang.reflect.Method;

public class EventHandlerAdapter
{
	private Method method;

	public EventHandlerAdapter(Method method)
	{
		this.method = method;
	}

	public void apply(AggregateImpl aggregate, Object event)
	{
		try
		{
			this.method.invoke(aggregate.root, event);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
}

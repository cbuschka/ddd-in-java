package dddwj.ddd.internal;

import java.lang.reflect.Method;
import java.util.List;

public class CommandHandlerAdapter
{
	private Method method;

	public CommandHandlerAdapter(Method method)
	{
		this.method = method;
	}

	public List<Event> execute(AggregateImpl<?> aggregate, Object command)
	{
		try
		{
			return (List<Event>) this.method.invoke(aggregate.getRoot(), command);
		}
		catch (ReflectiveOperationException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}

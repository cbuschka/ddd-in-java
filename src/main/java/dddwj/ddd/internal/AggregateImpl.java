package dddwj.ddd.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dddwj.ddd.Aggregate;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AggregateImpl<T> implements Aggregate<T>
{
	AggregateMetadata<T> metadata;

	Long id;

	UUID uuid;

	Integer loadVersion;

	Integer version = 0;

	Date createdAt;

	Date updatedAt;

	Date deletedAt;

	T root;

	@JsonIgnore
	List<Event> events;

	protected AggregateImpl()
	{
	}

	public T getRoot()
	{
		return root;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public void execute(Object command)
	{
		List<?> eventDatas = this.metadata.getCommandHandlerFor(command.getClass()).execute(this, command);
		for (Object eventData : eventDatas)
		{
			this.metadata.getEventHandlerFor(eventData.getClass()).apply(this, eventData);
			Event event = new Event();
			event.data = eventData;
			event.uuid = UUID.randomUUID();
			event.createdAt = new Date();
			event.aggregateUuid = this.uuid;
			this.version = this.version + 1;
			event.version = this.version;
			this.events.add(event);
		}
	}
}

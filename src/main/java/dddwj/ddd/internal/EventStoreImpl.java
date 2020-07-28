package dddwj.ddd.internal;

import dddwj.ddd.Aggregate;
import dddwj.ddd.EventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventStoreImpl implements EventStore
{
	@Autowired
	private AggregatePersister aggregatePersister;
	@Autowired
	private EventPersister eventPersister;

	private Map<Class<?>, AggregateMetadata> metadataMap = new HashMap<>();

	public EventStoreImpl(Map<Class<?>, AggregateMetadata> metadataMap)
	{
		this.metadataMap = metadataMap;
	}

	public <T> Aggregate<T> create(Class<T> rootType)
	{
		AggregateMetadata<T> aggregateMetaData = this.metadataMap.get(rootType);
		AggregateImpl<T> aggregate = new AggregateImpl<>();
		aggregate.root = aggregateMetaData.create();
		aggregate.uuid = UUID.randomUUID();
		aggregate.createdAt = new Date();
		aggregate.updatedAt = new Date();
		aggregate.loadVersion = null;
		aggregate.deletedAt = null;
		aggregate.events = new ArrayList<>();
		aggregate.metadata = aggregateMetaData;
		return aggregate;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public <T> AggregateImpl<T> load(Class<T> rootType, UUID aggregateUuid)
	{
		AggregateMetadata<T> metadata = this.metadataMap.get(rootType);
		AggregateImpl<T> aggregateImpl = this.aggregatePersister.select(metadata, aggregateUuid);
		if (aggregateImpl == null)
		{
			throw new IllegalArgumentException("Not found.");
		}

		aggregateImpl.events = this.eventPersister.select(aggregateUuid, aggregateImpl.metadata);

		return aggregateImpl;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public <T> void store(Aggregate<T> aggregate)
	{
		AggregateImpl<T> aggregateImpl = (AggregateImpl<T>) aggregate;
		if (aggregateImpl.id == null)
		{
			this.aggregatePersister.insert(aggregateImpl);
		}
		else
		{
			this.aggregatePersister.update(aggregateImpl);
		}

		List<Event> newEvents = aggregateImpl.events.stream()
				.filter(e -> e.id == null)
				.collect(Collectors.toList());
		this.eventPersister.insert(newEvents, aggregateImpl.metadata);
	}

}

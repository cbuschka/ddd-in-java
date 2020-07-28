package dddwj.ddd;

import dddwj.ddd.internal.AggregateIntrospector;
import dddwj.ddd.internal.AggregateMetadata;
import dddwj.ddd.internal.EventStoreImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventStoreConfiguration
{
	@Autowired
	private AggregateIntrospector aggregateIntrospector;
	@Autowired
	private List<EventStoreConfigurer> eventStoreConfigurers;

	@Bean
	public EventStore eventStore()
	{
		Map<Class<?>, AggregateMetadata> aggregateMetadataMap = new HashMap<>();
		EventStoreBuilder builder = new EventStoreBuilder()
		{
			@Override
			public void registerAggregateRoot(Class<?> aggregateRoot)
			{
				aggregateMetadataMap.put(aggregateRoot, aggregateIntrospector.reflect(aggregateRoot));
			}
		};
		this.eventStoreConfigurers.forEach(c -> c.configure(builder));

		EventStoreImpl eventStore = new EventStoreImpl(aggregateMetadataMap);
		return eventStore;
	}
}

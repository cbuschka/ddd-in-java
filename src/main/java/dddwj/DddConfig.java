package dddwj;

import dddwj.ddd.EnableEventStore;
import dddwj.ddd.EventStoreBuilder;
import dddwj.ddd.EventStoreConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEventStore
public class DddConfig implements EventStoreConfigurer
{
	@Override
	public void configure(EventStoreBuilder eventStoreBuilder)
	{
		eventStoreBuilder.registerAggregateRoot(Order.class);
	}
}

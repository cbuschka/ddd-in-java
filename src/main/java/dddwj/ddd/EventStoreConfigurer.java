package dddwj.ddd;

import org.springframework.stereotype.Component;

@Component
public interface EventStoreConfigurer
{
	void configure(EventStoreBuilder eventStoreBuilder);
}

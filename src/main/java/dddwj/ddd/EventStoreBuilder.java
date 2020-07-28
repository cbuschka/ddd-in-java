package dddwj.ddd;

public interface EventStoreBuilder
{
	void registerAggregateRoot(Class<?> aggregateRoot);
}

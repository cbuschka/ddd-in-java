package dddwj.ddd;

import java.util.UUID;

public interface EventStore
{
	<T> Aggregate<T> create(Class<T> rootType);

	<T> Aggregate<T> load(Class<T> rootType, UUID aggregateUuid);

	<T> void store(Aggregate<T> aggregate);
}

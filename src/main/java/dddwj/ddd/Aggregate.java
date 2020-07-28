package dddwj.ddd;

import java.util.UUID;

public interface Aggregate<T>
{
	UUID getUuid();

	T getRoot();

	<C> void execute(C command);
}

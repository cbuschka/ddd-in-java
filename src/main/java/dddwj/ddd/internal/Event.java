package dddwj.ddd.internal;

import java.util.Date;
import java.util.UUID;

public class Event
{
	public Long id;

	public UUID uuid;

	public UUID aggregateUuid;

	public Date createdAt;

	public Integer version;

	public Object data;
}

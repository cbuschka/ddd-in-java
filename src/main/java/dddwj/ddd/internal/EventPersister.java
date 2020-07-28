package dddwj.ddd.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class EventPersister
{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ObjectMapper objectMapper;

	public void insert(List<Event> events, AggregateMetadata<?> aggregateMetadata)
	{
		String insertSql = String.format("insert into %s ( uuid, aggregate_uuid, version, created_at, type, data) values ( ?::uuid, ?::uuid, ?, ?, ?, ?::jsonb )", "ev");
		jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException
			{
				Event event = events.get(i);
				if (event.id != null)
				{
					throw new IllegalStateException("Event already saved.");
				}

				String dataJson = toJson(event);
				String type = aggregateMetadata.getTypeByEventClass(event.data.getClass());
				event.createdAt = new Date();

				ps.setString(1, event.uuid.toString());
				ps.setString(2, event.aggregateUuid.toString());
				ps.setInt(3, event.version);
				ps.setTimestamp(4, new java.sql.Timestamp(event.createdAt.getTime()));
				ps.setString(5, type);
				ps.setString(6, dataJson);
			}

			@Override
			public int getBatchSize()
			{
				return events.size();
			}
		});
	}

	private <T> T fromJson(String json, Class<T> type)
	{
		try
		{
			return this.objectMapper.readerFor(type).readValue(json);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private String toJson(Event event)
	{
		try
		{
			return this.objectMapper.writeValueAsString(event.data);
		}
		catch (JsonProcessingException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public List<Event> select(UUID aggregateUuid, AggregateMetadata<?> metadata)
	{
		String selectSql = String.format("select id, uuid, aggregate_uuid, version, created_at, type, data from %s where aggregate_uuid = ?::uuid order by version asc", "ev");
		return this.jdbcTemplate.query(selectSql, new Object[]{aggregateUuid.toString()}, new RowMapper<Event>()
		{
			@Override
			public Event mapRow(ResultSet rs, int i) throws SQLException
			{
				Event event = new Event();
				event.id = rs.getLong("id");
				event.uuid = UUID.fromString(rs.getString("uuid"));
				event.aggregateUuid = UUID.fromString(rs.getString("aggregate_uuid"));
				event.version = rs.getInt("version");
				event.createdAt = rs.getTimestamp("created_at");
				String type = rs.getString("type");
				event.data = fromJson(rs.getString("data"), metadata.getEventClassByType(type));
				return event;
			}
		});
	}
}

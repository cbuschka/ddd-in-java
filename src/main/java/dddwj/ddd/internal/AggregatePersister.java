package dddwj.ddd.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.UUID;

@Component
public class AggregatePersister
{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ObjectMapper objectMapper;

	public <T> void insert(AggregateImpl<T> aggregate)
	{
		String aggregateJson = toJson(aggregate);
		aggregate.updatedAt = new Date();

		String insertSql = String.format("insert into %s ( uuid, version, created_at, updated_at, deleted_at, type, data) values ( ?::uuid, ?, ?, ?, ?, ?, ?::jsonb )", "agg");
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(
				new PreparedStatementCreator()
				{
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
					{
						PreparedStatement ps =
								connection.prepareStatement(insertSql, new String[]{"id"});
						ps.setString(1, aggregate.uuid.toString());
						ps.setInt(2, aggregate.version);
						ps.setTimestamp(3, new java.sql.Timestamp(aggregate.createdAt.getTime()));
						ps.setTimestamp(4, new java.sql.Timestamp(aggregate.updatedAt.getTime()));
						if (aggregate.deletedAt == null)
						{
							ps.setNull(5, Types.TIMESTAMP);
						}
						else
						{
							ps.setTimestamp(5, new java.sql.Timestamp(aggregate.deletedAt.getTime()));
						}
						ps.setString(6, aggregate.metadata.getAggregateName());
						ps.setString(7, aggregateJson);
						return ps;
					}
				},
				keyHolder);
		aggregate.id = keyHolder.getKey().longValue();
		aggregate.loadVersion = aggregate.version;

	}

	private <T> String toJson(AggregateImpl<T> aggregate)
	{
		try
		{
			return this.objectMapper.writeValueAsString(aggregate.root);
		}
		catch (JsonProcessingException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public <T> void update(AggregateImpl<T> aggregate)
	{
		String aggregateJson = toJson(aggregate);
		aggregate.updatedAt = new Date();
		String updateSql = String.format("update %s set version=?, updated_at=?, deleted_at=?, type=?, data=?::jsonb where uuid = ?::uuid and id = ? and version = ?", "agg");
		int count = jdbcTemplate.update(updateSql, new Object[]{aggregate.version,
				aggregate.updatedAt,
				aggregate.deletedAt,
				aggregate.metadata.getAggregateName(),
				aggregateJson,
				aggregate.uuid.toString(),
				aggregate.id,
				aggregate.loadVersion});
		if (count != 1)
		{
			throw new RuntimeException("Updated failed.");
		}

	}

	public <T> AggregateImpl<T> select(AggregateMetadata metadata, UUID aggregateUuid)
	{
		String selectSql = String.format("select id, uuid, version, created_at, updated_at, deleted_at, type, data::text from %s where uuid = ?::uuid", "agg");
		AggregateImpl<T> aggregate = this.jdbcTemplate.queryForObject(selectSql, new Object[]{aggregateUuid.toString()}, new RowMapper<AggregateImpl<T>>()
		{
			@Override
			public AggregateImpl<T> mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				String dataJson = rs.getString("data");
				try
				{
					T root = objectMapper.readerFor(metadata.getRootType()).readValue(dataJson);
					AggregateImpl<T> aggregate = new AggregateImpl<>();
					aggregate.root = root;
					aggregate.id = rs.getLong("id");
					aggregate.uuid = aggregateUuid;
					aggregate.loadVersion = rs.getInt("version");
					aggregate.version = rs.getInt("version");
					aggregate.createdAt = rs.getTimestamp("created_at");
					aggregate.updatedAt = rs.getTimestamp("updated_at");
					aggregate.deletedAt = rs.getTimestamp("deleted_at");
					aggregate.metadata = metadata;
					return aggregate;
				}
				catch (IOException ex)
				{
					throw new RuntimeException(ex);
				}
			}
		});

		return aggregate;
	}
}

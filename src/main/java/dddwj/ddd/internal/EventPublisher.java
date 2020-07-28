package dddwj.ddd.internal;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
public class EventPublisher
{
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional(propagation = Propagation.REQUIRED)
	public void publish()
	{
		Long maxPublishedAs = this.jdbcTemplate.queryForObject("select coalesce(max(published_as),0) from ev where published_as is not null", Long.class);
		List<Long> ids = this.jdbcTemplate.queryForList("select id from ev where published_as is null order by id for update",
				Long.class);
		this.jdbcTemplate.batchUpdate("update ev set published_as = ? where id = ?", new BatchPreparedStatementSetter()
		{
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException
			{
				ps.setLong(1, maxPublishedAs + 1 + i);
				ps.setLong(2, ids.get(i));
			}

			@Override
			public int getBatchSize()
			{
				return ids.size();
			}
		});
	}
}

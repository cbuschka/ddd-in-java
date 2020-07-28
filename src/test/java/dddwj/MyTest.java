package dddwj;

import dddwj.ddd.Aggregate;
import dddwj.ddd.EventStore;
import dddwj.ddd.internal.EventPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MyTest
{
	@Autowired
	private EventStore eventStore;
	@Autowired
	private EventPublisher eventPublisher;

	@Test
	public void testIt()
	{
		Aggregate<Order> orderAggregate = eventStore.create(Order.class);
		eventStore.store(orderAggregate);

		orderAggregate.execute(new PlaceOrderCommand("O123"));
		eventStore.store(orderAggregate);

		orderAggregate = eventStore.load(Order.class, orderAggregate.getUuid());
		eventStore.store(orderAggregate);

		eventPublisher.publish();
	}
}

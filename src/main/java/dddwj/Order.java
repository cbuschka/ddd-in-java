package dddwj;

import dddwj.ddd.CommandHandler;
import dddwj.ddd.EventHandler;

import java.util.Collections;
import java.util.List;

public class Order
{
	public String orderNo;

	public OrderState state = OrderState.NEW;

	@CommandHandler
	private List<?> placeOrder(PlaceOrderCommand cmd)
	{
		if (state != OrderState.NEW)
		{
			throw new IllegalStateException();
		}

		OrderPlacedEvent ev = new OrderPlacedEvent();
		ev.orderNo = cmd.orderNo;
		return Collections.singletonList(ev);
	}

	@EventHandler
	void orderPlaced(OrderPlacedEvent ev)
	{
		this.state = OrderState.PLACED;
		this.orderNo = ev.orderNo;
	}
}

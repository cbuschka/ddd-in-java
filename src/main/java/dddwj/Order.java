package dddwj;

import dddwj.ddd.CommandHandler;
import dddwj.ddd.EventHandler;

import java.util.Collections;
import java.util.List;

public class Order
{
	public String orderNo;

	public OrderState state = OrderState.NEW;

	public String cancelReason;

	public String receiverSignature;

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

	@CommandHandler
	private List<?> markOrderDelivered(MarkOrderDeliveredCommand cmd)
	{
		if (state != OrderState.PLACED)
		{
			throw new IllegalStateException();
		}

		OrderDeliveredEvent ev = new OrderDeliveredEvent();
		ev.signature = cmd.signature;
		return Collections.singletonList(ev);
	}

	@CommandHandler
	private List<?> cancelOrder(CancelOrderCommand cmd)
	{
		if (state != OrderState.PLACED)
		{
			throw new IllegalStateException();
		}

		OrderCancelledEvent ev = new OrderCancelledEvent();
		ev.reason = cmd.reason;
		return Collections.singletonList(ev);
	}

	@EventHandler
	void orderPlaced(OrderPlacedEvent ev)
	{
		this.state = OrderState.PLACED;
		this.orderNo = ev.orderNo;
	}

	@EventHandler
	void orderDelivered(OrderDeliveredEvent ev)
	{
		this.state = OrderState.DELIVERED;
		this.receiverSignature = ev.signature;
	}

	@EventHandler
	void orderCancelled(OrderCancelledEvent ev)
	{
		this.state = OrderState.CANCELLED;
		this.cancelReason = ev.reason;
	}
}

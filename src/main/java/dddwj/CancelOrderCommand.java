package dddwj;

public class CancelOrderCommand
{
	public String reason;

	public CancelOrderCommand(String reason)
	{
		this.reason = reason;
	}
}

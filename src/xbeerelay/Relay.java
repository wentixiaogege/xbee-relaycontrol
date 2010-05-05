package xbeerelay;

/**
 * This represents a relay.  Relays are either on/off (status variable)
 * and have a number associated with them.
 * 
 * <p>Relays need to be configured on the MCU side as well, for now.
 * Another possible design is to have it completely configured from the PC
 * side, where all you'd have to do is send a pin number to the MCU and it
 * would turn that pin on or off.
 * 
 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington </a>
 *
 */
public class Relay {
	
	/**
	 * Same as other constructor, sets label to empty string.
	 * 
	 * @param inInitialDP pin number on MCU associated with this relay
	 * @param inRelayNumber number of this relay
	 * @throws RelayException doesn't really ever throw this
	 */
	public Relay(int inDP, int inRelayNumber, XbeeDigitalIOPin inXbeePin) 
		throws RelayException {
		this(inDP, inRelayNumber, inXbeePin, "");
	}
	
	/**
	 * Sets parameters and sets the initial status to unitialized.
	 * 
	 * @param inDP pin number on MCU associated with this relay
	 * @param inRelayNumber number of this relay
	 * @param inXbeePin Xbee pin monitoring this relay
	 * @param inLabel label for this relay (only used on PC side)
	 * @throws RelayException if label is null string
	 */
	public Relay(int inDP, int inRelayNumber, XbeeDigitalIOPin inXbeePin, String inLabel) 
		throws RelayException {
		dp = inDP;
		relayNumber = inRelayNumber;
		xbeePin = inXbeePin;
		if (inLabel != null) {
			label = inLabel;
		} else {
			throw new RelayException("A relay's label cannot be null!");
		}
		status = RelayStatus.UNITIALIZED;
	}
	
	/**
	 * Returns the relay number associated with this relay.
	 */
	public int getNumber() {
		return relayNumber;
	}
	
	/**
	 * Change the pin associated with this relay.
	 * 
	 * @param inDP a valid digital I/O pin on Xbee Series 2
	 */
	public void setPin(int inDP) {
		dp = inDP;
	}
	
	/**
	 * Get the digital I/O pin this relay is currently being monitored by.
	 * 
	 * @return a valid digital I/O pin on Xbee Series 2
	 */
	public int getPin() {
		return dp;
	}
	
	/**
	 * Gets status of this relay.
	 * 
	 * @return UNITIALIZED if this relay isn't valid, ON or OFF otherwise
	 */
	public RelayStatus getStatus() {
		return status;
	}
	
	/**
	 * Sets the status of this relay.
	 * <p>Protected since we want only RelayManagers to be able to do this.
	 * 
	 * @param inStatus status to set this relay to
	 */
	protected void setStatus(RelayStatus inStatus) {
		status = inStatus;
	}
	
	/**
	 * Sets the string label of this relay. (only used on PC side)
	 * 
	 * @param inLabel string label
	 * @throws RelayException if the string is null
	 */
	public void setLabel(String inLabel) 
		throws RelayException {
		if (inLabel != null) {
			label = inLabel;
		} else {
			throw new RelayException("A relay's label cannot be null!");
		}
	}
	
	/**
	 * Gets the string label for this Relay.  In our application, this generally
	 * corresponds to an appliance name, but it could easily be used for something 
	 * else.
	 * 
	 * @return string label for this relay
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Gets a String representation of this relay's status, which is internally
	 * represented as an instance of the enum RelayStatus, and is either On, Off,
	 * or Unitialized.
	 * 
	 * @return
	 */
	public String getStatusString() {
		if (status == RelayStatus.UNITIALIZED)
			return "Unitialized";
		else if (status == RelayStatus.ON)
			return "On";
		else
			return "Off";
	}
	
	/**
	 * Gets the XBee digital IO pin that is set up to monitor this relay's
	 * control port.  The remote XBee periodically queries its I/O ports and
	 * sends the results to its coordinator, which is connected to the PC.
	 * 
	 * @return
	 */
	public XbeeDigitalIOPin getXbeePin() {
		return xbeePin;
	}
	
	/**
	 * Sets the XBee digital IO pin that is set up to monitor this relay's
	 * control port.
	 * 
	 * @param inXbeePin
	 */
	public void setXbeePin(XbeeDigitalIOPin inXbeePin) {
		xbeePin = inXbeePin;
	}
	
	
	// So we can use these as the key to a HashMap
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dp;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + relayNumber;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((xbeePin == null) ? 0 : xbeePin.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relay other = (Relay) obj;
		if (dp != other.dp)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (relayNumber != other.relayNumber)
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (xbeePin == null) {
			if (other.xbeePin != null)
				return false;
		} else if (!xbeePin.equals(other.xbeePin))
			return false;
		return true;
	}



	private int relayNumber;
	private int dp;
	private RelayStatus status;
	private String label;
	private XbeeDigitalIOPin xbeePin; // the pin used to monitor the status of this relay
	
	/**
	 * Represents the status of a relay.
	 * <p>Unitialized corresponds to a relay that has not yet been queried.
	 * <p>ON/OFF correspond to the relay being ON and OFF (duh).
	 * 
	 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington</a>
	 *
	 */
	public enum RelayStatus
	{
		/** We don't know what the relay status is since we haven't checked its value yet. */
		UNITIALIZED,
		/** We've checked the relay's status, and it is on. */
		ON,
		/** We've checked the relay's status,a nd it is off. */
		OFF
	}
	
	/**
	 * Represents the Xbee digital IO pin that is used to monitor
	 * the status of this relay.
	 * 
	 * <p>See http://code.google.com/p/xbee-api/wiki/XBeePins for what these mean.
	 * 
	 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington</a>
	 *
	 */
	public enum XbeeDigitalIOPin
	{
		D0,
		D1,
		D2,
		D3,
		D4,
		D5,
		D6,
		D7,
		D10,
		D11,
		D12
	}
}

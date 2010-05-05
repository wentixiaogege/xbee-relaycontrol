/**
 * 
 */
package xbeerelay;

import java.util.HashMap;
import java.util.List;

import xbeerelay.Relay.RelayStatus;

import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;

/**
 * This class is used for actually managing our relays remotely using the
 * Xbee chips and Xbee API.  It makes use of the XbeeManager class to send
 * commands (which are just String payloads) to the remote XBee/Arduino 
 * system that is currently controlling the relays (running our relay 
 * control software).
 * 
 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington</a>
 *
 */
public class XbeeRelayManager extends RelayManager {

	/**
	 * Constructs a new XbeeRelayManager instance without any initial relays.
	 * 
	 * @param inXbeeManager the XbeeManager to use when sending commands
	 * @param inXbAddress the 8-integer array representing a 64-bit xb address
	 */
	public XbeeRelayManager(XbeeManager inXbeeManager, int[] inXbAddress) {
		xbManager = inXbeeManager;
		xbAddress = new XBeeAddress64(inXbAddress);
		managedRelays = new HashMap<Integer, Relay>();
	}
	
	/**
	 * Constructs a new XbeeRelayManager instance with initial relays.
	 * 
	 * @param inRelays the initial relays to be managed by this relay manager
	 * @param inXbeeManager the XbeeManager to use when sending commands
	 * @param inXbAddress the 8-integer array representing a 64-bit xb address
	 * @throws RelayException
	 */
	public XbeeRelayManager(Relay[] inRelays, XbeeManager inXbeeManager, int[] inXbAddress) 
		throws RelayException {
		xbManager = inXbeeManager;
		addManagedRelays(inRelays);
		xbAddress = new XBeeAddress64(inXbAddress);
		managedRelays = new HashMap<Integer, Relay>();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipsercp.xbeegui.model.RelayManager#turnOff(int[])
	 */
	@Override
	public void turnOff(int inPinNumber) 
		throws RelayException {
		try {
			xbManager.sendCommand(String.format("CMD ROFF%02d", inPinNumber), xbAddress);
		} catch (XBeeException e) {
			throw new RelayException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsercp.xbeegui.model.RelayManager#turnOn(int[])
	 */
	@Override
	public void turnOn(int inPinNumber) 
		throws RelayException {
		try {
			xbManager.sendCommand(String.format("CMD RON%02d", inPinNumber), xbAddress);
		} catch (XBeeException e) {
			throw new RelayException(e);
		}
	}

	@Override
	public void turnOff(List<Integer> inPinNumbers) 
		throws RelayException {
		StringBuilder cmd = new StringBuilder("CMD ");
		for (Integer i : inPinNumbers)
			cmd.append(String.format("ROFF%02d ", i));
		try {
			xbManager.sendCommand(cmd.toString(), xbAddress);
		} catch (XBeeException e) {
			throw new RelayException(e);
		}
	}

	@Override
	public void turnOn(List<Integer> inPinNumbers) throws RelayException {
		StringBuilder cmd = new StringBuilder("CMD ");
		for (Integer i : inPinNumbers)
			cmd.append(String.format("RON%02d ", i));
		try {
			xbManager.sendCommand(cmd.toString(), xbAddress);
		} catch (XBeeException e) {
			throw new RelayException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsercp.xbeegui.model.RelayManager#updateRelayStatus(int)
	 */
	@Override
	public RelayStatus updateRelayStatus(int inPinNumber)
			throws RelayException {
		// TODO Auto-generated method stub
		return null;
	}

	private XbeeManager xbManager;
	private XBeeAddress64 xbAddress;
}

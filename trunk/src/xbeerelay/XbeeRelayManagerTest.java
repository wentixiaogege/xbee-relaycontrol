package xbeerelay;

import xbeerelay.Relay.XbeeDigitalIOPin;

/**
 * This is basically just a simple test program to play with the 
 * rest of this relay management API.  It simply constructs a new 
 * XBeeManager to send commands, a  new XbeeRelayManager to manage relays
 * via the aforementioned XbeeManager, and then tries to turn on Relay 1
 * on the remote Arduino/Xbee system for 5 seconds before turning it back
 * off.
 * 
 * <p>This is probably the best starting point for understanding at a 
 * very basic level what all of this software does.
 * 
 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington</a>
 *
 */
public class XbeeRelayManagerTest {

	/**
	 * See the class description.  About the simplest relay control example
	 * I could concoct.
	 * 
	 * @param args none
	 * @throws RelayException if something goes wrong turning on or off relays
	 */
	public static void main(String[] args) throws RelayException {
		
		XbeeManager xbManager = new XbeeManager(null);
		int[] xbAddress = {0, 0x13, 0xa2, 0, 0x40, 0x3d, 0xb1, 0x5b};
		XbeeRelayManager xbRelayManager = new XbeeRelayManager(xbManager, xbAddress);
		
		xbRelayManager.addManagedRelay(new Relay(2, 1, XbeeDigitalIOPin.D2));
		xbRelayManager.turnOn(2);
		
		try {
			// wait a bit then send another packet
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		
		xbRelayManager.turnOff(1);
	}

}

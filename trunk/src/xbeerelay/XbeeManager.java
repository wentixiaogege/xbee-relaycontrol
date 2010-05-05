package xbeerelay;

import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;

/**
 * This is the class that interacts with the Xbee chips.  It makes use
 * of the open source Xbee API to essentially just send a command (or 
 * really any payload) to a remote Xbee.
 * 
 * <p>The way this class sends data is modeled off of examples provided with
 * the xbee-api open source package.
 * 
 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington</a>
 *
 */
public class XbeeManager {
	
	/**
	 * Constructs a new XbeeManager instance.  This instance will use the 
	 * specified XBee object for sending commands/payloads.
	 * 
	 * @param inXbee the xbee chip to use for sending commands with this XbeeManager
	 */
	public XbeeManager(XBee inXbee) {
		xbee = inXbee;
	}
	
	/**
	 * Send a command to a remote Xbee, specified by a 64-bit address.  Xbee addresses
	 * are SH + SL (the serial  number).  The command is essentially any payload, so 
	 * we could use this method for sending arbitrary data (not just "commands") to a 
	 * remote XBee.
	 * 
	 * <p>The payload must be 72 bytes or less in length.  If you try to send a packet with
	 * a bigger payload, this might fail ungracefully -- it's up the user to check that
	 * he/she is not trying to send too much in a single packet.
	 * 
	 * @param inCommand the string payload, up to 72 bytes in length
	 * @param inAddr64 the 64-bit address of the remote XBee
	 * @return true if successful (acked), false if not
	 * @throws XBeeException if something fails
	 */
	public boolean sendCommand(String inCommand, XBeeAddress64 inAddr64) 
		throws XBeeException {

		// Create the payload that we will use to send the command to the remote
		// Xbee
		int[] payload = ByteUtils.stringToIntArray(inCommand);

		// Construct the request to send, containing the above payload (with the command)
		// to the specified address.
		ZNetTxRequest request = new ZNetTxRequest(inAddr64, payload);

		try {
			ZNetTxStatusResponse response = (ZNetTxStatusResponse) xbee.sendSynchronous(request, 10000);
			
			// We need to update the frameID for each request (otherwise we won't know which packet is being
			//  acked, the last one sent or the current one.
			request.setFrameId(xbee.getNextFrameId());

			if (response.getDeliveryStatus() == ZNetTxStatusResponse.DeliveryStatus.SUCCESS) {
				// the packet was successfully delivered
				if (response.getRemoteAddress16().equals(XBeeAddress16.ZNET_BROADCAST)) {
					// specify 16-bit address for faster routing?.. really only need to do this when it changes
					request.setDestAddr16(response.getRemoteAddress16());
				}
				return true;
			} else {						
				// packet failed.
				// TODO: Log error.
				return false;
			}			
		} catch (XBeeTimeoutException e) {
			// TODO: Log warning, inform someone!
			return false;
		}
	}
	
	private XBee xbee;
}

package xbeerelay;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import xbeerelay.Relay.RelayStatus;

/**
 * This is the base class for all relay-managing classes.
 * 
 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington</a>
 *
 */
public abstract class RelayManager {
	
	/**
	 * Associates a relay with this manager.
	 * 
	 * @param inRelay relay to associate
	 * @throws RelayException 
	 */
	public void addManagedRelay(Relay inRelay) 
		throws RelayException {
		if (!managedRelays.containsKey(inRelay.getNumber())) {
			managedRelays.put(inRelay.getNumber(), inRelay);	
		} else {
			// Relay number already in use
			throw new RelayException("Relay number " + inRelay.getNumber() + " already in use!");
		}
		
	}
	
	/**
	 * Disassociates a relay from this manager.
	 * <p>Note that if the relay is not currently associated with this manager,
	 * nothing happens.
	 * 
	 * @param inRelayNum number of relay to disassociate
	 */
	public void removeManagedRelay(int inRelayNum) {
		if (managedRelays.containsKey(inRelayNum)) {
			managedRelays.remove(inRelayNum);
		}
	}
	
	/**
	 * Associates multiple relays with this manager.
	 * 
	 * @param inRelays array of relays to associate
	 * @throws RelayException 
	 */
	public void addManagedRelays(Relay[] inRelays) 
		throws RelayException {
		for (Relay inRelay : inRelays) {
			addManagedRelay(inRelay);
		}
	}
	
	/**
	 * Disassociates multiple relays with this manager.
	 * <p>Note that if any relay in the input is not currently associated
	 * with this manager, nothing happens.
	 * 
	 * @param inRelayNums numbers of relays to disassociate
	 */
	public void removeManagedRelays(int[] inRelayNums) {
		for (int inRelayNum : inRelayNums) {
			removeManagedRelay(inRelayNum);
		}
	}
	
	/**
	 * Turns on the specified relay.
	 * <p>Note that if that relay is already on, nothing happens.
	 * 
	 * @param inRelayNumber relay number of relay to turn on
	 * @throws RelayException if relay number is invalid (not associated with this manager)
	 */
	public abstract void turnOn(int inRelayNumber)
		throws RelayException;
	
	/**
	 * Turns off the specified relay.
	 * <p>Note that if that relay is already off, nothing happens.
	 * 
	 * @param inRelayNumbers integers of relay numbers to turn on
	 * @throws RelayException if relay number is invalid (not associated with this manager)
	 */
	public abstract void turnOff(int inRelayNumber)
		throws RelayException;
	
	/**
	 * Turns on the specified relays.
	 * 
	 * @param inRelayNumbers relay numbers to turn on
	 * @throws RelayException if any relay number is invalid (not associated with this manager)
	 */
	public void turnOn(List<Integer> inRelayNumbers) 
		throws RelayException {
		for (int inRelayNumber : inRelayNumbers) {
			turnOn(inRelayNumber);
		}
	}
	
	
	/**
	 * Turns off the specified relays.
	 * 
	 * @param inRelayNumbers relay numbers to turn off
	 * @throws RelayException if any relay number is invalid (not associated with this manager)
	 */
	public void turnOff(List<Integer> inRelayNumbers) 
		throws RelayException {
		for (int inRelayNumber : inRelayNumbers) {
			turnOff(inRelayNumber);
		}
	}
	
	/**
	 * Gets the locally-stored status of the given relay number.
	 * <p>Note that this method does NOT do anything to query the relay
	 * or update its status.  For that, see updateRelayStatus.
	 * 
	 * @param inRelayNumber number of relay we are checking
	 * @return locally stored relay status
	 * @throws RelayException if relay number is invalid
	 */
	public RelayStatus getRelayStatus(int inRelayNumber) 
		throws RelayException
	{
		if (managedRelays.containsKey(new Integer(inRelayNumber))) {
			return managedRelays.get(new Integer(inRelayNumber)).getStatus();
		} else {
			throw new RelayException("Invalid relay number: " + inRelayNumber);
		}
	}
	
	/**
	 * Goes to source and actually updates the locally-stored status of the relay.
	 * 
	 * @param inRelayNumber relay number of relay to update
	 * @return status of relay after update
	 * @throws RelayException if relay number is invalid
	 */
	public abstract RelayStatus updateRelayStatus(int inRelayNumber)
		throws RelayException;
	
	/**
	 * Goes to source and actually updates the locally-stored status of the relays.
	 * 
	 * @param inRelayNumbers relay numbers of relays to update
	 * @throws RelayException if any relay number is invalid
	 */
	public void updateRelayStatus(int[] inRelayNumbers)
		throws RelayException {
		for (int relayNum : inRelayNumbers) {
			updateRelayStatus(relayNum);
		}
	}
	
	/**
	 * Gets the number of relays currently managed by this instance of RelayManager.
	 * 
	 * @return the number of relays being managed
	 */
	public int getNumManagedRelays() {
		return managedRelays.size();
	}
	
	/**
	 * Gets a collection of the actual Relay objects currently being managed 
	 * by this instance of RelayManager.
	 * 
	 * @return collection of relays being managed
	 */
	public Collection<Relay> getManagedRelays() {
		return managedRelays.values();
	}
	
	// Stores the relays being managed by this RelayManager
	// protected and not private so it can be inherited
	protected Map<Integer,Relay> managedRelays;
}

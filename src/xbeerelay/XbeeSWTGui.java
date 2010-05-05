package xbeerelay;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import xbeerelay.Relay.RelayStatus;
import xbeerelay.Relay.XbeeDigitalIOPin;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;

/**
 * This is the main class for the PC side.  It makes use of the XbeeRelayManager
 * API which can turn on and off relays by sending commands to a remote Arduino/Xbee
 * system.
 * 
 * <p>Running main in this class launches two windows: one for relay monitoring and
 * control, and another for plotting the current power consumption in the simulated
 * house.  This plot is updated in real time, and the remote Xbee chip is used for 
 * IO sampling.  See our main report for a description of the Xbee settings that
 * are required for this to work.
 * 
 * @author <a href=mailto:cdw38@cornell.edu>Casey Worthington</a>
 *
 */
public class XbeeSWTGui {
	
		
	protected static final int PIN_NUMBER_COLUMN = 2;
	protected static final int STATUS_COLUMN = 3;
	private static XbeeRelayManager relayManager;
	private static XBee xbee;
	private static int MAX_ITEM_AGE;
	private static Integer NUM_RELAYS;
	private static List<Relay> RELAY_LIST;
	private static int[] XB_ADDRESS;
	private static String SERIAL_ADDRESS;
	private static double ACTUAL_VCC;
	private static int CURRENT_RESISTOR;
	
	/**
	 * Returns an instance of XbeeDigitalIOPin given a String representation of that pin.
	 * See the enum (or the code) for valid values of this pin.
	 * 
	 * @param inXbeePinString string representation of an Xbee digital IO pin
	 * @return XbeeDigitalIOPin object corresponding to the input string
	 * @throws RelayException if the digital IO pin is invalid
	 */
	private static XbeeDigitalIOPin getXbeePinFromString(String inXbeePinString) 
		throws RelayException {
		if (inXbeePinString.equals("D0")) {
			return XbeeDigitalIOPin.D0;
		} else if (inXbeePinString.equals("D1")) {
			return XbeeDigitalIOPin.D1;
		} else if (inXbeePinString.equals("D2")) {
			return XbeeDigitalIOPin.D2;
		} else if (inXbeePinString.equals("D3")) {
			return XbeeDigitalIOPin.D3;
		} else if (inXbeePinString.equals("D4")) {
			return XbeeDigitalIOPin.D4;
		} else if (inXbeePinString.equals("D5")) {
			return XbeeDigitalIOPin.D5;
		} else if (inXbeePinString.equals("D6")) {
			return XbeeDigitalIOPin.D6;
		} else if (inXbeePinString.equals("D7")) {
			return XbeeDigitalIOPin.D7;
		} else if (inXbeePinString.equals("D10")) {
			return XbeeDigitalIOPin.D10;
		} else if (inXbeePinString.equals("D11")) {
			return XbeeDigitalIOPin.D11;
		} else if (inXbeePinString.equals("D12")) {
			return XbeeDigitalIOPin.D12;
		} else {
			throw new RelayException("Invalid Xbee Port for relay");
		}
	}
	
	/**
	 * This method simply checks if a Xbee pin is sensed as ON.
	 * <p>To do this, it makes use of the methods in ZNetRxIoSampleResponse.
	 * 
	 * @param inIOResponse 
	 * @param inXbPin
	 * @return
	 */
	private static boolean isXbeeDigitalPinOn(ZNetRxIoSampleResponse inIOResponse, XbeeDigitalIOPin inXbPin) {
		
		boolean isPinOn = false;
		
		if (inXbPin == XbeeDigitalIOPin.D0) {
			isPinOn = inIOResponse.isD0On();
		} else if (inXbPin == XbeeDigitalIOPin.D1) {
			isPinOn = inIOResponse.isD10On();
		} else if (inXbPin == XbeeDigitalIOPin.D2) {
			isPinOn = inIOResponse.isD2On();
		} else if (inXbPin == XbeeDigitalIOPin.D3) {
			isPinOn = inIOResponse.isD3On();
		} else if (inXbPin == XbeeDigitalIOPin.D4) {
			isPinOn = inIOResponse.isD4On();
		} else if (inXbPin == XbeeDigitalIOPin.D5) {
			isPinOn = inIOResponse.isD5On();
		} else if (inXbPin == XbeeDigitalIOPin.D6) {
			isPinOn = inIOResponse.isD6On();
		} else if (inXbPin == XbeeDigitalIOPin.D7) {
			isPinOn = inIOResponse.isD7On();
		} else if (inXbPin == XbeeDigitalIOPin.D10) {
			isPinOn = inIOResponse.isD10On();
		} else if (inXbPin == XbeeDigitalIOPin.D11) {
			isPinOn = inIOResponse.isD11On();
		} else if (inXbPin == XbeeDigitalIOPin.D12) {
			isPinOn = inIOResponse.isD12On();
		}
		
		return isPinOn;
	}
	
	/**
	 * Simply returns an instance of the enum RelayStatus based on whether or not a pin is on.
	 * 
	 * @param inIsPinOn true if the pin is on
	 * @return RelayStatus.ON if pin is on, RelayStatus.OFF if not
	 */
	private static RelayStatus getRelayStatusFromPinStatus(boolean inIsPinOn) {
		if (inIsPinOn) 
			return RelayStatus.ON;
		else
			return RelayStatus.OFF;
	}
	
	/**
	 * Reads the configuration file for this program.  I have this set in my home 
	 * directory (on a Linux machine).  You'll need to change this in order to make it
	 * work for you, unless your name is Casey and your home directory on a Linux machine
	 * is /home/casey, and you want to call your configuration file defaultCurrent.config.
	 * 
	 * @throws IOException
	 * @throws RelayException
	 */
	private static void readConfig() throws IOException, RelayException {
		// Read in config file
		Properties configFile = new Properties();
		FileInputStream inConfigFile = null;
		try {
			inConfigFile = new FileInputStream("/home/casey/defaultCurrent.config");
			configFile.load(inConfigFile);
		} finally {
			if (inConfigFile != null)
				inConfigFile.close();
		}
		MAX_ITEM_AGE  = Integer.valueOf(configFile.getProperty("MAX_ITEM_AGE"));
		
		// Relays
		NUM_RELAYS = Integer.valueOf(configFile.getProperty("NUM_RELAYS"));
		RELAY_LIST = new LinkedList<Relay>();
		for (int i = 1; i <= NUM_RELAYS; i++) {
			String label = configFile.getProperty(String.format("RELAY_LABEL_%d", i));
			int relaynum = Integer.valueOf(configFile.getProperty(String.format("RELAY_NUM_%d", i)));
			int pinnum = Integer.valueOf(configFile.getProperty(String.format("PIN_NUM_%d", i)));
			String xbeepinstring = configFile.getProperty(String.format("XBEE_PIN_%d", i));
			XbeeDigitalIOPin xbeepin = getXbeePinFromString(xbeepinstring);
			RELAY_LIST.add(new Relay(pinnum, relaynum, xbeepin, label));			
		}
		
		// Xbee
		SERIAL_ADDRESS = configFile.getProperty("SERIAL_ADDRESS");
		XB_ADDRESS = new int[8];
		for (int i = 0; i < 8; i++) {
			XB_ADDRESS[i] = Integer.parseInt(configFile.getProperty(String.format("XB_ADDRESS_%d", i)), 16);
		}
		
		ACTUAL_VCC = Double.valueOf(configFile.getProperty("ACTUAL_VCC", "5.0"));
		CURRENT_RESISTOR = Integer.valueOf(configFile.getProperty("CURRENT_RESISTOR"));
	}
	
	/**
	 * Starts the GUI.  This includes a relay-control and monitoring window, which lists the 
	 * relays configured in defaultConfig.config and their current status.  It also includes
	 * a graph of the current power consumption to the load.
	 * 
	 * @param args
	 * @throws RelayException
	 * @throws XBeeException
	 * @throws IOException
	 */
	public static void main(String[] args) throws RelayException, XBeeException, IOException {
		try {
			// Get configuration info
			readConfig();
			
			// Plotting stuff
			JFrame frame = new JFrame("Power Monitor");
			final PowerPlotter panel = new PowerPlotter(MAX_ITEM_AGE);
			frame.getContentPane().add(panel, BorderLayout.CENTER);
			frame.setBounds(200, 120, 600, 280);
			frame.setVisible(true);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			
			// Relay control stuff
			final Display display = new Display();
			Shell shell = new Shell(display);
			final Table table = new Table(shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION| SWT.CHECK);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			xbee = new XBee();
			
			final Map<Integer, TableItem> relayTableItemMap = new HashMap<Integer, TableItem>();

			// Add a menutoString on enum java
			Menu menuBar = new Menu(shell, SWT.BAR);
			// File menu
			Menu fileMenu = new Menu(menuBar);
			Menu relayMenu = new Menu(menuBar);
			// Create items in menuBar
			// File
			MenuItem fileItem = new MenuItem(menuBar, SWT.CASCADE);
			fileItem.setText("File");
			fileItem.setMenu(fileMenu);
			// Relay
			MenuItem relayItem = new MenuItem(menuBar, SWT.CASCADE);
			relayItem.setText("Relay");
			relayItem.setMenu(relayMenu);

			// Create file menu items
			MenuItem newItem = new MenuItem(fileMenu, SWT.NONE);
			newItem.setText("New");
			MenuItem openItem = new MenuItem(fileMenu, SWT.NONE);
			openItem.setText("Open...");
			MenuItem saveItem = new MenuItem(fileMenu, SWT.NONE);
			saveItem.setText("Save Relay Config");
			MenuItem saveAsItem = new MenuItem(fileMenu, SWT.NONE);
			saveAsItem.setText("Save Relay Config As...");

			// Create relay menu items
			MenuItem turnOnItem = new MenuItem(relayMenu, SWT.NONE);
			turnOnItem.setText("Turn selected relays on");

			// Listener to turn relays on
			turnOnItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Create a list of the relays to turn on
					List<Integer> turnOnList = new LinkedList<Integer>();
					for (TableItem ti : table.getItems()) {
						if (ti.getChecked()) {
							// This relay is checked -- add it to the list of relays
							// to turn on
							turnOnList.add(Integer.valueOf(ti.getText(PIN_NUMBER_COLUMN)));
						}
					}
					synchronized (relayManager) {
						try {
							relayManager.turnOn(turnOnList);
						} catch (NumberFormatException e1) {
							e1.printStackTrace();
						} catch (RelayException e1) {
							e1.printStackTrace();
						}
					}
				}
			});

			MenuItem turnOffItem = new MenuItem(relayMenu, SWT.NONE);
			turnOffItem.setText("Turn selected relays off");
			turnOffItem.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					List<Integer> turnOffList = new LinkedList<Integer>();
					for (TableItem ti : table.getItems()) {
						if (ti.getChecked()) {
							turnOffList.add(Integer.valueOf(ti.getText(PIN_NUMBER_COLUMN)));
						}
					}
					synchronized (relayManager) {
						try {
							relayManager.turnOff(turnOffList);
						} catch (NumberFormatException e1) {
							e1.printStackTrace();
						} catch (RelayException e1) {
							e1.printStackTrace();
						}
					}
				}
			});

			MenuItem updateStatusItem = new MenuItem(relayMenu, SWT.NONE);
			updateStatusItem.setText("Update relay status");
			updateStatusItem.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					synchronized (relayManager) {
						for (Relay r : relayManager.getManagedRelays()) {
							TableItem item = relayTableItemMap.get(r.getNumber());
							item.setText(new String[] {
									r.getLabel(), 
									String.valueOf(r.getNumber()), 
									String.valueOf(r.getPin()), 
									r.getStatusString()});
						}
					}
				}
				
			});

			// Set the menu bar so it's actually displayed
			shell.setMenuBar(menuBar);

			// Create the table
			String[] titles = { "Label", "Relay #", "Pin #", "Status" };
			for (int i = 0; i < titles.length; i++) {
				TableColumn column = new TableColumn(table, SWT.NONE);
				column.setText(titles[i]);
			}
			
			
			relayManager = new XbeeRelayManager(new XbeeManager(xbee), XB_ADDRESS);
			try {
				for (Relay r : RELAY_LIST) {
					relayManager.addManagedRelay(r);
				}
			} catch (RelayException e) {
				System.out.println(e);
				throw e;
			}


			// Populate items
			for (Relay r : relayManager.getManagedRelays()) {
				TableItem item = new TableItem(table, SWT.NONE);
				relayTableItemMap.put(r.getNumber(), item);
				item.setText(0, r.getLabel());
				item.setText(1, String.valueOf(r.getNumber()));
				item.setText(PIN_NUMBER_COLUMN, String.valueOf(r.getPin()));
				item.setText(STATUS_COLUMN, r.getStatusString());
			}

			for (int i=0; i<titles.length; i++) {
				table.getColumn (i).pack ();
			}     

			table.setSize(table.computeSize(SWT.DEFAULT, 200));

			// TODO: Read in configuration file to figure out what relays should be listed
			// Set the XBee up
			xbee.open(SERIAL_ADDRESS, 9600);
			xbee.addPacketListener(new PacketListener() {

				@Override
				public void processResponse(XBeeResponse response) {
					if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
						ZNetRxIoSampleResponse ioSample = (ZNetRxIoSampleResponse) response;
						
						System.out.println("received i/o sample packet.  contains analog is " + ioSample.containsAnalog() + ", contains digital is " + ioSample.containsDigital());
						
						// Update chart
						synchronized(panel) {
							if (ioSample.containsAnalog()) {
								System.out.println("Fart");
								int analogreading = ioSample.getAnalog0();
								System.out.println(analogreading);
								
								// This is the voltage at the current measuring resistor
								double voltageatmeasure = analogreading*1200.0/1024.0;
								double currentatmeasure = voltageatmeasure/CURRENT_RESISTOR;
								// Whatever voltage we don't observe on our resistor goes to load (the simulated house)
								// Need to multiply ACTUAL_VCC by 1000 since its in volts
								double voltagetoload = ACTUAL_VCC*1000 - voltageatmeasure;
								// Power = voltage * current
								double powertoload = voltagetoload * currentatmeasure; 
								panel.addPowerReading(powertoload);
							}
						}
						
						// Synchronize on relayManager here since other threads access that
						synchronized (relayManager) {
							// Update the status
							for (Relay r : relayManager.getManagedRelays()) {
								r.setStatus(getRelayStatusFromPinStatus(isXbeeDigitalPinOn(ioSample, r.getXbeePin())));
							}
						}
						
					}
				}				
			});

			
			shell.pack();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();
		} finally {
			if (xbee != null)
				xbee.close();
		}
	}
}


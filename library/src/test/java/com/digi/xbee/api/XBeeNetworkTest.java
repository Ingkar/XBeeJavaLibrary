/**
 * Copyright (c) 2014 Digi International Inc.,
 * All rights not expressly granted are reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
 * =======================================================================
 */
package com.digi.xbee.api;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.digi.xbee.api.connection.IConnectionInterface;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeProtocol;

@PrepareForTest({XBeeDevice.class, NodeDiscovery.class, XBeeNetwork.class})
@RunWith(PowerMockRunner.class)
public class XBeeNetworkTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	// Variables.
	private XBeeNetwork network;
	
	private XBeeDevice deviceMock;
	
	private NodeDiscovery ndMock;
	
	private RemoteXBeeDevice idFoundDevice;
	
	public XBeeNetworkTest() {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ndMock = PowerMockito.mock(NodeDiscovery.class);
		deviceMock = PowerMockito.mock(XBeeDevice.class);
		IConnectionInterface cInterfaceMock = PowerMockito.mock(IConnectionInterface.class);
		
		PowerMockito.when(deviceMock.getConnectionInterface()).thenReturn(cInterfaceMock);
		PowerMockito.when(cInterfaceMock.toString()).thenReturn("Mocked IConnectionInterface for XBeeNetwork test.");
		
		idFoundDevice = new RemoteXBeeDevice(deviceMock, new XBee64BitAddress("0013A20040A9E77E"), 
				XBee16BitAddress.UNKNOWN_ADDRESS, "id");
		
		List<RemoteXBeeDevice> idFoundDevices = new ArrayList<RemoteXBeeDevice>(1);
		idFoundDevices.add(idFoundDevice);
		
		PowerMockito.whenNew(NodeDiscovery.class).withArguments(deviceMock).thenReturn(ndMock);
		PowerMockito.when(ndMock.discoverDeviceByID(Mockito.anyString(), Mockito.anyLong())).thenReturn(idFoundDevice);
		PowerMockito.when(ndMock.discoverAllDevicesByID(Mockito.anyString(), Mockito.anyLong())).thenReturn(idFoundDevices);
		
		network = PowerMockito.spy(new XBeeNetwork(deviceMock));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#XBeeNetwork(XBeeDevice)}.
	 * 
	 * <p>A {@code NullPointerException} exception must be thrown when passing a 
	 * {@code null} local device is passed.</p>
	 */
	@Test
	public final void testCreateXBeeNetworkNullDevice() {
		// Setup the resources for the test.
		exception.expect(NullPointerException.class);
		exception.expectMessage(is(equalTo("Local XBee device cannot be null.")));
				
		// Call the method under test.
		new XBeeNetwork(null);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverDeviceByID(String)}.
	 * 
	 * <p>A {@code NullPointerException} exception must be thrown when passing a 
	 * {@code null} id.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverDeviceByIDOnlyIdNull() throws XBeeException {
		// Setup the resources for the test.
		exception.expect(NullPointerException.class);
		exception.expectMessage(is(equalTo("Device identifier cannot be null.")));
						
		// Call the method under test.
		network.discoverDeviceByID(null);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverDeviceByID(String)}.
	 * 
	 * <p>An {@code IllegalArgumentException} exception must be thrown when 
	 * passing an empty id.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverDeviceByIDOnlyIdEmpty() throws XBeeException {
		// Setup the resources for the test.
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(is(equalTo("Device identifier cannot be an empty string.")));
						
		// Call the method under test.
		network.discoverDeviceByID("");
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverDeviceByID(String)}.
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverDeviceByIDOnlyIdValid() throws XBeeException {
		// Setup the resources for the test.
		String id = "id";
		
		// Call the method under test.
		RemoteXBeeDevice found = network.discoverDeviceByID(id);
		
		// Verify the result.
		Mockito.verify(ndMock, Mockito.times(1)).discoverDeviceByID(id, NodeDiscovery.USE_DEVICE_TIMEOUT);
		
		assertThat("Found device must not be null", found, is(not(nullValue())));
		assertThat("Not expected 64-bit address in found device", found.get64BitAddress(), is(equalTo(idFoundDevice.get64BitAddress())));
		assertThat("Not expected 16-bit address in found device", found.get16BitAddress(), is(equalTo(idFoundDevice.get16BitAddress())));
		assertThat("Not expected id in found device", found.getNodeID(), is(equalTo(idFoundDevice.getNodeID())));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverDeviceByID(String, long)}.
	 * 
	 * <p>A {@code NullPointerException} exception must be thrown when passing a 
	 * {@code null} id.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverDeviceByIDIdNull() throws XBeeException {
		// Setup the resources for the test.
		exception.expect(NullPointerException.class);
		exception.expectMessage(is(equalTo("Device identifier cannot be null.")));
						
		// Call the method under test.
		network.discoverDeviceByID(null, 500);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverDeviceByID(String, long)}.
	 * 
	 * <p>An {@code IllegalArgumentException} exception must be thrown when 
	 * passing an empty id.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverDeviceByIDIdEmpty() throws XBeeException {
		// Setup the resources for the test.
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(is(equalTo("Device identifier cannot be an empty string.")));
						
		// Call the method under test.
		network.discoverDeviceByID("", 500);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverDeviceByID(String, long)}.
	 * 
	 * <p>An {@code IllegalArgumentException} exception must be thrown when 
	 * passing a wait forever.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverDeviceByIDWaitForever() throws XBeeException {
		// Setup the resources for the test.
		String id = "id";
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(is(equalTo("The discovery devices process cannot block forever.")));
		
		// Call the method under test.
		network.discoverDeviceByID(id, NodeDiscovery.WAIT_FOREVER);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverDeviceByID(String, long)}.
	 * 
	 * <p>An {@code IllegalArgumentException} exception must be thrown when 
	 * passing a negative timeout.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverDeviceByIDNegativeTimeout() throws XBeeException {
		// Setup the resources for the test.
		String id = "id";
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(is(equalTo("The timeout must be bigger than 0.")));
		
		// Call the method under test.
		network.discoverDeviceByID(id, -5);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverDeviceByID(String, long)}.
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverDeviceByIDValidTimeout() throws XBeeException {
		// Setup the resources for the test.
		String id = "id";
		long timeout = 500;
		
		// Call the method under test.
		RemoteXBeeDevice found = network.discoverDeviceByID(id, timeout);
		
		// Verify the result.
		Mockito.verify(ndMock, Mockito.times(1)).discoverDeviceByID(id, timeout);
		
		assertThat("Found device must not be null", found, is(not(nullValue())));
		assertThat("Not expected 64-bit address in found device", found.get64BitAddress(), is(equalTo(idFoundDevice.get64BitAddress())));
		assertThat("Not expected 16-bit address in found device", found.get16BitAddress(), is(equalTo(idFoundDevice.get16BitAddress())));
		assertThat("Not expected id in found device", found.getNodeID(), is(equalTo(idFoundDevice.getNodeID())));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverAllDevicesByID(String, long)}.
	 * 
	 * <p>A {@code NullPointerException} exception must be thrown when passing a 
	 * {@code null} id.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverAllDevicesByIDNullId() throws XBeeException {
		// Setup the resources for the test.
		exception.expect(NullPointerException.class);
		exception.expectMessage(is(equalTo("Device identifier cannot be null.")));
		
		// Call the method under test.
		network.discoverAllDevicesByID(null, NodeDiscovery.USE_DEVICE_TIMEOUT);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverAllDevicesByID(String, long)}.
	 * 
	 * <p>An {@code IllegalArgumentException} exception must be thrown when 
	 * passing an empty id.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverAllDevicesByIDEmptyId() throws XBeeException {
		// Setup the resources for the test.
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(is(equalTo("Device identifier cannot be an empty string.")));
		
		// Call the method under test.
		network.discoverAllDevicesByID("", NodeDiscovery.USE_DEVICE_TIMEOUT);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverAllDevicesByID(String, long)}.
	 * 
	 * <p>An {@code IllegalArgumentException} exception must be thrown when 
	 * passing a wait forever.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverAllDevicesByIDWaitForever() throws XBeeException {
		// Setup the resources for the test.
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(is(equalTo("The discovery devices process cannot block forever.")));
		
		// Call the method under test.
		network.discoverAllDevicesByID("id", NodeDiscovery.WAIT_FOREVER);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverAllDevicesByID(String, long)}.
	 * 
	 * <p>An {@code IllegalArgumentException} exception must be thrown when 
	 * passing a negative timeout.</p>
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverAllDevicesByIDNegativeTimeout() throws XBeeException {
		// Setup the resources for the test.
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(is(equalTo("The timeout must be bigger than 0.")));
		
		// Call the method under test.
		network.discoverAllDevicesByID("id", -6);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#discoverAllDevicesByID(String, long)}.
	 * 
	 * @throws XBeeException 
	 */
	@Test
	public final void testDiscoverAllDevicesByIDValidTimeout() throws XBeeException {
		// Setup the resources for the test.
		String id = "id";
		long timeout = 500;
		
		// Call the method under test.
		List<RemoteXBeeDevice> found = network.discoverAllDevicesByID(id, timeout);
		
		// Verify the result.
		Mockito.verify(ndMock, Mockito.times(1)).discoverAllDevicesByID(id, timeout);
		
		assertThat("Found device list must not be null", found, is(not(nullValue())));
		assertThat("Found device list must have one device", found.size(), is(equalTo(1)));
		
		RemoteXBeeDevice d = found.get(0);
		assertThat("Not expected 64-bit address in found device", d.get64BitAddress(), is(equalTo(idFoundDevice.get64BitAddress())));
		assertThat("Not expected 16-bit address in found device", d.get16BitAddress(), is(equalTo(idFoundDevice.get16BitAddress())));
		assertThat("Not expected id in found device", d.getNodeID(), is(equalTo(idFoundDevice.getNodeID())));
	}
	
	// TODO test for all discoverDevices methods
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevice(RemoteXBeeDevice)}.
	 * 
	 * <p>A {@code NullPointerException} exception must be thrown when 
	 * passing a null device.</p>
	 */
	@Test
	public final void testAddRemoteDeviceNullDevice() {
		// Setup the resources for the test.
		RemoteXBeeDevice device = null;
		exception.expect(NullPointerException.class);
		exception.expectMessage(is(equalTo("Remote device cannot be null.")));
		
		// Call the method under test.
		network.addRemoteDevice(device);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevice(RemoteXBeeDevice)}.
	 * 
	 * <p>Add a complete device to an empty network.</p>
	 */
	@Test
	public final void testAddRemoteDeviceCompleteDeviceNotInNetwork() {
		// Setup the resources for the test.
		PowerMockito.when(deviceMock.getXBeeProtocol()).thenReturn(XBeeProtocol.ZIGBEE);
		XBee64BitAddress addr64 = new XBee64BitAddress("0013A20040A9E78B");
		RemoteXBeeDevice device = new RemoteZigBeeDevice(deviceMock, 
				addr64, new XBee16BitAddress("1256"), "id");
		
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(0)));
		assertThat(add16Map.size(), is(equalTo(0)));
		
		// Call the method under test.
		RemoteXBeeDevice addedDevice = network.addRemoteDevice(device);
		
		// Verify the result.
		RemoteXBeeDevice devInNetwork = network.getDeviceBy64BitAddress(addr64);
		
		assertThat("There must be 1 device in the network", network.getNumberOfDevices(), is(equalTo(1)));
		assertThat("The added and returned references must be the same", addedDevice == device, is(equalTo(true)));
		
		assertThat("The added reference and the one in the network must be the same", addedDevice == devInNetwork, is(equalTo(true)));
		
		assertThat("There must be 1 device in the 64-bit map", add64Map.size(), is(equalTo(1)));
		assertThat("The 16-bit map must be empty", add16Map.size(), is(equalTo(0)));
		assertThat("The 64-bit map must contain the added device", add64Map.containsValue(addedDevice), is(equalTo(true)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevice(RemoteXBeeDevice)}.
	 * 
	 * <p>Add a complete device to a network that already contains it.</p>
	 */
	@Test
	public final void testAddRemoteDeviceCompleteDeviceAlreadyInNetwork() {
		// Setup the resources for the test.
		PowerMockito.when(deviceMock.getXBeeProtocol()).thenReturn(XBeeProtocol.ZIGBEE);
		XBee64BitAddress addr64 = new XBee64BitAddress("0013A20040A9E78B");
		RemoteXBeeDevice deviceToAdd = new RemoteZigBeeDevice(deviceMock, 
				new XBee64BitAddress("0013A20040A9E78B"), new XBee16BitAddress("1256"), "id_new");
		RemoteXBeeDevice deviceAlreadyInNetwork = PowerMockito.spy(new RemoteZigBeeDevice(deviceMock, 
				addr64, new XBee16BitAddress("1256"), "id"));
		
		network.addRemoteDevice(deviceAlreadyInNetwork);
		
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(1)));
		assertThat(add16Map.size(), is(equalTo(0)));
		
		// Call the method under test.
		RemoteXBeeDevice addedDevice = network.addRemoteDevice(deviceToAdd);
		
		// Verify the result.
		RemoteXBeeDevice devInNetwork = network.getDeviceBy64BitAddress(addr64);
		
		assertThat("There must be 1 device in the network", network.getNumberOfDevices(), is(equalTo(1)));
		
		assertThat("The reference to add and the one in the network must be different", deviceAlreadyInNetwork == deviceToAdd, is(equalTo(false)));
		
		assertThat("The added and returned references must be the different", deviceToAdd == addedDevice, is(equalTo(false)));
		assertThat("The added reference and the one in the network must be the same", addedDevice == deviceAlreadyInNetwork, is(equalTo(true)));
		
		assertThat("The added reference and the one in the network must be the same", addedDevice == devInNetwork, is(equalTo(true)));
		
		assertThat("There must be 1 device in the 64-bit map", add64Map.size(), is(equalTo(1)));
		assertThat("The 16-bit map must be empty", add16Map.size(), is(equalTo(0)));
		assertThat("The 64-bit map must contain the added device", add64Map.containsValue(addedDevice), is(equalTo(true)));
		
		Mockito.verify(deviceAlreadyInNetwork, Mockito.times(1)).updateDeviceDataFrom(deviceToAdd);
		assertThat("The Node id of the device must be updated", addedDevice.getNodeID(), is(equalTo(deviceToAdd.getNodeID())));
		assertThat(addedDevice.get64BitAddress() == deviceToAdd.get64BitAddress(), is(equalTo(false)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevice(RemoteXBeeDevice)}.
	 * 
	 * <p>Add a only 16-bit device to an empty network.</p>
	 */
	@Test
	public final void testAddRemoteDevice16BitDeviceNotInNetwork() {
		// Setup the resources for the test.
		PowerMockito.when(deviceMock.getXBeeProtocol()).thenReturn(XBeeProtocol.RAW_802_15_4);
		XBee16BitAddress addr16 = new XBee16BitAddress("1256");
		RemoteXBeeDevice device = new RemoteRaw802Device(deviceMock, XBee64BitAddress.UNKNOWN_ADDRESS, addr16, "id");
		
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(0)));
		assertThat(add16Map.size(), is(equalTo(0)));
		
		// Call the method under test.
		RemoteXBeeDevice addedDevice = network.addRemoteDevice(device);
		
		// Verify the result.
		RemoteXBeeDevice devInNetwork = network.getDeviceBy16BitAddress(addr16);
		
		assertThat("There must be 1 device in the network", network.getNumberOfDevices(), is(equalTo(1)));
		
		assertThat("The added and returned references must be the same", addedDevice == device, is(equalTo(true)));
		
		assertThat("The added reference and the one in the network must be the same", addedDevice == devInNetwork, is(equalTo(true)));
		
		assertThat("The 64-bit map must be empty", add64Map.size(), is(equalTo(0)));
		assertThat("There must be 1 device in the 16-bit map", add16Map.size(), is(equalTo(1)));
		assertThat("The 16-bit map must contain the added device", add16Map.containsValue(addedDevice), is(equalTo(true)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevice(RemoteXBeeDevice)}.
	 * 
	 * <p>Add a only 16-bit device to a network that already contains it.</p>
	 */
	@Test
	public final void testAddRemoteDevice16BitDeviceAlreadyInNetwork() {
		// Setup the resources for the test.
		PowerMockito.when(deviceMock.getXBeeProtocol()).thenReturn(XBeeProtocol.RAW_802_15_4);
		XBee16BitAddress addr16 = new XBee16BitAddress("1256");
		RemoteXBeeDevice deviceToAdd = new RemoteRaw802Device(deviceMock, 
				XBee64BitAddress.UNKNOWN_ADDRESS, addr16, "id_new");
		RemoteXBeeDevice deviceAlreadyInNetwork = PowerMockito.spy(new RemoteRaw802Device(deviceMock, 
				XBee64BitAddress.UNKNOWN_ADDRESS, addr16, "id"));
		
		network.addRemoteDevice(deviceAlreadyInNetwork);
		
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(0)));
		assertThat(add16Map.size(), is(equalTo(1)));
		
		// Call the method under test.
		RemoteXBeeDevice addedDevice = network.addRemoteDevice(deviceToAdd);
		
		// Verify the result.
		RemoteXBeeDevice devInNetwork = network.getDeviceBy16BitAddress(addr16);
		
		assertThat("There must be 1 device in the network", network.getNumberOfDevices(), is(equalTo(1)));
		
		assertThat("The added and returned references must be the different", deviceAlreadyInNetwork == deviceToAdd, is(equalTo(false)));
		
		assertThat("The reference to add and the one in the network must be different", deviceToAdd == addedDevice, is(equalTo(false)));
		assertThat("The added reference and the one in the network must be the same", addedDevice == deviceAlreadyInNetwork, is(equalTo(true)));
		
		assertThat("The added reference and the one in the network must be the same", addedDevice == devInNetwork, is(equalTo(true)));
		
		assertThat("The 64-bit map must be empty", add64Map.size(), is(equalTo(0)));
		assertThat("There must be 1 device in the 16-bit map", add16Map.size(), is(equalTo(1)));
		assertThat("The 16-bit map must contain the added device", add16Map.containsValue(addedDevice), is(equalTo(true)));
		
		Mockito.verify(deviceAlreadyInNetwork, Mockito.times(1)).updateDeviceDataFrom(deviceToAdd);
		assertThat("The Node id of the device must be updated", addedDevice.getNodeID(), is(equalTo(deviceToAdd.getNodeID())));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevice(RemoteXBeeDevice)}.
	 * 
	 * <p>Add a only 16-bit device to a network that already contains it.</p> 
	 */
	@Test
	public final void testAddRemoteDevice16BitDeviceAlreadyInNetworkWithValid64BitAddr() {
		// Setup the resources for the test.
		PowerMockito.when(deviceMock.getXBeeProtocol()).thenReturn(XBeeProtocol.RAW_802_15_4);
		XBee16BitAddress addr16 = new XBee16BitAddress("1256");
		XBee64BitAddress addr64 = new XBee64BitAddress("0013A20040A9E78B");
		RemoteXBeeDevice deviceToAdd = new RemoteRaw802Device(deviceMock, 
				addr64, addr16, "id_new");
		RemoteXBeeDevice deviceAlreadyInNetwork = PowerMockito.spy(new RemoteRaw802Device(deviceMock, 
				XBee64BitAddress.UNKNOWN_ADDRESS, addr16, "id"));
		
		network.addRemoteDevice(deviceAlreadyInNetwork);
		
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(0)));
		assertThat(add16Map.size(), is(equalTo(1)));
		
		// Call the method under test.
		RemoteXBeeDevice addedDevice = network.addRemoteDevice(deviceToAdd);
		
		// Verify the result.
		RemoteXBeeDevice devInNetwork = network.getDeviceBy16BitAddress(addr16);
		
		assertThat("There must be 1 device in the network", network.getNumberOfDevices(), is(equalTo(1)));
		
		assertThat("The added and returned references must be the different", deviceAlreadyInNetwork == deviceToAdd, is(equalTo(false)));
		
		assertThat("The reference to add and the one in the network must be different", deviceToAdd == addedDevice, is(equalTo(false)));
		assertThat("The added reference and the one in the network must be the same", addedDevice == deviceAlreadyInNetwork, is(equalTo(true)));
		
		assertThat("The added reference and the one in the network must be the same", addedDevice == devInNetwork, is(equalTo(true)));
		
		assertThat("There must be 1 device in the 64-bit map", add64Map.size(), is(equalTo(1)));
		assertThat("The 16-bit map must be empty", add16Map.size(), is(equalTo(0)));
		assertThat("The 64-bit map must contain the added device", add64Map.containsValue(addedDevice), is(equalTo(true)));
		
		Mockito.verify(deviceAlreadyInNetwork, Mockito.times(1)).updateDeviceDataFrom(deviceToAdd);
		assertThat("The Node id of the device must be updated", addedDevice.getNodeID(), is(equalTo(deviceToAdd.getNodeID())));
		assertThat(addedDevice.get64BitAddress(), is(equalTo(addr64)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevices(java.util.List)}.
	 * 
	 * <p>A {@code NullPointerException} exception must be thrown when 
	 * passing a null list.</p>
	 */
	@Test
	public final void testAddRemoteDevicesNullList() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = null;
		exception.expect(NullPointerException.class);
		exception.expectMessage(is(equalTo("The list of remote devices cannot be null.")));
		
		// Call the method under test.
		network.addRemoteDevices(list);
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevices(java.util.List)}.
	 * 
	 * When passing an empty list nothing is done.
	 */
	@Test
	public final void testAddRemoteDevicesEmptyList() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = new ArrayList<RemoteXBeeDevice>(0);
		
		
		// Call the method under test.
		List<RemoteXBeeDevice> added = network.addRemoteDevices(list);
		
		// Verify the result.
		Mockito.verify(network, Mockito.never()).addRemoteDevice(Mockito.any(RemoteXBeeDevice.class));
		
		assertThat("Returned list must be empty", added.size(), is(equalTo(0)));
		assertThat("The network must be empty", network.getNumberOfDevices(), is(equalTo(0)));
		assertThat("The added reference and the returned must be the different", list == added, is(equalTo(false)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevices(java.util.List)}.
	 * 
	 * Add a list of devices to an empty network.
	 */
	@Test
	public final void testAddRemoteDevicesToEmptyNetwork() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = new ArrayList<RemoteXBeeDevice>(0);
		for (int i = 0; i < 3; i++)
			list.add(new RemoteXBeeDevice(deviceMock, 
					new XBee64BitAddress("0013A20040A9E78"+i), 
					XBee16BitAddress.UNKNOWN_ADDRESS, "id"+i));
		
		// Call the method under test.
		List<RemoteXBeeDevice> added = network.addRemoteDevices(list);
		
		// Verify the result.
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(3)));
		assertThat(add16Map.size(), is(equalTo(0)));
		
		Mockito.verify(network, Mockito.times(list.size())).addRemoteDevice(Mockito.any(RemoteXBeeDevice.class));
		
		assertThat("Returned list must contain " + list.size(), added.size(), is(equalTo(list.size())));
		assertThat("The network must contain " + list.size(), network.getNumberOfDevices(), is(equalTo(list.size())));
		assertThat("The added reference and the returned must be the different", list == added, is(equalTo(false)));
		
		for (int i = 0; i < list.size(); i++)
			assertThat("The device reference in the network and in the list to add must be the same", 
					list.get(i) == added.get(i), is(equalTo(true)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevices(java.util.List)}.
	 * 
	 * Add a list of devices to not empty network and a already existing device.
	 */
	@Test
	public final void testAddRemoteDevicesToNetworkWithAlreadyExistingDevices() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = new ArrayList<RemoteXBeeDevice>(0);
		for (int i = 0; i < 3; i++)
			list.add(new RemoteXBeeDevice(deviceMock, 
					new XBee64BitAddress("0013A20040A9E78"+i), 
					XBee16BitAddress.UNKNOWN_ADDRESS, "id"+i));
		
		RemoteXBeeDevice deviceAlreadyInNetwork = PowerMockito.spy(
				new RemoteXBeeDevice(deviceMock, 
				new XBee64BitAddress("0013A20040A9E780"), 
				XBee16BitAddress.UNKNOWN_ADDRESS, "id0_old"));
		network.addRemoteDevice(deviceAlreadyInNetwork);
		
		// Call the method under test.
		List<RemoteXBeeDevice> added = network.addRemoteDevices(list);
		
		// Verify the result.
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(3)));
		assertThat(add16Map.size(), is(equalTo(0)));
		
		Mockito.verify(network, Mockito.times(list.size() + 1 /*We are adding in the setup*/)).addRemoteDevice(Mockito.any(RemoteXBeeDevice.class));
		
		assertThat("Returned list must contain " + list.size(), added.size(), is(equalTo(list.size())));
		assertThat("The network must contain " + list.size(), network.getNumberOfDevices(), is(equalTo(list.size())));
		assertThat("The added reference and the returned must be the different", list == added, is(equalTo(false)));
		
		Mockito.verify(deviceAlreadyInNetwork, Mockito.times(1)).updateDeviceDataFrom(list.get(0));
		assertThat("The reference of existing device in the network and in the list to add must be different", 
				list.get(0) == added.get(0), is(equalTo(false)));
		for (int i = 1; i < list.size(); i++)
			assertThat("The device reference in the network and in the list to add must be the same", 
					list.get(i) == added.get(i), is(equalTo(true)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevices(java.util.List)}.
	 * 
	 * Add a list of only 16-bit devices to an empty network.
	 */
	@Test
	public final void testAddRemoteDevices16BitToEmptyNetwork() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = new ArrayList<RemoteXBeeDevice>(0);
		for (int i = 0; i < 3; i++)
			list.add(new RemoteXBeeDevice(deviceMock, 
					XBee64BitAddress.UNKNOWN_ADDRESS, 
					new XBee16BitAddress("123"+i), "id"+i));
		
		// Call the method under test.
		List<RemoteXBeeDevice> added = network.addRemoteDevices(list);
		
		// Verify the result.
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(0)));
		assertThat(add16Map.size(), is(equalTo(3)));
		
		Mockito.verify(network, Mockito.times(list.size())).addRemoteDevice(Mockito.any(RemoteXBeeDevice.class));
		
		assertThat("Returned list must contain " + list.size(), added.size(), is(equalTo(list.size())));
		assertThat("The network must contain " + list.size(), network.getNumberOfDevices(), is(equalTo(list.size())));
		assertThat("The added reference and the returned must be the different", list == added, is(equalTo(false)));
		
		for (int i = 0; i < list.size(); i++)
			assertThat("The device reference in the network and in the list to add must be the same", 
					list.get(i) == added.get(i), is(equalTo(true)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevices(java.util.List)}.
	 * 
	 * Add a list of devices to not empty network and a already existing device.
	 */
	@Test
	public final void testAddRemoteDevices16BitToNetworkWithAlreadyExistingDevices() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = new ArrayList<RemoteXBeeDevice>(0);
		for (int i = 0; i < 3; i++)
			list.add(new RemoteXBeeDevice(deviceMock, 
					XBee64BitAddress.UNKNOWN_ADDRESS, 
					new XBee16BitAddress("123"+i), "id"+i));
		
		RemoteXBeeDevice deviceAlreadyInNetwork = PowerMockito.spy(
				new RemoteXBeeDevice(deviceMock, 
				XBee64BitAddress.UNKNOWN_ADDRESS, 
				new XBee16BitAddress("1230"), "id0_old"));
		network.addRemoteDevice(deviceAlreadyInNetwork);
		
		// Call the method under test.
		List<RemoteXBeeDevice> added = network.addRemoteDevices(list);
		
		// Verify the result.
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(0)));
		assertThat(add16Map.size(), is(equalTo(3)));
		
		Mockito.verify(network, Mockito.times(list.size() + 1 /*We are adding in the setup*/)).addRemoteDevice(Mockito.any(RemoteXBeeDevice.class));
		
		assertThat("Returned list must contain " + list.size(), added.size(), is(equalTo(list.size())));
		assertThat("The network must contain " + list.size(), network.getNumberOfDevices(), is(equalTo(list.size())));
		assertThat("The added reference and the returned must be the different", list == added, is(equalTo(false)));
		
		Mockito.verify(deviceAlreadyInNetwork, Mockito.times(1)).updateDeviceDataFrom(list.get(0));
		assertThat("The reference of existing device in the network and in the list to add must be different", 
				list.get(0) == added.get(0), is(equalTo(false)));
		for (int i = 1; i < list.size(); i++)
			assertThat("The device reference in the network and in the list to add must be the same", 
					list.get(i) == added.get(i), is(equalTo(true)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevices(java.util.List)}.
	 * 
	 * Add a list of devices to not empty network and a already existing device.
	 */
	@Test
	public final void testAddRemoteDevices64BitToNetworkWithAlreadyExisting16BitDevices() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = new ArrayList<RemoteXBeeDevice>(0);
		for (int i = 0; i < 3; i++)
			list.add(new RemoteXBeeDevice(deviceMock, 
					new XBee64BitAddress("0013A20040A9E78"+i),
					new XBee16BitAddress("123"+i), "id"+i));
		
		RemoteXBeeDevice deviceAlreadyInNetwork = PowerMockito.spy(
				new RemoteXBeeDevice(deviceMock, 
				XBee64BitAddress.UNKNOWN_ADDRESS, 
				new XBee16BitAddress("1230"), "id0_old"));
		network.addRemoteDevice(deviceAlreadyInNetwork);
		
		// Call the method under test.
		List<RemoteXBeeDevice> added = network.addRemoteDevices(list);
		
		// Verify the result.
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(3)));
		assertThat(add16Map.size(), is(equalTo(0)));
		
		Mockito.verify(network, Mockito.times(list.size() + 1 /*We are adding in the setup*/)).addRemoteDevice(Mockito.any(RemoteXBeeDevice.class));
		
		assertThat("Returned list must contain " + list.size(), added.size(), is(equalTo(list.size())));
		assertThat("The network must contain " + list.size(), network.getNumberOfDevices(), is(equalTo(list.size())));
		assertThat("The added reference and the returned must be the different", list == added, is(equalTo(false)));
		
		Mockito.verify(deviceAlreadyInNetwork, Mockito.times(1)).updateDeviceDataFrom(list.get(0));
		assertThat("The reference of existing device in the network and in the list to add must be different", 
				list.get(0) == added.get(0), is(equalTo(false)));
		for (int i = 1; i < list.size(); i++)
			assertThat("The device reference in the network and in the list to add must be the same", 
					list.get(i) == added.get(i), is(equalTo(true)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#addRemoteDevices(java.util.List)}.
	 * 
	 * Add a list of devices to not empty network and a already existing device.
	 */
	@Test
	public final void testAddRemoteDevices16BitToNetworkWithAlreadyExisting64BitDevices() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = new ArrayList<RemoteXBeeDevice>(0);
		for (int i = 0; i < 3; i++)
			list.add(new RemoteXBeeDevice(deviceMock, 
					XBee64BitAddress.UNKNOWN_ADDRESS,
					new XBee16BitAddress("123"+i), "id"+i));
		
		RemoteXBeeDevice deviceAlreadyInNetwork = PowerMockito.spy(
				new RemoteXBeeDevice(deviceMock, 
						new XBee64BitAddress("0013A20040A9E780"), 
				new XBee16BitAddress("1230"), "id0_old"));
		network.addRemoteDevice(deviceAlreadyInNetwork);
		
		// Call the method under test.
		List<RemoteXBeeDevice> added = network.addRemoteDevices(list);
		
		// Verify the result.
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(1)));
		assertThat(add16Map.size(), is(equalTo(2)));
		
		Mockito.verify(network, Mockito.times(list.size() + 1 /*We are adding in the setup*/)).addRemoteDevice(Mockito.any(RemoteXBeeDevice.class));
		
		assertThat("Returned list must contain " + list.size(), added.size(), is(equalTo(list.size())));
		assertThat("The network must contain " + list.size(), network.getNumberOfDevices(), is(equalTo(list.size())));
		assertThat("The added reference and the returned must be the different", list == added, is(equalTo(false)));
		
		Mockito.verify(deviceAlreadyInNetwork, Mockito.times(1)).updateDeviceDataFrom(list.get(0));
		assertThat("The reference of existing device in the network and in the list to add must be different", 
				list.get(0) == added.get(0), is(equalTo(false)));
		for (int i = 1; i < list.size(); i++)
			assertThat("The device reference in the network and in the list to add must be the same", 
					list.get(i) == added.get(i), is(equalTo(true)));
	}
	
	/**
	 * Test method for {@link com.digi.xbee.api.XBeeNetwork#clear()}.
	 * 
	 * Clear non-empty network.
	 */
	@Test
	public final void testClear() {
		// Setup the resources for the test.
		List<RemoteXBeeDevice> list = new ArrayList<RemoteXBeeDevice>(0);
		for (int i = 0; i < 3; i++)
			list.add(new RemoteXBeeDevice(deviceMock, 
					new XBee64BitAddress("0013A20040A9E78"+i), 
					XBee16BitAddress.UNKNOWN_ADDRESS, "id"+i));
		
		network.addRemoteDevices(list);
		
		Map<XBee64BitAddress, RemoteXBeeDevice> add64Map = Whitebox.<Map<XBee64BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy64BitAddr");
		Map<XBee16BitAddress, RemoteXBeeDevice> add16Map = Whitebox.<Map<XBee16BitAddress, RemoteXBeeDevice>> getInternalState(network, "remotesBy16BitAddr");
		
		assertThat(add64Map.size(), is(equalTo(3)));
		assertThat(add16Map.size(), is(equalTo(0)));
		
		assertThat("There must be " + list.size() + " devices in the network", network.getNumberOfDevices(), is(equalTo(list.size())));
		
		// Call the method under test.
		network.clear();
		
		// Verify the result.
		assertThat(add64Map.size(), is(equalTo(0)));
		assertThat(add16Map.size(), is(equalTo(0)));
		
		assertThat("The network must be empty", network.getNumberOfDevices(), is(equalTo(0)));
	}
}
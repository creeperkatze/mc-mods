package dev.creeperkatze.illuminate.output.openrgb;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// A blocking connection to an OpenRGB SDK server
final class OpenRgbClient implements Closeable {
	// Version 5 changes the controller layout and is not needed for direct colours
	private static final int MAX_PROTOCOL = 4;
	private static final int CONNECT_TIMEOUT_MS = 1500;
	private static final int READ_TIMEOUT_MS = 2000;
	private static final int MAX_PAYLOAD = 16 * 1024 * 1024;
	private static final byte[] MAGIC = "ORGB".getBytes(StandardCharsets.US_ASCII);

	private static final int REQUEST_CONTROLLER_COUNT = 0;
	private static final int REQUEST_CONTROLLER_DATA = 1;
	private static final int REQUEST_PROTOCOL_VERSION = 40;
	private static final int SET_CLIENT_NAME = 50;
	private static final int DEVICE_LIST_UPDATED = 100;
	private static final int UPDATE_LEDS = 1050;
	private static final int SET_CUSTOM_MODE = 1100;
	private static final int UPDATE_MODE = 1101;

	private final Socket socket;
	private final DataInputStream in;
	private final OutputStream out;
	private int protocol;
	private boolean listChanged;

	private OpenRgbClient(Socket socket) throws IOException {
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = socket.getOutputStream();
	}

	static OpenRgbClient connect(String host, int port, String clientName) throws IOException {
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
			socket.setSoTimeout(READ_TIMEOUT_MS);
			socket.setTcpNoDelay(true);
			OpenRgbClient client = new OpenRgbClient(socket);
			client.send(0, SET_CLIENT_NAME, (clientName + "\0").getBytes(StandardCharsets.UTF_8));
			client.negotiateProtocol();
			return client;
		} catch (IOException e) {
			socket.close();
			throw e;
		}
	}

	private void negotiateProtocol() throws IOException {
		send(0, REQUEST_PROTOCOL_VERSION, buffer(4).putInt(MAX_PROTOCOL).array());
		try {
			protocol = Math.min(read(REQUEST_PROTOCOL_VERSION).getInt(), MAX_PROTOCOL);
		} catch (SocketTimeoutException e) {
			// Servers from before protocol versions never answer
			protocol = 0;
		}
	}

	int protocol() {
		return protocol;
	}

	int controllerCount() throws IOException {
		send(0, REQUEST_CONTROLLER_COUNT, new byte[0]);
		return read(REQUEST_CONTROLLER_COUNT).getInt();
	}

	ByteBuffer controllerData(int device) throws IOException {
		send(device, REQUEST_CONTROLLER_DATA, protocol > 0 ? buffer(4).putInt(protocol).array() : new byte[0]);
		return read(REQUEST_CONTROLLER_DATA);
	}

	void setCustomMode(int device) throws IOException {
		send(device, SET_CUSTOM_MODE, new byte[0]);
	}

	void updateMode(int device, int mode, byte[] modeData) throws IOException {
		ByteBuffer payload = buffer(8 + modeData.length);
		payload.putInt(8 + modeData.length).putInt(mode).put(modeData);
		send(device, UPDATE_MODE, payload.array());
	}

	void updateLeds(int device, int[] colors) throws IOException {
		int size = 4 + 2 + colors.length * 4;
		ByteBuffer payload = buffer(size);
		payload.putInt(size).putShort((short) colors.length);
		for (int color : colors) {
			payload.put((byte) (color >> 16)).put((byte) (color >> 8)).put((byte) color).put((byte) 0);
		}
		send(device, UPDATE_LEDS, payload.array());
	}

	// True once the server reported that devices were added or removed
	boolean deviceListChanged() throws IOException {
		while (in.available() >= 16) {
			readPacket();
		}
		return listChanged;
	}

	private void send(int device, int id, byte[] data) throws IOException {
		ByteBuffer packet = buffer(16 + data.length);
		packet.put(MAGIC).putInt(device).putInt(id).putInt(data.length).put(data);
		out.write(packet.array());
		out.flush();
	}

	private ByteBuffer read(int expectedId) throws IOException {
		while (true) {
			Packet packet = readPacket();
			if (packet.id == expectedId) {
				return packet.data;
			}
		}
	}

	private Packet readPacket() throws IOException {
		byte[] header = new byte[16];
		in.readFully(header);
		ByteBuffer headerBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
		byte[] magic = new byte[4];
		headerBuffer.get(magic);
		if (!Arrays.equals(magic, MAGIC)) {
			throw new IOException("Unexpected data from OpenRGB");
		}
		headerBuffer.getInt();
		int id = headerBuffer.getInt();
		int size = headerBuffer.getInt();
		if (size < 0 || size > MAX_PAYLOAD) {
			throw new IOException("OpenRGB packet too large: " + size);
		}
		byte[] data = new byte[size];
		in.readFully(data);
		if (id == DEVICE_LIST_UPDATED) {
			listChanged = true;
		}
		return new Packet(id, ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
	}

	private static ByteBuffer buffer(int size) {
		return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

	private record Packet(int id, ByteBuffer data) {
	}
}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class Sender {

	static int winSizeBytes = 536;
	static DatagramPacket finPack;
	static int RTT = 1000;
	
	/**
	 * method to convert string filepath to byte array
	 * @param filePath filepath of file in string form
	 * @return byte[] representation of file as byte array
	 * @throws IOException
	 */
	public static byte[] file2Bytes(String filePath) throws IOException 
	{
		FileInputStream input = null;
		File file = new File(filePath);
		byte[] arr = new byte[(int) file.length()];
		
		input = new FileInputStream(file);
		input.read(arr);
		input.close();
		
		return arr;
	}
	
	/**
	 * method to make an array of packets from file data represented as a byte array
	 * @param numSegment the number of packets needed to package the data
	 * @param args the args from the command line
	 * @return DatagramPacket[] array of udp packets ready for transmission
	 * @throws IOException
	 */
	public static DatagramPacket[] makePackets(int numSegment, String[] args) throws IOException 
	{
		DatagramPacket[] packArr = new DatagramPacket[numSegment];
		byte[] arr = file2Bytes(args[0]);
		int i, j;
		for (i = 0, j = 0; i < numSegment && j < arr.length; i++, j+=winSizeBytes)
		{
			Data data = new Data();
			if (j+(winSizeBytes-1) < arr.length-1)
				data.setData(Arrays.copyOfRange(arr, j, j+(winSizeBytes)));
			else
				data.setData(Arrays.copyOfRange(arr, j, arr.length));

			data.setSeqNum(i);
			data.setDstPort((char) Integer.parseInt(args[2]));
			data.setSrcPort((char) Integer.parseInt(args[3]));
			//System.out.println(data.getSeqNum());
			DatagramPacket pack = new DatagramPacket(data.toByteArray(), data.toByteArray().length, 
					InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
			packArr[i] = pack;
		}
		
		Data finData = new Data();
		finData.setSeqNum(i);
		finData.setFlags((byte) 0x01);
		finData.setData(new byte[winSizeBytes]);
		finPack = new DatagramPacket(finData.toByteArray(), finData.toByteArray().length, 
				InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
				
		return packArr;
	}
		
	public static void main(String[] args) throws NumberFormatException, IOException {
		// check that command line arguments are correctly formatted
		if (args.length < 6) {
			System.out.println("usage: " + 
					"sender <filename> <remote_IP> <remote_port> <ack_port_num> <log_filename> <window_size>");
			return ;
		}
		
		// set up the data udp socket and the tpc ack socket
		DatagramSocket dataSocket = new DatagramSocket();
        ServerSocket ackServer = new ServerSocket(
                Integer.parseInt(args[3]));
        ackServer.setReuseAddress(true);
		Socket ackSocket = ackServer.accept();
		
		// input reader to receive acks from ack socket
		BufferedReader in = new BufferedReader(new InputStreamReader(ackSocket.getInputStream()));
		
		int winStart = 0;
		
		// calculate number of segments needed to package data from file
		int len = (int) new File(args[0]).length();
		int numSegment = (int) Math.floorDiv(len, 536) + 1;	
		
		// make the datagram packets
		DatagramPacket[] packets = makePackets(numSegment, args);
		
		// begin sending of packets
		// stop when the window start is not less than the number of packets to be sent
		while (winStart < packets.length)
		{
			//dataSocket.setSoTimeout(RTT);
			
			// test condition
			// delete after testing
			// packets[2] = packets[3];
			
			// send all packets in window
			int i;
			for (i = winStart; i < Math.min(packets.length, winStart+Integer.parseInt(args[5])); i++) 
			{
				dataSocket.send(packets[i]);
            }
			
			// read acks and increment winStart accordingly
			int c, d;
			while (!dataSocket.isClosed() && 
					(winStart < i) && 
					(c = in.read()) != -1)
			{
				String v = Integer.toString(Character.getNumericValue(c));
				while ((d = in.read()) != '|')
					v += Integer.toString(Character.getNumericValue(d));
				
				if (Integer.parseInt(v) == (winStart+1))
					++winStart;
				//dataSocket.setSoTimeout(RTT);
			}
			
			
			
		}
		
		dataSocket.send(finPack);
		System.out.println("I'm finished");
		
		// close sockets
		ackSocket.close();
		dataSocket.close();
		in.close();
	}
}

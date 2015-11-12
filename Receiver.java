import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.net.InetAddress;

public class Receiver 
{	
	
	static int winSizeBytes = 576;
	
	/**
	 * simple method to get sequence number from received packet
	 * @param data the udp payload, or a tcp-like packet
	 * @return int the sequence number of the packet
	 */
	public static int getSeqNum(byte[] data)
	{
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 4, 8));
		return bb.getInt();
	}
	
	public static int getSrcPort(byte[] data) 
	{
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 2));
		return (int) bb.getChar();
	}
	
	public static int getDstPort(byte[] data) 
	{
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 2, 4));
		return (int) bb.getChar();
	}
	
	public static boolean finCheck(byte[] data)
	{
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 13, 14));
		byte dta = bb.get();
		//System.out.println((int) dta);
		return (dta & 1) == 1;
	}
	
	public static int getFlags(byte[] data)
	{
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 13, 14));
		return (int) bb.get();
	}
	
	public static void write2Log(FileWriter writerLog, byte[] data, int ackNum) throws IOException 
	{
		writerLog.write((new Date()).toString() + " ");
		writerLog.write(getSrcPort(data) + " ");
		writerLog.write(getDstPort(data) + " ");
		writerLog.write(getSeqNum(data) + " ");
		writerLog.write(ackNum + " ");
		writerLog.write(getFlags(data) + "\n");
		writerLog.flush();
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException 
	{
		// check that command line arguments are of proper format
		if (args.length < 5) 
		{
			System.out.println("usage: " + 
					"receiver <filename> <listening_port> <sender_IP> <sender_port> <log_filename>");
			return ;
		}
		
		// empty packet to be filled with data
        Socket ackSendSocket = new Socket(InetAddress.getByName(args[2]), 
                Integer.parseInt(args[3]));

        while (!ackSendSocket.isConnected())
            System.out.println("not connected");

		// create sockets for receiving data and sending acks
		DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[1]));

		// output writer to send acks over tcp connection
		PrintWriter out = new PrintWriter(ackSendSocket.getOutputStream());
		
		// file writer to write received data to file
		FileWriter writer = new FileWriter(
				new File(args[0]));
		
		// file writer to write logs to logfile
		FileWriter writerLog = new FileWriter(
				new File(args[4]));
			
		// receive data until fin packet sent by sender
		for (int expecNum = 0;;)
		{
			// receive data
			DatagramPacket packet = new DatagramPacket(new byte[winSizeBytes], winSizeBytes);
			socket.receive(packet);
			byte[] data = packet.getData();
			
			//System.out.println("hi there");
			if (finCheck(data))
				break;
			
			// send ack for next sequence number expected if packet in order
			// else send ack for last received packet sequence number
			if (expecNum == getSeqNum(data))
			{
				out.print(++expecNum + "|");
				// System.out.println(expecNum + "|");
				out.flush();
				write2Log(writerLog, Arrays.copyOfRange(data, 0, 20), expecNum);
			} else 
			{
				//out.print((char) expecNum);
				out.print(expecNum + "|");
				// System.out.println(expecNum + "|");
				out.flush();
				write2Log(writerLog, Arrays.copyOfRange(data, 0, 20), expecNum);
				continue;
			}
			
			String string = new String(Arrays.copyOfRange(data, 20, data.length-20), 0);
			
			writer.write(string);	
			System.out.print(string);
			writer.flush();
		}
		
		//socket.close();
		//ackSocket.close();
		//out.close();
		//writer.close();
	}
}

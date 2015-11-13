import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

// class to represent TCP packet
public class Data {
	
	// data fields
	private char srcPort;
	private char dstPort;
	private int seqNum;
	private int ackNum;
	private byte headLen = (byte) 0x50;
	private byte flags;
	private char rcvWin = 100;
	private char iNetSum;
	private char urgPtr;
	private byte[] data;
	
	// constructors
	
	
	// methods
	public byte[] toByteArray() throws IOException 
	{
		byte[] byte1 = ByteBuffer.allocate(2).putChar(srcPort).array();
		byte[] byte2 = ByteBuffer.allocate(2).putChar(dstPort).array();
		byte[] byte3 = ByteBuffer.allocate(4).putInt(seqNum).array();
		byte[] byte4 = ByteBuffer.allocate(4).putInt(ackNum).array();
		byte[] byte5 = ByteBuffer.allocate(2).putChar(rcvWin).array();
		byte[] byte6 = ByteBuffer.allocate(2).putChar(iNetSum).array();
		byte[] byte7 = ByteBuffer.allocate(2).putChar(urgPtr).array();
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(byte1);
		outputStream.write(byte2);
		outputStream.write(byte3);
		outputStream.write(byte4);
		outputStream.write(headLen);
		outputStream.write(flags);
		outputStream.write(byte5);
		outputStream.write(byte6);
		outputStream.write(byte7);
		if (data != null)
			outputStream.write(data);
		
		return outputStream.toByteArray();
	}
	
	public static char checkPakSum(byte[] byteArr) throws IOException
	{
		byte a = byteArr[16];
		byte b = byteArr[17];
		byteArr[16] = byteArr[17] = 0;
		char c = checkSum(byteArr);
		byteArr[16] = a;
		byteArr[16] = b;
		return c;
	}
	
	public char checkSum() throws IOException
	{
		return checkSum(this.toByteArray());
	}
	
	private static char checkSum(byte[] byteArr) throws IOException 
	{
		char[] charArr = byteArr.toString().toCharArray();
		
		char chek = 0;
		for (int i = 0; i < charArr.length; i++)
		{
			chek ^= charArr[i];
		}
		
		return chek;
	}
	
	// construct byte array
	// construct empty char array = one char for every two bytes
	// iterate through every other byte. add two bytes into an int
	// if int value is greater than max value of char, cast to char and add 1
	// add char to arr
	
	// setters
	public void setSrcPort(char srcPort) {this.srcPort = srcPort;}
	
	public void setDstPort(char dstPort) {this.dstPort = dstPort;}
	
	public void setSeqNum(int seqNum) {this.seqNum = seqNum;}
	
	public void setAckNum(int ackNum) {this.ackNum = ackNum;}
	
	public void setHeadLen(byte headLen) {this.headLen = headLen;}
	
	public void setFlags(byte flags) {this.flags = flags;}
	
	public void setRcvWin(char rcvWin) {this.rcvWin = rcvWin;}
	
	public void setInetSum(char iNetSum) {this.iNetSum = iNetSum;}
	
	public void setUrgPtr(char urgPtr) {this.urgPtr = urgPtr;}
	
	public void setData(byte[] data) {this.data = data;}
	
	// getters
	public char getSrcPort() {return this.srcPort;}
	
	public char getDstPort() {return this.dstPort;}
	
	public int getSeqNum() {return this.seqNum;}
	
	public int getAckNum() {return this.ackNum;}
	
	public byte getHeadLen() {return this.headLen;}
	
	public byte getFlags() {return this.flags;}
	
	public char getRcvWin() {return this.rcvWin;}
	
	public char getINetSum() {return this.iNetSum;}
	
	public char getUrgPtr() {return this.urgPtr;}
	
	public byte[] getData() {return this.data;}


	
	


}

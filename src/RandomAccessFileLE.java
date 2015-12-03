import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileLE extends RandomAccessFile
{
	public RandomAccessFileLE(String name, String mode)
			throws FileNotFoundException {
		super(name, mode);
	}

	public final int readIntLE() throws IOException 
	{
		int ch1 = this.read();
		int ch2 = this.read();
		int ch3 = this.read();
		int ch4 = this.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
	}
	
	public final long readLongLE() throws IOException
	{
		byte[] b = new byte[8];
		this.readFully(b);
		return ((b[0] & 0xFF << 0) +
				((b[1] & 0xFF) << 8) +
				((b[2] & 0xFF) << 16) +
				((b[3] & 0xFF) << 24) +
				((long)(b[4] & 0xFF) << 32) +
				((long)(b[5] & 0xFF) << 40) +
				((long)(b[6] & 0xFF) << 48) +
				((long)(b[7] & 0xFF) << 56));
	}
	
	public final char[] readChars(int len) throws IOException
	{
		char[] c = new char[len];
		for(int i = 0; i < c.length; i++)
			c[i] = (char) this.read();
		return c;
	}
	
	public final void writeIntLE(int v) throws IOException
	{
		write((v >>> 0) & 0xFF);
		write((v >>> 8) & 0xFF);
		write((v >>> 16) & 0xFF);
		write((v >>> 24) & 0xFF);
	}
	
	public final void writeShortLE(short v) throws IOException
	{
		write((v >>> 0) & 0xFF);
		write((v >>> 8) & 0xFF);
	}
}

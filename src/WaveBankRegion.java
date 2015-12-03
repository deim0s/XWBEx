import java.io.IOException;

public class WaveBankRegion 
{
	public int offset;
	public int length;
	
	public static WaveBankRegion read(RandomAccessFileLE ras) throws IOException
	{
		WaveBankRegion result = new WaveBankRegion();
		result.offset = ras.readIntLE();
		result.length = ras.readIntLE();
		return result;
	}
}

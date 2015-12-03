import java.io.IOException;


public class WaveBankEntry 
{
	public int value;
	public WaveBankMiniWaveFormat format;
	public WaveBankRegion playRegion;
	public WaveBankSampleRegion loopRegion;
	
	public static WaveBankEntry read(RandomAccessFileLE ras) throws IOException
	{
		WaveBankEntry result = new WaveBankEntry();
		result.value = ras.readIntLE();
		result.format = WaveBankMiniWaveFormat.read(ras);
		result.playRegion = WaveBankRegion.read(ras);
		result.loopRegion = WaveBankSampleRegion.read(ras);
		return result;
	}
	
	public int flags()
	{
		return (value >> 0) & 0xF;
	}
	
	public int duration()
	{
		return (value >> 4) & 0xFFFFFFF;
	}
}

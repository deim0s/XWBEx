import java.io.IOException;


public class WaveBankSampleRegion 
{
	public int startSample;
	public int totalSamples;
	
	public static WaveBankSampleRegion read(RandomAccessFileLE ras) throws IOException
	{
		WaveBankSampleRegion result = new WaveBankSampleRegion();
		result.startSample = ras.readIntLE();
		result.totalSamples = ras.readIntLE();
		return result;
	}
}

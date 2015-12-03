import java.io.IOException;

public class WaveBankHeader 
{
	public int signature;
	public int version;
	public int headerVersion;
	public WaveBankRegion[] waveBankRegion;
	
	public static WaveBankHeader read(RandomAccessFileLE ras) throws IOException
	{
		WaveBankHeader result = new WaveBankHeader();
		result.signature = ras.readIntLE();
		result.version = ras.readIntLE();
		result.headerVersion = ras.readIntLE();
		result.waveBankRegion = new WaveBankRegion[WaveBankSegIdx.WAVEBANK_SEGIDX_COUNT.ordinal()];
		for(int i = 0; i < result.waveBankRegion.length; i++)
			result.waveBankRegion[i] = WaveBankRegion.read(ras);
		return result;
	}
}

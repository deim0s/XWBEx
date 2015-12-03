import java.io.IOException;

public class WaveBankMiniWaveFormat 
{
	final static int WAVEBANKMINIFORMAT_TAG_PCM = 0x0;
	final static int WAVEBANKMINIFORMAT_TAG_XMA = 0x1;
	final static int WAVEBANKMINIFORMAT_TAG_ADPCM = 0x2;
	final static int WAVEBANKMINIFORMAT_TAG_WMA = 0x3;

	final static int WAVEBANKMINIFORMAT_BITDEPTH_8 = 0x0;
	final static int WAVEBANKMINIFORMAT_BITDEPTH_16 = 0x1;
	
	final static int ADPCM_MINIWAVEFORMAT_BLOCKALIGN_CONVERSION_OFFSET = 22;
	final static int MAX_WMA_BLOCK_ALIGN_ENTRIES = 17;
	final static int MAX_WMA_AVG_BYTES_PER_SEC_ENTRIES = 7;
	final static int XMA_OUTPUT_SAMPLE_BYTES = 2;
	final static int XMA_OUTPUT_SAMPLE_BITS = XMA_OUTPUT_SAMPLE_BYTES * 8;
	
	final static int[] aWMABlockAlign =
	{
	    929,
	    1487,
	    1280,
	    2230,
	    8917,
	    8192,
	    4459,
	    5945,
	    2304,
	    1536,
	    1485,
	    1008,
	    2731,
	    4096,
	    6827,
	    5462,
	    1280
	};
	final static int[] aWMAAvgBytesPerSec =
	{
	    12000,
	    24000,
	    4000,
	    6000,
	    8000,
	    20000,
	    2500
	};
	
	public int value;
	
	public static WaveBankMiniWaveFormat read(RandomAccessFileLE ras) throws IOException
	{
		WaveBankMiniWaveFormat result = new WaveBankMiniWaveFormat();
		result.value = ras.readIntLE();
		return result;
	}
	
	public int BitsPerSample()
    {
        if (wFormatTag() == WAVEBANKMINIFORMAT_TAG_XMA)
            return XMA_OUTPUT_SAMPLE_BITS;
        if (wFormatTag() == WAVEBANKMINIFORMAT_TAG_WMA)
            return 16;
        if (wFormatTag() == WAVEBANKMINIFORMAT_TAG_ADPCM)
            return 4;
        return ((wBitsPerSample() == WAVEBANKMINIFORMAT_BITDEPTH_16) ? 16 : 8);
    }
	
	public int BlockAlign()
    {
        switch (wFormatTag())
        {
            case WAVEBANKMINIFORMAT_TAG_PCM:
                return wBlockAlign();
            case WAVEBANKMINIFORMAT_TAG_XMA:
                return (nChannels() * XMA_OUTPUT_SAMPLE_BITS / 8);
            case WAVEBANKMINIFORMAT_TAG_ADPCM:
                return (wBlockAlign() + ADPCM_MINIWAVEFORMAT_BLOCKALIGN_CONVERSION_OFFSET) * nChannels();
            case WAVEBANKMINIFORMAT_TAG_WMA:
                int dwBlockAlignIndex = wBlockAlign() & 0x1F;
                if (dwBlockAlignIndex < MAX_WMA_BLOCK_ALIGN_ENTRIES)
                    return aWMABlockAlign[dwBlockAlignIndex];
                break;
        }
        return 0;
    }
	
	public int AvgBytesPerSec()
	{
		switch(wFormatTag())
		{
			case WAVEBANKMINIFORMAT_TAG_PCM:
	        case WAVEBANKMINIFORMAT_TAG_XMA:
	        	return nSamplesPerSec() * wBlockAlign();
	        case WAVEBANKMINIFORMAT_TAG_ADPCM:
	        	int blockAlign = BlockAlign();
	        	int samplesPerAdpcmBlock = AdpcmSamplesPerBlock();
	        	return blockAlign * nSamplesPerSec() / samplesPerAdpcmBlock;
	        case WAVEBANKMINIFORMAT_TAG_WMA:
	        	int dwBytesPerSecIndex = wBlockAlign() >> 5;
                if (dwBytesPerSecIndex < MAX_WMA_AVG_BYTES_PER_SEC_ENTRIES)
                    return aWMAAvgBytesPerSec[dwBytesPerSecIndex];
		}
		return 0;
	}
	
	public int AdpcmSamplesPerBlock()
    {
        int nBlockAlign = (wBlockAlign() + ADPCM_MINIWAVEFORMAT_BLOCKALIGN_CONVERSION_OFFSET) * nChannels();
        return nBlockAlign * 2 / nChannels() - 12;
    }
	
	public int wFormatTag()
    {
        return (value >> 0) & 0x3;
    }
	
	public int nChannels()
    {
        return (value >> 2) & 0x7;
    }
	
	public int nSamplesPerSec()
    {
		return (value >> 5) & 0x3FFFF;
    }
	
	public int wBlockAlign()
    {
        return (value >> 23) & 0xFF;
    }
	
	public int wBitsPerSample()
    {
        return (value >> 31) & 0x1;
    }
}

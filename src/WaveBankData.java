import java.io.IOException;

public class WaveBankData 
{
	final static int WAVEBANK_BANKNAME_LENGTH = 64;
	final static int WAVEBANK_FLAGS_ENTRYNAMES = 0x00010000;
	final static int WAVEBANK_FLAGS_COMPACT = 0x00020000;
	final static int WAVEBANK_FLAGS_SYNC_DISABLED = 0x00040000;
	final static int WAVEBANK_FLAGS_SEEKTABLES = 0x00080000;
	final static int WAVEBANK_FLAGS_MASK = 0x000F0000;
	
	public int flags;
	public int entryCount;
	public String bankName;
	public int entryMetaDataElementSize;
	public int entryNameElementSize;
	public int alignment;
	public WaveBankMiniWaveFormat compactFormat;
	public long buildTime;
	
	public static WaveBankData read(RandomAccessFileLE ras) throws IOException
	{
		WaveBankData result = new WaveBankData();
		result.flags = ras.readIntLE();
		result.entryCount = ras.readIntLE();
		result.bankName = new String(ras.readChars(WAVEBANK_BANKNAME_LENGTH));
		result.entryMetaDataElementSize = ras.readIntLE();
		result.entryNameElementSize = ras.readIntLE();
		result.alignment = ras.readIntLE();
		result.compactFormat = WaveBankMiniWaveFormat.read(ras);
		result.buildTime = ras.readLongLE();
		return result;
	}
	
	/*public boolean EntryNameFlag()
	{
		return (flags & WAVEBANK_FLAGS_ENTRYNAMES) == WAVEBANK_FLAGS_ENTRYNAMES;
	}
	
	public boolean CompactFlag()
	{
		return (flags & WAVEBANK_FLAGS_COMPACT) == WAVEBANK_FLAGS_COMPACT;
	}
	
	public boolean SyncDisabledFlag()
	{
		return (flags & WAVEBANK_FLAGS_SYNC_DISABLED) == WAVEBANK_FLAGS_SYNC_DISABLED;
	}
	
	public boolean SeekTablesFlag()
	{
		return (flags & WAVEBANK_FLAGS_SEEKTABLES) == WAVEBANK_FLAGS_SEEKTABLES;
	}*/
}

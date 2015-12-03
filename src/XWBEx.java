import java.io.File;


public class XWBEx 
{

    public static void main(String[] args) throws Exception
    {	
        //String file = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Monaco\\Audio\\MusicWaves.xwb";
    	  //String file = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Rogue Legacy\\Content\\Audio\\MusicWaveBank.xwb";
        //String outf = "C:\\Users\\Alex\\Desktop\\Monaco";
        //File dir = new File(outf);
        //dir.mkdirs();
        
    	  String file = args[0];
        File dir = new File(args[1]);
        dir.mkdirs();
        
        String format = "%5d %4d %9d %6s %4d %8d %5d %12d %12d\n";
        System.out.println("Index|Flags|Duration|Format|Chan|SmplRate|Align|   Offset   |   Length   ");
        System.out.println("-------------------------------------------------------------------------");
        
        RandomAccessFileLE ras = new RandomAccessFileLE(file, "rw");
        WaveBankHeader wbh = WaveBankHeader.read(ras);
        WaveBankData wbd = WaveBankData.read(ras);
        int wdo = wbh.waveBankRegion[WaveBankSegIdx.WAVEBANK_SEGIDX_ENTRYWAVEDATA.ordinal()].offset;
        
        for(int i = 0; i < wbd.entryCount; i++)
        {
        	WaveBankEntry wbe = WaveBankEntry.read(ras);
        	long reset = ras.getFilePointer();
        	String loc;
        	
        	switch(wbe.format.wFormatTag())
        	{
        		case WaveBankMiniWaveFormat.WAVEBANKMINIFORMAT_TAG_PCM:
        			loc = String.format("%s/%s_%05d.wav", dir.getAbsolutePath(), wbd.bankName.trim(), i);
        			System.out.printf(format, i, wbe.flags(), wbe.duration(),
        					"PCM", wbe.format.nChannels(), wbe.format.nSamplesPerSec(),
        					wbe.format.BlockAlign(), wbe.playRegion.offset, wbe.playRegion.length);
            		Wav.writePCM(ras, wbe, loc, wdo);
            		break;
            		
        		case WaveBankMiniWaveFormat.WAVEBANKMINIFORMAT_TAG_ADPCM:
        			loc = String.format("%s/%s_%05d.wav", dir.getAbsolutePath(), wbd.bankName.trim(), i);
        			System.out.printf(format, i, wbe.flags(), wbe.duration(),
        					"ADPCM", wbe.format.nChannels(), wbe.format.nSamplesPerSec(),
        					wbe.format.BlockAlign(), wbe.playRegion.offset, wbe.playRegion.length);
            		Wav.writeADPCM(ras, wbe, loc, wdo);
            		break;
            		
        		/*case WaveBankMiniWaveFormat.WAVEBANKMINIFORMAT_TAG_WMA:
        			loc = String.format("%s/%s_%05d.xwma", dir.getAbsolutePath(), wbd.bankName.trim(), i);
        			System.out.printf(format, i, wbe.flags(), wbe.duration(),
        					"XWMA", wbe.format.nChannels(), wbe.format.nSamplesPerSec(),
        					wbe.format.BlockAlign(), wbe.playRegion.offset, wbe.playRegion.length);
            		Wav.writeXWMA(ras, wbe, loc, wdo);
            		//Wav.decodeXWMA(loc);
            		break;*/
        	}
        	
        	ras.seek(reset);
        }
        
        ras.close();
    }
}

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;


public class Wav 
{
	/*
	  	Offset  Size  Name             Description

		The canonical WAVE format starts with the RIFF header:
		
		0         4   ChunkID          Contains the letters "RIFF" in ASCII form
		                               (0x52494646 big-endian form).
		4         4   ChunkSize        36 + SubChunk2Size, or more precisely:
		                               4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
		                               This is the size of the rest of the chunk 
		                               following this number.  This is the size of the 
		                               entire file in bytes minus 8 bytes for the
		                               two fields not included in this count:
		                               ChunkID and ChunkSize.
		8         4   Format           Contains the letters "WAVE"
		                               (0x57415645 big-endian form).
		
		The "WAVE" format consists of two subchunks: "fmt " and "data":
		The "fmt " subchunk describes the sound data's format:
		
		12        4   Subchunk1ID      Contains the letters "fmt "
		                               (0x666d7420 big-endian form).
		16        4   Subchunk1Size    16 for PCM.  This is the size of the
		                               rest of the Subchunk which follows this number.
		20        2   AudioFormat      PCM = 1 (i.e. Linear quantization)
		                               Values other than 1 indicate some 
		                               form of compression.
		22        2   NumChannels      Mono = 1, Stereo = 2, etc.
		24        4   SampleRate       8000, 44100, etc.
		28        4   ByteRate         == SampleRate * NumChannels * BitsPerSample/8
		32        2   BlockAlign       == NumChannels * BitsPerSample/8
		                               The number of bytes for one sample including
		                               all channels. I wonder what happens when
		                               this number isn't an integer?
		34        2   BitsPerSample    8 bits = 8, 16 bits = 16, etc.
		          2   ExtraParamSize   if PCM, then doesn't exist
		          X   ExtraParams      space for extra parameters
		
		The "data" subchunk contains the size of the data and the actual sound:
		
		36        4   Subchunk2ID      Contains the letters "data"
		                               (0x64617461 big-endian form).
		40        4   Subchunk2Size    == NumSamples * NumChannels * BitsPerSample/8
		                               This is the number of bytes in the data.
		                               You can also think of this as the size
		                               of the read of the subchunk following this 
		                               number.
		44        *   Data             The actual sound data.
	 */
	
	public static void writePCM(RandomAccessFileLE source, WaveBankEntry pcm, String location, int offset) throws IOException
	{
		RandomAccessFileLE wav = new RandomAccessFileLE(location, "rw");

		wav.writeInt(0x52494646);
		wav.writeIntLE(0);
		wav.writeInt(0x57415645);
        
		wav.writeInt(0x666d7420);
		wav.writeIntLE(0x10);
		wav.writeShortLE((short) 0x1);
		wav.writeShortLE((short) pcm.format.nChannels());
		wav.writeIntLE(pcm.format.nSamplesPerSec());
		wav.writeIntLE(pcm.format.AvgBytesPerSec());
		wav.writeShortLE((short) pcm.format.BlockAlign());
		wav.writeShortLE((short) pcm.format.BitsPerSample());
		
		writeDATA(source, wav, (offset + pcm.playRegion.offset), pcm.playRegion.length);
		
		wav.close();
	}
	
	public static void writeADPCM(RandomAccessFileLE source, WaveBankEntry adpcm, String location, int offset) throws IOException
	{
		RandomAccessFileLE wav = new RandomAccessFileLE(location, "rw");
		short[][] aCoef = new short[][] { {256, 0}, {512, -256}, {0, 0}, {192, 64}, {240, 0}, {460, -208}, {392, -232} };

		wav.writeInt(0x52494646);
		wav.writeIntLE(0);
		wav.writeInt(0x57415645);

		wav.writeInt(0x666d7420);
		wav.writeIntLE(0x32);
		wav.writeShortLE((short) 0x2);
		wav.writeShortLE((short) adpcm.format.nChannels());
		wav.writeIntLE(adpcm.format.nSamplesPerSec());
		wav.writeIntLE(adpcm.format.AvgBytesPerSec());
		wav.writeShortLE((short) adpcm.format.BlockAlign());
        wav.writeShortLE((short) adpcm.format.BitsPerSample());
        wav.writeShortLE((short) 0x20);
        wav.writeShortLE((short) adpcm.format.AdpcmSamplesPerBlock());
        wav.writeShortLE((short) aCoef.length);
        for(int i = 0; i < aCoef.length; i++)
        	for(int j = 0; j < aCoef[i].length; j++)
        		wav.writeShortLE(aCoef[i][j]);
        
		writeDATA(source, wav, (offset + adpcm.playRegion.offset), adpcm.playRegion.length);
		
		wav.close();
	}
	
	public static void writeXWMA(RandomAccessFileLE source, WaveBankEntry xwma, String location, int offset) throws IOException
	{
		RandomAccessFileLE wav = new RandomAccessFileLE(location, "rw");

		wav.writeInt(0x52494646);
		wav.writeIntLE(0);
		wav.writeInt(0x58574d41);

		wav.writeInt(0x666d7420);
		wav.writeIntLE(0x12);
		wav.writeShortLE((short) 0x161);
		wav.writeShortLE((short) xwma.format.nChannels());
		wav.writeIntLE(xwma.format.nSamplesPerSec());
		wav.writeIntLE(xwma.format.AvgBytesPerSec());
		wav.writeShortLE((short) xwma.format.BlockAlign());
		wav.writeShortLE((short) xwma.format.BitsPerSample());
		wav.writeShortLE((short) 0);

        int packetNum = xwma.playRegion.length / xwma.format.BlockAlign();
        int allBlocks = round4096(xwma.duration() * 2) / 4096;
        int avgBlocksPerPacket = allBlocks / packetNum;
        int spareBlocks = allBlocks - (avgBlocksPerPacket * packetNum);
        int accumulated = 0;
        wav.writeInt(0x64706473);
        wav.writeIntLE(packetNum * 4);
        for(int j = 0; j < packetNum; j++) {
            accumulated += avgBlocksPerPacket * 4096;
            if (spareBlocks > 0) {
                accumulated += 4096;
                spareBlocks--;
            }
            wav.writeIntLE(accumulated);
        }
		
		writeDATA(source, wav, (offset + xwma.playRegion.offset), xwma.playRegion.length);
		
		wav.close();
	}
	
	public static void writeDATA(RandomAccessFileLE source, RandomAccessFileLE wav, int offset, int length) throws IOException
	{
		wav.writeInt(0x64617461);
		wav.writeIntLE(length);
		source.seek(offset);
		
		byte[] buffer = new byte[1024 * 16];
        int tBytes = 0;
        int rBytes = 0;
        while(tBytes < length)
        {
            rBytes = ((tBytes + buffer.length) > length) ? (length - tBytes) : buffer.length;
            tBytes += source.read(buffer, 0, rBytes);
            wav.write(buffer, 0, rBytes);
        }

        wav.seek(0x4);
        wav.writeIntLE((int) wav.length() - 8);
        wav.close();
	}
	
	public static void decodeXWMA(String location) throws ExecuteException, IOException
	{
		String source = Wav.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        source = URLDecoder.decode(source, "utf-8");
        source = (new File(source)).getParent();
        
        File xwma = new File(location);
        int ext = xwma.getName().indexOf('.');
        File wav = new File(xwma.getParentFile(), (xwma.getName().substring(0, ext)) + ".wav");
        
        CommandLine command = new CommandLine(new File(source, "xWMAEncode.exe"));
        command.addArgument("${Input_File}", true);
        command.addArgument("${Output_File}", true);
        
        Map<Object, Object> subMap = new HashMap<Object, Object>();
        subMap.put("Input_File", xwma.getAbsolutePath());
        subMap.put("Output_File", wav.getAbsolutePath());
        command.setSubstitutionMap(subMap);
        
        DefaultExecutor executor = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(new NullOutputStream());
        executor.setStreamHandler(streamHandler);
        //executor.setWorkingDirectory(new File(source));
        executor.setExitValue(0);
        executor.execute(command);
        
        xwma.delete();
	}
	
	public static int round4096(int value)
	{
		return (value % 4096 != 0) ? (1 + (value / 4096)) * 4096 : value;
	}
	
	public static class NullOutputStream extends OutputStream {
	    public void write(int i) throws IOException {}
	}
}

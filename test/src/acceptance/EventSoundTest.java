package acceptance;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.zip.DataFormatException;

import com.flagstone.transform.Background;
import com.flagstone.transform.Movie;
import com.flagstone.transform.ShowFrame;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.datatype.ColorTable;
import com.flagstone.transform.datatype.WebPalette;
import com.flagstone.transform.sound.DefineSound;
import com.flagstone.transform.sound.SoundInfo;
import com.flagstone.transform.sound.StartSound;
import com.flagstone.transform.util.sound.SoundProvider;

public class EventSoundTest
{
    protected void playSounds(File sourceDir, String[] files, File destDir) throws IOException, DataFormatException
    {
        File sourceFile;
        File destFile;  
        DefineSound sound;
        SoundProvider provider;
        
        if (!destDir.exists() && !destDir.mkdirs()) {
        	fail();
        }
        
        for (String file : files)
        {
        	sourceFile = new File(sourceDir, file);
        	destFile = new File(destDir, file.substring(0, file.lastIndexOf('.')) + ".swf");
            //TODO sound = provider.defineSound(1);
        	//TODO playSound(sound, destFile);
        }
    }
    
    protected void playSound(DefineSound sound, File file) throws IOException, DataFormatException
    {
        float framesPerSecond = 12.0f;

        Movie movie = new Movie();

        movie.setFrameSize(new Bounds(0, 0, 8000, 4000));
        movie.setFrameRate(framesPerSecond);
        movie.add(new Background(WebPalette.LIGHT_BLUE.color()));

        float duration = ((float) sound.getSampleCount() / (float) sound.getRate());
        int numberOfFrames = (int) (duration * framesPerSecond);

        movie.add(sound);
        movie.add(new StartSound(new SoundInfo(sound.getIdentifier(), SoundInfo.Mode.START, 0, null)));

        for (int j=0; j<numberOfFrames; j++) {
            movie.add(ShowFrame.getInstance());
        }

        movie.encodeToFile(file.getPath());
    }
}

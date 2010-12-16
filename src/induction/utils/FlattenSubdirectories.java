package induction.utils;

import induction.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author konstas
 */
public class FlattenSubdirectories
{
    private String targetDir;

    public FlattenSubdirectories(String targetDir)
    {
        this.targetDir = targetDir;
    }

    private void addPath(String path, String prefix)
    {
        File file = new File(path);
        if(file.isDirectory())
        {
            for(String fileStr : Utils.sortWithEmbeddedInt(file.list()))
            {
                addPath(path + "/" + fileStr, prefix + fileStr + "-");
            } // for
        } // if
        else
        {
            try
            {
                copy(new File(path),
                     new File(targetDir + "/" + prefix.substring(0, prefix.length() - 1)));
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void copy(File src, File dst) throws IOException
    {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    public static void main(String[] args)
    {
        String sourceDir = "data/weather-data-full/data";
        String targetDir = "data/weather-data-full/flat";
        FlattenSubdirectories fsdir = new FlattenSubdirectories(targetDir);
        fsdir.addPath(sourceDir, "");
    }
}

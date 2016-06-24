package fr.inria.smilk.ws.relationextraction.lucene;

/**
 * Created by dhouib on 20/06/2016.
 */

import java.io.File;
import java.io.FileFilter;

public class TextFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".txt");
    }
}
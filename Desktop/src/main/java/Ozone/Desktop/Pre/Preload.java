package Ozone.Desktop.Pre;


import arc.backend.sdl.jni.SDL;
import arc.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static Ozone.Interface.restart;

//have you load library today ?
public class Preload {
    private static volatile boolean init = false;



    public static boolean checkLibrary(String AtomDownload, File atom) {
        //try to download if doesn't exists
        if (!atom.exists())
            try {
                //Inform user
                Log.infoTag("Ozone", "Downloading Library");
                //there is no "no" option
                SDL.SDL_ShowSimpleMessageBox(64, "Ozone", atom.getAbsolutePath() + " doesn't exists. Click OK to continue");
                //how to download a file synchronously
                URL jitpack = new URL(AtomDownload);
                File temp = new File(atom.getParent(), "/" + System.currentTimeMillis() + ".temp");
                DownloadSwing download = new DownloadSwing(jitpack, temp);
                download.display();
                download.run();
                Files.copy(temp.toPath(), atom.toPath(), StandardCopyOption.REPLACE_EXISTING);
                temp.deleteOnExit();
                //its exists
                if (atom.exists()) {
                    SDL.SDL_ShowSimpleMessageBox(64, "Ozone", "Atom library has been downloaded: " + atom.getAbsolutePath());
                    //Q: Why restart ???
                    //A: its just cool to restart
                    restart();
                }
                //if its reach to here, then its must not exists and there no internet connection ? wtf
            } catch (Throwable t) {
                //oh no internet error
                SDL.SDL_ShowSimpleMessageBox(16, "Ozone", "Atom library can't be downloaded: " + t.toString());
                Log.err(t);
            }
        //reliable shit
        return atom.exists();
    }

    public static void incites(File atom, String AtomDownload, Object clz) throws NoSuchMethodException, MalformedURLException, InvocationTargetException, IllegalAccessException, FileNotFoundException {
        //don't run more than once
        if (init) return;
        init = true;
        //check if we are on right classloader, sometime java is full of surprise
        if (!(clz.getClass().getClassLoader() instanceof URLClassLoader))
            throw new RuntimeException(clz + " Classloader is not URLClassloader, how it could be ???");
        //check library needed to load Ozone
        if (!checkLibrary(AtomDownload, atom))
            throw new FileNotFoundException("Atom Library can't be downloaded/not found");
        //Inform users
        Log.infoTag("Ozone", "Loading library");
        //add Atom to URL classloader to be used
        //good ol reflection
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(clz.getClass().getClassLoader(), atom.toURI().toURL());
        Log.infoTag("Ozone", "Library loaded by using java.net.URLClassLoader.addURL(java.net.URL)");
        //shit we did it without any error
    }
}
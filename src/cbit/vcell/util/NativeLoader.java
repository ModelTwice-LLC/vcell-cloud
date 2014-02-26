package cbit.vcell.util;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.vcell.util.FileUtils;

/**
 * class to load native libraries. Requires external class to set
 * {@link #setNativeLibraryDirectory(String)} and {@link #setOsType(OsType)}
 * prior to creating NativeLoader object.
 * 
 * Not necessarily threadsafe
 * @author gweatherby
 *
 */
public class NativeLoader {

	/**
	 * maximum number of times to try loading libraries before giving up
	 * and throwing exeption
	 */
	public static final int NUM_ATTEMPTS = 50;

	
	public enum OsType {
		LINUX,
		WINDOWS,
		MAC
	}

	/**
	 * set os type
	 * @param osType
	 */
	public static void setOsType(OsType osType) {
		switch (osType) {
		case LINUX:
			systemLibRegex = LINUX_REGEX;
			break;
		case WINDOWS:
			systemLibRegex = WINDOWS_REGEX; 
			break;
		case MAC:
			systemLibRegex = MAC_REGEX; 
			break;
		default:
			throw new IllegalStateException("unknown os type " + osType);
		}
	}
	

	/**
	 * file separator for os
	 */
	private final static String FILESEP = System.getProperty("file.separator");
	
	/**
	 * our preferences key
	 */
	private final static String PREFS_KEY = "nativeLibs" ;
	/**
	 * regex for linux shared libraries
	 */
	private final static String LINUX_REGEX = ".*so[.\\d]*$";
	/**
	 * regex for Windows shared libraries
	 */
	private final static String WINDOWS_REGEX = ".*dll$";
	/**
	 * regex for Mac OS X shared libraries
	 */
	private final static String MAC_REGEX = ".*jnlib";
	
	private static String nativeLibraryDirectory  = null;
	
	private static String systemLibRegex = null;

	/**
	 * preferences to use
	 */
	private Preferences pref = Preferences.systemNodeForPackage(NativeLoader.class);
	/**
	 * map of files in native lib directory
	 */
	private Map<String,Boolean> libsPresentMap = new HashMap<String,Boolean>( );
	/**
	 * ordered list of libraries
	 */
	private List<String>  storedLoadOrder = new LinkedList<String>( );
	/**
	 * if true, stored list is out of date
	 */
	private boolean listDirty = false;
	/**
	 * path to native library directory
	 */
	private String dirPath = nativeLibraryDirectory;
	/**
	 * last recorded load error, for messaging
	 */
	private List<Error> failErrors = null;
	
	private final String NATIVE_PATH_PROP = "java.library.path";

	public NativeLoader() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (nativeLibraryDirectory == null) {
			throw new IllegalStateException(getClass( ).getName() + " created before native library location set");
		}
		if (systemLibRegex == null) {
			throw new IllegalStateException(getClass( ).getName() + " created before os type set"); 
		}
		//verify on system native lib path, in case other code searches for it (e.g. H5)
		boolean found = false;
		File nativeDir = new File(nativeLibraryDirectory);
		String jlp = System.getProperty(NATIVE_PATH_PROP);
		Collection<File> files = FileUtils.toFiles(FileUtils.splitPathString(jlp));
		for (File f: files) {
			//System.err.println(f.getAbsolutePath());
			if (nativeDir.equals(f)) {
				found = true;
				break;
			}
		}
		if (!found) {
			files.add(nativeDir);
			String newPath = FileUtils.pathJoinFiles(files);
			System.setProperty(NATIVE_PATH_PROP,newPath);
			 
			Field fieldSysPath;
			fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
			fieldSysPath.setAccessible( true );
			fieldSysPath.set( null, null );
		}
		
	}
	
	/**
	 * set native library directory for this OS
	 * @param nativeLibraryDirectory
	 */
	public static void setNativeLibraryDirectory(String nativeLibraryDirectory) {
		NativeLoader.nativeLibraryDirectory = nativeLibraryDirectory;
		
	}
	

	/**
	 * attempt to load all libraries in nativelibs directory.
	 * try multiple times in case some libraries are dependent on others
	 * if successful, record order of libraries in local preferences file
	 * for faster subsequent startups; 
	 * rebuild local list if libraries added / removed from nativelibs directory
	 */
	public void loadNativeLibraries( ) throws Error {
		assert FILESEP != null : "bad file sep";
		assert FileUtils.PATHSEP != null : "bad path sep";
		loadMap( );
		loadListFromPreferences();
		Iterator<String> iter = storedLoadOrder.iterator();
		List<String> failed = new LinkedList<String>( );
		//try to load names stored in preferences
		while (iter.hasNext()) {
			String lName = iter.next( );
			//make sure still present
			if (!libsPresentMap.containsKey(lName)) {
				listDirty = true; //library removed
				iter.remove();
				continue;
			}
			if (!attemptLoad(lName)) {
				listDirty = true; //stored order no longer works
				iter.remove();
				failed.add(lName);
				continue;
			}
			//library is present in directory and preferences and loads correctly
			libsPresentMap.put(lName, true);
		}
		iter = null;
		//check for libraries not stored in preferences
		for (Map.Entry<String,Boolean> entry : libsPresentMap.entrySet()) {
			if (!entry.getValue()) {
				listDirty = true; //library added
				failed.add(entry.getKey());
			}
		}
		if (!listDirty) {
			//nothing unexpected, we're done
			return;
		}

		//non-standard loop to set flag on last iteration
		for (int i = 1; failed.size() > 0 && i <= NUM_ATTEMPTS; i++) {
			if (i == NUM_ATTEMPTS) {  //record error messages on last go through
				setFailErrors();
			}
			Iterator<String> fIter = failed.iterator();
			while (fIter.hasNext()) {
				String lib = fIter.next();
				if (attemptLoad(lib)) {
					storedLoadOrder.add(lib);
					fIter.remove( );
				}
			}
		}
		checkForFailure(failed);
		storeListToPreferences();
	}
	
	/**
	 * throw detailed exception if couldn't load everything
	 */
	private void checkForFailure(List<String> failed) { 
		if (failed.size( ) > 0) {
			String msg = "After " + NUM_ATTEMPTS + " attempts, unable to load native libs: ";
			for (Error e : failErrors) {
				msg += "\n" + e.getMessage();
			}
		throw new IllegalStateException(msg);
		}
	}
	

	/**
	 * load {@link #libsPresentMap} with listing of files in native lib directory
	 */
	private void loadMap() {
		File dir = new File(dirPath);
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dirPath + " is not directory");
		}
		File list[] = dir.listFiles();
		for (File f : list) {
			if (f.isFile()) {
				String name = f.getName();
				if (name.matches(NativeLoader.systemLibRegex)) {
					libsPresentMap.put(name, false);
				}
			}
		}
	}
	
	/**
	 * load {@link #storedLoadOrder} list from user preferences
	 */
	private void loadListFromPreferences( ) {
		String depsBlob = pref.get(PREFS_KEY, "");
		Collection<String> paths = FileUtils.splitPathString(depsBlob);
		storedLoadOrder.addAll(paths);
		listDirty = false;
	}

	/**
	 * store loadOrder list to user preferences
	 */
	private void storeListToPreferences( ) {
		String depsBlob = FileUtils.pathJoinStrings(storedLoadOrder);
		pref.put(PREFS_KEY, depsBlob);
		listDirty = false;
	}
	/**
	 * attempt to load library in #dirPath
	 * @param lib to load 
	 * @return true if loads okay
	 */
	private boolean attemptLoad(String lib)  {
		String fullpath = dirPath + FILESEP + lib;
		try {
			System.load(fullpath);
			return true;
		}
		catch (Error e) {
			if (isFailErrors()) {
				recordError(e);
			}
			//System.err.println(e.getMessage());
			return false;
		}
	}
	/**
	 * are we recording failure errors?
	 * @return true if yes
	 */
	private boolean isFailErrors() {
		return failErrors != null;
	}

	/**
	 * activate recording of failure errors
	 */
	private void setFailErrors() {
		failErrors = new LinkedList<Error>( );
	}
	
	/**
	 * record link #Error
	 * @param e to record
	 * @throws AssertionError if {@link #setFailErrors()} not set
	 */
	private void recordError(Error e) {
		assert failErrors != null : "logic error";
		failErrors.add(e);
	}

	public static void main(String[] args) {
		try {
			boolean clean = true; //set true to reload from scratch
			if (clean) {
				Preferences pref = Preferences.systemNodeForPackage(NativeLoader.class);
				pref.remove(PREFS_KEY);
			}
			
			Runtime r = Runtime.getRuntime();
			long startTime = System.nanoTime();
			NativeLoader nl = new NativeLoader();
			long memBefore = r.totalMemory();
			JOptionPane.showConfirmDialog(null,"Ready to load?");
			nl.loadNativeLibraries();
			JOptionPane.showConfirmDialog(null,"Ready to proceed?");
			long memAfter = r.totalMemory();
			long endTime = System.nanoTime();
			double took = endTime - startTime;
			took /= 1e9;
			double incr = memAfter - memBefore;
			System.out.println("took " + took + " seconds");
			System.out.println("memory usage increased " + incr);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

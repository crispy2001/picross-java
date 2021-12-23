package me.Proj.Picross;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class Main
{
	public static void main(String[] args) {
		GameLauncher.main(args);
	}

	public static String getPath() {
		File dir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		return dir.getParent();
	}

	public static URL getResource(String name) {
		return Main.class.getResource(name);
	}

	public static InputStream getResourceAsStream(String name) {
		return Main.class.getResourceAsStream(name);
	}
}

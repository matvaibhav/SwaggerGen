package com.infa.rest.swagger.serv;

import java.io.File;
import java.util.Properties;

public class GlobalConfig {

	private static GlobalConfig config = null;

	private boolean debugMode = false;
	private boolean dumpAPI = false;
	private boolean dumpAPIAtFail = false;
	private String savedStateLocation = "../savedStates/";
	private String swaggerDeployLocation = "../webapps/swaggerdeploy/";
	private String tempUploadPath = "../temp";

	private GlobalConfig() {
		Properties prop = new Properties();
		try {
			prop.load(GlobalConfig.class.getResourceAsStream("/sg.properties"));

			String depLoc = prop.getProperty(ServerProperties.SpecDeployPath
					.name());
			String stateLoc = prop.getProperty(ServerProperties.APIDumpPath
					.name());
			String tempUploadLoc = prop
					.getProperty(ServerProperties.TMPUploadPath.name());
			savedStateLocation = (stateLoc == null) ? savedStateLocation
					: stateLoc;
			swaggerDeployLocation = (depLoc == null) ? swaggerDeployLocation
					: depLoc;
			tempUploadPath = (tempUploadLoc == null) ? tempUploadPath
					: tempUploadLoc;
			File deployLocation = new File(swaggerDeployLocation);
			File savedStates = new File(savedStateLocation);
			File tempUpload = new File(tempUploadPath);

			if (!deployLocation.exists())
				deployLocation.mkdir();

			if (!savedStates.exists())
				savedStates.mkdir();

			if (!tempUpload.exists())
				tempUpload.mkdirs();

			String mode = prop.getProperty(ServerProperties.Mode.name());
			if ("DEBUG".equalsIgnoreCase(mode)) {
				config.debugMode = true;
				dumpAPIAtFail = true;
			}

			String val = prop.getProperty(ServerProperties.DumpAPIInfo.name());
			if ("FAILED".equals(val)) {
				dumpAPIAtFail = true;
			}
			if ("ALL".equals(val)) {
				dumpAPI = true;
			}

		} catch (Throwable e) {
		}

	}

	public static GlobalConfig getInstance() {

		if (config != null)
			return config;
		synchronized (GlobalConfig.class) {
			if (config == null)
				config = new GlobalConfig();
		}
		return config;
	}

	public boolean isDebug() {
		return debugMode;
	}

	public boolean isDumpAPI() {
		return dumpAPI;
	}

	public boolean isDumpAPIAtFail() {
		return dumpAPIAtFail;
	}

	public String getSavedStateLocation() {
		return savedStateLocation;
	}

	public String getSwaggerDeployLocation() {
		return swaggerDeployLocation;
	}

	public String getTempUploadPath() {
		return tempUploadPath;
	}

}

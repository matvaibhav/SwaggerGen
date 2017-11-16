package com.infa.rest.swagger.serv;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.informatica.sdk.helper.common.ApiException;

public class Util {

	public static boolean isValidJson(String jsonContent) throws ApiException{
		try {
			new JSONObject(jsonContent);
		} catch (JSONException ex) {
			try {
				new JSONArray(jsonContent);
			} catch (JSONException e) {
				throw new ApiException(600,
						"Invalid Raw body JSON." + e.getMessage());
			}
		}
		return true;
	}
	public static String writeSwaggerToFile(SavedState state, String fileName)
			throws IOException {

		File path = new File(GlobalConfig.getInstance()
				.getSwaggerDeployLocation() + "/" + state.getSessionId()+state.getPad());

		if (!path.exists()) {
			path.mkdirs();
		}

		String fullFileName = path.getAbsolutePath() + "/" + fileName;

		try (PrintWriter out = new PrintWriter(fullFileName)) {
			out.println(state.getApiResponse().getSwagger());
		}

		return fullFileName.substring(fullFileName.indexOf("webapps") + 7);
	}

	public static String deployExistingSwagger(File source, String destFolder,
			String fileName) throws IOException {

		File path = new File(GlobalConfig.getInstance()
				.getSwaggerDeployLocation() + "/" + destFolder);

		if (!path.exists()) {
			path.mkdirs();
		}

		File fullPath = new File(path.getAbsolutePath()+ "/" + fileName);

		Files.copy(source.toPath(), fullPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
		String fullFileName = fullPath.getAbsolutePath();

		return fullFileName.substring(fullFileName.indexOf("webapps") + 7);
	}

	public static String getHostURL(HttpServletRequest request) {
		String hostURLParts[] = request.getRequestURL().toString()
				.split("/", 4);
		String hostURL = hostURLParts[0] + "//" + hostURLParts[2];
		return hostURL;
	}

}

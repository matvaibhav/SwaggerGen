package com.infa.rest.swagger.serv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.infa.data.crypt.AESencrp;
import com.infa.rest.swagger.impl.RestEndPoint;
import com.informatica.sdk.helper.common.ApiException;
import com.informatica.sdk.helper.common.JsonUtil;

@MultipartConfig
@WebServlet("/SG/*")
public class Generator extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String store[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E",
			"F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
			"v", "w", "x", "y", "z" };
	private static Map<String, SavedState> state = new HashMap<>();
	private Boolean isDebug = GlobalConfig.getInstance().isDebug();
	private String hostURL = null;
	private String filePath;

	public Generator() {
		super();
	}

	public void init() {
		// Get the file location where it would be stored.
		filePath = getServletContext().getInitParameter("file-upload");

		File tmpDeployPath = new File(filePath);
		if (!tmpDeployPath.exists()) {
			tmpDeployPath.mkdirs();
		}
		filePath = tmpDeployPath.getAbsolutePath();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, String> cookieMap = new HashMap<String, String>();
		PrintWriter out = response.getWriter();
		String responseStr = "{}";

		Cookie[] cookies = request.getCookies();
		if (null != cookies && cookies.length > 0) {
			for (int i = cookies.length - 1; i >= 0; i--)
				cookieMap.put(cookies[i].getName(), cookies[i].getValue());
		}

		String requestPath = request.getPathInfo();
		if (null == requestPath) {
			requestPath = "/";
		}
		SavedState state = getState(cookieMap.get("ref"));

		switch (requestPath) {
		case "/download":
			if (state != null) {
				generateSwaggerDowload(state, response);
				return;
			}
			break;
		}
		responseStr = new ErrorBuilder("Not Supported.").build();
		out.println(responseStr);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");

		Map<String, String> cookieMap = new HashMap<String, String>();
		PrintWriter out = response.getWriter();
		String responseStr = "{}";

		Cookie[] cookies = request.getCookies();
		if (null != cookies && cookies.length > 0) {
			for (int i = cookies.length - 1; i >= 0; i--)
				cookieMap.put(cookies[i].getName(), cookies[i].getValue());
		}

		String requestPath = request.getPathInfo();
		if (null == requestPath) {
			requestPath = "/";
		}
		SavedState state = getState(cookieMap.get("ref"));

		switch (requestPath) {
		case "/":
			SavedState resume = generateNewSession();
			String padding = getID();
			resume.setPad(padding);
			addCookies(resume, response);
			break;
		case "/session":
			if (state != null) {
				// addCookies(state, response);
				responseStr = "{\"id\":\"" + state.getPad() + "\",\"ref\":\"" + state.getSessionId() + "\"}";
			}
			break;
		case "/session/restore":
			if (state != null) {
				try {
					responseStr = JsonUtil.serialize(state.getApiRequest());
				} catch (ApiException e) {
					e.printStackTrace();
				}
			}
			break;
		case "/verbs":
			responseStr = getVerbs();
			break;
		case "/userResponse":
			if (!ServletFileUpload.isMultipartContent(request)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				responseStr = "No, File uploaded..";
			}
			try {
				Part filePart = request.getPart("userresponse");
				String key = getSessionAESKey(state);
				String tmpFileName = filePath + File.separator + key;
				if (filePart != null) {
					filePart.write(tmpFileName);
					state.setUserResponseFileName(tmpFileName);
				} else {
					throw new Exception("Something's Wrong.. Try again.");
				}
			} catch (Exception ex) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				responseStr = "Upload Error!!" + ex.getMessage();
			}
			break;
		case "/generate":
			if (state != null) {
				String key = getSessionAESKey(state);
				String data = getBody(request);
				try {
					data = AESencrp.decrypt(key, data);
					state.setApiRequest(data);
					RestEndPoint ep = new RequestBuilder(data).build();
					responseStr = new ResponseBuilder(ep).build();
					state.setApiResponse(responseStr);
					out.print(responseStr);
					try {
						int statusCode = ep.getApis().get(0).getStatusCode();
						if (GlobalConfig.getInstance().isDumpAPI())
							serlizeState(state);
						else if ((statusCode != 200 || statusCode != 204)
								&& GlobalConfig.getInstance().isDumpAPIAtFail())
							serlizeState(state);
					} catch (Throwable e) {
						if (isDebug)
							e.printStackTrace();
					}

				} catch (ApiException e) {
					// "Not able to authenticate session, Please re-try.";
					responseStr = new ErrorBuilder(e).build();
					if (isDebug)
						e.printStackTrace();
				} catch (Exception e) {
					responseStr = new ErrorBuilder(e).build();// "Not able to authenticate session, Please re-try.";
					if (isDebug)
						e.printStackTrace();
				}

				break;
			}
		case "/deploy":
			if (state != null) {
				Map<String, String> qP = getQueryParams(request.getQueryString());

				if (state.getApiResponse().getStatus().getCode().equals("200")) {
					try {
						String fileDeplPath = Util.writeSwaggerToFile(state, qP.get("n"));
						String hostURL = getHostURL(request) + fileDeplPath;
						hostURL = hostURL.replace('\\', '/');

						responseStr = "{\"url\":\"" + hostURL + "\"}";
					} catch (Exception e) {
						responseStr = new ErrorBuilder("Error during deploy: " + e).build();// "Not able to deploy."
						if (isDebug)
							e.printStackTrace();
					}
				} else {
					responseStr = new ErrorBuilder("Unable to deploy swagger specification due to error in request.")
							.build();
				}
			}
			break;
		default:
			break;
		}

		out.println(responseStr);
	}

	private String getHostURL(HttpServletRequest request) {
		if (hostURL == null) {
			hostURL = Util.getHostURL(request);
		}
		return hostURL;
	}

	private void generateSwaggerDowload(SavedState state, HttpServletResponse response) {
		try {
			String filename = "swagger" + state.getSessionId() + ".json";
			String filepath = System.getProperty("java.io.tmpdir");

			String fullPath = filepath + filename;

			PrintWriter out = response.getWriter();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

			FileInputStream fileInputStream = new FileInputStream(fullPath);

			int i;
			while ((i = fileInputStream.read()) != -1) {
				out.write(i);
			}
			fileInputStream.close();
			out.close();
		} catch (IOException e) {
			if (isDebug)
				e.printStackTrace();
		}
	}

	private String getVerbs() {
		return "[\"GET\",\"POST\",\"PUT\",\"DELETE\"]";
	}

	private void addCookies(SavedState state, HttpServletResponse response) {
		Cookie ckId = new Cookie("id", state.getPad());
		ckId.setPath("/");
		response.addCookie(ckId);
		Cookie ckRef = new Cookie("ref", state.getSessionId());
		ckRef.setPath("/");
		response.addCookie(ckRef);
	}

	private String getSessionAESKey(SavedState state) {
		String tmpKey = String.format("%16d", Long.parseLong(state.getSessionId()));
		tmpKey = tmpKey.replace(" ", state.getPad());
		return tmpKey;
	}

	private SavedState generateNewSession() {
		int counter = state.size() + 1;
		String sessionId = String.format("%16d", counter);
		Checksum cs = new CRC32();
		cs.update(sessionId.getBytes(), 0, sessionId.length());
		sessionId = String.format("%d", cs.getValue());
		state.put(sessionId, new SavedState(sessionId));
		return getState(sessionId);
	}

	private SavedState getState(String sessionId) {
		return state.get(sessionId);
	}

	private String getBody(HttpServletRequest request) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

	private String getID() {
		return store[Math.abs(new Random().nextInt()) % 62];
	}

	private Map<String, String> getQueryParams(String query) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
					URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}

	private void serlizeState(SavedState state) throws IOException {

		FileOutputStream fout = new FileOutputStream(
				GlobalConfig.getInstance().getSavedStateLocation() + "/" + state.getSessionId() + ".state");
		ObjectOutputStream out = new ObjectOutputStream(fout);

		out.writeObject(state);
		out.flush();
		out.close();
		fout.close();
	}
}

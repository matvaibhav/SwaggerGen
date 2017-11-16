package com.infa.rest.swagger.serv;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.informatica.sdk.helper.swagger.proxy.SwaggerException;
import com.informatica.sdk.helper.swagger.proxy.SwaggerInstance;

@MultipartConfig
@WebServlet("/upload/*")
public class SwaggerUpload extends HttpServlet {

	private static final long serialVersionUID = -6323416432357270585L;
	private boolean isMultipart;
	private String filePath;
	private File file;
	private String hostURL = null;

	public void init() {
		// Get the file location where it would be stored.
		filePath = getServletContext().getInitParameter("file-upload");

		File tmpDeployPath = new File(filePath);
		if (!tmpDeployPath.exists()) {
			tmpDeployPath.mkdirs();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException {
		Map<String, String> cookieMap = new HashMap<String, String>();
		getHostURL(request);
		isMultipart = ServletFileUpload.isMultipartContent(request);
		response.setContentType("text/html");
		java.io.PrintWriter out = response.getWriter();

		if (!isMultipart) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print("No, File uploaded..");
			return;
		}

		Cookie[] cookies = request.getCookies();
		if (null != cookies && cookies.length > 0) {
			for (int i = cookies.length - 1; i >= 0; i--)
				cookieMap.put(cookies[i].getName(), cookies[i].getValue());
		}
		try {

			Part filePart = request.getPart("swaggerfile");
			String fileName = getSubmittedFileName(filePart);

			if (fileName.lastIndexOf("\\") >= 0) {
				file = new File(filePath
						+ fileName.substring(fileName.lastIndexOf("\\")));
			} else {
				file = new File(filePath
						+ fileName.substring(fileName.lastIndexOf("\\") + 1));
			}
			filePart.write(file.getAbsolutePath());

			if (isValidSwagger(file)) {
				String url = deploy(cookieMap, file);
				if (url != null && !url.isEmpty())
					out.print("<a target='_blank' href='" + url + "'>" + url
							+ "</a>");
			}

			// cleanup upload
			file.delete();
		} catch (SwaggerException ex) {
			out.print("Failed to deploy, Reason: Invalid Swagger. "
					+ ex.getMessage());
		 }catch (JsonProcessingException ex) {
			out.print("Failed to deploy, Reason: Invalid JSON. "
					+ ex.getMessage());
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	private String getSubmittedFileName(Part part) {
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}

	private boolean isValidSwagger(File uploadedFile) throws SwaggerException,
			IOException {
		SwaggerInstance.createSwaggerAsMetadata(null,
				uploadedFile.getAbsolutePath());
		return true;
	}

	private String deploy(Map<String, String> cookieMap, File uploadedFile) {
		try {
			String folder = cookieMap.get("ref") + cookieMap.get("id");
			String hostURL = this.hostURL
					+ Util.deployExistingSwagger(uploadedFile, folder,
							uploadedFile.getName());

			hostURL = hostURL.replace('\\', '/');

			return hostURL;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getHostURL(HttpServletRequest request) {
		if (hostURL == null) {
			hostURL = Util.getHostURL(request);
		}
		return hostURL;
	}
}

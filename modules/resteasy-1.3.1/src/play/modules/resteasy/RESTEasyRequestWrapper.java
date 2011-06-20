/*
    This file is part of resteasy-play-module.
    
    This is an adaptation of code in the RESTEasy project, under ASLv2 License 
    (http://www.apache.org/licenses/LICENSE-2.0), probably Copyright JBoss or Bill Burke.
    
    This adaptation Copyright Lunatech Research 2010

    resteasy-play-module is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    resteasy-play-module is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Lesser Public License
    along with resteasy-play-module.  If not, see <http://www.gnu.org/licenses/>.
*/
package play.modules.resteasy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.AsynchronousResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.util.Encode;

import play.data.parsing.DataParser;
import play.data.parsing.UrlEncodedParser;
import play.mvc.Http.Request;

public class RESTEasyRequestWrapper implements HttpRequest {

	private Request request;
	private HttpHeaders headers;
	protected MultivaluedMap<String, String> formParameters;
	protected MultivaluedMap<String, String> decodedFormParameters;
	private UriInfoImpl uriInfo;
	private String preProcessedPath;
	private InputStream overridenStream;

	public RESTEasyRequestWrapper(Request request, String prefix) {
		this.request = request;
		headers = ServletUtil.extractHttpHeaders(request);
		uriInfo = ServletUtil.extractUriInfo(request, prefix);
	      preProcessedPath = uriInfo.getPath(false);
	}

	public Object getAttribute(String key) {
		return request.args.get(key);
	}

	public MultivaluedMap<String, String> getDecodedFormParameters() {
		// Tomcat does not set getParameters() if it is a PUT request
		// so pull it out manually
		if (request.method.equals("PUT"))
		{
			return getPutDecodedFormParameters();
		}
		if (decodedFormParameters != null) return decodedFormParameters;
		decodedFormParameters = new MultivaluedMapImpl<String, String>();
		String query = request.querystring;
		Map<String, String[]> params;
		// read it from the body if possible
		if (getHttpHeaders().getMediaType().isCompatible(MediaType.valueOf("application/x-www-form-urlencoded")))
			params = DataParser.parsers.get("application/x-www-form-urlencoded").parse(request.body);
		else{
			params = new HashMap<String, String[]>();
		}
		Map<String, String[]> queryParams;

		if(query == null || query.length() == 0){
			// read it from the query string
			queryParams = UrlEncodedParser.parse(query);
		}else{
			queryParams = new HashMap<String, String[]>();
		}
		for (Map.Entry<String, String[]> entry : params.entrySet())
		{
			String name = entry.getKey();
			String[] values = entry.getValue();
			String[] queryValues = queryParams.get(name);
			if (queryValues == null)
			{
				for (String val : values) decodedFormParameters.add(name, val);
			}
			else
			{
				List queryValuesList = Arrays.asList(queryValues);
				for (String val : values)
				{
					if (!queryValuesList.contains(val))
					{
						decodedFormParameters.add(name, val);
					}
				}
			}
		}
		return decodedFormParameters;
	}

	public MultivaluedMap<String, String> getFormParameters() {
		// Tomcat does not set getParameters() if it is a PUT request
		// so pull it out manually
		if (request.method.equals("PUT"))
		{
			return getPutFormParameters();
		}
		if (formParameters != null) return formParameters;
		formParameters = Encode.encode(getDecodedFormParameters());
		return formParameters;
	}

	public HttpHeaders getHttpHeaders() {
		return headers;
	}

	public String getHttpMethod() {
		return request.method;
	}

	public InputStream getInputStream() {
		if (overridenStream != null) return overridenStream;
		return request.body;
	}

	public String getPreprocessedPath() {
		return preProcessedPath;
	}

	public UriInfo getUri() {
		return uriInfo;
	}


	public void removeAttribute(String key) {
		request.args.remove(key);
	}

	public void setAttribute(String key, Object value) {
		request.args.put(key, value);
	}

	public void setInputStream(InputStream is) {
		overridenStream = is;
	}

	public void setPreprocessedPath(String path) {
	      preProcessedPath = path;
	}

	public MultivaluedMap<String, String> getPutFormParameters()
	{
		if (formParameters != null) return formParameters;
		if (getHttpHeaders().getMediaType().isCompatible(MediaType.valueOf("application/x-www-form-urlencoded")))
		{
			try
			{
				formParameters = FormUrlEncodedProvider.parseForm(getInputStream());
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			throw new IllegalArgumentException("Request media type is not application/x-www-form-urlencoded");
		}
		return formParameters;
	}
	public MultivaluedMap<String, String> getPutDecodedFormParameters()
	{
		if (decodedFormParameters != null) return decodedFormParameters;
		decodedFormParameters = Encode.decode(getFormParameters());
		return decodedFormParameters;
	}

	//
	// Async stuff
	
	public AsynchronousResponse createAsynchronousResponse(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public AsynchronousResponse getAsynchronousResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initialRequestThreadFinished() {
		// TODO Auto-generated method stub

	}

	public boolean isInitial() {
		return request.isNew;
	}

	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return false;
	}

}

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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.specimpl.HttpHeadersImpl;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.resteasy.util.MediaTypeHelper;
import org.jboss.resteasy.util.PathHelper;

import play.mvc.Http.Header;
import play.mvc.Http.Request;

public class ServletUtil
{
	public static UriInfoImpl extractUriInfo(Request request, String servletPrefix)
	{
		String contextPath = "";
		if (servletPrefix != null && servletPrefix.length() > 0)
		{
			if (!servletPrefix.startsWith("/"))
				contextPath += "/";
			contextPath += servletPrefix;
		}
		URI absolutePath = null;
		try
		{
			URL absolute = new URL(request.getBase() + request.url);

			UriBuilderImpl builder = new UriBuilderImpl();
			builder.scheme(request.secure ? "https" : absolute.getProtocol());
			builder.host(absolute.getHost());
			builder.port(absolute.getPort());
			builder.path(absolute.getPath());
			absolutePath = builder.build();
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}

		String path = PathHelper.getEncodedPathInfo(absolutePath.getRawPath(), contextPath);
		List<PathSegment> pathSegments = PathSegmentImpl.parseSegments(path);

		URI baseURI = absolutePath;
		if (!path.trim().equals(""))
		{
			String tmpContextPath = contextPath;
			if (!tmpContextPath.endsWith("/")) tmpContextPath += "/";
			baseURI = UriBuilder.fromUri(absolutePath).replacePath(tmpContextPath).build();
		}

		UriInfoImpl uriInfo = new UriInfoImpl(absolutePath, baseURI, path, request.querystring, pathSegments);
		return uriInfo;
	}

	public static HttpHeaders extractHttpHeaders(Request request)
	{
		HttpHeadersImpl headers = new HttpHeadersImpl();

		MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(request);
		headers.setRequestHeaders(requestHeaders);
		List<MediaType> acceptableMediaTypes = extractAccepts(requestHeaders);
		List<String> acceptableLanguages = extractLanguages(requestHeaders);
		headers.setAcceptableMediaTypes(acceptableMediaTypes);
		headers.setAcceptableLanguages(acceptableLanguages);
		headers.setLanguage(requestHeaders.getFirst(HttpHeaderNames.CONTENT_LANGUAGE));

		String contentType = request.contentType;
		if (contentType != null) headers.setMediaType(MediaType.valueOf(contentType));

		Map<String, Cookie> cookies = extractCookies(request);
		headers.setCookies(cookies);
		return headers;

	}

	static Map<String, Cookie> extractCookies(Request request)
	{
		Map<String, Cookie> cookies = new HashMap<String, Cookie>();
		if (request.cookies != null)
		{
			for (play.mvc.Http.Cookie cookie : request.cookies.values())
			{
				// FIXME: how do we know the cookie version?
				// http://java.sun.com/javaee/5/docs/api/javax/servlet/http/Cookie.html#getVersion%28%29
				cookies.put(cookie.name, new Cookie(cookie.name, cookie.value, cookie.path, cookie.domain, 1));

			}
		}
		return cookies;
	}

	public static List<MediaType> extractAccepts(MultivaluedMap<String, String> requestHeaders)
	{
		List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
		List<String> accepts = requestHeaders.get(HttpHeaderNames.ACCEPT);
		if (accepts == null) return acceptableMediaTypes;

		for (String accept : accepts)
		{
			acceptableMediaTypes.addAll(MediaTypeHelper.parseHeader(accept));
		}
		return acceptableMediaTypes;
	}

	public static List<String> extractLanguages(MultivaluedMap<String, String> requestHeaders)
	{
		List<String> acceptable = new ArrayList<String>();
		List<String> accepts = requestHeaders.get(HttpHeaderNames.ACCEPT_LANGUAGE);
		if (accepts == null) return acceptable;

		for (String accept : accepts)
		{
			String[] splits = accept.split(",");
			for (String split : splits) acceptable.add(split.trim());
		}
		return acceptable;
	}

	@SuppressWarnings("unchecked")
	public static MultivaluedMap<String, String> extractRequestHeaders(Request request)
	{
		Headers<String> requestHeaders = new Headers<String>();

		for (Header header : request.headers.values())
		{
			for(String value : header.values){
				requestHeaders.add(header.name, value);
			}
		}
		return requestHeaders;
	}
}

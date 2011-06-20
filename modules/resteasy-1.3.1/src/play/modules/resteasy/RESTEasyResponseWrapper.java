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
import java.io.OutputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Error;

public class RESTEasyResponseWrapper implements HttpResponse {
	
	private Response response;
	private HttpServletResponseHeaders responseHeaders;
	private ResteasyProviderFactory factory;
	private Request request;

	public RESTEasyResponseWrapper(Request request, Response response, ResteasyProviderFactory factory) {
		this.request = request;
		this.response = response;
		responseHeaders = new HttpServletResponseHeaders(response, factory);
		this.factory = factory;
	}

	public void addNewCookie(NewCookie newCookie) {
		Http.Cookie cookie = new Http.Cookie();
		cookie.name = newCookie.getName();
		cookie.value = newCookie.getValue();
		cookie.path = newCookie.getPath();
		cookie.domain = newCookie.getDomain();
		cookie.maxAge = newCookie.getMaxAge();
		cookie.secure = newCookie.isSecure();
		response.cookies.put(cookie.name, cookie);
	}

	public MultivaluedMap<String, Object> getOutputHeaders() {
		return responseHeaders;
	}

	public OutputStream getOutputStream() throws IOException {
		return response.out;
	}

	public int getStatus() {
		return response.status != null ? response.status : 0;
	}

	public boolean isCommitted() {
		// FIXME: how do we know this?
		return false;
	}

	public void reset() {
		response.reset();
		responseHeaders = new HttpServletResponseHeaders(response, factory);
	}

	public void sendError(int status) throws IOException {
		Error error = new play.mvc.results.Error(status, "Internal error");
		error.apply(request, response);
	}

	public void sendError(int status, String text) throws IOException {
		Error error = new play.mvc.results.Error(status, text);
		error.apply(request, response);
	}

	public void setStatus(int status) {
		response.status = status;
	}

}

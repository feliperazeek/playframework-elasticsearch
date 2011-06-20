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

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.CaseInsensitiveMap;

import play.mvc.Http.Response;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpServletResponseHeaders implements MultivaluedMap<String, Object>
{

   private CaseInsensitiveMap cachedHeaders = new CaseInsensitiveMap();
   private Response response;
   private ResteasyProviderFactory factory;

   public HttpServletResponseHeaders(Response response, ResteasyProviderFactory factory)
   {
      this.response = response;
      this.factory = factory;
   }

   public void putSingle(String key, Object value)
   {
      cachedHeaders.putSingle(key, value);
      RuntimeDelegate.HeaderDelegate delegate = factory.createHeaderDelegate(value.getClass());
      if (delegate != null)
      {
         //System.out.println("addResponseHeader: " + key + " " + delegate.toString(value));
         response.setHeader(key, delegate.toString(value));
      }
      else
      {
         //System.out.println("addResponseHeader: " + key + " " + value.toString());
         response.setHeader(key, value.toString());
      }
   }

   public void add(String key, Object value)
   {
      cachedHeaders.add(key, value);
      addResponseHeader(key, value);
   }

   protected void addResponseHeader(String key, Object value)
   {
      RuntimeDelegate.HeaderDelegate delegate = factory.createHeaderDelegate(value.getClass());
      if (delegate != null)
      {
         //System.out.println("addResponseHeader: " + key + " " + delegate.toString(value));
         response.setHeader(key, delegate.toString(value));
      }
      else
      {
         //System.out.println("addResponseHeader: " + key + " " + value.toString());
         response.setHeader(key, value.toString());
      }
   }

   public Object getFirst(String key)
   {
      return cachedHeaders.getFirst(key);
   }

   public int size()
   {
      return cachedHeaders.size();
   }

   public boolean isEmpty()
   {
      return cachedHeaders.isEmpty();
   }

   public boolean containsKey(Object o)
   {
      return cachedHeaders.containsKey(o);
   }

   public boolean containsValue(Object o)
   {
      return cachedHeaders.containsValue(o);
   }

   public List<Object> get(Object o)
   {
      return cachedHeaders.get(o);
   }

   public List<Object> put(String s, List<Object> objs)
   {
      for (Object obj : objs)
      {
         addResponseHeader(s, obj);
      }
      return cachedHeaders.put(s, objs);
   }

   public List<Object> remove(Object o)
   {
      throw new RuntimeException("Removing a header is illegal for an HttpServletResponse");
   }

   public void putAll(Map<? extends String, ? extends List<Object>> map)
   {
      for (Map.Entry<? extends String, ? extends List<Object>> entry : map.entrySet())
      {
         List<Object> objs = entry.getValue();
         for (Object obj : objs)
         {
            add(entry.getKey(), obj);
         }
      }
   }

   public void clear()
   {
      throw new RuntimeException("Removing a header is illegal for an HttpServletResponse");
   }

   public Set<String> keySet()
   {
      return cachedHeaders.keySet();
   }

   public Collection<List<Object>> values()
   {
      return cachedHeaders.values();
   }

   public Set<Entry<String, List<Object>>> entrySet()
   {
      return cachedHeaders.entrySet();
   }

   public boolean equals(Object o)
   {
      return cachedHeaders.equals(o);
   }

   public int hashCode()
   {
      return cachedHeaders.hashCode();
   }
}

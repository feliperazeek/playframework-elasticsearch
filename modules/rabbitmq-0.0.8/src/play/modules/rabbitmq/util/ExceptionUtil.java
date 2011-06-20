/** 
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Felipe Oliveira (http://mashup.fm)
 * 
 */
package play.modules.rabbitmq.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// TODO: Auto-generated Javadoc
/**
 * The Class ExceptionUtil.
 */
public abstract class ExceptionUtil {

	/**
	 * Gets the stack trace.
	 * 
	 * @param exception
	 *            the exception
	 * @return the stack trace
	 */
	public static String getStackTrace(Exception exception) {
		return getStackTrace((Throwable) exception);
	}

	/**
	 * Gets the stack trace.
	 * 
	 * @param exception
	 *            the exception
	 * @return the stack trace
	 */
	public static String getStackTrace(Throwable exception) {
		return getStackTrace(null, exception);
	}

	/**
	 * Gets the stack trace.
	 * 
	 * @param title
	 *            the title
	 * @param exception
	 *            the exception
	 * @return the stack trace
	 */
	public static String getStackTrace(String title, Exception exception) {
		return getStackTrace(title, (Throwable) exception);
	}

	/**
	 * Gets the stack trace.
	 * 
	 * @param title
	 *            the title
	 * @param exception
	 *            the exception
	 * @return the stack trace
	 */
	public static String getStackTrace(String title, Throwable exception) {
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		if (title != null) {
			sb.append(title);
			sb.append("\n\n");
		}
		if (exception != null) {
			ByteArrayOutputStream ostr = new ByteArrayOutputStream();
			exception.printStackTrace(new PrintStream(ostr));
			sb.append(ostr);
		}
		return sb.toString();
	}
}
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.codehaus.jackson.map.ObjectMapper;

import play.Logger;
import play.Play;

/**
 * The Interface MessageMapper.
 */
public interface MsgMapper {

	/**
	 * Gets the bytes.
	 * 
	 * @param object
	 *            the object
	 * @return the bytes
	 * @throws Exception
	 *             the exception
	 */
	byte[] getBytes(Object object) throws Exception;

	/**
	 * Gets the object.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param object
	 *            the object
	 * @return the object
	 * @throws Exception
	 *             the exception
	 */
	Object getObject(Class clazz, byte[] object) throws Exception;

	/**
	 * The Enum Type.
	 */
	public static enum Type {

		/** The pojo. */
		pojo(POJO.class),

		/** The json. */
		json(JSON.class);

		/** The clazz. */
		private Class clazz;

		/**
		 * Instantiates a new type.
		 * 
		 * @param clazz
		 *            the clazz
		 */
		private Type(Class clazz) {
			this.clazz = clazz;
		}

		/**
		 * Gets the.
		 * 
		 * @return the message mapper
		 */
		public MsgMapper get() {
			try {
				return (MsgMapper) this.clazz.newInstance();
			} catch (Throwable t) {
				Logger.error(ExceptionUtil.getStackTrace(t));
				throw new RuntimeException(t.fillInStackTrace());
			}
		}
	}

	/**
	 * The Class POJO.
	 */
	public static class POJO implements MsgMapper {

		/**
		 * Gets the bytes.
		 * 
		 * @param object
		 *            the object
		 * @return the bytes
		 * @throws Exception
		 *             the exception
		 */
		@Override
		public byte[] getBytes(Object object) throws Exception {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(object);
			out.close();
			byte[] bytes = bos.toByteArray();
			bos.close();
			return bytes;
		}

		/**
		 * Gets the object.
		 * 
		 * @param object
		 *            the object
		 * @return the object
		 * @throws Exception
		 *             the exception
		 */
		@Override
		public Object getObject(Class clazz, byte[] object) throws Exception {
			ByteArrayInputStream bis = new ByteArrayInputStream(object);
			ObjectInputStream ois = new ObjectInputStream(bis);
			return ois.readObject();
		}

	}

	/**
	 * The Class JSON.
	 */
	public static class JSON implements MsgMapper {

		/** The mapper. */
		private transient ObjectMapper mapper;

		/**
		 * Instantiates a new jSON.
		 */
		public JSON() {
			this.mapper = new ObjectMapper();
		}

		/**
		 * Gets the bytes.
		 * 
		 * @param object
		 *            the object
		 * @return the bytes
		 * @throws Exception
		 *             the exception
		 */
		@Override
		public byte[] getBytes(Object object) throws Exception {
			String value = mapper.writeValueAsString(object);
			return value.getBytes();
		}

		/**
		 * Gets the object.
		 * 
		 * @param object
		 *            the object
		 * @return the object
		 * @throws Exception
		 *             the exception
		 */
		@Override
		public Object getObject(Class clazz, byte[] object) throws Exception {
			Object data = mapper.readValue(new String(object), clazz);
			return data;
		}

	}

}

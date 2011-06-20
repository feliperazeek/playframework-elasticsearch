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
package controllers;

import play.Logger;
import play.modules.rabbitmq.util.ExceptionUtil;
import play.mvc.Controller;
import play.mvc.WebSocketController;

/**
 * The Class RabbitMQStreamer.
 */
public class RabbitMQWebSocket extends Controller {

	/**
	 * The Class ActivitySocket.
	 */
	public static class StreamSocket extends WebSocketController {

		/**
		 * Index.
		 */
		public static void index() {
			while (inbound.isOpen()) {
				try {
					play.libs.F.Promise<String> promise = play.modules.rabbitmq.RabbitMQPlugin.statsService().liveStream.nextEvent();
					String event = await(promise);
					Logger.info("Publishing Event %s to Outbound Subscribers", event);
					outbound.send(event);

				} catch (Throwable t) {
					Logger.error(ExceptionUtil.getStackTrace(t));
				}
			}
		}

	}

}

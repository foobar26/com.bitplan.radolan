/**
 * Copyright (c) 2018 BITPlan GmbH
 *
 * http://www.bitplan.com
 *
 * This file is part of the Opensource project at:
 * https://github.com/BITPlan/com.bitplan.radolan
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
 * Parts which are derived from https://gitlab.cs.fau.de/since/radolan are also
 * under MIT license.
 */
package com.bitplan.radolan;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* simple ErrorHandler
* @author wf
*
*/
public class ErrorHandler {
 protected static Logger LOGGER = Logger.getLogger("com.bitplan.radolan");

 /***
  * handle the given Throwable
  *
  * @param th
  */
 public static void handle(Throwable th, String msg) {
   if (msg==null) {
     msg="";
   } else {
     msg="("+msg+")";
   }
   LOGGER.log(Level.WARNING, "Error " + th.getClass().getName()+msg+":"+ th.getMessage());
   StringWriter sw = new StringWriter();
   th.printStackTrace(new PrintWriter(sw));
   LOGGER.log(Level.WARNING, "Stacktrace: " + sw.toString());
 }

 /**
  * issue a warning
  * @param msg
  */
 public static void warn(String msg) {
   LOGGER.log(Level.WARNING,msg);
 }

 public static void handle(Throwable th) {
   handle(th,null);
 }
}


/*
 * Copyright 2017 Neustar, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package biz.neustar.tdi.fw.exception;

/**
 * Exception class for internal exceptions (checked).
 */
public class FrameworkException extends Exception {
  /**
   * Default serial version ID.
   */
  private static final long serialVersionUID = -642489717314485504L;

  /**
   * Constructor with argument
   * 
   * @param message : an instance of {@link String}.
   */
  public FrameworkException(String message) {
    super(message);
  }
  
  /**
   * Constructor with argument
   * 
   * @param cause : an instance of {@link Throwable}.
   */
  public FrameworkException(Throwable cause) {
    super(cause);
  }
}

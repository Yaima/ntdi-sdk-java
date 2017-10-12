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

package biz.neustar.tdi.fw.platform.facet.time;

import biz.neustar.tdi.fw.platform.facet.TdiPlatformFacetShape;

/**
 * Platform facet: Abstracted Time and date.
 */
public interface TdiPlatformTimeShape extends TdiPlatformFacetShape {
  /**
   * Method to return timestamp from the passed dateStr, based on the platform
   * implementation.
   * 
   * @param timeDateStr
   *          : Time/Date string
   * 
   * @return Long timestamp.
   */
  public Long timestamp(String timeDateStr);

  /**
   * Method to return a time/date string from the passed timestamp, based on the
   * platform implementation.
   * 
   * @param timestamp
   *          Long timestamp.
   * 
   * @return Converted Time/Date String.
   */
  public String isoDate(Long timestamp);
}

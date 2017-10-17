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

package biz.neustar.tdi.platform.facet;

import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.fw.platform.facet.time.TdiPlatformTimeShape;
import biz.neustar.tdi.platform.Constants;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.lang.Math;

/**
 * Implementation of TimeFacet class.
 *
 */
public class TimeFacet implements TdiPlatformTimeShape {

  private TdiPlatformShape pf;

  /**
   * TimeFacet class constructor.
   * 
   * @param pf
   *          : Object of {@link TdiPlatformShape}
   */
  public TimeFacet(TdiPlatformShape pf) {
    this.pf = pf;
  }

  /**
   * Method to get platform object.
   * 
   * @return Object of {@link TdiPlatformShape} class
   */
  @Override
  public TdiPlatformShape getPlatform() {
    return pf;
  }

  /**
   * Init method for {@link TimeFacet} class.
   * 
   * @return Completed {@link CompletableFuture}&lt;Void&gt; object.
   */
  @Override
  public CompletableFuture<Void> init() {
    return CompletableFuture.completedFuture(null);
  }

  /**
   * timestamp method expects time stamp string in below format. -
   * 2017-08-18T07:18:02.389Z - yyyy-MM-dd'T'HH:mm:ss'Z' Method returns epoch
   * time seconds.
   * 
   * @param timeDateStr
   *          : Can be empty, or timestamp string in format
   *          "yyyy-MM-dd'T'HH:mm:ss'Z"
   * 
   * @return Long eponch time seconds
   */
  @Override
  public Long timestamp(String timeDateStr) {
    if (!StringUtils.isEmpty(timeDateStr)) {
      Instant instant = Instant.parse(timeDateStr);
      Long timeInSec = (Long) Math.floor(instant.toEpochMilli() / 1000);
      return timeInSec;
    } else {
      // As we don't have time stamp string, return current epoch time
      return ((Long) Math.floor(Instant.now().toEpochMilli() / 1000));
    }
  }

  /**
   * Method to add minutes to current iso date stamp. Method returns time stamp
   * in ISO format "yyyy-MM-dd'T'HH:mm:ss'Z'".
   * 
   * @param timestamp
   *          : Long minutes need to be added to the date time stamp
   * 
   * @return Current date time stamp in ISO format with added number of minutes
   */
  @Override
  public String isoDate(Long timestamp) {

    // We are expecting int here as minutes to add to iso date
    int minute = java.lang.Math.toIntExact(timestamp);

    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat(Constants.ISO_TIMESTAMP_FORMAT);
    df.setTimeZone(tz);

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(Instant.now().toEpochMilli()));
    cal.add(Calendar.SECOND, minute);

    String nowAsIso = df.format(cal.getTime());
    return nowAsIso;
  }
}

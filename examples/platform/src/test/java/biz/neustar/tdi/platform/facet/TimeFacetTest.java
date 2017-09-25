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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import biz.neustar.tdi.fw.platform.TdiPlatformShape;
import biz.neustar.tdi.platform.facet.TimeFacet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Junit test class for TimeFacet class.
 * 
 */
public class TimeFacetTest {

  static TimeFacet timeFacetApi;
  public static TdiPlatformShape pf;
  
  /**
   * Junit step method for TimeFacetTest class.
   * 
   */
  @BeforeClass
  public static void setup() {
    pf = TestData.getDummyLibraryPlatform();
    timeFacetApi = new TimeFacet(pf);
  }  

  @Test
  public void testTimestamp() {
    
    String timeStr = "2017-08-18T12:18:02.564Z";
    Long time = timeFacetApi.timestamp(timeStr);
    assertTrue((time != 0));
    assertTrue((1503058682 == time));

    time = timeFacetApi.timestamp("");
    Long currentTime = Instant.now().toEpochMilli() / 1000;
    assertTrue((time != 0));
    assertTrue((currentTime != time));
  }
  
  @Test(expected = DateTimeParseException.class)
  public void testTimestampInvalidFormat() {
    String timeStr = "08-2017-18 12:18:02.564";
    Long time = timeFacetApi.timestamp(timeStr);
    assertTrue((time != 0));
    assertTrue((1503058682 == time));
  }
  
  @Test
  public void testTimestampNullFormat() {
    Long time = timeFacetApi.timestamp(null);
    Long currentTime = Instant.now().toEpochMilli() / 1000;
    assertTrue((time != 0));
    assertTrue((currentTime != time));
  }
  
  @Test
  public void testTimestampNullTimeStr() {
    Long time = timeFacetApi.timestamp(null);
    assertFalse((time == 0));
  }
    
  @Test
  public void testIsoDate() {
    Long minutes = 0L;
    String isoTime = timeFacetApi.isoDate(minutes);
    assertNotNull(isoTime);
    
    minutes = 20L;
    isoTime = timeFacetApi.isoDate(minutes);
    assertNotNull(isoTime);
  }
  
  @Test
  public void testGetPlatform() {
    TdiPlatformShape pf = timeFacetApi.getPlatform();
    assertNotNull(pf);
  }

  @Test
  public void testInit() 
      throws InterruptedException, 
      ExecutionException {
    CompletableFuture<Void> future = timeFacetApi.init();
    assertNotNull(future);
    future.get();
  }
  
}

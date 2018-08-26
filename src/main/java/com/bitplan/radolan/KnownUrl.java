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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.bitplan.dateutils.DateUtils;

/**
 * handle known urls
 * 
 * @author wf
 *
 */
public class KnownUrl {
  public static int OPEN_DATA = 0;
  public static int GRIDS = 1;
  public static String knownUrls[] = {
      "https://opendata.dwd.de/weather/radar/radolan",
      "ftp://ftp-cdc.dwd.de/pub/CDC/grids_germany/daily/radolan" };
  public static final String RADOLAN_HISTORY = knownUrls[GRIDS];
  public static final String RADOLAN_OPENDATA = knownUrls[OPEN_DATA];

  public static final DateFormat hourFormat = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm");

  /**
   * get the recent SF data url for the given local date
   * 
   * @param date
   * @return - the url
   */
  public static String getSFRecent(LocalDate date) {
    return getSFRecent(date.getYear(), date.getMonthValue(),
        date.getDayOfMonth());
  }

  /**
   * get the recent SF data url for the given parameters
   * 
   * @param year
   * @param month
   * @param dayOfMonth
   * @return - the url
   */
  public static String getSFRecent(int year, int month, int dayOfMonth) {
    String url = String.format(
        RADOLAN_HISTORY + "recent/raa01-sf_10000-%02d%02d%02d1650-dwd---bin.gz",
        year % 2000, month, dayOfMonth);
    return url;
  }

  /**
   * get a TimeStamp for the given date
   * 
   * @param date
   * @param hour
   * @param min
   * @return the time Stamp
   */
  public static String getTimeStamp(LocalDate date, int hour, int min) {
    String timeStamp = getTimeStamp(date.getYear(), date.getMonthValue(),
        date.getDayOfMonth(), hour, min);
    return timeStamp;
  }

  /**
   * get the timeStamp for the given dateTime
   * 
   * @param dateTime
   * @return the timeStamp
   */
  public static String getTimeStamp(LocalDateTime dateTime) {
    String timeStamp = getTimeStamp(dateTime.getYear(),
        dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(),
        dateTime.getMinute());
    return timeStamp;
  }

  /**
   * get a valid timeStamp for the given dateTime for the given product
   * 
   * @param product
   * @param dateTime
   * @return - a dateTime that has been
   */
  public static String getTimeStampForProduct(String product,
      LocalDateTime dateTime) {
    switch (product) {
    case "sf":
    case "rw":
      // make the time end in :50
      while (dateTime.getMinute()!=50) {
        dateTime=dateTime.minus(Duration.ofMinutes(1));
      }
      break;
    case "ry":
      while (dateTime.getMinute()%5!=0) {
        dateTime=dateTime.minus(Duration.ofMinutes(1));
      }
      break;
    }
    String timeStamp = getTimeStamp(dateTime);
    return timeStamp;
  }

  /**
   * get the time Stamp for the given parameters
   * 
   * @param year
   * @param month
   * @param dayOfMonth
   * @param hour
   * @param min
   * @return - the time Stamp
   */
  public static String getTimeStamp(int year, int month, int dayOfMonth,
      int hour, int min) {
    String timeStamp = String.format("%02d%02d%02d%02d%02d", year % 2000, month,
        dayOfMonth, hour, min);
    return timeStamp;
  }

  /**
   * get the FileName for the given product and timeStamp
   * 
   * @param product
   *          - e.g. sf, rw, ry
   * @param timeStamp
   *          - e.g. 1808251150 or "latest"
   * @return the filename
   */
  public static String getFileNameForProduct(String product, String timeStamp) {
    String fileName = String.format("raa01-%s_10000-%s-dwd---bin", product,
        timeStamp);
    return fileName;
  }

  /**
   * get the url for the given Product and dateTime
   * 
   * @param product
   * @param dateTime
   * @return - the url
   */
  public static String getUrlForProduct(String product,
      LocalDateTime dateTime) {
    String timeStamp = getTimeStampForProduct(product,dateTime);
    String fileName = getFileNameForProduct(product, timeStamp);
    String url = String.format("%s/%s/%s", RADOLAN_OPENDATA, product, fileName);
    return url;
  }

  /**
   * get the url for the given product and time Description
   * 
   * @param productDescription
   * @param timeDescription
   * @return the url
   * @throws Exception
   */
  public static String getUrl(String productDescription, String timeDescription)
      throws Exception {
    String product = productDescription.toLowerCase();
    switch (product) {
    case "daily":
    case "dailysum":
      product = "sf";
      break;
    case "hourly":
    case "hourlysum":
      product = "rw";
      break;
    case "5min":
    case "5minsum":
      product = "ry";
    }
    switch (product) {
    case "sf":
    case "rw":
    case "ry":
      break;
    default:
      throw new Exception(String.format("productDescription %s (%s),is unknown",
          productDescription, product));
    }
    String timeStamp = timeDescription;
    switch (timeDescription) {
    case "latest":
      break;
    default:
      if (timeDescription.startsWith("20")) {
        Date dateTime = hourFormat.parse(timeDescription);
        LocalDateTime localDateTime = DateUtils.asLocalDateTime(dateTime);
        timeStamp = getTimeStampForProduct(product,localDateTime);
      } else if (timeDescription.startsWith("-")) {

      }
      break;
    }
    String fileName = getFileNameForProduct(product, timeStamp);
    String url = String.format("%s/%s/%s", RADOLAN_OPENDATA, product, fileName);
    return url;
  }

}

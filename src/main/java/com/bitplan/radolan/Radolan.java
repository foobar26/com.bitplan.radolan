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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.Option;

import com.bitplan.javafx.Main;

import cs.fau.de.since.radolan.Composite;
import javafx.application.Platform;
import javafx.scene.image.Image;

/**
 * Main for radolan.jar and radolan.exe
 * 
 * @author wf
 *
 */
public class Radolan extends Main {

  @Option(name = "-s", aliases = {
      "--show" }, usage = "show\nshow resulting image")
  protected boolean showImage = true;

  @Option(name = "-cp", aliases = {
      "--cachePath" }, usage = "path to Cache\nthe path to the Cache")
  protected String cachePath = System.getProperty("user.home")
      + java.io.File.separator + ".radolan";;

  @Option(name = "-nc", aliases = {
      "--noCache" }, usage = "noCache\ndo not use local cache")
  protected boolean noCache = false;

  @Option(name = "-t", aliases = {
      "--showTime" }, usage = "showTime\nshow result for the given time in seconds")
  protected int showTimeSecs = Integer.MAX_VALUE / 1100; // over 20 years

  @Option(name = "-i", aliases = {
      "--input" }, usage = "input\nurl/file of the input")
  protected String input = "https://www.dwd.de/DWD/wetter/radar/radfilm_brd_akt.gif"; // "https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg";

  @Option(name = "-o", aliases = {
      "--output" }, usage = "output/e.g. path of png/jpg/gif file")
  protected String output;

  private DisplayContext displayContext;

  /**
   * stop the application with the given exit Code
   * 
   * @param pExitCode
   */
  public void stop(int pExitCode) {
    Platform.runLater(() -> {
      Platform.exit();
    });
    exitCode = pExitCode;
    if (!testMode) {
      System.exit(pExitCode);
    }
  }

  /**
   * prepare the ImageViewer
   */
  protected void prepareImageViewer() {
    ImageViewer.testMode = testMode;
    ImageViewer.toolkitInit();
  }

  /**
   * show the given image
   * 
   * @param image
   * @param title
   * @param composite
   */
  public void showImage(DisplayContext displayContext) {
    prepareImageViewer();
    ImageViewer imageViewer = new ImageViewer(displayContext);
    imageViewer.limitShowTime(this.showTimeSecs);
    imageViewer.show();
    imageViewer.waitOpen();
    // do we show a radolan image?
    if (displayContext.composite != null) {
      displayContext.view = imageViewer.imageView;
      displayContext.toolTip = imageViewer.toolTip;
      Radolan2Image.activateToolTipOnShowEvent(displayContext);
    }
    imageViewer.waitClose();
  }

  /**
   * show the image from the given input
   * 
   * @param input
   */
  public void showImage(String input) {
    prepareImageViewer();
    Image image = new Image(input);
    displayContext = new DisplayContext(null, null);
    displayContext.image = image;
    displayContext.title = input;
    showImage(displayContext);
  }

  /**
   * do the required work
   */
  public void work() {
    if (debug) {
      Composite.debug = true;
      Radolan2Image.debug = true;
    }
    if (noCache) {
      Composite.useCache = false;
    } else {
      Composite.cacheRootPath = cachePath;
    }
    if (showVersion)
      this.showVersion();
    if (showHelp)
      this.showHelp();
    else {
      // is the input a showable image?
      if (input.contains(".png") || input.contains(".jpg")
          || input.contains(".gif")) {
        if (this.showImage)
          showImage(input);
        else {
          // silently fail here?
        }
      } else {
        try {
          Composite composite = new Composite(input);
          displayContext = new DisplayContext(composite,
              "2_bundeslaender/2_hoch.geojson");
          displayContext.title = String.format("%s-image (%s) showing %s",
              composite.getProduct(), composite.getDataUnit(),
              composite.getForecastTime());
          if (debug)
            LOGGER.log(Level.INFO, displayContext.title);
          Radolan2Image.getImage(displayContext);
          if (this.showImage)
            showImage(displayContext);
        } catch (Throwable th) {
          ErrorHandler.handle(th);
        }
      }
      // shall we save the image
      if (displayContext!=null && displayContext.image != null && output != null && !output.isEmpty()) {
        String ext = FilenameUtils.getExtension(output);
        File outputFile = new File(output);
        BufferedImage bImage = ImageViewer.fromFXImage(displayContext.image, null);
        String formatName = ext;
        try {
          ImageIO.write(bImage, formatName, outputFile);
        } catch (IOException e) {
          ErrorHandler.handle(e);
        }
      }
    }
  }

  /**
   * main routine
   * 
   * @param args
   */
  public static void main(String[] args) {
    Radolan radolan = new Radolan();
    radolan.maininstance(args);
    if (!testMode)
      System.exit(exitCode);
  }

  @Override
  public String getSupportEMail() {
    return "support@bitplan.com";
  }

  @Override
  public String getSupportEMailPreamble() {
    return "Dear Radolan user";
  }
}

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
package com.bitplan.display;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openweathermap.weather.Coord;

import com.bitplan.geo.DPoint;

import cs.fau.de.since.radolan.FloatFunction;
import cs.fau.de.since.radolan.vis.Vis;
import cs.fau.de.since.radolan.vis.Vis.ColorRange;
import de.dwd.geoserver.Station;
import de.dwd.geoserver.StationManager;
import javafx.scene.paint.Color;

/**
 * display of evaporation
 * 
 * @author wf
 *
 */
public class EvaporationView {

  public static final ColorRange[] DWD_Style_Colors = {
      new ColorRange(6.0f, 999999.0f, Color.rgb(204, 0, 0)),
      new ColorRange(5.0f, 5.9999f, Color.rgb(220, 91, 0)),
      new ColorRange(4.0f, 4.9999f, Color.rgb(232, 174, 0)),
      new ColorRange(3.0f, 3.9999f, Color.rgb(237, 237, 0)),
      new ColorRange(2.0f, 2.9999f, Color.rgb(127, 195, 0)),
      new ColorRange(1.0f, 1.9999f, Color.rgb(68, 171, 0)),
      new ColorRange(0.0f, 0.9999f, Color.rgb(0, 139, 0)) };

  public static FloatFunction<Color> heatmap = Vis.RangeMap(DWD_Style_Colors);

  private StationManager sm;

  Map<Object, Object> evapmap;
  Map<String, Station> stationmap;

  /**
   * create an evaporationView for the given stationManager
   * 
   * @param sm
   */
  public EvaporationView(StationManager sm) {
    this.sm = sm;
    evapmap = sm.g().V().hasLabel("observation").has("name", "evaporation")
        .group().by("stationid").by(values("value").mean()).next();
    stationmap = new HashMap<String, Station>();
    sm.g().V().hasLabel("station").forEachRemaining(v -> {
      Station s = new Station();
      s.fromVertex(v);
      stationmap.put(s.id, s);
    });
  }

  /**
   * draw the evaporation
   * 
   * @param borderDraw
   * @param radius
   * @param opacity
   */
  public void draw(BorderDraw borderDraw, double radius, double opacity) {
    FloatFunction<Color> evapColorMap = EvaporationView.heatmap;
    for (Station s : stationmap.values()) {
      // get the station location
      Coord coord = s.getCoord();
      // translate it to the borderDraw coordinates
      DPoint p = borderDraw.translateLatLonToView(coord.getLat(),
          coord.getLon());
      // get the evaporation for this station
      Number evap = (Number) evapmap.get(s.getId());
      // calculate the color for that evaporation
      Color evapColor = evapColorMap.apply(evap.floatValue());
      // show a circle with the given color and the the short name of the
      // station
      Draw.drawCircleWithText(borderDraw.getPane(), s.getShortName(), radius,
          evapColor, opacity, Color.BLUE, p.x, p.y, true);
    }
    ;
  }

  /**
   * draw interpolated
   * 
   * @param borderDraw
   * @param gridx
   * @param gridy
   */
  public void drawInterpolated(BorderDraw borderDraw, int gridx, int gridy) {
    Map<Coord, List<Station>> gridMap = this.prepareGrid(120.0, gridx, gridy);
    for (Coord c : gridMap.keySet()) {
      double lat=c.getLat();
      double lon=c.getLon();
      DPoint p = borderDraw.translateLatLonToView(lat,lon);
      Color evapColor = Color.AQUA;
      //String text = c.toString();
      String text=String.format("%5.1f %s\n%5.1f %s", lat,lat>=0?"N":"S",lon,lon>=0?"E":"W");
      Draw.drawCircleWithText(borderDraw.getPane(), text, 4., evapColor, 0.5,
          Color.BLUE, p.x, p.y, true);
    }
  }

  /**
   * prepare a grid with the given density
   * 
   * @param gridx
   * @param gridy
   * @return - coordinates and a list of stations influencing
   */
  public Map<Coord, List<Station>> prepareGrid(double radius, int gridx,
      int gridy) {
    Map<Coord, List<Station>> gridMap = new HashMap<Coord, List<Station>>();
    Coord nw = sm.getNorthWest();
    Coord se = sm.getSouthEast();
    double dx = (se.getLon() - nw.getLon()) / gridx;
    double dy = (se.getLat() - nw.getLat()) / gridy;

    for (double lat = nw.getLat(); lat < se.getLat(); lat += dy) {
      for (double lon = nw.getLon(); lon < se.getLon(); lon += dx) {
        Coord c = new Coord(lat, lon);
        List<Station> stations = getStationsWithinRadius(c, radius);
        gridMap.put(c, stations);
      }
    }
    return gridMap;
  }

  /**
   * get a list of stations that are with the given radius
   * 
   * @param c
   * @param radius
   * @return the list of stations
   */
  private List<Station> getStationsWithinRadius(Coord c, double radius) {
    List<Station> stations = new ArrayList<Station>();
    for (Station station : this.stationmap.values()) {
      Coord sc = station.getCoord();
      double dist = sc.distance(c);
      if (dist < radius)
        stations.add(station);
    }
    return stations;
  }
}

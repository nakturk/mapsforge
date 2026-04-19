/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
      * Copyright 2014 Ludwig M Brinckmann
      * Copyright 2015-2018 devemux86
      *
      * This program is free software: you can redistribute it and/or modify it under the
      * terms of the GNU Lesser General Public License as published by the Free Software
      * Foundation, either version 3 of the License, or (at your option) any later version.
      *
      * This program is distributed in the hope that it will be useful, but WITHOUT ANY
      * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
      * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
      *
      * You should have received a copy of the GNU Lesser General Public License along with
      * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.layer.renderer;

import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Tag;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.datastore.Way;

import java.util.List;

/**
 * A PolylineContainer encapsulates the way data retrieved from a map file.
     * <p/>
     * The class uses deferred evaluation for computing the absolute and relative
     * pixel coordinates of the way as many ways will not actually be rendered on a
     * map. In order to save memory, after evaluation, the internally stored way is
     * released.
     */
public class PolylineContainer implements ShapeContainer {

    private Point center;
        private Point[][] coordinatesAbsolute;
        private Point[][] coordinatesRelativeToTile;
        private final List<Tag> tags;
        private final byte layer;
        private final Tile upperLeft;
        private final Tile lowerRight;
        private final boolean isClosedWay;
        private final float height;
        private Way way;

    public PolylineContainer(Way way, Tile upperLeft, Tile lowerRight) {
                this.coordinatesAbsolute = null;
                this.coordinatesRelativeToTile = null;
                this.tags = way.tags;
                this.upperLeft = upperLeft;
                this.lowerRight = lowerRight;
                this.layer = way.layer;
                this.way = way;
                this.isClosedWay = LatLongUtils.isClosedWay(way.latLongs[0]);
                this.height = extractHeight(this.tags);
                if (this.way.labelPosition != null) {
                                this.center = MercatorProjection.getPixelAbsolute(this.way.labelPosition, this.upperLeft.mapSize);
                }
    }

    public Point getCenterAbsolute() {
                return this.center;
    }

    public Point[][] getCoordinatesAbsolute() {
                if (this.coordinatesAbsolute == null && this.way != null) {
                                this.coordinatesAbsolute = new Point[this.way.latLongs.length][];
                                for (int i = 0; i < this.way.latLongs.length; ++i) {
                                                    this.coordinatesAbsolute[i] = MercatorProjection.getPixelsAbsolute(this.way.latLongs[i], this.upperLeft.mapSize);
                                }
                                if (this.isClosedWay) {
                                                    this.way = null;
                                }
                }
                return this.coordinatesAbsolute;
    }

    public Point[][] getCoordinatesRelativeToTile(Tile tile) {
                if (this.coordinatesRelativeToTile == null) {
                                Point[][] absolute = getCoordinatesAbsolute();
                                this.coordinatesRelativeToTile = new Point[absolute.length][];
                                for (int i = 0; i < absolute.length; ++i) {
                                                    this.coordinatesRelativeToTile[i] = MercatorProjection.getRelativePixelByAbsolute(absolute[i], tile.getOrigin());
                                }
                                this.coordinatesAbsolute = null;
                }
                return this.coordinatesRelativeToTile;
    }

    @Override
        public byte getLayer() {
                    return this.layer;
        }

    @Override
        public float getHeight() {
                    return this.height;
        }

    @Override
        public ShapeType getShapeType() {
                    return ShapeType.POLYLINE;
        }

    public List<Tag> getTags() {
                return this.tags;
    }

    public boolean isClosedWay() {
                return this.isClosedWay;
    }

    public Tile getUpperLeft() {
                return this.upperLeft;
    }

    public Tile getLowerRight() {
                return this.lowerRight;
    }

    private static float extractHeight(List<Tag> tags) {
                if (tags == null) return 0;
                for (Tag tag : tags) {
                                if ("height".equals(tag.key)) {
                                                    try {
                                                                            return Float.parseFloat(tag.value);
                                                    } catch (NumberFormatException e) {
                                                                            // ignore
                                                    }
                                }
                                if ("building:levels".equals(tag.key)) {
                                                    try {
                                                                            return Float.parseFloat(tag.value) * 3;
                                                    } catch (NumberFormatException e) {
                                                                            // ignore
                                                    }
                                }
                }
                return 0;
    }
}

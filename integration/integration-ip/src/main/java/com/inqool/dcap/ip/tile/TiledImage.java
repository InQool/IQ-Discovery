package com.inqool.dcap.ip.tile;

import javax.xml.bind.annotation.*;
import java.util.List;

@SuppressWarnings("unused")
@XmlRootElement(name = "Image", namespace = "http://schemas.microsoft.com/deepzoom/2008")
@XmlAccessorType(XmlAccessType.FIELD)
public class TiledImage {
    @XmlAttribute(name = "Format")
    private String format;

    @XmlAttribute(name = "Overlap")
    private Integer overlap;

    @XmlAttribute(name = "TileSize")
    private Integer tileSize;

    @XmlElement(name = "Size", namespace = "http://schemas.microsoft.com/deepzoom/2008")
    private Size size;

    @XmlTransient
    private List<Tile> tiles;

    public TiledImage() {
    }

    public TiledImage(String format, Integer overlap, Integer tileSize, Integer width, Integer height, List<Tile> tiles) {
        this.format = format;
        this.overlap = overlap;
        this.tileSize = tileSize;
        this.size = new Size(width, height);
        this.tiles = tiles;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getOverlap() {
        return overlap;
    }

    public void setOverlap(Integer overlap) {
        this.overlap = overlap;
    }

    public Integer getTileSize() {
        return tileSize;
    }

    public void setTileSize(Integer tileSize) {
        this.tileSize = tileSize;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Size {
        @XmlAttribute(name = "Width")
        private Integer width;

        @XmlAttribute(name = "Height")
        private Integer height;

        public Size() {
        }

        public Size(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }
    }
}

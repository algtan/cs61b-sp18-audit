import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private final int MIN_DEPTH = 0;
    private final int MAX_DEPTH = 7;
    private final int MIN_TILE = 0;
    private final int LON_DIRECTION = 1;
    private final int LAT_DIRECTION = -1;
    private final double MAX_LON_DPP = calculateDpp(MapServer.ROOT_ULLON, MapServer.ROOT_LRLON, MapServer.TILE_SIZE);

    public Rasterer() {
        // YOUR CODE HERE
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        // System.out.println(params);
        double queryStartingLon = params.get("ullon");
        double queryEndingLon = params.get("lrlon");
        double queryStartingLat = params.get("ullat");
        double queryEndingLat = params.get("lrlat");

        double queryLonDpp = calculateDpp(queryStartingLon, queryEndingLon, params.get("w"));
        int depth = findDepth(queryLonDpp);
        double rasterLonDpp = calculateDpp(MapServer.ROOT_ULLON, MapServer.ROOT_LRLON, MapServer.TILE_SIZE * Math.pow(2, depth));
        double rasterLatDpp = calculateDpp(MapServer.ROOT_ULLAT, MapServer.ROOT_LRLAT, MapServer.TILE_SIZE * Math.pow(2, depth));

        int startingXTile = findTile(depth, rasterLonDpp, queryStartingLon, CoordinateType.LONGITUDE);
        int endingXTile = findTile(depth, rasterLonDpp, queryEndingLon, CoordinateType.LONGITUDE);
        int startingYTile = findTile(depth, rasterLatDpp, queryStartingLat, CoordinateType.LATITUDE);
        int endingYTile = findTile(depth, rasterLatDpp, queryEndingLat, CoordinateType.LATITUDE);

        double rasterUllon = findTileCornerCoordinate(startingXTile, rasterLonDpp, CoordinateType.LONGITUDE, TileCorner.UL);
        double rasterLrlon = findTileCornerCoordinate(endingXTile, rasterLonDpp, CoordinateType.LONGITUDE, TileCorner.LR);
        double rasterUllat = findTileCornerCoordinate(startingYTile, rasterLatDpp, CoordinateType.LATITUDE, TileCorner.UL);
        double rasterLrlat = findTileCornerCoordinate(endingYTile, rasterLatDpp, CoordinateType.LATITUDE, TileCorner.LR);

        Map<String, Object> results = new HashMap<>();
        System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
                           + "your browser.");
        System.out.println("depth: " + String.valueOf(depth));
        System.out.println("starting X tile: " + String.valueOf(startingXTile));
        System.out.println("ending X tile: " + String.valueOf(endingXTile));
        System.out.println("starting Y tile: " + String.valueOf(startingYTile));
        System.out.println("ending Y tile: " + String.valueOf(endingYTile));
        System.out.println("raster ullon: " + String.valueOf(rasterUllon));
        System.out.println("raster lrlon: " + String.valueOf(rasterLrlon));
        System.out.println("raster ullat: " + String.valueOf(rasterUllat));
        System.out.println("raster lrlat: " + String.valueOf(rasterLrlat));
        return results;
    }

    public double calculateDpp(double startingCoordinate, double endingCoordinate, double width) {
        return Math.abs(startingCoordinate - endingCoordinate) / width;
    }

    public int findDepth(double queryLonDpp) {
        double estimatedDepth = Math.log(MAX_LON_DPP / queryLonDpp) / Math.log(2);
        Double depth = Math.ceil(estimatedDepth);

        if (depth <= MIN_DEPTH) {
            return MIN_DEPTH;
        }

        if (depth >= MAX_DEPTH) {
            return MAX_DEPTH;
        }

        return depth.intValue();
    }

    public int findTile(int depth, double rasterDpp, double queryCoordinate, CoordinateType coordinateType) {
        int maxTile = (int) Math.pow(2, depth) - 1;

        double startingCoordinate = MapServer.ROOT_ULLON;
        int direction = LON_DIRECTION;

        if (coordinateType == CoordinateType.LATITUDE) {
            startingCoordinate = MapServer.ROOT_ULLAT;
            direction = LAT_DIRECTION;
        }

        double estimatedTile = (queryCoordinate - startingCoordinate) * direction / rasterDpp / MapServer.TILE_SIZE;
        Double tile = Math.floor(estimatedTile);

        if (tile <= MIN_TILE) {
            return MIN_TILE;
        }

        if (tile >= maxTile) {
            return maxTile;
        }

        return tile.intValue();
    }

    public double findTileCornerCoordinate(int tile, double rasterDpp, CoordinateType coordinateType, TileCorner tileCorner) {
        double startingCoordinate = MapServer.ROOT_ULLON;
        int direction = LON_DIRECTION;

        if (coordinateType == CoordinateType.LATITUDE) {
            startingCoordinate = MapServer.ROOT_ULLAT;
            direction = LAT_DIRECTION;
        }

        double coordinate = startingCoordinate + (direction * tile * rasterDpp * MapServer.TILE_SIZE);

        if (tileCorner == TileCorner.LR) {
            coordinate = coordinate + (direction * rasterDpp * MapServer.TILE_SIZE);
        }

        return coordinate;
    }

    private enum CoordinateType {
        LONGITUDE,
        LATITUDE
    }

    private enum TileCorner {
        UL,
        LR
    }
}

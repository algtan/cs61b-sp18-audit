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
    private final int LONG_DIRECTION = 1;
    private final int LAT_DIRECTION = -1;
    private final double MAX_LONG_DPP = calculateDpp(MapServer.ROOT_ULLON, MapServer.ROOT_LRLON, MapServer.TILE_SIZE);

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
        double queryStartingLong = params.get("ullon");
        double queryEndingLong = params.get("lrlon");
        double queryStartingLat = params.get("ullat");
        double queryEndingLat = params.get("lrlat");

        double queryLongDpp = calculateDpp(queryStartingLong, queryEndingLong, params.get("w"));
        int depth = findDepth(queryLongDpp);
        double rasterLongDpp = calculateDpp(MapServer.ROOT_ULLON, MapServer.ROOT_LRLON, MapServer.TILE_SIZE * Math.pow(2, depth));
        double rasterLatDpp = calculateDpp(MapServer.ROOT_ULLAT, MapServer.ROOT_LRLAT, MapServer.TILE_SIZE * Math.pow(2, depth));

        int startingXTile = findTile(depth, rasterLongDpp, queryStartingLong, CoordinateType.LONGITUDE);
        int endingXTile = findTile(depth, rasterLongDpp, queryEndingLong, CoordinateType.LONGITUDE);
        int startingYTile = findTile(depth, rasterLatDpp, queryStartingLat, CoordinateType.LATITUDE);
        int endingYTile = findTile(depth, rasterLatDpp, queryEndingLat, CoordinateType.LATITUDE);

        Map<String, Object> results = new HashMap<>();
        System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
                           + "your browser.");
        System.out.println("depth: " + String.valueOf(depth));
        System.out.println("starting X tile: " + String.valueOf(startingXTile));
        System.out.println("ending X tile: " + String.valueOf(endingXTile));
        System.out.println("starting Y tile: " + String.valueOf(startingYTile));
        System.out.println("ending Y tile: " + String.valueOf(endingYTile));
        return results;
    }

    public double calculateDpp(double startingCoordinate, double endingCoordinate, double width) {
        return Math.abs(startingCoordinate - endingCoordinate) / width;
    }

    public int findDepth(double queryLongDpp) {
        double estimatedDepth = Math.log(MAX_LONG_DPP / queryLongDpp) / Math.log(2);
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
        int direction = LONG_DIRECTION;

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

    private enum CoordinateType {
        LONGITUDE,
        LATITUDE
    }
}

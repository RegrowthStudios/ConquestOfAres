package Game;
import java.util.*;

/**
 * Created by brb55_000 on 2/6/2015.
 */
public class Territory {

    public enum TerrainType {
        Ocean,
        Grassland,
        Mountain,
        Desert,
        Forest
    }

    public Vector<Territory> neighbors = new Vector<Territory>(); ///< Pointers to neighbor territories
    public Army army = null; ///< Pointer to residing army
    public Player owner = null; ///< Owning player
    public int power = 0; ///< Power of the territory
    public float x; ///< x coordinate of center
    public float y; ///< y coordinate of center
    public float height; ///< Terrain height value
    public TerrainType terrainType;
}

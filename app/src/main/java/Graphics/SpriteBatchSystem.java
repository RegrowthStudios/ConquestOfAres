package Graphics;

import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

import Game.Unit;

/**
 * Created by Nathan on 1/12/2015.
 */
public class SpriteBatchSystem {
    public enum BufferType{
        Vertices, TextureCoordinates, Colors
    }

    public static Vector sprites = new Vector();

    public static class sprite{
        int texture;
        FloatBuffer vBuf;
        FloatBuffer tBuf;
        ByteBuffer cBuf;
    }

    public static void Initialize(int count){
        GeometryHelper.initializeMaster();
        GeometryHelper.initializeSoldier(count);
    }

    public static void addSprite(String name, Quadrilateral quadrilateral, int rid){
        GeometryHelper.addToBatch(quadrilateral, name);
        if(!(sprites.contains(name))){
            sprites.add(name);
        }
    }

    public static void clear() {
        sprites.clear();
        GeometryHelper.clear();
    }

    public static sprite getSprite(String name){
        sprite s = new sprite();

        s.texture = TextureHelper.getTexture(name);
        s.vBuf = GeometryHelper.getVertBuff(name);
        s.tBuf = GeometryHelper.getTextBuff(name);
        s.cBuf = GeometryHelper.getColorBuff(name);

        return s;
    }

    public static void addUnit(Unit.Type type, float x, float y, byte[] color){
        Quadrilateral quad = new Quadrilateral();
        switch (type){
            case soldier:
                quad = Quadrilateral.getQuad(quad, x, y, 0, .1f, .1f, color);
                addSprite("soldier", quad, TextureHelper.getTexture("soldier"));
        }
    }
}

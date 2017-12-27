package com.osreboot.ld38;

import static com.osreboot.ridhvl.painter.painter2d.HvlPainter2D.hvlDrawQuadc;

import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlCoord2D;
import com.osreboot.ridhvl.HvlMath;

public class Tile {

	public enum State{
		WHITE, BLACK, NONE
	}

	public int x, y;
	public State state;
	public float size = 1f;

	public Tile(int xArg, int yArg, State stateArg){
		x = xArg;
		y = yArg;
		state = stateArg;
	}

	public void draw(float delta, float x, float y, boolean cursor){
		float dist = (float)Math.pow(Math.abs(1f - size), 0.8f);
		size = HvlMath.stepTowards(size, delta*dist*4f, 1f);
		if(cursor){
			hvlDrawQuadc(x, y, Main.TILE_SIZE*size, Main.TILE_SIZE*size, Main.getTexture(Main.TEX_CURSOR));
		}else if(state == State.WHITE){
			hvlDrawQuadc(x, y, Main.TILE_SIZE*size, Main.TILE_SIZE*size, Main.getTexture(Main.TEX_TILE));
			hvlDrawQuadc(x, y, Main.TILE_SIZE*size, Main.TILE_SIZE*size, Main.getTexture(Main.TEX_TILEC), Color.black);
		}else if(state == State.BLACK){
			hvlDrawQuadc(x, y, Main.TILE_SIZE*size, Main.TILE_SIZE*size, Main.getTexture(Main.TEX_TILE), Color.black);
			hvlDrawQuadc(x, y, Main.TILE_SIZE*size, Main.TILE_SIZE*size, Main.getTexture(Main.TEX_TILEC));
		}
	}

	public HvlCoord2D getScreenCoords(){
		return new HvlCoord2D((1280f/2f) + (((float)x) * Main.TILE_SIZE), (720f/2f) + (((float)y) * Main.TILE_SIZE));
	}

}

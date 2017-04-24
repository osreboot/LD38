package com.osreboot.ld38;

import static com.osreboot.ridhvl.painter.painter2d.HvlPainter2D.hvlDrawQuadc;

import java.util.ArrayList;

import com.osreboot.ridhvl.HvlCoord;

public class Group {

	public ArrayList<Tile> tiles = new ArrayList<Tile>();
	public HvlCoord center;

	public Group(Group gArg){
		center = gArg.center;
		tiles = gArg.tiles;
	}
	
	public Group(HvlCoord centerArg, ArrayList<Tile> tilesArg){
		center = centerArg;
		tiles = tilesArg;
	}

	public void draw(float delta, float xArg, float yArg, boolean cursor){
		hvlDrawQuadc(xArg, yArg, 4, 4, Main.getTexture(Main.TEX_DOT));
		for(Tile t : tiles) t.draw(delta, 
				xArg + ((float)t.x * Main.TILE_SIZE) - (center.x * Main.TILE_SIZE),
				yArg + ((float)t.y * Main.TILE_SIZE) - (center.y * Main.TILE_SIZE), cursor);
	}

	public ArrayList<HvlCoord> getDrawLocations(float xArg, float yArg){
		ArrayList<HvlCoord> output = new ArrayList<>();
		for(Tile t : tiles){
			output.add(new HvlCoord(
					xArg + ((float)t.x * Main.TILE_SIZE) - (center.x * Main.TILE_SIZE),
					yArg + ((float)t.y * Main.TILE_SIZE) - (center.y * Main.TILE_SIZE)));
		}
		return output;
	}

}

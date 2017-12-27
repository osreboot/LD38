package com.osreboot.ld38;

import static com.osreboot.ridhvl.painter.painter2d.HvlPainter2D.hvlDrawLinec;
import static com.osreboot.ridhvl.painter.painter2d.HvlPainter2D.hvlDrawQuadc;

import java.util.ArrayList;
import java.util.Arrays;

import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlCoord2D;
import com.osreboot.ridhvl.HvlMath;
import com.osreboot.ridhvl.painter.HvlCursor;

public class Game {

	public static final HvlCoord2D LOC_SPAWNER = new HvlCoord2D((1280f/2f)-(1280f/4f), 720f/2f);

	//score elements

	public static final float SCORE_SECONDS = 1f, 
			SCORE_CLEAR = 40f;

	public float score = 0f;

	public ArrayList<ScoreParticle> scoreParticles = new ArrayList<>();
	//end score elements

	public boolean isRunning = false, soundStart = false;
	public int level = 1;
	public float totalTime = -5f, levelTime = 0f, spawnCooldown = 0f, queueOffset = 0f, effectZoom = 1f;

	public ArrayList<Tile> tiles = new ArrayList<>();
	public ArrayList<Group> groups = new ArrayList<>();
	public ArrayList<Group> groupQueue = new ArrayList<>();
	public Group groupCurrent;

	public HvlCoord2D cursorLoc = new HvlCoord2D(LOC_SPAWNER), cursorLocGoal = new HvlCoord2D(LOC_SPAWNER);

	public Game(){
		initializeGroups();
		for(int x = -4; x <= 4; x++){
			for(int y = -4; y <= 4; y++){
				tiles.add(new Tile(x, y, HvlMath.distance(x, y, 0, 0) <= 4.2f ? Tile.State.WHITE : 
					HvlMath.distance(x, y, 0, 0) <= 4.3f ? Tile.State.BLACK : Tile.State.NONE));
			}
		}
	}

	public void addScore(float addArg, boolean levelArg, boolean display){
		float add = addArg*getPercentCoverage()*((float)level)*
				(levelArg ? ((20f-levelTime)/20f) : 1);
		score += add;
		if(display) scoreParticles.add(new ScoreParticle(add));
	}

	public void onClick(){
		boolean changed = false;
		if(isMoveOk(cursorLocGoal)){
			for(HvlCoord2D c : groupCurrent.getDrawLocations(cursorLocGoal.x, cursorLocGoal.y)){
				for(Tile t : tiles){
					if(c.equals(new HvlCoord2D(t.getScreenCoords().x, t.getScreenCoords().y))){
						if(t.state != Tile.State.NONE){
							if(t.state == Tile.State.BLACK) t.state = Tile.State.WHITE;
							else if(t.state == Tile.State.WHITE) t.state = Tile.State.BLACK;
							changed = true;
							t.size = 1.2f;
						}
					}
				}
			}
		}
		if(changed){
			cursorLoc = new HvlCoord2D(LOC_SPAWNER);
			groupCurrent = new Group(groupQueue.get(0));
			groupQueue.remove(0);
			populateQueue();
			queueOffset = 1f;
			Main.getSound(Main.SND_PLACE).playAsSoundEffect(1f, 0.5f, false);
		}
	}

	public void draw(float delta, Main main){
		if(isRunning) totalTime += delta;
		for(Tile t : tiles) t.draw(delta, t.getScreenCoords().x, t.getScreenCoords().y, false);
		if(isRunning && totalTime > 0){
			float distQueue = (float)Math.pow(queueOffset, 0.8f);
			queueOffset = HvlMath.stepTowards(queueOffset, delta*distQueue*10, 0);
			HvlCoord2D newCursorLocGoal = new HvlCoord2D(
					(Math.round((HvlCursor.getCursorX() - (1280f/2f))/Main.TILE_SIZE)*Main.TILE_SIZE) + (1280f/2f), 
					(Math.round((HvlCursor.getCursorY() - (720f/2f))/Main.TILE_SIZE)*Main.TILE_SIZE) + (720f/2f));
			if(!cursorLocGoal.equals(newCursorLocGoal)/* && isMoveOk(newCursorLocGoal)*/) cursorLocGoal = newCursorLocGoal;
			float dist = (float)Math.pow(HvlMath.distance(cursorLocGoal, cursorLoc), 0.8f);
			cursorLoc = new HvlCoord2D(
					HvlMath.stepTowards(cursorLoc.x, delta*dist*50, cursorLocGoal.x),
					HvlMath.stepTowards(cursorLoc.y, delta*dist*50, cursorLocGoal.y));

			//gameplay elements
			if(getPercentCoverage() == 1f){
				for(Tile t : tiles){
					if(t.x == 0 && t.y == 3){
						t.state = Tile.State.BLACK;
						t.size = 0.5f;
					}
					if(t.x == 0 && t.y == -3){
						t.state = Tile.State.BLACK;
						t.size = 0.5f;
					}
				}
				level++;
				effectZoom = 1f;
				Main.getSound(Main.SND_LEVEL).playAsSoundEffect(1f, 0.5f, false);
				addScore(SCORE_CLEAR, true, true);
				levelTime = 0f;
			}
			levelTime += delta;
			spawnCooldown = HvlMath.stepTowards(spawnCooldown, delta, 0);
			if(spawnCooldown == 0){
				Tile t = getWhiteAdjacentTiles().get(HvlMath.randomInt(getWhiteAdjacentTiles().size()));
				t.state = Tile.State.BLACK;
				t.size = 0.5f;
				spawnCooldown = 10f/(((float)level) + ((float)getWhiteAdjacentTiles().size()/10f));
			}
			if(levelTime > 20f){
				levelTime = 0f;
				level++;
				effectZoom = 1f;
				Main.getSound(Main.SND_LEVEL).playAsSoundEffect(1f, 0.5f, false);
			}
			if(totalTime > 0 && !soundStart){
				soundStart = true;
				Main.getSound(Main.SND_START).playAsSoundEffect(1f, 1f, false);
			}
			//score elements
			addScore(delta*SCORE_SECONDS, false, false);
			if(getPercentCoverage() < Main.SCORE_LOSS) main.fail();
		}
		drawProgressBar(delta);
		Main.font.drawWordc("level " + level, 1280/2,  720*29/32, 
				level >= 30 ? Color.black : level >= 20 ? Color.red : level >= 10 ? Color.orange : Color.white,
						0.5f);
		if(effectZoom < 2f && totalTime > 0f){
			effectZoom += delta;
			Main.font.drawWordc("level " + level, 1280/2,  720*29/32,
					level >= 30 ? new Color(0f, 0f, 0f, 1f-effectZoom + 0.8f) : level >= 20 ? new Color(1f, 0f, 0f, 1f-effectZoom + 0.8f) : 
						level >= 10 ? new Color(1f, 0.5f, 0f, 1f-effectZoom + 0.8f) : new Color(1f, 1f, 1f, 1f-effectZoom + 0.8f), 0.5f*effectZoom);
		}
		Main.font.drawWordc(Math.round(totalTime*100f)/100f + "s", 1280/2, 720*31/32, 
				totalTime >= 360 ? Color.black : totalTime >= 240 ? Color.red : totalTime >= 120 ? Color.orange : Color.white,
						0.25f);
		Main.font.drawWordc(Math.round(score*10f)/10f + "", 1280/2,  720*4/32, 
				score >= 9000 ? Color.black : score >= 6000 ? Color.red : score >= 3000 ? Color.orange : Color.white,
						0.5f);
		if(totalTime < 0 && isRunning) Main.font.drawWordc(-(int)totalTime + 1 + "", 1280/2,  720/2, Color.black, 1f);
		for(ScoreParticle p : scoreParticles) if(p.life < ScoreParticle.DRAW_LIFE) p.draw(delta);
	}

	public void drawCursor(float delta){
		for(int i = 0; i < 5; i++){
			groupQueue.get(i).draw(delta, LOC_SPAWNER.x - (192f*i) - (queueOffset*192f), LOC_SPAWNER.y, true);
		}
		if(isRunning){
			groupCurrent.draw(delta, cursorLoc.x, cursorLoc.y, true);
		}
	}

	private boolean isMoveOk(HvlCoord2D goalArg){
		boolean output = false;
		for(HvlCoord2D c : groupCurrent.getDrawLocations(goalArg.x, goalArg.y)){
			for(Tile t : tiles){
				if(c.equals(new HvlCoord2D(t.getScreenCoords().x, t.getScreenCoords().y)) && t.state != Tile.State.NONE){
					output = true;
				}
			}
		}
		return output;
	}

	public void initializeGroups(){
		groups.add(new Group(new HvlCoord2D(1, 1), new ArrayList<Tile>(Arrays.asList(new Tile[]{
				new Tile(0, 0, Tile.State.WHITE),
				new Tile(1, 1, Tile.State.WHITE),
		}))));
		groups.add(new Group(new HvlCoord2D(0, 1), new ArrayList<Tile>(Arrays.asList(new Tile[]{
				new Tile(0, 1, Tile.State.WHITE),
				new Tile(1, 0, Tile.State.WHITE),
		}))));
		groups.add(new Group(new HvlCoord2D(0, 1), new ArrayList<Tile>(Arrays.asList(new Tile[]{
				new Tile(0, 0, Tile.State.WHITE),
				new Tile(0, 1, Tile.State.WHITE),
		}))));
		groups.add(new Group(new HvlCoord2D(1, 0), new ArrayList<Tile>(Arrays.asList(new Tile[]{
				new Tile(0, 0, Tile.State.WHITE),
				new Tile(1, 0, Tile.State.WHITE),
		}))));
		for(int i = 0; i < 10; i++){
			populateQueue();
		}
		groupCurrent = new Group(groupQueue.get(0));
	}

	public void populateQueue(){
		groupQueue.add(new Group(groups.get(HvlMath.randomInt(groups.size()))));
	}

	public float currentProgress = 0.5f;

	public void drawProgressBar(float delta){
		float dist = (float)Math.pow(Math.abs(currentProgress - ((getPercentCoverage() - Main.SCORE_LOSS)/Main.SCORE_LOSS)), 0.8f);
		currentProgress = HvlMath.stepTowards(currentProgress, delta*dist*10, Math.max(((getPercentCoverage() - Main.SCORE_LOSS)/Main.SCORE_LOSS), 0));
		Color color = currentProgress > 0.75f ? Color.white : 
			currentProgress > 0.5f ? Color.orange : 
				currentProgress > 0.25f ? Color.red : Color.black;
		hvlDrawQuadc(1280f/2f, 720f/16f, currentProgress*512, 20, color);
		hvlDrawLinec((1280f/2f) + 256f, (720f/16f) - 18, 0, 16, Color.white, 1);
		hvlDrawLinec((1280f/2f) - 256f, (720f/16f) - 18, 0, 16, Color.white, 1);
		hvlDrawLinec((1280f/2f), (720f/16f) - 18, 0, 16, Color.red, 1);
		hvlDrawLinec(1280f/2f, (720f/16f) - 10, 512, 0, Color.gray, 2);
	}

	public ArrayList<Tile> getRealTiles(){
		ArrayList<Tile> output = new ArrayList<>();
		for(Tile t : tiles) if(t.state != Tile.State.NONE) output.add(t);
		return output;
	}

	public ArrayList<Tile> getWhiteAdjacentTiles(){
		ArrayList<Tile> black = new ArrayList<>();
		for(Tile t : tiles) if(t.state == Tile.State.BLACK) black.add(t);
		ArrayList<Tile> output = new ArrayList<>();
		for(Tile w : black){
			for(Tile t : tiles){
				if(t.state == Tile.State.WHITE &&
						((t.x == w.x && t.y == w.y + 1) ||
								(t.x == w.x && t.y == w.y - 1) ||
								(t.x == w.x + 1 && t.y == w.y) ||
								(t.x == w.x - 1 && t.y == w.y))) output.add(t);
			}
		}
		return output;
	}

	public float getPercentCoverage(){
		float wCount = 0;
		float tCount = 0;
		for(Tile t : tiles){
			if(t.state == Tile.State.WHITE) wCount++;
			if(t.state != Tile.State.NONE) tCount++;
		}
		return wCount/tCount;
	}

}

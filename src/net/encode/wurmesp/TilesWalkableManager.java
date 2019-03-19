package net.encode.wurmesp;

import java.util.Iterator;

import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.backend.Queue;

import net.encode.wurmesp.util.RenderUtils;

public class TilesWalkableManager {
	private Queue _queuePick;
	private World _world;
	public boolean _first = true;
	
	public TilesWalkableManager(){
		
	}
	
	public void _setWQ(World world, Queue queue) {
		this._world = world;
		this._queuePick = queue;
	}
	
	public void _refreshData() {
		WurmEspMod._closeByWalkableTerrain.clear();
		WurmEspMod._terrainBuffer2 = this._world.getNearTerrainBuffer();
		
		PlayerPosition pos = this._world.getPlayer().getPos();
		
		int px = pos.getTileX();
		int py = pos.getTileY();
		int size = 6;
		int sx = px - size / 2;
		int sy = py - size / 2;
		
		final float ox = this._world.getRenderOriginX();
        final float oy = this._world.getRenderOriginY();
		
		for (int x = 0; x < size+1; x++) {
			for (int y = 0; y < size+1; y++) {
				float tileX = x + sx;
				float tileY = y + sy;
				
				float curX = tileX * 4 - ox;
				float curY = tileY * 4 - oy;
				float nextX = (tileX + 1) * 4 - ox;
				float nextY = (tileY + 1) * 4 - oy;

				float x0 = curX + 0.2F;
				float y0 = curY + 0.2F;
				float x1 = nextX - 0.2F;
				float y1 = nextY - 0.2F;
				
				float z0 = WurmEspMod._terrainBuffer2.getHeight((int)tileX, (int)tileY);
				float z1 = WurmEspMod._terrainBuffer2.getHeight((int)tileX +1, (int)tileY);
				float z2 = WurmEspMod._terrainBuffer2.getHeight((int)tileX, (int)tileY +1);
				float z3 = WurmEspMod._terrainBuffer2.getHeight((int)tileX +1, (int)tileY +1);
				
				WurmEspMod._closeByWalkableTerrain.add(new float[]{
						x0,z0,y0,
						x1,z1,y0,
						x0,z2,y1,
						x1,z3,y1
						});
			}
		}
	}
	public void _queueTiles() {
		if(WurmEspMod._closeByWalkableTerrain == null)
		{
			return;
		}
		for (Iterator<float[]> iterator = WurmEspMod._closeByWalkableTerrain.iterator();iterator.hasNext();) {
			float[] t = (float[]) iterator.next();
			
			if(isNotRideable(t))
			{
				float[] color = new float[]{1.0F, 0.0F, 0.0F, 0.5F};
				int[] indexdata = new int[] { 1, 0, 0, 2, 2, 3, 3, 1};
				
				RenderUtils.renderPrimitiveLines(4, t, indexdata, this._queuePick, color);
			}
		}
	}

	private boolean isNotRideable(float[] tile) {
		return getTileSteepness(tile)[1] >= WurmEspMod.tilenotrideable;
	}
	
	public static short[] getTileSteepness(float[] tile)
	  {
	    short highest = -100;
	    short lowest = 32000;
	    for (int i = 1; i <= 10; i+=3) {
	    	  short height = 0;
	          height = (short) (tile[i]*10);
	          if (height > highest) {
	            highest = height;
	          }
	          if (height < lowest) {
	            lowest = height;
	          }
	      }
	    int med = (highest + lowest) / 2;
	    return new short[] { (short)med, (short)(highest - lowest) };
	  }
}

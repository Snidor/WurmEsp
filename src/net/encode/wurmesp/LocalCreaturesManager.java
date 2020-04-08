package net.encode.wurmesp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.gui.CreatureWindow;
import com.wurmonline.client.renderer.gui.CreatureWindow.LocalCreatureTreeListItem;

public class LocalCreaturesManager 
{
	private World _world;
	private CreatureWindow mCreatureWindow;
	public boolean _first = true;
	
	private long timelapse;
	private long time;
	private long remaining;
	private long future;
	@SuppressWarnings("unused")
	private long last;
	
	public static Logger logger = Logger.getLogger("LocalCreatureManager");
	
	public LocalCreaturesManager(){
		
	}
	
	public void _setWW(World world, CreatureWindow pWindow) 
	{
		this._world = world;
		this.mCreatureWindow = pWindow;
	}
	
	public void _refreshData() {
		PlayerPosition lPos = this._world.getPlayer().getPos();
		
		mCreatureWindow.setWorld( _world );
		
		int lPX = lPos.getTileX();
		int lPY = lPos.getTileY();
		Map<Long, LocalCreatureTreeListItem> lMap = mCreatureWindow.getCreatureMap();
		Set <Long> lSet = lMap.keySet();
		List<Long> lIdList = new ArrayList<>(lSet);
		
		for ( int i = 0; i < lIdList.size(); i ++ )
		{
			if (  ( ( Math.sqrt( Math.pow( ( lMap.get( lIdList.get( i ) ).mCX - lMap.get( lIdList.get( i ) ).mPX ), 2) ) > 90 ) || ( Math.sqrt( Math.pow( ( lMap.get( lIdList.get( i ) ).mCY - lMap.get( lIdList.get( i ) ).mPY ), 2) ) > 90 ) ) ||
					( _world.getServerConnection().getServerConnectionListener().findCreature( lIdList.get( i ) ) == null ) )
			{
				mCreatureWindow.removeEntry( lIdList.get( i ) );
			}
			else if ( ( lPX != lMap.get( lIdList.get( i ) ).mPX ) || ( lPY != lMap.get( lIdList.get( i ) ).mPY ) )
			{
				lMap.get( lIdList.get( i ) ).mPX = lPX;
				lMap.get( lIdList.get( i ) ).mPY = lPY;
			}
		}
		mCreatureWindow.sortList();
	}

	public void restart( long pTime ) 
	{
		this.timelapse = pTime;
		this.time = System.currentTimeMillis();
		this.future = time + this.timelapse;
		this.remaining = ( this.future - System.currentTimeMillis() ) / 1000;
		this.last = this.remaining;
	}
	
	public boolean hasEnded()
	{
		return ( System.currentTimeMillis() > this.future ) ? true : false;
	}
}

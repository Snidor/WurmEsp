package com.wurmonline.client.renderer.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.client.GameCrashedException;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;

public class CreatureWindow extends WWindow 
{
    private Map<Long, LocalCreatureTreeListItem> mLocalCreaturesMap;
    private LocalCreatureList mList;
    private TargetWindow mTargetWindow;
    public static boolean mColoredText = true;
    
    private World mWorld;
    private Map<Long, CreatureCellRenderable> mCreatures;
    
	public static Logger logger = Logger.getLogger("CreatureWindow");

    public CreatureWindow( TargetWindow pTargetWindow ) 
    {
        super( "Local Creature Window" );
        this.setTitle( "Local Creatures" );
        int[] colWidths = new int[]{ 60, 60, 35, 80, 30, 30};
        String[] colNames = new String[]{ "Condition","Age", "Gender", "Color", "Dir","Dis" };
        this.mLocalCreaturesMap = new HashMap<>();
        this.mList = new LocalCreatureList("Local Creature", colWidths, colNames);
        this.setComponent( this.mList );
        this.closeable = true;
        this.mTargetWindow = pTargetWindow;
    }

    public void addToLocalCreatureList( long pId, String pName, String pAge, char pGender, String pColorString, int pCX, int pCY, int pPX, int pPY, CreatureCellRenderable pCCR, Color pColor, String pCondition ) 
    {
    	
        if ( !this.mLocalCreaturesMap.containsKey( pId ) ) 
        {
        	LocalCreatureTreeListItem lItem = new LocalCreatureTreeListItem( pId, pName, pAge, pGender, pColorString, pCX, pCY, pPX, pPY, pCCR, pColor, pCondition );
            this.mLocalCreaturesMap.put( pId, lItem );
            this.mList.addTreeListItem(lItem, null);
        }
        else
        {
        	if ( ( this.mWorld != null ) && ( mWorld.getServerConnection().getServerConnectionListener().getCreatures().get( pId ) != null ) )
        	{
        		this.mList.getNode( mLocalCreaturesMap.get( pId ) ).item.setCX( ( (int)mWorld.getServerConnection().getServerConnectionListener().getCreatures().get( pId ).getXPos() / 4 ) );
        		this.mList.getNode( mLocalCreaturesMap.get( pId ) ).item.setCY( ( (int)mWorld.getServerConnection().getServerConnectionListener().getCreatures().get( pId ).getYPos() / 4 ) );
        		this.mList.getNode( mLocalCreaturesMap.get( pId ) ).item.setPX( pPX );
        		this.mList.getNode( mLocalCreaturesMap.get( pId ) ).item.setPY( pPY );
        		this.mLocalCreaturesMap.get( pId ).mCX = ( (int)mWorld.getServerConnection().getServerConnectionListener().getCreatures().get( pId ).getXPos() / 4 );
        		this.mLocalCreaturesMap.get( pId ).mCY = ( (int)mWorld.getServerConnection().getServerConnectionListener().getCreatures().get( pId ).getYPos() / 4 );
        		this.mLocalCreaturesMap.get( pId ).mPX = ( pPX );
        		this.mLocalCreaturesMap.get( pId ).mPY = ( pPY );
        	}
        }
        
        if ( mColoredText )
        {
        	this.mList.getNode( mLocalCreaturesMap.get( pId ) ).item.setCustomColor( pColor.r, pColor.g, pColor.b );
        	this.mList.getNode( mLocalCreaturesMap.get( pId ) ).item.setCustomSecondaryColor( pColor.r, pColor.g, pColor.b );        	
        }
    }

    void closePressed() 
    {
    	hud.hideComponent( this );
    }
    
    public class LocalCreatureList extends WurmTreeList<LocalCreatureTreeListItem> 
    {
    	private int sortColumn, sortOrder;

    	LocalCreatureList( String _name, int[] colWidths, String[] colNames ) 
    	{
            super(_name, colWidths, colNames);
            sortColumn = 1;
            sortOrder = -1;
        }	
    	
        @SuppressWarnings("rawtypes")
		@Override
        public void buttonClicked(WButton button) {
            if (button instanceof WurmTreeList.TreeListButton) {
                this.setSort(((WurmTreeList.TreeListButton) button).index);
            }
        }

        void reSort() {
            getNode(null).sortOn(sortColumn, sortOrder);
            recalcLines();
        }

        void setSort(int newSort) {
            if (sortColumn == newSort) {
                sortOrder = -sortOrder;
            } else {
                sortColumn = newSort;
                sortOrder = 4;
            }
            reSort();
        }

        @Override
        protected void leftPressed(int xMouse, int yMouse, int clickCount) {
            CreatureWindow.this.rightPressed(xMouse, yMouse, clickCount);
        }
        
        public LocalCreatureList getCreaturesTree()
        {
        	return this;
        }
    }
    
    public LocalCreatureList getCreatureList()
    {
    	return this.mList;
    }
    
    public Map<Long, LocalCreatureTreeListItem> getCreatureMap()
    {
    	return this.mLocalCreaturesMap;
    }

    public class LocalCreatureTreeListItem
    extends TreeListItem {
        private final long mId;
        private final String mName;
        private final String mAge;
        private final String mGender;
        private final String mColorString;
        private final String mCondition;
        private final CreatureCellRenderable mCCR;
        private Color mColor;
        public int mCX;
        public int mCY;
        public int mPX;
        public int mPY;
        public int mDistance = 0;
        public String mDirection;

        LocalCreatureTreeListItem( long pId, String pName, String pAge, char pGender, String pColorString, int pCX, int pCY, int pPX, int pPY, CreatureCellRenderable pCCR, Color pColor, String pCondition ) 
        {
            this.mId = pId;
            this.mName = pName;
            this.mAge = pAge;
            this.mGender = Character.toString( pGender );
            this.mColorString = pColorString;
            this.mCX = pCX;
            this.mCY = pCY;
            this.mPX = pPX;
            this.mPY = pPY;
            this.mCCR = pCCR;
            this.mColor = pColor;
            this.mCondition = pCondition;
        }

        String getName() 
        {
            return this.mName;
        }

        public long getId() 
        {
            return this.mId;
        }

        public void getHoverDescription( PickData pData ) 
        {
        	pData.addText( this.mAge + " " +this.mName );
        }

        String getParameter(int param) 
        {
            switch (param) 
            {
            	case -1:
            	{
            		return mName;
            	}
            	case 0:
            	{
            		return mCondition;
            	}
                case 1: 
                {
                    return mAge;
                }
                case 2: 
                {
                    return mGender;
                }
                case 3: 
                {
                    return mColorString;
                }
                case 4: 
                {
                	mDirection = "";
                	boolean lOnPlayer = true;
                    if ( mCY < mPY )
                    {
                    	mDirection += "N";
                    	lOnPlayer = false;
                    }
                    else if ( mCY > mPY )
                    {
                    	mDirection += "S";
                    	lOnPlayer = false;
                    }
                    if ( mCX < mPX )
                    {
                    	mDirection += "W";
                    	lOnPlayer = false;
                    }
                    else if ( mCX > mPX )
                    {
                    	mDirection += "E";
                    	lOnPlayer = false;
                    }
                    if ( lOnPlayer == true )
                    {
                    	mDirection += "X";
                    }

                    return mDirection;
                }
                case 5:
                {
                	int lX = mCX - mPX;
                	int lY = mCY - mPY;
                	if ( ( lX == 0 ) || ( lY == 0 ) )
                	{
                		mDistance = (int)Math.sqrt( Math.pow( ( lX + lY ), 2) );
                	}
                	else
                	{
                    	mDistance = (int)Math.sqrt( Math.pow( ( lX ), 2) + Math.pow( ( lY ), 2) );
                	}
                	return Integer.toString( mDistance );
                }
            }
            return "Test2";
        }

        int compareTo( TreeListItem pItem, int pSortOn ) 
        {
//            logger.log(Level.INFO, "DEBUG COMPARE: " + pSortOn);
            if ( !( pItem instanceof LocalCreatureTreeListItem ) ) 
            {
                GameCrashedException.warn( ( String ) "DEBUG ERROR 1" );
                return 0;
            }
            LocalCreatureTreeListItem pOther = ( LocalCreatureTreeListItem ) pItem;
            switch ( pSortOn ) 
            {
                case -2: 
                {
                	String lTmp = this.mName + this.mCondition;
                	String lTest = pOther.mName + pOther.mCondition;
                	return lTmp.compareToIgnoreCase( lTest );
                }
                case -1: 
                {
                	return Long.toString( this.mId ).compareToIgnoreCase( Long.toString( pOther.mId ) );  
                }
                case 0:
                {
                	return this.mCondition.compareToIgnoreCase( pOther.mCondition );
                }
                case 1: 
                {
                	return this.mAge.compareToIgnoreCase( pOther.mAge );
                }
                case 2: 
                {
                	return this.mGender.compareToIgnoreCase( pOther.mGender );
                }
                case 3: 
                {
                	return this.mColorString.compareToIgnoreCase( pOther.mColorString );
                }
                case 4: 
                {
                	return this.mDirection.compareToIgnoreCase( pOther.mDirection );
                }
                case 5: 
                {
                	return Integer.compare( this.mDistance, pOther.mDistance );
                }
            }
            return 0;
        }
        
        public void setCX( int pCX )
        {
        	this.mCX = pCX;
        }
        
        public void setCY( int pCY )
        {
        	this.mCY = pCY;
        }
        
        public void setPX( int pPX )
        {
        	this.mPX = pPX;
        }
        
        public void setPY( int pPY )
        {
        	this.mPY = pPY;
        }
        
        @Override
        void leftClick(int xMouse, int yMouse) {
            CreatureWindow.this.leftPressed(xMouse, yMouse, 0);
        }
    }
    
    @Override
    protected void leftPressed(int xMouse, int yMouse, int clickCount) 
    {
    	LocalCreatureTreeListItem lCreature = mList.getSelections().get( 0 );
    	mTargetWindow.setTarget( new Long( lCreature.getId() ), "Test", lCreature.mCCR );
    }
    
	public void removeEntry( long pListId )
    {
		if ( this.mLocalCreaturesMap.containsKey( pListId ) )
		{
			this.mList.removeTreeListItem( mLocalCreaturesMap.get( pListId ) );
			this.mLocalCreaturesMap.remove( pListId );
		}
    }
	
	public void sortList()
	{
		mList.reSort();
	}
	
	public void setWorld( World pWorld )
	{
		mWorld = pWorld;
		getCreatures();
	}
	
	public void getCreatures()
	{
		mCreatures = mWorld.getServerConnection().getServerConnectionListener().getCreatures();		
		logger.log(Level.INFO, "DEBUG number of creatures:" + mCreatures.size() );
	}
	
	public void setColoredText( boolean pColored )
	{
		mColoredText = pColored;
	}
}
package com.wurmonline.client.renderer.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.client.GameCrashedException;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;

public class CreatureWindow extends WWindow 
{
    private final Map<Long, LocalCreatureTreeListItem> mLocalCreaturesMap;
    private final LocalCreatureList mList;
    private final TargetWindow mTargetWindow;
	public static Logger logger = Logger.getLogger("CreatureWindow");

    public CreatureWindow( TargetWindow pTargetWindow ) 
    {
        super( "Local Creature Window" );
        this.setTitle( "Local Creatures" );
        int[] colWidths = new int[]{ 60, 35, 80, 30, 30};
        String[] colNames = new String[]{ "Age", "Gender", "Color", "Dir","Dis" };
        this.mLocalCreaturesMap = new HashMap<>();
        this.mList = new LocalCreatureList("Local Creature", colWidths, colNames);
        this.setComponent( this.mList );
        this.closeable = true;
        this.mTargetWindow = pTargetWindow;
    }

    public void addToLocalCreatureList( long pId, String pName, String pAge, char pGender, String pColor, int pCX, int pCY, int pPX, int pPY, CreatureCellRenderable pCCR ) 
    {
        if ( !this.mLocalCreaturesMap.containsKey( pId ) ) 
        {
        	LocalCreatureTreeListItem lItem = new LocalCreatureTreeListItem( pId, pName, pAge, pGender, pColor, pCX, pCY, pPX, pPY, pCCR );
            this.mLocalCreaturesMap.put( pId, lItem );
            this.mList.addTreeListItem(lItem, null);
        }
        else
        {
    		logger.log(Level.INFO, "DEBUG UPDATE MODEL POS:" + pId );
    		this.mList.getNode( mLocalCreaturesMap.get( pId ) ).item.setCX( pCX );
    		this.mList.getNode( mLocalCreaturesMap.get( pId ) ).item.setCY( pCY );
    		this.mLocalCreaturesMap.get( pId ).mCX = pCX;
    		this.mLocalCreaturesMap.get( pId ).mCY = pCY;
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
        private final String mColor;
        private final CreatureCellRenderable mCCR;
        public int mCX;
        public int mCY;
        public int mPX;
        public int mPY;
        public String mDirection;
        public String mDistance = " ";

        LocalCreatureTreeListItem( long pId, String pName, String pAge, char pGender, String pColor, int pCX, int pCY, int pPX, int pPY, CreatureCellRenderable pCCR ) 
        {
            this.mId = pId;
            this.mName = pName;
            this.mAge = pAge;
            this.mGender = Character.toString( pGender );
            this.mColor = pColor;
            this.mCX = pCX;
            this.mCY = pCY;
            this.mPX = pPX;
            this.mPY = pPY;
            this.mCCR = pCCR;
            logger.log(Level.INFO, "DEBUG NEW ITEM:" + this.mId);
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
                    return mAge;
                }
                case 1: 
                {
                    return mGender;
                }
                case 2: 
                {
                    return mColor;
                }
                case 3: 
                {
                	mDirection = "";
                    if ( mCY < mPY )
                    {
                    	mDirection += "N";
                    }
                    else if ( mCY > mPY )
                    {
                    	mDirection += "S";
                    }
                    if ( mCX < mPX )
                    {
                    	mDirection += "W";
                    }
                    else if ( mCX > mPX )
                    {
                    	mDirection += "E";
                    }

                    return mDirection;
                }
                case 4:
                {
                	mDistance = Integer.toString( (int)Math.sqrt( Math.pow( ( mCX - mPX ), 2) + ( mCY - mPY ) ) );
                	return mDistance;
                }
            }
            return "Test2";
        }

        int compareTo( TreeListItem pItem, int pSortOn ) 
        {
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
                    return Long.toString( this.mId ).compareToIgnoreCase( Long.toString( pOther.mId ) ); 
                }
                case -1: 
                {
                    return this.mName.compareToIgnoreCase( pOther.mName ); 
                }
                case 0: 
                {
                	return this.mAge.compareToIgnoreCase( pOther.mAge );
                }
                case 1: 
                {
                	return this.mGender.compareToIgnoreCase( pOther.mGender );
                }
                case 2: 
                {
                	return this.mColor.compareToIgnoreCase( pOther.mColor );
                }
                case 3: 
                {
                	return this.mDirection.compareToIgnoreCase( pOther.mDirection );
                }
                case 4: 
                {
                	return this.mDistance.compareToIgnoreCase( pOther.mDistance );
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
}
package net.encode.wurmesp;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.PickRenderer;
import com.wurmonline.client.renderer.PickRenderer.CustomPickFillDepthRender;
import com.wurmonline.client.renderer.PickRenderer.CustomPickFillRender;
import com.wurmonline.client.renderer.PickRenderer.CustomPickOutlineRender;
import com.wurmonline.client.renderer.PickableUnit;
import com.wurmonline.client.renderer.backend.Primitive;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.backend.RenderState;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;

import net.encode.wurmesp.WurmEspMod.SEARCHTYPE;

public class Unit {
	private long id;
	private String modelName;
	private String hoverName;
	private String mColorName;
	private char mGender;
	private float mX;
	private float mY;
	private Color mColor;
	
	private PickableUnit pickableUnit;
	private float[] color = new float[]{0.0f,0.0f,0.0f};
	private float[] conditionedcolor = new float[]{0.0f,0.0f,0.0f};
	private String condition;
	
	public static float[] colorPlayers = {0.0f, 0.0f, 0.0f};
	public static float[] colorPlayersEnemy = {0.0f, 0.0f, 0.0f};
	public static float[] colorMobs = {0.0f, 0.0f, 0.0f};
	public static float[] colorMobsAggro = {0.0f, 0.0f, 0.0f};
	public static float[] colorSpecials = {0.0f, 0.0f, 0.0f};
	public static float[] colorSpotted = {0.0f, 0.0f, 0.0f};
	public static float[] colorUniques = {0.0f, 0.0f, 0.0f};
	public static float[] colorAlert = {0.0f, 0.0f, 0.0f};
	public static float[] colorAngry = {0.0f, 0.0f, 0.0f};
	public static float[] colorChampion = {0.0f, 0.0f, 0.0f};
	public static float[] colorDiseased = {0.0f, 0.0f, 0.0f};
	public static float[] colorFierce = {0.0f, 0.0f, 0.0f};
	public static float[] colorGreenish = {0.0f, 0.0f, 0.0f};
	public static float[] colorHardened = {0.0f, 0.0f, 0.0f};
	public static float[] colorLurking = {0.0f, 0.0f, 0.0f};
	public static float[] colorRaging = {0.0f, 0.0f, 0.0f};
	public static float[] colorScared = {0.0f, 0.0f, 0.0f};
	public static float[] colorSlow = {0.0f, 0.0f, 0.0f};
	public static float[] colorSly = {0.0f, 0.0f, 0.0f};
	
	public static Logger logger = Logger.getLogger("UNIT");
	
	public static String[] aggroMOBS;
	
	public static String[] uniqueMOBS;
	
	public static String[] specialITEMS;
	
	public static String[] spottedITEMS;
	
	public static String[] conditionedMOBS;
	
	public Unit(long id, PickableUnit pickableUnit, String modelName, String hoverName, int pX, int pY )
	{
		this.id = id;
		this.pickableUnit = pickableUnit;
		this.modelName = modelName;
		this.hoverName = hoverName;
		this.mColorName = getColorName( modelName );
		this.mGender = getGender( modelName );
		this.mX = pX;
		this.mY = pY;
		this.mColor = Color.WHITE;
		
		this.determineColor();
	}
	
	public long getId()
	{
		return this.id;
	}
	
	public PickableUnit getPickableUnit()
	{
		return this.pickableUnit;
	}
	
	public float[] getColor()
	{
		return this.color;
	}
	
	public float[] getConditionedColor()
	{
		return this.conditionedcolor;
	}
	
	public String getHoverName()
	{
		return this.hoverName;
	}
	
	public String getModelName()
	{
		return this.modelName;
	}
	
	public boolean isPlayer()
	{
		return (this.getModelName().contains("model.creature.humanoid.human.player") && !this.getModelName().contains("zombie"));
	}
	
	public boolean isEnemyPlayer()
	{
		return this.getModelName().contains("ENEMY");
	}
	
	public boolean isMob()
	{
		return this.getModelName().contains("model.creature") && !this.getModelName().contains("humanoid.human");
	}
	
	public boolean isAggroMob()
	{
		for(String mob : aggroMOBS)
		{
			if(this.getHoverName().contains(mob))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isChampion() {
		if(this.getHoverName().contains("champion"))
		{
			return true;
		}
		return false;
	}
	
	public boolean isConditioned()
	{
		for(String condition : conditionedMOBS)
		{
			if(this.getHoverName().contains(condition))
			{
				this.condition = condition;
				return true;
			}
		}
		return false;
	}
	
	public boolean isUnique()
	{
		for(String mob : uniqueMOBS)
		{
			if(this.getHoverName().contains(mob))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isSpecial()
	{
		for(String item : specialITEMS)
		{
			if(this.getHoverName().contains(item))
			{
				return true;
			}
			if(this.getModelName().contains(WurmEspMod.search))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isSpotted()
	{
		for(String item : spottedITEMS)
		{
			if(this.getHoverName().contains(item))
			{
				return true;
			}
			if(this.getModelName().contains(WurmEspMod.search))
			{
				return true;
			}
		}
		if(WurmEspMod.searchType == SEARCHTYPE.HOVER)
		{
			if(this.getHoverName().contains(WurmEspMod.search))
			{
				return true;
			}
		}else if(WurmEspMod.searchType == SEARCHTYPE.MODEL)
		{
			if(this.getModelName().contains(WurmEspMod.search))
			{
				return true;
			}
		}else if(WurmEspMod.searchType == SEARCHTYPE.BOTH)
		{
			if(this.getHoverName().contains(WurmEspMod.search))
			{
				return true;
			}else if(this.getModelName().contains(WurmEspMod.search))
			{
				return true;
			}
		}
		return false;
	}
	
	public Color getCol()
	{
		return mColor;
	}
	
	private void determineColor()
	{
		if(this.isPlayer())
		{
			if(!this.isEnemyPlayer())
			{
				this.color = colorPlayers;
			}
			else
			{
				this.color = colorPlayersEnemy;
			}
		}
		else if(this.isUnique())
		{
			this.color = colorUniques;
		}
		else if(this.isAggroMob())
		{
			this.color = colorMobsAggro;
		}
		else if(this.isMob())
		{
			this.color = colorMobs;
		}
		else if(this.isSpecial())
		{
			this.color = colorSpecials;
		}
		else if(this.isSpotted())
		{
			this.color = colorSpotted;
		}
		if(this.isConditioned())
		{
			float[] color = new float[] {0.0f, 0.0f, 0.0f};
			switch(this.condition) {
				case "alert":
					color = colorAlert;
					break;
				case "angry":
					color = colorAngry;
					break;
				case "champion":
					color = colorChampion;
					break;
				case "diseased":
					color = colorDiseased;
					break;
				case "fierce":
					color = colorFierce;
					break;
				case "greenish":
					color = colorGreenish;
					break;
				case "hardened":
					color = colorHardened;
					break;
				case "lurking":
					color = colorLurking;
					break;
				case "raging":
					color = colorRaging;
					break;
				case "scared":
					color = colorScared;
					break;
				case "slow":
					color = colorSlow;
					break;
				case "sly":
					color = colorSly;
					break;
			}
						
			
			this.conditionedcolor = color;
		}
	}
	
	private String getColorName( String pModelName )
	{
		String lReturnString = "";
		if ( pModelName.contains( "horse" ) || pModelName.contains( "foal" ) ) 
		{
            if ( pModelName.contains( ".hell" ) ) 
            {
                if ( pModelName.contains( ".cinder" ) ) lReturnString = "Cinder";
                else if ( pModelName.contains( ".envious" ) ) lReturnString = "Envious";
                else if ( pModelName.contains( ".shadow" ) ) lReturnString = "Shadow";
                else if ( pModelName.contains( ".pestilential" ) ) lReturnString = "Pestilential";
                else if ( pModelName.contains( ".nightshade" ) ) lReturnString = "Nightshade";
                else if ( pModelName.contains( ".incandescent" ) ) lReturnString = "Incandescent";
                else if ( pModelName.contains( ".molten" ) ) lReturnString = "Molten";
                else lReturnString= "Ash";
            }
            else 
            {
                if ( pModelName.contains(".brown" ) ) lReturnString = "Brown";
                else if ( pModelName.contains( ".skewbaldpinto" ) ) lReturnString = "Skewbald pinto";
                else if ( pModelName.contains( ".goldbuckskin" ) ) lReturnString = "Gold buckskin";
                else if ( pModelName.contains( ".blacksilver" ) ) lReturnString = "Black silver";
                else if ( pModelName.contains( ".appaloosa" ) ) lReturnString = "Appaloosa";
                else if ( pModelName.contains( ".chestnut" ) ) lReturnString = "Chestnut";
                else if ( pModelName.contains( ".gold" ) ) lReturnString = "Gold";
                else if ( pModelName.contains( ".black" ) ) lReturnString = "Black";
                else if ( pModelName.contains( ".white" ) ) lReturnString = "White";
                else if ( pModelName.contains( ".piebaldpinto" ) ) lReturnString = "Piebald Pinto";
                else if ( pModelName.contains( ".bloodbay" ) ) lReturnString = "Blood Bay";
                else if ( pModelName.contains( ".ebonyblack" ) ) lReturnString = "Ebony";
                else lReturnString= "Gray";
            }
		}
		else if ( pModelName.contains( "sheep" ) )
		{
			if ( pModelName.contains( ".black" ) ) lReturnString = "Black";
			else lReturnString = "White";
		}
		else if ( pModelName.contains( ".hen" ) || pModelName.contains( ".rooster" ) )
		{
			if ( pModelName.contains( ".brown" ) ) lReturnString = "Brown";
			if ( pModelName.contains( ".black" ) ) lReturnString = "Black";
			else lReturnString = "White";
		}
		return lReturnString;
	}
	
	public String getColorName()
	{
		return this.mColorName;
	}
	
	private char getGender( String pModelName )
	{
		char lReturnChar = 'M';
		if ( pModelName.contains( "female" ) ) lReturnChar = 'F';
		return lReturnChar;
	}
	
	public char getGender()
	{
		return this.mGender;
	}
	
	public float getX()
	{
		return this.mX;
	}
	
	public float getY()
	{
		return this.mY;
	}
	
	public CreatureCellRenderable getCCR()
	{
		return (CreatureCellRenderable)this.pickableUnit;
	}
	
	public String getAge()
	{
		if ( hoverName.startsWith( "Young" ) ) return "Young";
		else if ( hoverName.startsWith( "Adolescent" ) ) return "Adolescent";
		else if ( hoverName.startsWith( "Mature" ) ) return "Mature";
		else if ( hoverName.startsWith( "Aged" ) ) return "Aged";
		else if ( hoverName.startsWith( "Old" ) ) return "Old";
		else if ( hoverName.startsWith( "Venerable" ) ) return "Venerable";
		else return "";
	}
	
	public String getCondition()
	{
		if ( this.isConditioned() )
		{
			return condition;
		}
		else
		{
			return " ";
		}
	}
	
	public String getCreature()
	{
		String lCreature = "Placeholder";
		
		if ( modelName.contains( "quadraped" ) )
		{
			lCreature = modelName.substring( 25 );
			if ( lCreature.contains( "horse.hell" ) )
			{
				lCreature = "hell horse";
			}
			else
			{
				lCreature = lCreature.substring( 0, lCreature.indexOf( "." ) );				
			}
		}
		else if ( modelName.contains( "humanoid" ) )
		{
			lCreature = modelName.substring( 24 );
			lCreature = lCreature.substring( 0, lCreature.indexOf( "." ) );
		}
		else if ( modelName.contains( "multiped" ) )
		{
			if ( modelName.contains( "spider" ) )
			{
				if ( modelName.contains( "huge" ) ) lCreature = "huge spider";
				if ( modelName.contains( "lava" ) ) lCreature = "lava spider";
				if ( modelName.contains( "fog" ) ) lCreature = "fog spider";				
			}
			else if ( modelName.contains( "scorpion" ) )
			{
				if ( modelName.contains( "hell" ) ) lCreature = "hell scorpion";
				else lCreature = "scorpion";
			}
		}
		
		if ( lCreature.length() > 0 )
		{
			lCreature = lCreature.substring(0, 1).toUpperCase() + lCreature.substring(1);			
		}
		
		return lCreature;
	}
	
	public void renderUnit(Queue queue, boolean showconditioned) {
		if (this.pickableUnit == null) {
			return;
		}
		
	    float br = 3.5F;
	    
	    RenderState renderStateFill = new RenderState();
	    RenderState renderStateFillDepth = new RenderState();
	    RenderState renderStateOutline = new RenderState();
	    Color color = new Color();
	    if(this.isConditioned() && showconditioned)
	    {
	    	color.set(this.conditionedcolor[0], this.conditionedcolor[1], this.conditionedcolor[2]);
	    }
	    else
	    {
	    	color.set(this.color[0], this.color[1], this.color[2]);
	    }
	    
	    color.a = br;
	    
	    renderStateFill.alphaval = color.a;
	    color.a *= this.pickableUnit.getOutlineColor().a;
	    
	    PickRenderer tmp1217_1214 = WurmEspMod._pickRenderer;
	    CustomPickFillRender customPickFill = tmp1217_1214.new CustomPickFillRender();
	    
	    PickRenderer tmp1237_1234 = WurmEspMod._pickRenderer;
	    CustomPickFillDepthRender customPickFillDepth = tmp1237_1234.new CustomPickFillDepthRender();
	    
	    PickRenderer tmp1257_1254 = WurmEspMod._pickRenderer;
	    CustomPickOutlineRender customPickOutline = tmp1257_1254.new CustomPickOutlineRender();
	      
	    renderStateFill.twosided = false;
	    renderStateFill.depthtest = Primitive.TestFunc.ALWAYS;
	    renderStateFill.depthwrite = true;
	    renderStateFill.customstate = customPickFill;
	    
	    this.pickableUnit.renderPicked(queue, renderStateFill, color);
	    
	    color.a = (br * 0.25F);
	    renderStateOutline.alphaval = color.a;
	    color.a *= this.pickableUnit.getOutlineColor().a;
	    
	    renderStateOutline.twosided = false;
	    renderStateOutline.depthtest = Primitive.TestFunc.LESS;
	    renderStateOutline.depthwrite = false;
	    renderStateOutline.blendmode = Primitive.BlendMode.ALPHABLEND;
	    renderStateOutline.customstate = customPickOutline;
	    
	    this.pickableUnit.renderPicked(queue, renderStateOutline, color);
	    
	    renderStateFillDepth.customstate = customPickFillDepth;
	    renderStateFillDepth.depthtest = Primitive.TestFunc.ALWAYS;
	    this.pickableUnit.renderPicked(queue, renderStateFillDepth, color);
	    mColor = color;
	}
}

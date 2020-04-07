package net.encode.wurmesp;

import java.awt.Color;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
//import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import com.wurmonline.client.comm.SimpleServerConnectionClass;
import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.GroundItemData;
import com.wurmonline.client.renderer.PickRenderer;
import com.wurmonline.client.renderer.PickableUnit;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.cell.CellRenderable;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.cell.GroundItemCellRenderable;
import com.wurmonline.client.renderer.gui.CreatureWindow;
import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.MainMenu;
import com.wurmonline.client.renderer.gui.TargetWindow;
import com.wurmonline.client.renderer.gui.WurmComponent;
import com.wurmonline.client.renderer.gui.WurmEspWindow;
import com.wurmonline.client.settings.SavePosManager;
import com.wurmonline.client.sound.FixedSoundSource;
import com.wurmonline.client.sound.SoundSource;
import com.wurmonline.mesh.Tiles;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import net.encode.wurmesp.util.ConfigUtils;

public class WurmEspMod implements WurmClientMod, Initable, PreInitable, Configurable {
	public static HeadsUpDisplay hud;

	public static Logger logger = Logger.getLogger("WurmEspMod");
	public static Properties modProperties = new Properties();
	private List<Unit> pickableUnits = new ArrayList<Unit>();
	private List<Unit> toRemove = new ArrayList<Unit>();
	
	private CronoManager xrayCronoManager;
	private CronoManager tilesCloseByCronoManager;
	private CronoManager tilesHighlightCronoManager;
	private CronoManager tilesCloseByWalkableCronoManager;
	private LocalCreaturesManager mCreatureCronoManager;
	private XRayManager xrayManager;
	private TilesCloseByManager tilesCloseByManager;
	public static TilesHighlightManager tilesHighlightManager;
	private TilesWalkableManager tilesCloseByWalkableManager;
	private CreatureWindow mCreatureWindow;
	
	public static CaveDataBuffer _caveBuffer = null;
	public static NearTerrainDataBuffer _terrainBuffer = null;
	public static NearTerrainDataBuffer _terrainBuffer2 = null;
	public static List<float[]> _terrain = new ArrayList<float[]>();
	public static List<float[]> _closeByTerrain = new ArrayList<float[]>();
	public static List<float[]> _closeByWalkableTerrain = new ArrayList<float[]>();
	public static List<int[]> _tilesHighlightBase = new ArrayList<int[]>();
	public static List<float[]> _tilesHighlightTerrain = new ArrayList<float[]>();
	//public static List<float[]> _oreql = new ArrayList<float[]>();

	public static enum SEARCHTYPE {
		NONE, HOVER, MODEL, BOTH
	};

	public static String search = "defaultnosearch";
	public static SEARCHTYPE searchType = SEARCHTYPE.NONE;

	public static boolean players = true;
	public static boolean mobs = false;
	public static boolean specials = true;
	public static boolean items = true;
	public static boolean uniques = true;
	public static boolean conditioned = true;
	public static boolean tilescloseby = false;
	public static boolean deedsize = false;
	public static boolean tileshighlight = false;
	public static boolean tilesclosebynotrideable = false;
	public static boolean xray = false;
	public static boolean xraythread = true;
	public static boolean xrayrefreshthread = true;
	public static int xraydiameter = 32;
	public static int xrayrefreshrate = 5;
	public static int tilenotrideable = 40;
	public static boolean playsoundspecial = true;
	public static boolean playsounditem = true;
	public static boolean playsoundunique = true;
	public static String soundspecial = "sound.fx.conch";
	public static String sounditem = "sound.fx.conch";
	public static String soundunique = "sound.fx.conch";
	public static boolean conditionedcolorsallways = false;
	public static boolean championmcoloralways = false;
	/*
	public static boolean xrayshowql = true;
	public static int xrayqldiameter = 6;
	public static int serversize = 1024;
	*/
	public static PickRenderer _pickRenderer;

	public static boolean handleInput(final String cmd, final String[] data) {
		if (cmd.equals("esp")) {
			if (data.length == 2) {

				switch (data[1]) {
				case "players":
					players = !players;
					hud.consoleOutput("ESP players changed to: " + Boolean.toString(players));
					break;
				case "mobs":
					mobs = !mobs;
					hud.consoleOutput("ESP mobs changed to: " + Boolean.toString(mobs));
					break;
				case "specials":
					specials = !specials;
					hud.consoleOutput("ESP specials changed to: " + Boolean.toString(specials));
					break;
				case "uniques":
					uniques = !uniques;
					hud.consoleOutput("ESP uniques changed to: " + Boolean.toString(uniques));
					break;
				case "conditioned":
					conditioned = !conditioned;
					hud.consoleOutput("ESP champions changed to: " + Boolean.toString(conditioned));
					break;
				case "xray":
					xray = !xray;
					hud.consoleOutput("ESP xray changed to: " + Boolean.toString(xray));
					break;
				case "tilescloseby":
					tilescloseby = !tilescloseby;
					hud.consoleOutput("ESP tilescloseby changed to: " + Boolean.toString(tilescloseby));
					break;
				case "deedsize":
					deedsize = !deedsize;
					hud.consoleOutput("ESP deedsize changed to: " + Boolean.toString(deedsize));
					break;
				case "search":
					hud.consoleOutput("Usage: esp search {h/m/hm/off} <name>");
					break;
				case "planner":
					hud.consoleOutput("Usage: esp planner {n/s/e/w} <tiles> <times> <space>");
					hud.consoleOutput("Usage: esp planner square <startX> <startY> <endX> <endY>");
					hud.consoleOutput("Usage: esp planner square <radius>");
					hud.consoleOutput("Usage: esp planner tile <tileX> <tileY>");
					hud.consoleOutput("Usage: esp planner clear");
					break;
				case "reload":
					ConfigUtils.loadProperties("wurmesp");
					DoConfig(modProperties);
					hud.consoleOutput("[WurmEspMod] Config Reloaded");
					break;
				default:
					hud.consoleOutput("Usage: esp {players|mobs|specials|uniques|conditioned|xray|tilescloseby|deedsize|reload}");
				}
				return true;
			} else if (data.length > 2) {

				switch (data[1]) {
				case "search":
					if (data[2].equals("h")) {
						search = data[3];
						searchType = SEARCHTYPE.HOVER;
						hud.consoleOutput("Searching for " + search + " in HoverName");
					} else if (data[2].equals("m")) {
						search = data[3];
						searchType = SEARCHTYPE.MODEL;
						hud.consoleOutput("Searching for " + search + " in ModelName");
					} else if (data[2].equals("hm")) {
						search = data[3];
						searchType = SEARCHTYPE.BOTH;
						hud.consoleOutput("Searching for " + search + " in HoverName and ModelName");
					} else if (data[2].equals("off")) {
						search = "";
						searchType = SEARCHTYPE.NONE;
						hud.consoleOutput("Searching off");
					} else {
						hud.consoleOutput("Usage: esp search {h/m/hm/off} <name>");
					}
					break;
				case "planner":
					if(data.length == 3 && data[2].equals("clear")) {
						_tilesHighlightBase.clear();
						tileshighlight = false;
						hud.consoleOutput("Planner data cleared.");
						break;
					}
					else if(data.length == 3 && data[2].equals("tile"))
					{
						PlayerPosition pos = WurmEspMod.hud.getWorld().getPlayer().getPos();
						int tileX = pos.getTileX();
						int tileY = pos.getTileY();
						
						tilesHighlightManager._addData(tileX, tileY);
						
						tileshighlight = true;
						
						hud.consoleOutput("Added planner data. [TileX: "+String.valueOf(tileX)+"][tileY: "+String.valueOf(tileY)+"]");
					}
					else if(data.length == 4 && data[2].equals("square")) {
						int radius = Integer.parseInt(data[3]);
						
						tilesHighlightManager._addData(radius);
						
						tileshighlight = true;
						
						hud.consoleOutput("Added planner data. [radius: "+data[3]+"]");
						break;
					}
					else if (data.length == 5 && data[2].equals("tile")) {
						int tileX = Integer.parseInt(data[3]);
						int tileY = Integer.parseInt(data[4]);
						
						tilesHighlightManager._addData(tileX, tileY);
						
						tileshighlight = true;
						
						hud.consoleOutput("Added planner data. [TileX: "+data[3]+"][tileY: "+data[4]+"]");
					}
					else if (data.length == 6 && "nsew".contains(data[2])) {
						String direction = data[2];
						int tiles = Integer.parseInt(data[3]);
						int times = Integer.parseInt(data[4]);
						int space = Integer.parseInt(data[5]);
						
						tilesHighlightManager._addData(direction, tiles, times, space);
						
						tileshighlight = true;
						
						hud.consoleOutput("Added planner data. [direction: "+direction+"][tiles: "+data[3]+"][times: "+data[4]+"][space: "+data[5]+"]");
					}
					else if (data.length == 7 && data[2].equals("square")) {
						int startX = Integer.parseInt(data[3]);
						int startY = Integer.parseInt(data[4]);
						int endX = Integer.parseInt(data[5]);
						int endY = Integer.parseInt(data[6]);
						
						tilesHighlightManager._addData(startX, startY, endX, endY);
						
						tileshighlight = true;
						
						hud.consoleOutput("Added planner data. [startX: "+data[3]+"][startY: "+data[4]+"][endX: "+data[5]+"][endY: "+data[6]+"]");
					}
					else {
						hud.consoleOutput("Usage: esp planner {n/s/e/w} <tiles> <times> <space>");
						hud.consoleOutput("Usage: esp planner square <startX> <startY> <endX> <endY>");
						hud.consoleOutput("Usage: esp planner square <radius>");
						hud.consoleOutput("Usage: esp planner tile <tileX> <tileY>");
						hud.consoleOutput("Usage: esp planner clear");
					}
					break;
				default:
					hud.consoleOutput("Error.");
				}
				return true;
			} else {
				hud.consoleOutput("Error.");
			}
			return true;
		}
		return false;
	}

	private static float[] colorStringToFloatA(String color) {
		String[] colors = color.split(",");
		float[] colorf = { Float.valueOf(colors[0]) / 255.0f, Float.valueOf(colors[1]) / 255.0f,
				Float.valueOf(colors[2]) / 255.0f };
		return colorf;
	}

	private static String colorFloatAToString(float[] color) {
		String colors = String.valueOf(color[0] * 255.0f) + "," + String.valueOf(color[1] * 255.0f) + ","
				+ String.valueOf(color[2] * 255.0f);
		return colors;
	}
	
	@Override
	public void configure(Properties properties) {
		DoConfig(properties);

		logger.log(Level.INFO, "[WurmEspMod] Config loaded");
	}

	@SuppressWarnings("unused")
	@Override
	public void init() {
		
		logger.log(Level.INFO, "[WurmEspMod] Initializing");

		try {
			xrayManager = new XRayManager();
			tilesCloseByManager = new TilesCloseByManager();
			tilesHighlightManager = new TilesHighlightManager();
			tilesCloseByWalkableManager = new TilesWalkableManager();
			xrayCronoManager = new CronoManager(xrayrefreshrate*1000);
			tilesCloseByCronoManager = new CronoManager(1000);
			tilesHighlightCronoManager = new CronoManager(5000);
			tilesCloseByWalkableCronoManager = new CronoManager(1000);
			mCreatureCronoManager = new LocalCreaturesManager();
			
			ClassPool classPool = HookManager.getInstance().getClassPool();

			CtClass ctWurmConsole = classPool.getCtClass("com.wurmonline.client.console.WurmConsole");
			ctWurmConsole.getMethod("handleDevInput", "(Ljava/lang/String;[Ljava/lang/String;)Z")
					.insertBefore("if (net.encode.wurmesp.WurmEspMod.handleInput($1,$2)) return true;");
			logger.log(Level.INFO, "[WurmEspMod] Return inserted on handleDevInput");
			
			CtClass ctWurmArrow = classPool.getCtClass("com.wurmonline.client.renderer.cell.ProjectileCellRenderable");
			CtMethod m = CtNewMethod.make("public void initialize() { return; }", ctWurmArrow);
			ctWurmArrow.addMethod(m);
			logger.log(Level.INFO, "[WurmEspMod] Added method initialize on ProjectileCellRenderable");
			
			HookManager.getInstance().registerHook("com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V",
					() -> (proxy, method, args) -> {
						method.invoke(proxy, args);
						hud = (HeadsUpDisplay) proxy;
						this.initEspWR();
						return null;
					});
			logger.log(Level.INFO, "[WurmEspMod] HeadsUpDisplay.init hooked");
			
			HookManager.getInstance().registerHook("com.wurmonline.client.renderer.WorldRender", "renderPickedItem",
					"(Lcom/wurmonline/client/renderer/backend/Queue;)V", () -> (proxy, method, args) -> {
						method.invoke(proxy, args);
						Class<?> cls = proxy.getClass();

						World world = ReflectionUtil.getPrivateField(proxy, ReflectionUtil.getField(cls, "world"));

						PickRenderer pickRenderer = ReflectionUtil.getPrivateField(proxy,
								ReflectionUtil.getField(cls, "pickRenderer"));
						_pickRenderer = pickRenderer;

						Queue queuePick = ReflectionUtil.getPrivateField(proxy,
								ReflectionUtil.getField(cls, "queuePick"));
						
						for (Unit unit : this.pickableUnits) {
							if ((players && unit.isPlayer()) || (uniques && unit.isUnique())
									|| (conditioned && unit.isConditioned()) || (mobs && unit.isMob())
									|| (specials && unit.isSpecial() || (items && unit.isSpotted()))) {
								if((unit.isConditioned() && conditioned) || (unit.isConditioned() && conditionedcolorsallways) || (unit.isChampion() && championmcoloralways))
								{
									unit.renderUnit(queuePick, true);
								}
								else
								{
									unit.renderUnit(queuePick, false);
								}
								if ( unit.isMob() )
								{
//						    		logger.log(Level.INFO, "DEBUG CALL UNIT:" + unit.getId());
									mCreatureWindow.addToLocalCreatureList( unit.getId() , unit.getCreature(), unit.getAge(), unit.getGender(), unit.getColorName(), (int)unit.getX(), (int)unit.getY(), (int)world.getPlayer().getPos().getTileX(), (int)world.getPlayer().getPos().getTileY(), unit.getCCR() );									
								}
								else if ( unit.isPlayer() )
								{
//									if ( unit.getHoverName().contains( "Sumsum" ) )
//									{
//										long lE = 2757842558976L;
//										logger.log(Level.INFO, "DEBUG ElfinX:" + lE );
//										logger.log(Level.INFO, "DEBUG ElfinX:" + world.getServerConnection().getServerConnectionListener().findCreature(unit.getId()).getXPos() );
//										logger.log(Level.INFO, "DEBUG ElfinY:" + world.getServerConnection().getServerConnectionListener().findCreature(unit.getId()).getYPos() );
//										world.getWorldRenderer().toggleFreeCamera();
//									}
								}
							}
						}
						
						mCreatureCronoManager._setWW( world, mCreatureWindow );
						if ( mCreatureCronoManager._first )
						{
							mCreatureCronoManager._refreshData();
							mCreatureCronoManager._first = false;
						}
						else if ( mCreatureCronoManager.hasEnded() )
						{
							mCreatureCronoManager._refreshData();
							mCreatureCronoManager.restart(1000);
						}
						Thread lCreatureThread = new Thread(() -> {
							mCreatureCronoManager._refreshData();
						});
						
						if (tileshighlight) {
							tilesHighlightManager._setWQ(world,queuePick);
							
							if(tilesHighlightManager._first)
							{ 
								tilesHighlightManager._refreshData();
								tilesHighlightManager._first = false;
							}
							else if(tilesHighlightCronoManager.hasEnded())
							{
								tilesHighlightManager._refreshData();
								tilesHighlightCronoManager.restart(5000);
							}
							
							
							Thread tilesHighlightThread = new Thread(() -> {
								tilesHighlightManager._queueTilesHighlight();
							});
							
							tilesHighlightThread.setPriority(Thread.MAX_PRIORITY);
							
							tilesHighlightThread.start();
						}else {
							tilesHighlightManager._setW(world);
						}
						if (tilescloseby && world.getPlayer().getPos().getLayer() >= 0) {
							tilesCloseByManager._setWQ(world,queuePick);
							
							if(tilesCloseByManager._first)
							{ 
								tilesCloseByManager._refreshData();
								tilesCloseByManager._first = false;
							}
							else if(tilesCloseByCronoManager.hasEnded())
							{
								tilesCloseByManager._refreshData();
								tilesCloseByCronoManager.restart(1000);
							}
							
							
							Thread tilesThread = new Thread(() -> {
								tilesCloseByManager._queueTiles();
							});
							
							tilesThread.setPriority(Thread.MAX_PRIORITY);
							
							tilesThread.start();
						}
						if(tilesclosebynotrideable && world.getPlayer().getPos().getLayer() >= 0)
						{
							tilesCloseByWalkableManager._setWQ(world, queuePick);
							
							if(tilesCloseByWalkableManager._first)
							{ 
								tilesCloseByWalkableManager._refreshData();
								tilesCloseByWalkableManager._first = false;
							}
							else if(tilesCloseByWalkableCronoManager.hasEnded())
							{
								tilesCloseByWalkableManager._refreshData();
								tilesCloseByWalkableCronoManager.restart(1000);
							}
							
							
							Thread tilesWalkableThread = new Thread(() -> {
								tilesCloseByWalkableManager._queueTiles();
							});
							
							tilesWalkableThread.setPriority(Thread.MAX_PRIORITY);
							
							tilesWalkableThread.start();
						}
						if (xray && world.getPlayer().getPos().getLayer() < 0) {
							xrayManager._setWQ(world,queuePick);
							
							if(xrayManager._first)
							{ 
								if(xrayrefreshthread)
								{
									Thread refreshThread = new Thread(() -> {
										xrayManager._refreshData();
									});
									
									refreshThread.setPriority(Thread.MAX_PRIORITY);
									
									refreshThread.start();
								}
								else
								{
									xrayManager._refreshData();
								}
								xrayManager._first = false;
							}
							else if(xrayCronoManager.hasEnded())
							{
								if(xrayrefreshthread)
								{
									Thread refreshThread = new Thread(() -> {
										xrayManager._refreshData();
									});
									
									refreshThread.setPriority(Thread.MAX_PRIORITY);
									
									refreshThread.start();
								}
								else
								{
									xrayManager._refreshData();
								}
								xrayCronoManager.restart(xrayrefreshrate*1000);
							}
							
							
							if(xraythread)
							{
								Thread xrayThread = new Thread(() -> {
									xrayManager._queueXray();
								});
								
								xrayThread.setPriority(Thread.MAX_PRIORITY);
								
								xrayThread.start();
							}
							else
							{
								xrayManager._queueXray();
							}
						}

						return null;
					});
			logger.log(Level.INFO, "[WurmEspMod] WorldRender.renderPickedItem hooked");
			
			HookManager.getInstance().registerHook("com.wurmonline.client.renderer.cell.MobileModelRenderable",
					"initialize", "()V", () -> (proxy, method, args) -> {
						method.invoke(proxy, args);
						
						PickableUnit pUnit = (PickableUnit) proxy;
						
						Unit unit = new Unit(pUnit.getId(), pUnit, ((CreatureCellRenderable)proxy).getModelName().toString(),((CreatureCellRenderable)proxy).getHoverName(),(int)(((CreatureCellRenderable)proxy).getXPos() / 4 ),(int)(((CreatureCellRenderable)proxy).getYPos() / 4 ) );
						
						if (unit.isPlayer() || unit.isMob()) {
							this.pickableUnits.add(unit);
							if(unit.isUnique())
							{
								if(uniques && playsoundunique)
								{
									playSound(soundunique);
								}
							}
						} else if (unit.isSpecial()) {
							this.pickableUnits.add(unit);
							if(specials && playsoundspecial)
							{
								playSound(soundspecial);
							}
						}else if(unit.isSpotted())
						{
							this.pickableUnits.add(unit);
							if(items && playsounditem)
							{
								playSound(sounditem);
							}
						}
						return null;
					});
			logger.log(Level.INFO, "[WurmEspMod] MobileModelRenderable.initialize hooked");

			HookManager.getInstance().registerHook("com.wurmonline.client.renderer.cell.MobileModelRenderable",
					"removed", "(Z)V", () -> (proxy, method, args) -> {
						method.invoke(proxy, args);
						
						PickableUnit item = (PickableUnit) proxy;
						
						for (Unit unit : pickableUnits) {
							if (unit.getId() == item.getId()) {
								toRemove.add(unit);
							}
						}

						for (Unit unit : toRemove) {
							if (unit.getId() == item.getId()) {
								pickableUnits.remove(unit);
							}
						}

						toRemove.clear();

						return null;
					});
			logger.log(Level.INFO, "[WurmEspMod] MobileModelRenderable.removed hooked");

			HookManager.getInstance().registerHook("com.wurmonline.client.renderer.cell.GroundItemCellRenderable",
					"initialize", "()V", () -> (proxy, method, args) -> {
						method.invoke(proxy, args);
						Class<?> cls = proxy.getClass();
						PickableUnit pUnit = (PickableUnit) proxy;
						
						GroundItemData item = ReflectionUtil.getPrivateField(proxy,
								ReflectionUtil.getField(cls, "item"));
						
						Unit unit = new Unit(item.getId(), pUnit, item.getModelName().toString(),((PickableUnit)proxy).getHoverName(),(int)((GroundItemCellRenderable)proxy).getXPos(),(int)((GroundItemCellRenderable)proxy).getYPos());

						if (unit.isSpecial()) {
							this.pickableUnits.add(unit);
							if(specials && playsoundspecial)
							{
								playSound(soundspecial);
							}
						}else if(unit.isSpotted())
						{
							this.pickableUnits.add(unit);
							if(items && playsounditem)
							{
								playSound(sounditem);
							}
						}
						return null;
					});
			logger.log(Level.INFO, "[WurmEspMod] GroundItemCellRenderable.initialize hooked");

			HookManager.getInstance().registerHook("com.wurmonline.client.renderer.cell.GroundItemCellRenderable",
					"removed", "(Z)V", () -> (proxy, method, args) -> {
						method.invoke(proxy, args);
						Class<?> cls = proxy.getClass();
						GroundItemData item = ReflectionUtil.getPrivateField(proxy,
								ReflectionUtil.getField(cls, "item"));

						for (Unit unit : pickableUnits) {
							if (unit.getId() == item.getId()) {
								toRemove.add(unit);
							}
						}

						for (Unit unit : toRemove) {
							if (unit.getId() == item.getId()) {
								pickableUnits.remove(unit);
							}
						}

						toRemove.clear();

						return null;
					});
			logger.log(Level.INFO, "[WurmEspMod] GroundItemCellRenderable.removed hooked");
			
			HookManager.getInstance().registerHook("com.wurmonline.client.comm.SimpleServerConnectionClass",
					"reallyHandleCmdShowDeedPlan", "(Ljava/nio/ByteBuffer;)V", () -> (proxy, method, args) -> {
						if(deedsize) {
							ByteBuffer bb = (ByteBuffer) args[0];
							byte type = bb.get();
						    switch (type)
						    {
						    case 0: 
						      int qId = bb.getInt();
						      
						      SimpleServerConnectionClass simpleServerConnectionClass = ((SimpleServerConnectionClass)proxy);
						      Method readStringByteLengthMethod = simpleServerConnectionClass.getClass().getDeclaredMethod("readStringByteLength",ByteBuffer.class);
						      readStringByteLengthMethod.setAccessible(true);
						      Object[] readStringByteLengthArgs = new Object[1];
						      readStringByteLengthArgs[0] = bb;
						      
						      
						      String deedName = (String) readStringByteLengthMethod.invoke(simpleServerConnectionClass,readStringByteLengthArgs);;
						      int tokenX = bb.getInt();
						      int tokenY = bb.getInt();
						      int startX = bb.getInt();
						      int startY = bb.getInt();
						      int endX = bb.getInt();
						      int endY = bb.getInt();
						      int perimSize = bb.getInt();
						      
						      tilesHighlightManager._addData(startX, startY, endX, endY);
						      tileshighlight = true;
						      
						      break;
						    }
						}else {
							method.invoke(proxy, args);
						}
					    return null;
					});
			logger.log(Level.INFO, "[WurmEspMod] SimpleServerConnectionClass.reallyHandleCmdShowDeedPlan hooked");
			
			logger.fine("Loaded");
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Error loading WurmEspMod", e.getMessage());
		}
	}

	@Override
	public void preInit() {
	}

	@SuppressWarnings("unchecked")
	private void initEspWR() {
		try {
			WurmEspWindow wurmEspWindow = new WurmEspWindow();
			
			MainMenu mainMenu = (MainMenu) ReflectionUtil.getPrivateField(hud,
					ReflectionUtil.getField(hud.getClass(), "mainMenu"));
			mainMenu.registerComponent("Esp", wurmEspWindow);

			List<WurmComponent> components = (List<WurmComponent>) ReflectionUtil.getPrivateField(hud,
					ReflectionUtil.getField(hud.getClass(), "components"));
			components.add(wurmEspWindow);

			SavePosManager savePosManager = (SavePosManager) ReflectionUtil.getPrivateField(hud,
					ReflectionUtil.getField(hud.getClass(), "savePosManager"));
			savePosManager.registerAndRefresh(wurmEspWindow, "wurmespwindow");
			
			TargetWindow lTargetWindow = (TargetWindow) ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "targetWindow"));

			mCreatureWindow = new CreatureWindow( lTargetWindow );
			mainMenu.registerComponent( "Local Creatures", mCreatureWindow );
			components.add( mCreatureWindow );
			savePosManager.registerAndRefresh( mCreatureWindow, "localcreatureswindow" );
			
		} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
	
	//credit @bdew: https://github.com/bdew-wurm/LiveHudMap/blob/10a6a22eff60b9ada8fdaf8005f8f0401e2eab67/src/main/java/org/gotti/wurmonline/clientmods/livehudmap/renderer/MapRendererCave.java#L95
	/*
	public static int getQl(int x ,int y){
		Random r = new Random();
		r.setSeed((x + y * serversize) * 789221L);
		return 20 + r.nextInt(80);
	}
	*/
	
	public static void DoConfig(Properties properties) {
		players = Boolean.valueOf(properties.getProperty("players", Boolean.toString(players)));
		mobs = Boolean.valueOf(properties.getProperty("mobs", Boolean.toString(mobs)));
		specials = Boolean.valueOf(properties.getProperty("specials", Boolean.toString(specials)));
		items = Boolean.valueOf(properties.getProperty("items", Boolean.toString(items)));
		uniques = Boolean.valueOf(properties.getProperty("uniques", Boolean.toString(uniques)));
		conditioned = Boolean.valueOf(properties.getProperty("conditioned", Boolean.toString(conditioned)));
		tilescloseby = Boolean.valueOf(properties.getProperty("tilescloseby", Boolean.toString(tilescloseby)));
		deedsize = Boolean.valueOf(properties.getProperty("deedsize", Boolean.toString(deedsize)));
		xray = Boolean.valueOf(properties.getProperty("xray", Boolean.toString(xray)));
		xraythread = Boolean.valueOf(properties.getProperty("xraythread", Boolean.toString(xraythread)));
		xrayrefreshthread = Boolean.valueOf(properties.getProperty("xrayrefreshthread", Boolean.toString(xrayrefreshthread)));
		xraydiameter = Integer.parseInt(properties.getProperty("xraydiameter", Integer.toString(xraydiameter)));
		xrayrefreshrate = Integer.parseInt(properties.getProperty("xrayrefreshrate", Integer.toString(xrayrefreshrate)));
		tilenotrideable = Integer.parseInt(properties.getProperty("tilenotrideable", Integer.toString(tilenotrideable)));
		playsoundspecial = Boolean.valueOf(properties.getProperty("playsoundspecial", Boolean.toString(playsoundspecial)));
		playsounditem = Boolean.valueOf(properties.getProperty("playsounditem", Boolean.toString(playsounditem)));
		playsoundunique = Boolean.valueOf(properties.getProperty("playsoundunique", Boolean.toString(playsoundunique)));
		soundspecial = properties.getProperty("soundspecial", soundspecial);
		sounditem = properties.getProperty("sounditem", sounditem);
		soundunique = properties.getProperty("soundunique", soundunique);
		conditionedcolorsallways = Boolean.valueOf(properties.getProperty("conditionedcolorsallways", Boolean.toString(conditionedcolorsallways)));
		championmcoloralways = Boolean.valueOf(properties.getProperty("championmcoloralways", Boolean.toString(championmcoloralways)));
		//serversize = Integer.parseInt(properties.getProperty("serversize", Integer.toString(serversize)));

		Unit.colorPlayers = colorStringToFloatA(
				properties.getProperty("colorPlayers", colorFloatAToString(Unit.colorPlayers)));
		Unit.colorPlayersEnemy = colorStringToFloatA(
				properties.getProperty("colorPlayersEnemy", colorFloatAToString(Unit.colorPlayersEnemy)));
		Unit.colorMobs = colorStringToFloatA(properties.getProperty("colorMobs", colorFloatAToString(Unit.colorMobs)));
		Unit.colorMobsAggro = colorStringToFloatA(
				properties.getProperty("colorMobsAggro", colorFloatAToString(Unit.colorMobsAggro)));
		Unit.colorSpecials = colorStringToFloatA(
				properties.getProperty("colorSpecials", colorFloatAToString(Unit.colorSpecials)));
		Unit.colorSpotted = colorStringToFloatA(
				properties.getProperty("colorSpotted", colorFloatAToString(Unit.colorSpotted)));
		Unit.colorUniques = colorStringToFloatA(
				properties.getProperty("colorUniques", colorFloatAToString(Unit.colorUniques)));
		Unit.colorAlert = colorStringToFloatA(
				properties.getProperty("colorAlert", colorFloatAToString(Unit.colorAlert)));
		Unit.colorAngry = colorStringToFloatA(
				properties.getProperty("colorAngry", colorFloatAToString(Unit.colorAngry)));
		Unit.colorChampion = colorStringToFloatA(
				properties.getProperty("colorChampion", colorFloatAToString(Unit.colorChampion)));
		Unit.colorDiseased = colorStringToFloatA(
				properties.getProperty("colorDiseased", colorFloatAToString(Unit.colorDiseased)));
		Unit.colorFierce = colorStringToFloatA(
				properties.getProperty("colorFierce", colorFloatAToString(Unit.colorFierce)));
		Unit.colorGreenish = colorStringToFloatA(
				properties.getProperty("colorGreenish", colorFloatAToString(Unit.colorGreenish)));
		Unit.colorHardened = colorStringToFloatA(
				properties.getProperty("colorHardened", colorFloatAToString(Unit.colorHardened)));
		Unit.colorLurking = colorStringToFloatA(
				properties.getProperty("colorLurking", colorFloatAToString(Unit.colorLurking)));
		Unit.colorRaging = colorStringToFloatA(
				properties.getProperty("colorRaging", colorFloatAToString(Unit.colorRaging)));
		Unit.colorScared = colorStringToFloatA(
				properties.getProperty("colorScared", colorFloatAToString(Unit.colorScared)));
		Unit.colorSlow = colorStringToFloatA(
				properties.getProperty("colorSlow", colorFloatAToString(Unit.colorSlow)));
		Unit.colorSly = colorStringToFloatA(
				properties.getProperty("colorSly", colorFloatAToString(Unit.colorSly)));
		
		String oreColorOreIron = properties.getProperty("oreColorOreIron", "default");
		if(!oreColorOreIron.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_IRON, colorStringToFloatA(oreColorOreIron));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_IRON, Color.RED.darker());
		}
		
		String oreColorOreCopper = properties.getProperty("oreColorOreCopper", "default");
		if(!oreColorOreCopper.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER, colorStringToFloatA(oreColorOreCopper));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER, Color.GREEN);
		}
		
		String oreColorOreTin = properties.getProperty("oreColorOreTin", "default");
		if(!oreColorOreTin.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_TIN, colorStringToFloatA(oreColorOreTin));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_TIN, Color.GRAY);
		}
		
		String oreColorOreGold = properties.getProperty("oreColorOreGold", "default");
		if(!oreColorOreGold.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD, colorStringToFloatA(oreColorOreGold));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD, Color.YELLOW.darker());
		}
		
		String oreColorOreAdamantine = properties.getProperty("oreColorOreAdamantine", "default");
		if(!oreColorOreAdamantine.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE, colorStringToFloatA(oreColorOreAdamantine));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE, Color.CYAN);
		}
		
		String oreColorOreGlimmersteel = properties.getProperty("oreColorOreGlimmersteel", "default");
		if(!oreColorOreGlimmersteel.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL, colorStringToFloatA(oreColorOreGlimmersteel));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL, Color.YELLOW.brighter());
		}
		
		String oreColorOreSilver = properties.getProperty("oreColorOreSilver", "default");
		if(!oreColorOreSilver.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER, colorStringToFloatA(oreColorOreSilver));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER, Color.LIGHT_GRAY);
		}
		
		String oreColorOreLead = properties.getProperty("oreColorOreLead", "default");
		if(!oreColorOreLead.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD, colorStringToFloatA(oreColorOreLead));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD, Color.PINK.darker().darker());
		}
		
		String oreColorOreZinc = properties.getProperty("oreColorOreZinc", "default");
		if(!oreColorOreZinc.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC, colorStringToFloatA(oreColorOreZinc));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC, new Color(235, 235, 235));
		}
		
		String oreColorSlate = properties.getProperty("oreColorSlate", "default");
		if(!oreColorSlate.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_SLATE, colorStringToFloatA(oreColorSlate));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_SLATE, Color.BLACK);
		}
		
		String oreColorMarble = properties.getProperty("oreColorMarble", "default");
		if(!oreColorMarble.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_MARBLE, colorStringToFloatA(oreColorMarble));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_MARBLE, Color.WHITE);
		}
		
		String oreColorSandstone = properties.getProperty("oreColorSandstone", "default");
		if(!oreColorSandstone.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_SANDSTONE, colorStringToFloatA(oreColorSandstone));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_SANDSTONE, Color.YELLOW.darker().darker());
		}
		
		String oreColorRocksalt = properties.getProperty("oreColorRocksalt", "default");
		if(!oreColorRocksalt.equals("default"))
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ROCKSALT, colorStringToFloatA(oreColorRocksalt));
		}
		else
		{
			XrayColors.addMapping(Tiles.Tile.TILE_CAVE_WALL_ROCKSALT, Color.WHITE.darker());
		}
		

		Unit.aggroMOBS = properties.getProperty("aggroMOBS").split(";");
		Unit.uniqueMOBS = properties.getProperty("uniqueMOBS").split(";");
		Unit.specialITEMS = properties.getProperty("specialITEMS").split(";");
		Unit.spottedITEMS = properties.getProperty("spottedITEMS").split(";");
		Unit.conditionedMOBS = properties.getProperty("conditionedMOBS").split(";");
	}
	
	private void playSound(String sound) {
		PlayerPosition pos = CellRenderable.world.getPlayer().getPos();
		CellRenderable.world.getSoundEngine().play(sound, (SoundSource)new FixedSoundSource(pos.getX(), pos.getY(), 2.0f), 1.0f, 5.0f, 1.0f, false, false);
	}
}
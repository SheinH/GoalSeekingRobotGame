import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class GoalSeekingRobotGame{
	private class Tile{

		private int state;
		private TileType content;

		public Tile(char content){
			this.content = TileType.charToTileType(content);
			state = 0;
		}

		public void reveal(int state){
			this.state = Math.max(state, this.state);
		}

		public void consume(){
			content = TileType.TRAIL;
		}

		public int getState() {
			return state;
		}

		public char toChar(){
			switch(state){
				case 0:
					return ' ';
				case 1:
					return '?';
				default:
					return content.character;
			}
		}

		public TileType getContent() {
			return content;
		}
	}

	enum TileType{
		GOAL		('*',false,-1,false),
		EMPTY		(' ',false),
		OBSTACLE	('#',true),
		BATTERY		('+',false,10),
		TRAP		('-',false,-10),
		BOUNDARY	('%',true),
		UP			('u',false,-1,false),
		DOWN		('d',false,-1,false),
		LEFT		('l',false,-1,false),
		RIGHT		('r',false,-1,false),
		TRAIL		('.',false);


		public final char character;
		public final boolean rigid;
		public final int energyChange;
		public final boolean consumable;

		TileType(char character, boolean rigid, int energyChange, boolean consumable) {
			this.character = character;
			this.rigid = rigid;
			this.energyChange = energyChange;
			this.consumable = consumable;
		}

		TileType(char character, boolean rigid){
			this(character, rigid, -1, false);
		}

		TileType(char character, boolean rigid, int energyChange){
			this(character, rigid, energyChange, true);
		}

		public static TileType charToTileType(char c){
			switch(c){
				case ('*'):
					return GOAL;
				case ('#'):
					return OBSTACLE;
				case ('+'):
					return BATTERY;
				case ('-'):
					return TRAP;
				case ('%'):
					return BOUNDARY;
				case ('u'):
					return UP;
				case ('d'):
					return DOWN;
				case ('l'):
					return LEFT;
				case ('r'):
					return RIGHT;
				default:
					return EMPTY;
			}
		}
	}

	Tile[][] grid;
	int playerX, playerY;
	int revealXMin, revealXMax, revealYMin, revealYMax;
	int energy;
	int gameState;

	public GoalSeekingRobotGame(char[][] inputGrid, int playerY, int playerX, int energy) {
	    grid = new Tile[inputGrid.length][];
	    for(int y = 0; y < inputGrid.length; y++){
	    	grid[y] = new Tile[inputGrid[y].length];
	    	for(int x = 0; x < inputGrid[y].length; x++){
	    		grid[y][x] = new Tile(inputGrid[y][x]);
			}
		}
		this.playerY = revealYMax = revealYMin = playerY;
		this.playerX = revealXMax = revealXMin = playerX;
		this.energy = energy;
		reveal();
	}

	private void move(int dy, int dx){
	    if(!validLocation(playerY + dy, playerX + dx))
	    	return;
		Tile dest = grid[playerY + dy][playerX + dx];
		TileType type = dest.getContent();
		if(type.rigid)
			return;
		energy += type.energyChange;
		if(type.consumable)
			dest.consume();
		playerX += dx;
		playerY += dy;
		reveal();
	}
	private boolean validLocation(int y, int x){
		return (y > 0 && y < grid.length) && (x > 0 && x < grid[y].length);
	}
	private void reveal(){
		for(int dy = -3; dy <= +3 ; dy++){
			for(int dx = -3; dx <= +3; dx++){
				int y = playerY+dy;
				int x = playerX+dx;
				if(!validLocation(y,x))
					continue;
				if(dx == 3 || dx == -3 || dy == 3 || dy == -3)
					grid[y][x].reveal(1);
				else
					grid[y][x].reveal(2);
				updateRevealedRegion(y,x);
			}
		}
	}

	private void updateRevealedRegion(int y, int x){
		revealXMin = Math.min(revealXMin,x);
		revealXMax = Math.max(revealXMax,x);
		revealYMin = Math.min(revealYMin,y);
		revealYMax = Math.max(revealYMax,y);
	}

	private void printRevealed() {
		for (int y = revealYMin; y <= revealYMax; y++) {
			for (int x = revealXMin; x <= revealXMax; x++) {
			    if(playerX == x && playerY == y)
			    	System.out.print('o');
				else if(validLocation(y, x))
					System.out.print(grid[y][x].toChar());
				else
					System.out.print(' ');
			}
			System.out.println();
		}
	}

	public void run(){
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(energy);
		printRevealed();
		while(gameState == 0){
			handlePlayerInput(input);
			updateGameState();
			System.out.println(energy);
			printRevealed();
		}
		if(gameState == 1)
			System.out.println("Congratulations! You won the game!");
		else
			System.out.println("You are out of energy. You lose!");
	}

	private void updateGameState() {
		Tile tile = grid[playerY][playerX];
		if (tile.getContent() == TileType.GOAL) {
			gameState = 1;
		} else if (energy <= 0) {
			energy = 0;
			gameState = -1;
		}
	}

	private void handlePlayerInput(BufferedReader input){
		try {
			boolean matches = false;
			while (!matches){
				String line = input.readLine();
				Pattern p = Pattern.compile("(\\d*)([udlr]{1})");
				Matcher m = p.matcher(line);
				matches = m.lookingAt();
				if(!matches){
					System.out.println("Invalid Input!");
				}
				else{
					String rep = m.group(1);
					String dir = m.group(2);
				    int steps = 1;
					if(!(rep.length() == 0))
						steps = Integer.parseInt(rep);
					char direction = dir.charAt(0);
					int dy = 0, dx =  0;
					switch(direction) {
						case 'd':
							dy = 1;
							dx = 0;
							break;
						case 'u':
							dy = -1;
							dx = 0;
							break;
						case 'l':
						    dy = 0;
							dx = -1;
							break;
						case 'r':
							dy = 0;
							dx = 1;
							break;
					}
					for(;steps > 0; steps--){
						move(dy, dx);
					}
				}

			}
		} catch (IOException e) {
			System.out.println("SUM TING WONG");
		}

	}
	public static void main(String[] args){
		String fileName = "game.txt";
		Scanner loader = new Scanner(GoalSeekingRobotGame.class.getResourceAsStream(fileName));
		int numSteps = loader.nextInt();
		int yPosition = loader.nextInt();
		int xPosition = loader.nextInt();
		loader.nextLine();
		ArrayList<char[]> land = new ArrayList<>();
		while (loader.hasNext()){
			String nextLine = loader.nextLine();
			char[] nextRow = new char[nextLine.length()];
			for (int i = 0; i < nextLine.length(); i++) {
				nextRow[i] = nextLine.charAt(i);
			}

			land.add(nextRow);
		}

		char[][] inputMap = new char[land.size()][];
		for (int i = 0; i < inputMap.length; i++) {
			inputMap[i] = land.get(i);
		}

		GoalSeekingRobotGame game = new GoalSeekingRobotGame(inputMap, yPosition, xPosition, numSteps);
		game.run();
	}
}

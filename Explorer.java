import uk.ac.warwick.dcs.maze.logic.IRobot;

//Function that checks all 4 walls for a specific condition eg passage wall beenbefore
//Maybe headers to do the orientation of juctions thing, crossroads as well? im not thinkingj right now so i do not really know at the moment
//Check feedback to confirm whether random approach of selecting exits was appropriate
// should explorecontrol return a direction and then control robot faces the direction? and pollrun as well
//ask about initial heading and absolute heading


public class Explorer {

    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int explorerMode; // 1 = explore, 0 = backtrack

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};

    public void controlRobot(IRobot robot) {

        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            robotData = new RobotData(); //reset the data store
            explorerMode = 1;
        }

        exploreControl(robot); 

    }


    public void reset() {
        System.out.println("****************RESET**************");
        robotData.resetJunctionCounter();
        explorerMode = 1;
    }


    private void exploreControl(IRobot robot) {
        int direction = 0;
        int beenBefores = beenBeforeExits(robot);
        int exits = nonwallExits(robot);

        switch (exits){
            case 1: direction = atDeadEnd(robot);
                    if (beenBefores == 1){ explorerMode = 0; }      
                    break;
            case 2: direction = atCorridor(robot);
                    break;
            case 3: direction = atJunction(robot);
                    break;
            case 4: 
            default:
                    direction = atCrossroad(robot);
                    break;
        }

        //robotData.searchJunction(robot.getLocation().x, robot.getLocation().y);

        if ( (beenBefores == 1) && (exits >= 3) ){    //at a previously unecountered junction/crossroad 
            robotData.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
            JunctionRecorder.printJunction();
        }


        robot.face(direction);

        pollRun++; // Increment pollRun so that the data is not reset each time the robot moves
    }


    public void backtrackControl(IRobot robot) {
        int direction = 0;
        int passages = passageExits(robot);
        int exits = nonwallExits(robot);

        if (exits > 2){
            if (passages > 0){
                explorerMode = 1;
            }
            else{
                int intialHeading = searchJunction(robot.getLocation().x, robot.getLocation().y);
            }
        }

    }


  
    private int nonwallExits (IRobot robot){

        int exits = 0;

        for(int i = 0; i < 4; i++){
			if(robot.look(lookDirections[i]) != IRobot.WALL){		//check all 4 directions for exit
				exits++;		//if no wall, increase exits count
			}
			else continue;		//if wall, check next direction
		}
        return exits;
    }


    private int passageExits (IRobot robot){

        int passages = 0;

        for(int i = 0; i < 4; i++){
			if( robot.look(lookDirections[i]) == IRobot.PASSAGE ){		//check all 4 directions for passage
				passages++;		//if passage, increase passages count
			}
			else continue;		//if no passage, check next direction
		}
        return passages;
    }


    private int beenBeforeExits (IRobot robot){

        int beenBefores = 0;

        for(int i = 0; i < 4; i++){
			if( robot.look(lookDirections[i]) == IRobot.BEENBEFORE ){		//check all 4 directions for beenbefore
				beenBefores++;		//if beenbefore, increase passages count
			}
			else continue;		//if no beenbefore, check next direction
		}
        return beenBefores;
    }


    private int atDeadEnd (IRobot robot){

        int direction = 0;
        
        for(int i = 0; i < 4; i++){
            if(robot.look(lookDirections[i]) != IRobot.WALL){
                direction = lookDirections[i];
                break;
            }
        }
        return direction;
    }


    private int atCorridor (IRobot robot){

        int direction;
        //The specs do not mention choosing a random direction when starting out in the middle of a corrider, with the walls ahead and behind.

        if (robot.look(IRobot.LEFT) != IRobot.WALL){
            direction = IRobot.LEFT;
        }
        else if (robot.look(IRobot.RIGHT) != IRobot.WALL){
            direction = IRobot.RIGHT;
        }
        else{
            direction = IRobot.AHEAD;
        }

        return direction;
    }


    private int atJunction (IRobot robot){
        int passages = passageExits(robot);
        int direction;
        int randno;

        if (passages == 0){
             do {
                    randno = (int) (Math.random()*4); //probabilty is reduced but still the same for the 3 options
                    direction = lookDirections[randno];;
                } while (robot.look(direction) == IRobot.WALL);

        } else{
            //for cases 1, 2 and 3
            do {
                randno = (int) (Math.random()*4); //probabilty is reduced but still the same for the 3 options
                direction = lookDirections[randno];;
                } while (robot.look(direction) != IRobot.PASSAGE);
        }

        return direction;
    }


    private int atCrossroad (IRobot robot){
        
        int passages = passageExits(robot);
        int direction;
        int randno;

        if (passages == 0){
            randno = (int) (Math.random()*4);
            direction = lookDirections[randno];

        } else{ //For cases 1, 2, 3 and 4.
            do {
                // probabilty is reduced but still the same for the 1, 2 or 3 options. This method might be slower than [finding out the viable directions and then picking randomly]
                randno = (int) (Math.random()*4);    
                direction = lookDirections[randno];
                } while ( robot.look(direction) != IRobot.PASSAGE );
        }
        
        // int direction = atJunction(robot); Since junction and crossroads are almost the same
        return direction;
    }


}



class RobotData {

    private static int maxJunctions = 10000; // Max number likely to occur
    private static int junctionCounter = 0; // No. of junctions stored
    private static JunctionRecorder[] junctionRecorderArray = new JunctionRecorder[maxJunctions];

    public static int getJunctionCounter(){
        return junctionCounter;
    }

    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

    public void recordJunction(int robotX, int robotY, int arrived) {
        
        JunctionRecorder newJunction = new JunctionRecorder(robotX, robotY, arrived);
        junctionRecorderArray[junctionCounter] = newJunction;
        junctionCounter++;
        //System.out.println(newJunction.juncX);
        
    }

    public int searchJunction(int robotX, int robotY) {
        int heading = 0;
        for(int i = 0; i < maxJunctions; i++){
            if ( (junctionRecorderArray[i].getJuncX() == robotX) && (junctionRecorderArray[i].getJuncY() == robotY) ){
                System.out.println("same junc");
                heading = junctionRecorderArray[i].getRelativeArrived();
                break;
            }
        }
        return heading;
    }

}



class JunctionRecorder {

    private static int juncX; // X-coordinates of the junctions
    private static int juncY; // Y-coordinates of the junctions
    private static int arrived; // Heading the robot first arrived from (Absolute)
    private static int relativeArrived; // Initial heading of robot (Relative)

    public JunctionRecorder(int juncX, int juncY, int arrived){

        this.juncX = juncX;
        this.juncY = juncY;
        this.relativeArrived = arrived;
        this.arrived = toAbsoluteHeading(arrived); //Absolute heading as per the specs

    }

    public static int getJuncX(){
        return juncX;
    }

    public static int getJuncY(){
        return juncY;
    }

    public static int getArrived(){
        return arrived;
    }

    public static int getRelativeArrived(){
        return relativeArrived;
    }


    public int toAbsoluteHeading(int heading){
        int absDir;

        switch (heading){
            case IRobot.NORTH:  absDir = IRobot.SOUTH;
                                break;
            case IRobot.SOUTH:  absDir = IRobot.NORTH;
                                break;
            case IRobot.EAST:  absDir = IRobot.WEST;
                                break;
            case IRobot.WEST:  
            default:            absDir = IRobot.EAST;
                                break;
        }

        return absDir;
    }
    
     public static void printJunction(){
         String arrivedString = "";
         
         switch (arrived){
            case IRobot.NORTH: arrivedString = "NORTH";
                                break;
            case IRobot.SOUTH: arrivedString = "SOUTH";
                                break;
            case IRobot.EAST: arrivedString = "EAST";
                                break;
            case IRobot.WEST: arrivedString = "WEST";
                                break;     
         }

        System.out.println("Junction " + RobotData.getJunctionCounter() + " (x="+ juncX + ",y=" + juncY + ") heading "+ arrivedString);
    }
    

}
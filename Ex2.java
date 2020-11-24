import uk.ac.warwick.dcs.maze.logic.IRobot;

//Function that checks all 4 walls for a specific condition eg passage wall beenbefore
//Check feedback to confirm whether random approach of selecting exits was appropriate
//Better explore control backtrack cintrol interface 
//numeric notation for absoslute heading
//arraylists for max junctions etc
//ARRIVED STRING FOR LOOP EXERCISE 1
//WHAT HAPEN WHEN START ON JUNCTION CENTR EXERCISE 1

public class Ex2 {

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

        if (explorerMode == 1){
            exploreControl(robot);
            System.out.println("EX");
        }
        else{ //exmode 0
            backtrackControl(robot);
            System.out.println("BT");
        }
         
        pollRun++; // Increment pollRun so that the data is not reset each time the robot moves

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

        if ( (beenBefores == 1) && (exits > 2) ){    //at a previously unecountered junction/crossroad 
            robotData.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
            //JunctionRecorder.printJunction();
        }

        switch (exits){
            case 1: 
                    if (beenBefores == 1){ 
                        explorerMode = 0;
                    }else{
                        direction = atDeadEnd(robot);
                    }      
                    break;

            case 2: direction = atCorridor(robot);
                    break;

            case 3: 
                    if (beenBefores == 3){
                        explorerMode = 0;
                    }else{
                        direction = atJunction(robot);
                    }
                    break;

            case 4: 
            default:
                    if (beenBefores == 4){
                        explorerMode = 0;
                    }else{
                        direction = atCrossroad(robot);
                    }
                    break;
        }


        if(explorerMode == 0){
            backtrackControl(robot);
        }else{
            robot.face(direction);
        }
        
    }


    public void backtrackControl(IRobot robot) {
        int direction = 0;
        int passages = passageExits(robot);
        int exits = nonwallExits(robot);

        if (exits > 2){
            if (passages > 0){
                explorerMode = 1;
                exploreControl(robot);
            }
            else{
                int heading = robotData.searchJunction(robot.getLocation().x, robot.getLocation().y);
                robot.setHeading(heading);
            }
        }
        else if (exits == 2){
            direction = atCorridor(robot);
            robot.face(direction);
        }
        else{
            direction = atDeadEnd(robot);
            robot.face(direction);
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

         
            do {
                randno = (int) (Math.random()*4); //probabilty is reduced but still the same for the 3 options
                direction = lookDirections[randno];;
                } while (robot.look(direction) != IRobot.PASSAGE);
        

        return direction;
    }


    private int atCrossroad (IRobot robot){
        
        return atJunction(robot);

        // int passages = passageExits(robot);
        // int direction;
        // int randno;

        //     do {
        //         // probabilty is reduced but still the same for the 1, 2 or 3 options. This method might be slower than [finding out the viable directions and then picking randomly]
        //         randno = (int) (Math.random()*4);    
        //         direction = lookDirections[randno];
        //         } while ( robot.look(direction) != IRobot.PASSAGE );
    
        // return direction;
    }



}



class RobotData {

    private static int maxJunctions = 10000; // Max number likely to occur
    private static int junctionCounter = 0; // No. of junctions stored
    private static JunctionHeader[] JunctionHeaderArray = new JunctionHeader[maxJunctions];

    public static int getJunctionCounter(){
        return junctionCounter;
    }

    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

    public void recordJunctionHeader(int arrived) {
        
        JunctionHeader newJunctionHeader = new JunctionHeader(arrived);
        newJunction.printJunction();

        JunctionHeaderArray[junctionCounter] = newJunctionHeader;
        junctionCounter++;
    }

    public void deleteJunctionHeader(){}

    public int searchJunctionHeader(int arrived) {
        int heading = 0;
        for(int i = 0; i < maxJunctions; i++){
            if ( (junctionRecorderArray[i].getJuncX() == robotX) && (junctionRecorderArray[i].getJuncY() == robotY) ){
                heading = junctionRecorderArray[i].getArrived();
                break;
            }
        }
        return heading;
    }


}



class JunctionRecorder {

    private int arrived; // Heading the robot first arrived from (Absolute)

    public JunctionRecorder(int juncX, int juncY, int arrived){

        this.arrived = getAbsoluteHeading(arrived); //Absolute heading as per the specs

    }

    public int getArrived(){
        return arrived;
    }

    private int getAbsoluteHeading(int heading){
        int absDir;

        if( heading == IRobot.NORTH || heading == IRobot.EAST ){
            absDir = heading + 2;
        } else{
            absDir = heading - 2;
        }

        // switch (heading){
        //     case IRobot.NORTH:  absDir = IRobot.SOUTH;
        //                         break;
        //     case IRobot.SOUTH:  absDir = IRobot.NORTH;
        //                         break;
        //     case IRobot.EAST:   absDir = IRobot.WEST;
        //                         break;
        //     case IRobot.WEST:  
        //     default:            absDir = IRobot.EAST;
        //                         break;
        // }

        return absDir;
    }
    
    public void printJunction(){
        int[] headers = {IRobot.NORTH, IRobot.SOUTH, IRobot.EAST, IRobot.WEST};
        String[] headerStrings = {"NORTH", "SOUTH", "EAST", "WEST"};

        String arrivedString = "";
        
        for(int i = 0; i < 4; i++){
            if(arrived == headers[i]){
                arrivedString = headerStrings[i];
            } else{
                continue;
            }
        }
         
        //  switch (arrived){
        //     case IRobot.NORTH: arrivedString = "NORTH";
        //                         break;
        //     case IRobot.SOUTH: arrivedString = "SOUTH";
        //                         break;
        //     case IRobot.EAST: arrivedString = "EAST";
        //                         break;
        //     case IRobot.WEST: arrivedString = "WEST";
        //                         break;     
        //  }

        System.out.println("Junction " + (RobotData.getJunctionCounter()+1) + " (x="+ juncX + ",y=" + juncY + ") heading "+ arrivedString);
    }
    

}

class JunctionHeader{

    private int arrived;

    public JunctionHeader(int arrived){
        this.arrived = getAbsoluteHeading(arrived);
    }

    public int getArrived(){
        return arrived;
    }

    private int getAbsoluteHeading(int heading){
        int absDir;

        switch (heading){
            case IRobot.NORTH:  absDir = IRobot.SOUTH;
                                break;
            case IRobot.SOUTH:  absDir = IRobot.NORTH;
                                break;
            case IRobot.EAST:   absDir = IRobot.WEST;
                                break;
            case IRobot.WEST:  
            default:            absDir = IRobot.EAST;
                                break;
        }

        return absDir;
    }

}
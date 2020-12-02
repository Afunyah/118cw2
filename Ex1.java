import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

//Function that checks all 4 walls for a specific condition eg passage wall beenbefore
//Check feedback to confirm whether random approach of selecting exits was appropriate
//Better explore control backtrack cintrol interface 
//numeric notation for absoslute heading
//arraylists
//if look heading is used, implement try catch to get absolute direction from numeric notation.


public class Ex1 {

    private int pollRun = 0; // Incremented after each pass
    private RobotDataEx1 RobotDataEx1; // Data store for junctions
    private int explorerMode; // 1 = explore, 0 = backtrack

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};

    public void controlRobot(IRobot robot) {

        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            RobotDataEx1 = new RobotDataEx1(); //reset the data store
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
        RobotDataEx1.resetJunctionData();
        explorerMode = 1;
    }


    private void exploreControl(IRobot robot) {
        int direction = 0;
        int beenBefores = beenBeforeExits(robot);
        int exits = nonwallExits(robot);

        if ( (beenBefores == 1) && (exits > 2) ){    //at a previously unecountered junction/crossroad 
            RobotDataEx1.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
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
                int heading = RobotDataEx1.searchJunction(robot.getLocation().x, robot.getLocation().y);
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
                direction = lookDirections[randno];
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



class RobotDataEx1 {

    private static int junctionCounter = 0; // No. of junctions stored
    private static ArrayList<JunctionRecorder> junctionRecorderArray = new ArrayList<JunctionRecorder>();

    public static int getJunctionCounter(){
        return junctionCounter;
    }

    public void resetJunctionData() {
        junctionCounter = 0;
        junctionRecorderArray.clear();
    }

    public void recordJunction(int robotX, int robotY, int arrived) {
        
        JunctionRecorder newJunction = new JunctionRecorder(robotX, robotY, arrived);
        newJunction.printJunction();

        junctionRecorderArray.add(newJunction);
        junctionCounter++;
    }

    public int searchJunction(int robotX, int robotY) {
        int heading = 0;
        for(int i = 0; i < junctionRecorderArray.size(); i++){
            if ( (junctionRecorderArray.get(i).getJuncX() == robotX) && (junctionRecorderArray.get(i).getJuncY() == robotY) ){
                heading = junctionRecorderArray.get(i).getArrived();
                break;
            }
        }
        return heading;
    }


}



class JunctionRecorder {

    private int juncX; // X-coordinates of the junctions
    private int juncY; // Y-coordinates of the junctions
    private int arrived; // Heading the robot first arrived from (Absolute)

    public JunctionRecorder(int juncX, int juncY, int arrived){

        this.juncX = juncX;
        this.juncY = juncY;
        this.arrived = getAbsoluteHeading(arrived); //Absolute heading as per the specs

    }

    public int getJuncX(){
        return juncX;
    }

    public int getJuncY(){
        return juncY;
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

        System.out.println("Junction " + (RobotDataEx1.getJunctionCounter()+1) + " (x="+ juncX + ",y=" + juncY + ") heading "+ arrivedString);
    }
    

}
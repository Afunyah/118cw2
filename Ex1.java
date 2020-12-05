import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

//Function that checks all 4 walls for a specific condition eg passage wall beenbefore
//Check feedback to confirm whether random approach of selecting exits was appropriate
//Better explore control backtrack cintrol interface 
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
        }
        else{ //exmode 0
            backtrackControl(robot);
        }
         
        pollRun++; // Increment pollRun so that the data is not reset each time the robot moves

    }


    public void reset() {
        //System.out.println("****************RESET**************");
        RobotDataEx1.resetJunctionData();
        explorerMode = 1;
    }


    private void exploreControl(IRobot robot) {
        int direction = 0;
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

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
                    }
            case 4: 
            default:
                    if (beenBefores == 4){
                        explorerMode = 0;
                    } else{
                        direction = atJunction(robot);
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
        int passages = pathTypeCheck(robot, IRobot.PASSAGE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

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


    private int pathTypeCheck (IRobot robot, int pathType){
        int pathCounter = 0;

        for(int i = 0; i < 4; i++){
            if( robot.look(lookDirections[i]) == pathType ){
                pathCounter++;
            }
		}

        if(pathType == IRobot.WALL){
            pathCounter = 4 - pathCounter;
        }

        return pathCounter;
    }


    private int atDeadEnd (IRobot robot){

        int direction;
        int i = 0;
//starting position considered
        do{
            direction = lookDirections[i];
            i++;
        } while ( robot.look(direction) == IRobot.WALL );

        return direction;
    }


    private int atCorridor (IRobot robot){

        //The specs do not mention choosing a random direction when starting out in the middle of a corrider, with the walls ahead and behind.

        int direction;
        int i = 0;

        do{
            direction = lookDirections[i];
            i++;
        } while ( (robot.look(direction) == IRobot.WALL) || (direction == IRobot.BEHIND) );

        return direction;
    }


    private int atJunction (IRobot robot){
        int direction;
        int randno;

            do {
                randno = (int) (Math.random()*4); //probabilty is reduced but still the same for the 3 options
                direction = lookDirections[randno];
                } while (robot.look(direction) != IRobot.PASSAGE);
        
        return direction;
    }

}



class RobotDataEx1 {

    private static int junctionCounter = 0; // No. of junctions stored
    private static ArrayList<JunctionRecorderEx1> junctionRecorderArray = new ArrayList<JunctionRecorderEx1>();

    public static int getJunctionCounter(){
        return junctionCounter;
    }

    public void resetJunctionData() {
        junctionCounter = 0;
        junctionRecorderArray.clear();
    }

    public void recordJunction(int robotX, int robotY, int arrived) {

        JunctionRecorderEx1 newJunction = new JunctionRecorderEx1(robotX, robotY, arrived);
        //newJunction.printJunction();
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



class JunctionRecorderEx1 {

    private int juncX; // X-coordinates of the junctions
    private int juncY; // Y-coordinates of the junctions
    private int arrived; // Heading the robot first arrived from (Absolute)

    public JunctionRecorderEx1(int juncX, int juncY, int arrived){

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
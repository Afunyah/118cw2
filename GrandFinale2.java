import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

//Function that checks all 4 walls for a specific condition eg passage wall beenbefore
//Check feedback to confirm whether random approach of selecting exits was appropriate
//Better explore control backtrack cintrol interface 
//WHAT HAPEN WHEN START ON JUNCTION CENTR EXERCISE 1

//Switching controllers within themselves maybe is not the best solution. get better inerfacing.

public class GrandFinale2 {

    private int pollRun = 0; // Incremented after each pass
    private RobotDataFinale2 RobotDataFinale2; // Data store for junctions
    private int explorerMode; // 1 = explore, 0 = backtrack
    private boolean newMaze;

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};

    public void controlRobot(IRobot robot) {

        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            RobotDataFinale2 = new RobotDataFinale2(); //reset the data store
            explorerMode = 1;
            newMaze = true;
        }

        if(newMaze){
            if (explorerMode == 1){
                exploreControl(robot);
            }
            else{ //exmode 0
                backtrackControl(robot);
            }
        } else{
            memoryControl(robot);
        }
        
        System.out.println(robot.completed());
         
        pollRun++; // Increment pollRun so that the data is not reset each time the robot moves

    }


    public void reset() {
        if(newMaze){
            RobotDataFinale2.storeToPersistentJunctionArray();
            newMaze = false;
        }
        RobotDataFinale2.resetJunctionData();
        explorerMode = 1;
        pollRun = 0;
        System.out.println("****************RESET**************");
    }


    private void memoryControl(IRobot robot) {
        int exits = pathTypeCheck(robot, IRobot.WALL);

        switch (exits){
            case 2:
                try{
                    if( (robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL) || (robot.look(IRobot.AHEAD) != IRobot.WALL) && (robot.look(IRobot.BEHIND) == IRobot.WALL) ){
                        int header = RobotDataFinale2.retrievePersistentHeader();
                        robot.setHeading(header);
                    }
                    else{
                        int direction = atCorridor(robot);
                        robot.face(direction);
                    }
                } 
                catch(IndexOutOfBoundsException e){
                    int header = findTarget(robot);
                    robot.setHeading(header);
                }
                break;

            case 3:
            case 4:
            default:
                try{
                    int header = RobotDataFinale2.retrievePersistentHeader();
                    robot.setHeading(header);
                }
                catch(IndexOutOfBoundsException e){
                    int header = findTarget(robot);
                    robot.setHeading(header);
                }   
        }
    }


    private void exploreControl(IRobot robot) {
        int direction = 0;
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        switch (exits){
            case 1: 
                    if (beenBefores == 1){ 
                        direction = IRobot.BEHIND;
                        explorerMode = 0;
                    }else{
                        direction = atDeadEnd(robot);
                    }      
                    break;

            case 2: 
                    if (pollRun == 0){
                        direction = atCorridor(robot); //avoid storing first junction as header, since technically it never "arrived"
                    } else{
                        if( (robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL)){
                            RobotDataFinale2.recordJunction(robot.getHeading());
                        }

                        direction = atCorridor(robot);
                    }
                    break;

            case 3: 
            case 4: 
            default: //to be honest, a starting normal junction is not to be stored right, regardless
                    if (pollRun == 0){
                        direction = atJunction(robot); //avoid storing first junction as header, since technically it never "arrived"
                    }
                    else{
                        if (beenBefores < 3){
                            RobotDataFinale2.recordJunction(robot.getHeading());
                            direction = atJunction(robot);
                        }
                        else {
                            direction = IRobot.BEHIND;
                            explorerMode = 0;
                        }
                    }
                    break;
        }

        robot.face(direction);
    }


    public void backtrackControl(IRobot robot) {
        int direction = 0;
        int passages = pathTypeCheck(robot, IRobot.PASSAGE);
        int exits = pathTypeCheck(robot, IRobot.WALL);
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);

        if (exits > 2){
            if (passages > 0){
                direction = atJunction(robot);
                robot.face(direction);
                explorerMode = 1;
            }
            else{
                int heading = RobotDataFinale2.retrieveJunctionHeader();
                robot.setHeading(heading);
            }
        }
        else if (exits == 2){
            if( (robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL) && beenBefores == 2){
                int heading = RobotDataFinale2.retrieveJunctionHeader();
                robot.setHeading(heading);
            } else {
                if(beenBefores == 1){
                    explorerMode = 1; //for l junc, start then bt to junc then turn to normal junc
                }
                direction = atCorridor(robot);
                robot.face(direction);
            }

        }
        else{
            direction = atDeadEnd(robot);
            robot.face(direction);
        }

    }


    private int findTarget(IRobot robot){
        int header;

        if(robot.getLocation().y < robot.getTargetLocation().y){
            header = IRobot.SOUTH;
        }
        else if (robot.getLocation().y > robot.getTargetLocation().y) {
            header = IRobot.NORTH;
        }
        else if(robot.getLocation().x < robot.getTargetLocation().x){
            header = IRobot.EAST;
        }
        else{
            header = IRobot.WEST;
        }

        return header;
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
        int passages = pathTypeCheck(robot, IRobot.PASSAGE);
        int direction;
        int randno;

            do {
                randno = (int) (Math.random()*4); //probabilty is reduced but still the same for the 3 options
                direction = lookDirections[randno];;
                } while (robot.look(direction) != IRobot.PASSAGE);
        
        return direction;
    }


}



class RobotDataFinale2 {

    private static int junctionCounter = 0; // No. of junctions stored
    private static ArrayList<JunctionRecorderFinale2> junctionRecorderArray = new ArrayList<JunctionRecorderFinale2>();
    private static ArrayList<JunctionRecorderFinale2> PersistentJunctionArray = new ArrayList<JunctionRecorderFinale2>();


    public void storeToPersistentJunctionArray(){
        clearPersistentJunctionArray();
        for (JunctionRecorderFinale2 junction : junctionRecorderArray){
            PersistentJunctionArray.add(junction);
        }
        System.out.println(PersistentJunctionArray);
    }


    public int retrievePersistentHeader(){
        int header = PersistentJunctionArray.get(junctionCounter).getPersistentArrived();
        System.out.println("Retrieving: " + PersistentJunctionArray.get(junctionCounter) + " " + (junctionCounter) +" " + header);
        junctionCounter++;
        return header;
    }


    public void clearPersistentJunctionArray() {
        PersistentJunctionArray.clear();
    }


    public static int getJunctionCounter(){
        return junctionCounter;
    }


    public void resetJunctionData() {
        junctionCounter = 0;
        junctionRecorderArray.clear();
    }


    public void recordJunction(int arrived) {
        
        JunctionRecorderFinale2 JunctionRecorder = new JunctionRecorderFinale2(arrived);
        JunctionRecorder.printJunction();
        System.out.println("Storing: " + JunctionRecorder + " "+ junctionCounter + " " + JunctionRecorder.getArrived());
        junctionRecorderArray.add(JunctionRecorder);
        junctionCounter++;
    }


    public void deleteJunction(){
        junctionRecorderArray.remove(junctionCounter-1);
        junctionCounter--;
    }


    public int retrieveJunctionHeader() {
        int header;
        try{
            JunctionRecorderFinale2 currentJunction = junctionRecorderArray.get(junctionCounter-1);
            header = currentJunction.getArrived();
            System.out.println("Retrieving: " + currentJunction + " " + (junctionCounter-1) +" " + header);
            deleteJunction();
        } catch(Exception e){
            System.out.println("TargetUnreachableException: No more explorable paths.");
            header = IRobot.NORTH;
        }
        
        return header;
    }

}



class JunctionRecorderFinale2{

    private int arrived;

    public JunctionRecorderFinale2(int arrived){
        this.arrived = reverseHeading(arrived);
    }


    public int getArrived(){
        return arrived;
    }


    public int getPersistentArrived(){
        return reverseHeading(arrived);
    }


    private int reverseHeading(int heading){
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

        System.out.println("Junction " + (RobotDataFinale2.getJunctionCounter()+1) +  " heading "+ arrivedString);
    }

}
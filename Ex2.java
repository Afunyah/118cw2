import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;


public class Ex2 {

    private int pollRun = 0; // Incremented after each pass
    private RobotDataEx2 RobotDataEx2; // Data store for junctions
    private int explorerMode; // 1 = explore, 0 = backtrack

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};

    public void controlRobot(IRobot robot) {

        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            RobotDataEx2 = new RobotDataEx2(); //reset the data store
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
        RobotDataEx2.resetJunctionData();
        explorerMode = 1;
        pollRun = 0;
    }


    private void exploreControl(IRobot robot) {
        int direction = 0;
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        if ( (beenBefores == 1) && (exits > 2) ){    //at a previously unecountered junction/crossroad 
            RobotDataEx2.recordJunction(robot.getHeading());
        }

        switch (exits){
            case 1: 
                    if (beenBefores == 1){ 
                        explorerMode = 0;
                    }
                    direction = atDeadEnd(robot);      
                    break;

            case 2: direction = atCorridor(robot);
                    break;

            case 3: 
            case 4: 
            default:
                    direction = atJunction(robot);
                    break;
        }

        robot.face(direction);
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
                int heading = RobotDataEx2.retrieveJunctionRecorder();
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
                direction = lookDirections[randno];;
                } while (robot.look(direction) != IRobot.PASSAGE);
        

        return direction;
    }

}



class RobotDataEx2 {

    private static int junctionCounter = 0; // No. of junctions stored
    private static ArrayList<JunctionRecorderEx2> junctionRecorderArray = new ArrayList<JunctionRecorderEx2>();

    public static int getJunctionCounter(){
        return junctionCounter;
    }

    public void resetJunctionData() {
        junctionCounter = 0;
        junctionRecorderArray.clear();
    }

    public void recordJunction(int arrived) {
        
        JunctionRecorderEx2 newJunctionRecorder = new JunctionRecorderEx2(arrived);
        //newJunctionRecorder.printJunction();

        junctionRecorderArray.add(newJunctionRecorder);
        junctionCounter++;
    }

    public void deleteJunctionRecorder(){
        junctionRecorderArray.remove(junctionCounter-1);
        junctionCounter--;
    }

    public int retrieveJunctionRecorder() {
        int header;
        try{
            JunctionRecorderEx2 currentJunction = junctionRecorderArray.get(junctionCounter-1);
            header = currentJunction.getArrived();
            deleteJunctionRecorder();
        } catch(Exception e){
            System.out.println("TargetUnreachableException: No more explorable paths.");
            header = IRobot.NORTH;
        }
        
        return header;
    }


}



class JunctionRecorderEx2{

    private int arrived;

    public JunctionRecorderEx2(int arrived){
        this.arrived = getAbsoluteHeading(arrived);
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


        System.out.println("Junction " + (RobotDataEx2.getJunctionCounter()+1) +  " heading "+ arrivedString);
    }

}
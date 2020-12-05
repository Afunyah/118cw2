import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

//Function that checks all 4 walls for a specific condition eg passage wall beenbefore
//Check feedback to confirm whether random approach of selecting exits was appropriate
//Better explore control backtrack cintrol interface 
//WHAT HAPEN WHEN START ON JUNCTION CENTR EXERCISE 1

//Switching controllers within themselves maybe is not the best solution. get better inerfacing.

//Starting in l junction, different orientations of the robot

public class GrandFinale {

    private int pollRun = 0; // Incremented after each pass
    private RobotDataFinale RobotDataFinale; // Data store for junctions
    private int explorerMode; // 1 = explore, 0 = backtrack
    private boolean newMaze;

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};

    public void controlRobot(IRobot robot) {

        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            RobotDataFinale = new RobotDataFinale(); //reset the data store
            explorerMode = 1;
            newMaze = true;
        }
        System.out.println("New Maze: " +newMaze);
        
         if(newMaze){
            if (explorerMode == 1){
                exploreControl(robot);
                System.out.println("EX");
            }
            else{ //exmode 0
                backtrackControl(robot);
                System.out.println("BT");
            }
        }
        else{
            memoryControl(robot);
            System.out.println("memory control");
        }
         
        pollRun++; // Increment pollRun so that the data is not reset each time the robot moves
        
    }


    public void reset() {
        
        if(newMaze){
                RobotDataFinale.clearPersistentJunctionData();
                RobotDataFinale.storePersistentJunctionData();
                newMaze = false;
        }
        RobotDataFinale.resetJunctionData();

        pollRun = 0;
        explorerMode = 1;
        System.out.println("****************RESET**************");
    }

    private void memoryControl(IRobot robot){
        int direction;
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);
        int exits = pathTypeCheck(robot, IRobot.WALL);
        int passages = pathTypeCheck(robot, IRobot.PASSAGE);


        if(pollRun == 0){
                if(exits > 2 && (exits == passages)) {
                    int heading = RobotDataFinale.retrievePersistentMove(1);
                    robot.setHeading(heading);
                }
                else{int heading = RobotDataFinale.retrievePersistentMove(0);
                robot.setHeading(heading);}
            }else{
        switch (exits){
            case 1: 
                    direction = atDeadEnd(robot);
                    robot.face(direction);    
                    break;

            case 2: 
            
             if((robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL) && beenBefores == 1){
                    try{ 
                        int heading = RobotDataFinale.retrievePersistentJunctionHeader();
                        robot.setHeading(heading);
                    }
                    catch(Exception e){
                        if(robot.getLocation().y < robot.getTargetLocation().y){
                            robot.setHeading(IRobot.SOUTH);
                        }
                        else if (robot.getLocation().y > robot.getTargetLocation().y) {
                            robot.setHeading(IRobot.NORTH);
                        }
                        else if(robot.getLocation().x < robot.getTargetLocation().x){
                            robot.setHeading(IRobot.EAST);
                        }
                        else{
                            robot.setHeading(IRobot.WEST);
                        }

                        // explorerMode = 1;
                        // //newMaze = true;
                        // exploreControl(robot);
                    }
            }
            else{
                    direction = atCorridor(robot);
                    robot.face(direction);
            }
                    break;

            case 3: 
            case 4: 
            default:
                    try{ 
                        int heading = RobotDataFinale.retrievePersistentJunctionHeader();
                        robot.setHeading(heading);
                        
                    }
                    catch(Exception e){
                         //System.out.println(robot.getLocation().y+ " t " + robot.getTargetLocation().y);


                        if(robot.getLocation().y < robot.getTargetLocation().y){
                            robot.setHeading(IRobot.SOUTH);
                        }
                        else if (robot.getLocation().y > robot.getTargetLocation().y) {
                            robot.setHeading(IRobot.NORTH);
                        }
                        else if(robot.getLocation().x < robot.getTargetLocation().x){
                            robot.setHeading(IRobot.EAST);
                        }
                        else{
                            robot.setHeading(IRobot.WEST);

                        }
                        
                        
                        

                        
                        // explorerMode = 1;
                        // //newMaze = true;
                        // exploreControl(robot);
                    }
                    
                    break;
        }
        
            }

    } 

    private void exploreControl(IRobot robot) {
        int direction;
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
             if((robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL) && beenBefores == 1){RobotDataFinale.recordJunction(robot.getHeading());}
             
            direction = atCorridor(robot);
                    break;

            case 3: 
            case 4: 
            default: 
                    if (beenBefores < 3){
                        RobotDataFinale.recordJunction(robot.getHeading());
                        direction = atJunction(robot);
                    }
                    else {
                        direction = IRobot.BEHIND;
                        explorerMode = 0;
                    }
                    break;
        }

        robot.face(direction);
        
    }


    public void backtrackControl(IRobot robot) {
        int direction;
        int passages = pathTypeCheck(robot, IRobot.PASSAGE);
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        if (exits > 2){
            if (passages > 0){
                if (beenBefores == 1 && RobotDataFinale.getJunctionCounter() == 0 ){
                    RobotDataFinale.recordJunction(robot.getHeading());   
                }
                
                direction = atJunction(robot);
                robot.face(direction);
                explorerMode = 1;
                
            }
            else{
                int heading = RobotDataFinale.retrieveJunctionHeader();
                robot.setHeading(heading);
            }
        }
        else if (exits == 2){
            
            if((robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL))
            { 
                if (passages == 0){
                    RobotDataFinale.retrieveJunctionHeader();
                    
                }
                else{
                    RobotDataFinale.recordJunction(robot.getHeading());
                    explorerMode = 1;
                }
            }
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


    // private int atCrossroad (IRobot robot){
        
    //     return atJunction(robot);

    // }

}



class RobotDataFinale {

    private static int junctionCounter = 0; // No. of junctions stored
    private static ArrayList<JunctionRecorderFinale> junctionRecorderArray = new ArrayList<JunctionRecorderFinale>();

    private static ArrayList<JunctionRecorderFinale> PersistentJunctionArray = new ArrayList<JunctionRecorderFinale>();

    public static int getJunctionCounter(){
        return junctionCounter;
    }

    public void resetJunctionData() {
        junctionCounter = 0;
        junctionRecorderArray.clear();
    }

    // public void recordCorridor(int arrived) {

    // }

    public void recordJunction(int arrived) {
        
        JunctionRecorderFinale JunctionRecorder = new JunctionRecorderFinale(arrived);
        JunctionRecorder.printJunction();

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
            JunctionRecorderFinale currentJunction = junctionRecorderArray.get(junctionCounter-1);
            header = currentJunction.getArrived();
            deleteJunction();
        } catch(Exception e){
            System.out.println("TargetUnreachableException: No more explorable paths.");
            header = IRobot.NORTH;
        }
        
        return header;
    }

    public void storePersistentJunctionData() {
        for (JunctionRecorderFinale junction : junctionRecorderArray){
            PersistentJunctionArray.add(junction);
        }
        System.out.println(PersistentJunctionArray);
        System.out.println(PersistentJunctionArray.get(0).getPersistentArrived());

        System.out.println(PersistentJunctionArray.get(1).getPersistentArrived());

    }

    public int retrievePersistentJunctionHeader(){
        junctionCounter++;
        int header = PersistentJunctionArray.get(junctionCounter).getPersistentArrived();
        return header;
    } 

    // public void deleteFirstPersistentJunction(){
    //     PersistentJunctionArray.remove(0);
    // }

    public int retrievePersistentMove(int moveNum){
        int header = PersistentJunctionArray.get(moveNum).getPersistentArrived();
        System.out.println(header);
        System.out.println(PersistentJunctionArray.get(moveNum).getArrived());
        junctionCounter = moveNum;
        return header;
    } 

    public void clearPersistentJunctionData() {
        PersistentJunctionArray.clear();
    }

}



class JunctionRecorderFinale{

    private int arrived;

    public JunctionRecorderFinale(int arrived){
        this.arrived = reverseHeading(arrived);
    }


    public int getArrived(){
        return arrived;
    }

    public int getPersistentArrived(){
        return reverseHeading(arrived);
    }

 
    public int reverseHeading(int heading){
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

        System.out.println("Junction " + (RobotDataFinale.getJunctionCounter()+1) +  " heading "+ arrivedString);
    }

}
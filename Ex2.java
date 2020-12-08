
/*
* Marcel Afunyah - u2015484
* CS118 Coursework 2 Ex2 Preamble
*
* My Ex2 solution is just a modified version of Ex1. Instead of storing the coordinates and header of a junction, only the header is stored.
* The objective was to save space.
* 
* A junctionRecorder object only has a header. When a junction is reached while backtracking, the junction header is retrieved and the object 
* is removed from the array. So it operates as a stack data structure.
* Therefore when the target is reached, the only junctionRecorderEx2 objects which are stored are those which lead directly to the target.
* Not only is the location of junctions removed, but unneeded junctions are also cut of during runtime. It is like pruning a tree. 
*
*/


import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;


/**
* Ex2 implements a robot which only stores junction headers and thus reduces memory usage.
*
* @author Marcel Afunyah
* @version 1.0
*/
public class Ex2 {

    private int pollRun = 0;                // Incremented after each pass
    private RobotDataEx2 RobotDataEx2;      // Data store for junctions
    private int explorerMode;               // 1 = explore, 0 = backtrack

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};


    /**
    * Initializes and creates data store for new mazes. 
    * Selects a controller based on exploreMode.
    *
    * @param robot the IRobot under control
    */
    public void controlRobot(IRobot robot) {

        /* On the first move of the first run of a new maze */
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            RobotDataEx2 = new RobotDataEx2();      // Reset the data store
            explorerMode = 1;
        }

        /* Select a controller based on explorerMode */
        if (explorerMode == 1){
            exploreControl(robot);
        }
        else{
            backtrackControl(robot);
        }
         
        pollRun++;     // Increment pollRun so that the data is not reset each time the robot moves

    }


    /**
    * Resets junction data of a maze.
    * Resets the pollRun to 0 and exploreMode to 1. 
    * Is called when the maze resets after completion or when the reset button is pressed.
    */
    public void reset() {
        RobotDataEx2.resetJunctionData();
        explorerMode = 1;
        pollRun = 0;
    }


    /**
    * Decides the robot's next move when exploreMode is 1. 
    * Also stores new junction when exploring.
    *
    * @param robot the IRobot under control
    */
    private void exploreControl(IRobot robot) {

        int direction = 0;
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        /* Check for and store new junction */
        if ( (beenBefores == 1) && (exits > 2) ){     
            RobotDataEx2.recordJunction(robot.getHeading());    // Only the header is recorded
        }

        /* Choose a direction based on the number of exits */
        switch (exits){
            case 1: 
                    if (beenBefores == 1){ 
                        explorerMode = 0;    // The next step will be a backtrack
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



    /**
    * Decides the robot's next move when exploreMode is 0. 
    * Is able to retrieve the most recent junctionRecorder object and get its heading for junctions in 
    * the data store and backtrack in the arrived from header direction.
    *
    * @param robot the IRobot under control
    */
    public void backtrackControl(IRobot robot) {

        int direction = 0;
        int passages = pathTypeCheck(robot, IRobot.PASSAGE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        if (exits > 2){
            if (passages > 0){
                explorerMode = 1;
                exploreControl(robot);      // Switch to exploreControl when there is a passage
            }
            else{
                /* Retrieve the junction header on the stack */
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

  
    /**
    * Checks all 4 directions for the given path type and returns the number of exits for that path type. 
    * Note that the pathType IRobot.WALL will return the number of exits without a WALL.
    *
    * @param  robot        the IRobot under control
    * @param  pathType     either IRobot.WALL, IRobot.PASSAGE or IRobot.BEENBEFORE
    * @return pathCounter  the number of exits for a pathType
    */
    private int pathTypeCheck (IRobot robot, int pathType){

        int pathCounter = 0;

        for(int i = 0; i < 4; i++){
            if( robot.look(lookDirections[i]) == pathType ){
                pathCounter++;
            }
		}

        /* Invert the results if the pathType is IRobot.WALL */
        if(pathType == IRobot.WALL){
            pathCounter = 4 - pathCounter;
        }

        return pathCounter;
    }



    /**
    * Chooses a direction which is not blocked by a wall at a deadend.  
    * The do-while loop is needed to check for all directions since the robot can start at
    * a deadend in any orientation.
    *
    * @param  robot      the IRobot under control
    * @return direction  the selected direction which is not blocked by a wall
    */
    private int atDeadEnd (IRobot robot){

        int direction;
        int i = 0;

        do{
            direction = lookDirections[i];
            i++;
        } while(robot.look(direction) == IRobot.WALL);

        return direction;
    }


    /**
    * Chooses a direction which is not blocked by a wall and is not IRobot.BEHIND at a corridor.
    * The specification does not mention choosing a random direction when starting in the
    * middle of a corridor. Not randomised.
    * So the robot is biased to the pick the first viable option in the lookDirections array.
    *
    * @param  robot      the IRobot under control
    * @return direction  the selected direction which is not blocked by a wall
    *                    and is not IRobot.BEHIND
    */
    private int atCorridor (IRobot robot){

        int direction;
        int i = 0;

        do{
            direction = lookDirections[i];
            i++;
        } while ( (robot.look(direction) == IRobot.WALL) || (direction == IRobot.BEHIND) );

        return direction;
    }


    /**
    * Chooses a random direction which is an IRobot.PASSAGE at a passage.
    * Crossroads and junctions can be considered to be the same.
    *
    * @param  robot      the IRobot under control
    * @return direction  the selected direction which is an IRobot.PASSAGE
    */
    private int atJunction (IRobot robot){

        int direction;
        int randno;

            do {
                randno = (int) (Math.random()*4);       // The probabilty is reduced but still the same for the 3 options
                direction = lookDirections[randno];
                } while (robot.look(direction) != IRobot.PASSAGE);
        
        return direction;
    }

}




/**
* RobotDataEx2 contains a data store, variables and methods for working with this data store. 
* There should be only one RobotDataEx2 object at any point during runtime. 
*/
class RobotDataEx2 {

    private static int junctionCounter = 0;                                                                         // Number of junctions stored
    private static ArrayList<JunctionRecorderEx2> junctionRecorderArray = new ArrayList<JunctionRecorderEx2>();     // Stores JunctionRecorderEx2 objects


    /**
    * Gets the number of junctionRecorder objects stored in junctionRecorderArray.
    *
    * @return junctionCounter the number of junctions stored
    */
    public static int getJunctionCounter(){
        return junctionCounter;
    }


    /**
    * Resets the junctionRecorderArray and sets junctionCounter to 0.
    */
    public void resetJunctionData() {
        junctionCounter = 0;
        junctionRecorderArray.clear();
    }


    /**
    * Stores a junction and its data as a JunctionRecorderEx2 object in the junctionRecorderArray.
    *    
    * @param arrived the heading of the robot when it first arrived at the junction
    */
    public void recordJunction(int arrived) {
        
        JunctionRecorderEx2 newJunctionRecorder = new JunctionRecorderEx2(arrived);
        junctionRecorderArray.add(newJunctionRecorder);
        junctionCounter++;
    }


    /**
    * Deletes the most recent JunctionRecorderEx2 object in the junctionRecorderArray.
    * Since Junctioncounter starts from 0, (junctionCounter-1) is the index of the most recent junction.
    * JunctionCounter is decremented afterwards.   
    */
    public void deleteJunctionRecorder(){
        junctionRecorderArray.remove(junctionCounter-1);
        junctionCounter--;
    }


    /**
    * Retrieves the heading of the most recent junction, and deletes the JunctionRecorderEx2 object.
    * This is the equivalent of popping the stack.
    * An IndexOutOfBoundsException is thrown and caught in the case where the robot has explored all
    * possible paths and has not reached the target. 
    * In such a case, the target has probably been purposely blocked for testing purposes.
    *   
    * @return header  the header of the most recent junction.
    */
    public int retrieveJunctionRecorder() {
        int header;

        try{
            /* Retrieve junction header */
            JunctionRecorderEx2 currentJunction = junctionRecorderArray.get(junctionCounter-1);
            header = currentJunction.getArrived();
            deleteJunctionRecorder();
        } catch(Exception e){
            System.out.println("TargetUnreachableException: No more explorable paths.");
            header = IRobot.NORTH;      // Set header to NORTH
        }
        
        return header;
    }

}




/**
* JunctionRecorderEx2 is a junction to be stored.  
* Each junction has only an arrived from header.
* 
*/
class JunctionRecorderEx2{

    private int arrived;       // Heading the robot first arrived from (Absolute)


    /**
    * JunctionRecorderEx2 constructor
    */
    public JunctionRecorderEx2(int arrived){
        this.arrived = getAbsoluteHeading(arrived);     // Absolute heading as per the specification (reversed heading)
    }


    /**
    * Gets the header from which the robot initially arrived at the junction 
    *
    * @return arrived the header from which the robot initially arrived at the junction
    */
    public int getArrived(){
        return arrived;
    }


    /**
    * Gets the absolute heading, which is the reverse of the heading parameter.
    * It uses the +/- 2 header relationship to reverse headings required for the junctionRecorderArray
    *
    * @param  heading  the heading to be reversed
    * @return absDir   the absolute heading
    */
    private int getAbsoluteHeading(int heading){
        int absDir;

        if( heading == IRobot.NORTH || heading == IRobot.EAST ){
            absDir = heading + 2;
        } else{
            absDir = heading - 2;
        }

        return absDir;
    }

}
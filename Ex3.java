
/*
* Marcel Afunyah - u2015484
* CS118 Coursework 2 Ex3 Preamble
*
* The Ex3 solution is an improvement of Ex2.
* Ex2 could not solve loopy mazes becauses loopy mazes essentially forms a tree where siblings are connected through
* more ways other than parent nodes. Such a graph does not have terminating deadends where a loop has already been explored,
* so the robot has no way of knowing whether a set of loops is different from another set of loops.
*
* This solution makes use of Tremaux's algorithm to lock-off explored loops which do not lead to the target.
* It can trace a loop to the junction where it first entered and then trace back in the opposite direction to leave the loop.
* Tremaux's algorithm: https://en.wikipedia.org/wiki/Maze_solving_algorithm#Tr%C3%A9maux's_algorithm
*
*/


import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;


/**
* Ex2 implements a robot which only stores junction headers and can solve loopy mazes.
*
* @author Marcel Afunyah
* @version 1.0
*/
public class Ex3 {

    private int pollRun = 0;                // Incremented after each pass
    private RobotDataEx3 RobotDataEx3;      // Data store for junctions
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
            RobotDataEx3 = new RobotDataEx3();      // Reset the data store
            explorerMode = 1;
        }

        /* Select a controller based on explorerMode */
        if (explorerMode == 1){
            exploreControl(robot);
        }
        else{
            backtrackControl(robot);
        }
         
        pollRun++;      // Increment pollRun so that the data is not reset each time the robot moves

    }


    /**
    * Resets junction data of a maze.
    * Resets the pollRun to 0 and exploreMode to 1. 
    * Is called when the maze resets after completion or when the reset button is pressed.
    */
    public void reset() {
        RobotDataEx3.resetJunctionData();
        explorerMode = 1;
        pollRun = 0;
    }



    /**
    * Decides the robot's next move when exploreMode is 1. 
    * Also stores new junction when exploring.
    * 
    * The robot will store new junctions and pick a random passage.
    * The robot will reverse its direction and backtrack when it encounters an old junction.
    *
    * @param robot the IRobot under control
    */
    private void exploreControl(IRobot robot) {

        int direction = 0;
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        /* Choose a direction based on the number of exits */
        switch (exits){
            case 1: 
                    if (beenBefores == 1){ 
                        explorerMode = 0;          // The next step will be a backtrack
                    }
                    direction = atDeadEnd(robot);
                    break;

            case 2: direction = atCorridor(robot);
                    break;

            case 3: 
            case 4: 
            default: 
                    /* Store new junction. Also takes into account meeting a junction where the robot started. */
                    if (beenBefores < 3){  
                        RobotDataEx3.recordJunction(robot.getHeading());  
                        direction = atJunction(robot);
                    }
                    else {
                        direction = IRobot.BEHIND;
                        explorerMode = 0;          // The next step will be a backtrack
                    }
                    break;
        }

        robot.face(direction);   
    }


    /**
    * Decides the robot's next move when exploreMode is 0. 
    * Is able to retrieve the most recent junctionRecorder object in the data store,
    * get its heading and backtrack in the arrived from header direction.
    *
    * @param robot the IRobot under control
    */
    public void backtrackControl(IRobot robot) {

        int direction = 0;
        int passages = pathTypeCheck(robot, IRobot.PASSAGE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        if (exits > 2){
            if (passages > 0){
                direction = atJunction(robot);  // Select a random passage 
                robot.face(direction);          
                explorerMode = 1;               // Switch to exploreControl for the next move
            }
            else{
                /* Retrieve the junction header on the stack */
                int heading = RobotDataEx3.retrieveJunctionHeader();
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
* RobotDataEx3 contains a data store, variables and methods for working with this data store. 
* There should be only one RobotDataEx3 object at any point during runtime. 
*/
class RobotDataEx3 {

    private static int junctionCounter = 0;                                                                         // Number of junctions stored
    private static ArrayList<JunctionRecorderEx3> junctionRecorderArray = new ArrayList<JunctionRecorderEx3>();     // Stores JunctionRecorderEx3 objects


    /**
    * Gets the number of junctionRecorder objects stored in junctionRecorderArray.
    *
    * @return junctionCounter  the number of junctions stored
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
    * Stores a junction and its data as a JunctionRecorderEx3 object in the junctionRecorderArray.
    *    
    * @param arrived  the heading of the robot when it first arrived at the junction
    */
    public void recordJunction(int arrived) {
        
        JunctionRecorderEx3 newJunctionRecorder = new JunctionRecorderEx3(arrived);
        junctionRecorderArray.add(newJunctionRecorder);
        junctionCounter++;
    }


    /**
    * Deletes the most recent JunctionRecorderEx3 object in the junctionRecorderArray.
    * Since Junctioncounter starts from 0, (junctionCounter-1) is the index of the most recent junction.
    * JunctionCounter is decremented afterwards.   
    */
    public void deleteJunctionRecorder(){
        junctionRecorderArray.remove(junctionCounter-1);
        junctionCounter--;
    }


    /**
    * Retrieves the heading of the most recent junction, and deletes the JunctionRecorderEx3 object.
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
            JunctionRecorderEx3 currentJunction = junctionRecorderArray.get(junctionCounter-1);
            header = currentJunction.getArrived();
            deleteJunctionRecorder();
        } catch(Exception e){
            System.out.println("TargetUnreachableException: No more explorable paths.");
            header = IRobot.NORTH;      // Set header to NORTH
        }

}




/**
* JunctionRecorderEx3 is a junction to be stored.  
* Each junction has only an arrived from header.
* 
*/
class JunctionRecorderEx3{

    private int arrived;        // Heading the robot first arrived from (Absolute)


    /**
    * JunctionRecorderEx3 constructor
    */
    public JunctionRecorderEx3(int arrived){
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
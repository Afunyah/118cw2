
/*
* Marcel Afunyah - u2015484
* CS118 Coursework 2 GrandFinale Preamble
*
* The Grand Finale soolution is an improvement of Ex3.
* This solution still uses Tremaux's algortithm to solve mazes. From the guide, it sort of follows route B. However,
* My Ex2 solution followed route B in some sense before I even looked at the final part of the guide.
*
* This solution is route B, but with a slight twist. Instead of storing only pure junctions, it also stores corridor turns 
* as junctions. This is feasible because a corridor turn can be considered as an 'L-junction' where two paths of different
* orientations meet.
*
* By storing the headers of such corridors, the final array list contains all the headers taken from one end of a corridor,
* to the other end, with intermediate junctions. 
* 
* A memoryControl, newMaze boolean variable, a PersistentJunctionArray and methods for persistent data are introduced 
* After the first run of a new maze, the JunctionRecorderArray data is transferred to the PersistentJunctionArray.
* The newMaze variable is set to false after the first reset, so the PersistentJunctionArray is not overwritten afterwards,
*
* I added a memoryControl to control the robot using the stored array. It utilises the PersistentJunctionArray as a FIFO, 
* First In First Out, data structure after the first complete run of a maze.   
* It then takes the header of the first header recorded and moves in that direction until it reaches a junction or a corridor wall, 
* and repeats for the next header.
* 
* After the last junction, the robot will either be on the same latitude or longitude as the target. It can check its coordinates
* and set the final direction towards the target.
*
* I tried to include as many cases as possible. For example, when the robot starts on a junction originally, exploreControl
* will not invoke the recordJunction method. Technically, a starting junction does not have an 'arrived-from'.
*
* The robot can solve loopy mazes since it is an Ex3 extension.
*
* The robot can deal with new mazes. It can also run repeats of the same maze. The memoryControl takes after the first run of a
* newMaze. It only reads the PersistentJunctionArray, which is not overwritten unless the controller is reloaded.
* However, if the robot is reset DURING the FIRST run of a maze, the controller has to be reloaded, else it will not work for 
* subsequent runs since the full path to the target was not obtained.
* Because there was no function to check whether the robot was reset before or after completing the maze, it has to be reloaded.
*
* For loopy mazes, better paths might be unexplored.
*
* Tremaux's algorithm: https://en.wikipedia.org/wiki/Maze_solving_algorithm#Tr%C3%A9maux's_algorithm
*
*/


import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;


/**
* GrandFinale implements a learning robot which learns from its first run of a maze.
*
* @author Marcel Afunyah
* @version 1.0
*/
public class GrandFinale {

    private int pollRun = 0;                    // Incremented after each pass
    private RobotDataFinale RobotDataFinale;    // Data store for junctions
    private int explorerMode;                   // 1 = explore, 0 = backtrack
    private boolean newMaze;                    // First run of a new maze?

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};


    /**
    * Initializes and creates data store for new mazes. 
    * Selects a controller based on exploreMode and newMaze.
    *
    * @param robot the IRobot under control
    */
    public void controlRobot(IRobot robot) {

        /* On the first move of the first run of a new maze */
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            RobotDataFinale = new RobotDataFinale();    // Reset the data store
            explorerMode = 1;
            newMaze = true;                             // Sets newMaze to true
        }

        if(newMaze){
            /* Select a controller based on explorerMode */
            if (explorerMode == 1){
                exploreControl(robot);
            }
            else{ //exmode 0
                backtrackControl(robot);
            }
        } else{
            memoryControl(robot);   // Activate memory control for the second run and onwards for a maze
        }
        
        pollRun++;                  // Increment pollRun so that the data is not reset each time the robot moves
    }


    /**
    * Resets junction data of a maze.
    * Resets the pollRun to 0 and exploreMode to 1.
    * Stores persistent data after the very first run of a maze. 
    * Is called when the maze resets after completion or when the reset button is pressed.
    */
    public void reset() {
        if(newMaze){
            RobotDataFinale.storeToPersistentJunctionArray();
            newMaze = false;
        }
        RobotDataFinale.resetJunctionData();
        explorerMode = 1;
        pollRun = 0;
    }


    /**
    * Controls the robot by reading the persistent data. 
    * Activates after the first run of a new maze.
    * 
    * At each junction and corridor turn, retrieve the header of the next junction 
    * and proceed in that direction.
    *
    * RobotDataFinale.retrievePersistentHeader() throws an IndexOutOfBoundsException error
    * after the last junction is retrieved.
    *
    * At this point the target is either on the same longitude or latitude.
    * The findTarget() method determines this and provides the final direction.
    *
    * @param robot the IRobot under control
    */
    private void memoryControl(IRobot robot) {

        int exits = pathTypeCheck(robot, IRobot.WALL);

        /* Choose a direction based on the number of exits */
        switch (exits){
            /* No deadends will be reached */

            case 2:
                try{
                    /* Get the next header at a corridor turn. Adds condition for starting in different orientations */
                    if( (robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL) || (robot.look(IRobot.AHEAD) != IRobot.WALL) && (robot.look(IRobot.BEHIND) == IRobot.WALL) ){
                        int header = RobotDataFinale.retrievePersistentHeader();
                        robot.setHeading(header);
                    }
                    else{
                        /* Go straight ahead in a straight corridor */
                        int direction = atCorridor(robot);
                        robot.face(direction);
                    }
                } 
                catch(IndexOutOfBoundsException e){
                    /* Find the target after the last junction has been reached */
                    int header = findTarget(robot);
                    robot.setHeading(header);
                }
                break;

            case 3:
            case 4:
            default:
                try{
                    int header = RobotDataFinale.retrievePersistentHeader();
                    robot.setHeading(header);
                }
                catch(IndexOutOfBoundsException e){
                    /* Find the target after the last junction has been reached */
                    int header = findTarget(robot);
                    robot.setHeading(header);
                }   
        }
    }



    /**
    * Decides the robot's next move when exploreMode is 1. 
    * Also stores new junction when exploring.
    * 
    * The robot will store new junctions and pick a random passage.
    * The robot will reverse its direction and backtrack when it encounters an old junction.
    * Staring junctions and corridor turns will not be stored.
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
                        explorerMode = 0;       // The next step will be a backtrack
                    }
                    direction = atDeadEnd(robot);     
                    break;

            case 2: 
                    /* Prevents storage of a starting corridor turn on first step */
                    if (pollRun != 0){
                        /* Store a corridor turn as a junction */
                        if( (robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL)){
                            RobotDataFinale.recordJunction(robot.getHeading());
                        }
                    }

                    direction = atCorridor(robot);
                    break;

            case 3: 
            case 4: 
            default: 
                    /* Prevents storage of a starting junction on the first step */
                    if (pollRun == 0){
                        direction = atJunction(robot);    // Choose the next direction only
                    }
                    else{

                        /* Store new junction. Also takes into account meeting a junction where the robot started. */
                        if (beenBefores < 3){
                            RobotDataFinale.recordJunction(robot.getHeading());
                            direction = atJunction(robot);
                        }
                        else {
                            direction = IRobot.BEHIND;
                            explorerMode = 0;             // The next step will be a backtrack
                        }
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
    * Includes the case where the robot backtracks to a starting junction or corridor turn.
    * Since it did not store such a junction, it has no header.
    * However, as long as the target is reachable (not explicitly blocked off), the robot will
    * never backtrack to a starting junction where all its exits have been explored.
    *
    * @param robot the IRobot under control
    */
    public void backtrackControl(IRobot robot) {

        int direction = 0;
        int passages = pathTypeCheck(robot, IRobot.PASSAGE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        if (exits > 2){
            if (passages > 0){
                direction = atJunction(robot);   // Select a random passage
                robot.face(direction);
                explorerMode = 1;                // Switch to exploreControl for the next move
            }
            else{
                /* Retrieve the junction header on the stack */
                int heading = RobotDataFinale.retrieveJunctionHeader();
                robot.setHeading(heading);
            }
        }
        else if (exits == 2){
            /* Retrieve the corridor turn header if it is not a starting corridor turn */
            if( (robot.look(IRobot.AHEAD) == IRobot.WALL) && (robot.look(IRobot.BEHIND) != IRobot.WALL) && passages == 0){
                int heading = RobotDataFinale.retrieveJunctionHeader();
                robot.setHeading(heading);
            } 
            else {
                /* When backtracking to a starting corridor turn from a deadend */
                if(passages == 1){
                    explorerMode = 1;            // Switch to exploreControl for the next move
                }
                direction = atCorridor(robot);   // Pick the available passage
                robot.face(direction);
            }
        }
        else{
            direction = atDeadEnd(robot);
            robot.face(direction);
        }

    }


    /**
    * Finds the target when it shares the same latitude or longitude as the robot.
    *
    * @param  robot   the IRobot under control
    * @return header  the header direction in which the target is
    */
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
* RobotDataFinale contains a data store, variables and methods for working with this data store. 
* There should be only one RobotDataFinale object at any point during runtime.
* It contains the PersistentJunctionArray and methods for working with persistent data. 
*/
class RobotDataFinale {

    private static int junctionCounter = 0;                                                                                 // Number of junctions stored
    private static ArrayList<JunctionRecorderFinale> junctionRecorderArray = new ArrayList<JunctionRecorderFinale>();       // Stores temporary junction data during the first run
    private static ArrayList<JunctionRecorderFinale> PersistentJunctionArray = new ArrayList<JunctionRecorderFinale>();     // Stores permanent junction data after first run


    /**
    * Makes a shallow copy of the contents of junctionRecorderArray in PersistentJunctionArray.
    * This is called once, after the reset of the first run of a new maze.
    * The array is also cleared beforehand as a safety measure.
    */
    public void storeToPersistentJunctionArray(){
        clearPersistentJunctionArray();
        for (JunctionRecorderFinale junction : junctionRecorderArray){
            PersistentJunctionArray.add(junction);
        }
    }


    /**
    * Retrieves the heading of the junction in PersistentJunctionArray with index of junctionCounter.
    * This junction is the next junction or corridor turn that the robot will meet.
    *
    * junctionCounter is incremented afterwards for the next junction in store.
    *  
    * @return header  the header of the next junction in the maze.
    */
    public int retrievePersistentHeader(){
        int header = PersistentJunctionArray.get(junctionCounter).getPersistentArrived();
        junctionCounter++;
        return header;
    }


    /**
    * Resets the PersistentJunctionArray.
    */
    public void clearPersistentJunctionArray() {
        PersistentJunctionArray.clear();
    }


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
    * Stores a junction and its data as a JunctionRecorderFinale object in the junctionRecorderArray.
    *    
    * @param arrived the heading of the robot when it first arrived at the junction
    */
    public void recordJunction(int arrived) {
        
        JunctionRecorderFinale JunctionRecorder = new JunctionRecorderFinale(arrived);
        junctionRecorderArray.add(JunctionRecorder);
        junctionCounter++;
    }


    /**
    * Deletes the most recent JunctionRecorderFinale object in the junctionRecorderArray.
    * Since Junctioncounter starts from 0, (junctionCounter-1) is the index of the most recent junction.
    * JunctionCounter is decremented afterwards.   
    */
    public void deleteJunction(){
        junctionRecorderArray.remove(junctionCounter-1);
        junctionCounter--;
    }


    /**
    * Retrieves the heading of the most recent junction, and deletes the JunctionRecorderFinale object.
    * This is the equivalent of popping the stack.
    * An IndexOutOfBoundsException is thrown and caught in the case where the robot has explored all
    * possible paths and has not reached the target. 
    * In such a case, the target has probably been purposely blocked for testing purposes.
    *   
    * @return header  the header of the most recent junction.
    */
    public int retrieveJunctionHeader() {
        int header;

        try{
            /* Retrieve junction header */
            JunctionRecorderFinale currentJunction = junctionRecorderArray.get(junctionCounter-1);
            header = currentJunction.getArrived();
            deleteJunction();
        } catch(Exception e){
            System.out.println("TargetUnreachableException: No more explorable paths.");
            header = IRobot.NORTH;      // Set header to NORTH
        }
        
        return header;
    }

}




/**
* JunctionRecorderFinale is a junction to be stored.  
* Each junction has only an arrived from header.
* 
*/
class JunctionRecorderFinale{

    private int arrived;        // Heading the robot first arrived from (Absolute)


    /**
    * JunctionRecorderFinale constructor
    */
    public JunctionRecorderFinale(int arrived){
        this.arrived = reverseHeading(arrived);     // Absolute heading as per the specification (reversed heading)
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
    * Gets the inital header of the robot when it first arrived. 
    * This is just a reverse of the heading which is actually stored.
    * It returns the same value as robot.getHeading() for a junction.
    * It is required for persistent junctions because the robot is retracing its steps
    * from the start.
    *
    * @return arrived the header from which the robot initially arrived at the junction
    */
    public int getPersistentArrived(){
        return reverseHeading(arrived);
    }


    /**
    * Gets the absolute heading, which is the reverse of the heading parameter.
    * It uses the +/- 2 header relationship to reverse headings required for the junctionRecorderArray
    *
    * @param  heading  the heading to be reversed
    * @return absDir   the absolute heading
    */
    private int reverseHeading(int heading){
        int absDir;

        if( heading == IRobot.NORTH || heading == IRobot.EAST ){
            absDir = heading + 2;
        } else{
            absDir = heading - 2;
        }
        
        return absDir;
    }

}
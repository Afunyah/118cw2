
/*
* Marcel Afunyah - u2015484
* CS118 Coursework 2 Ex1 Preamble
*
* The methods for checking exits (passageExits etc) are implemented in the pathTypeCheck method. This method loops through all four  
* directions for a given pathtype (eg IRobot.PASSAGE), as the parameter, and returns the number of exits for that path type.
*
* For the four controller methods (atDeadend etc), a do-while look checks each direction for a specified path type and selects a 
* direction based on the required conditions.
* I combined the atJunction and atCrossroads function since they both require the same conditions. Essentially, they are the same.
*
* The JunctionRecorderEx1 class allows junctions to be created as objects with its x and y coordinates, and the absolute heading from 
* which the robot arrived at the junction.
*
* The RobotDataEx1 class contains an ArrayList which stores the JunctionRecorderEx1 objects when they are created. It also contains the
* method searchJunction which is used when backtracking.
*
* The explorer controller simply chooses a direction based on the number of exits, using the four controller methods (atDeadend etc)
* It also calls RobotDataEx1.recordJunction(x, y, arrived) when the robot encounters a new junction, therby storing the data.
* The explorer mode is set to 0 when a deadend is encountered. The direction is set to IRobot.BEHIND and the next step is handles
* by the backtracker.
*
* The backtracker chooses directions when backtracking. It can invoke RobotDataEx1.searchJunction(x, y) to obtain the 
* arrived from header of a junction and continue backtracking. It also sets the explorer mode to 1 and faces the direction of the 
* passage when appropriate.
*
* The controlRobot method switches between the explorer and backtracker based on the explorer mode, after every step
*
* The worst case analysis. As long as the target is reachable, the robot will always find the target in a Prim maze, but not in a 
* loopy maze.
* The maximum number of steps occurs when the worst possible path is taken. All the intersections and deadends would have been 
* explored and backtracked. Also, the robot would start adjacent to the target. It would the explore the rest of the maze and backtrack
* to the target.
*
* By analysing some mazes, I concluded that for any [N x M] maze, the worst setup would be a 'spinal arrangement', wherby after 
* every other step along (one and only one of) the length or height, there is a crossroads. Two exits will lead to deadends 
* and the other two lead to the next crossroads. The terminating crossroads at the end will have just 3 exits.
*
* From calculating the maximum number of steps for 2x1, 2x2, 2x3, 2x4; 3x1, 3x2, 3x3, 3x4 and more, it can be seen that everything  
* simplifies down to an arithmetic sequence. 
* Note that a maze of length 2 actually has 3 movable spaces , length 3 has 5 movable spaces, length 4 has 7 .... (horizontally) 
*
* The maximum number of steps for an [N x M] Prim maze is (4*N*M - 5). Tried and tested, to some degree. 
* 
*/


import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;


/**
* Ex1 implements a robot which stores junction data and has the ability to backtrack.
* The data stored is the x and y coordinates of the junction and the arrived from heading.
*
* @author Marcel Afunyah
* @version 1.0
*/
public class Ex1{

    private int pollRun = 0;                // Incremented after each pass
    private RobotDataEx1 RobotDataEx1;      // Data store for junctions
    private int explorerMode;               // 1 = explore, 0 = backtrack

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};


    /**
    * Initializes and creates data store for new mazes. 
    * Selects a controller based on exploreMode.
    *
    * @param robot the IRobot under control
    */
    public void controlRobot(IRobot robot){

        /* On the first move of the first run of a new maze */
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            RobotDataEx1 = new RobotDataEx1();      // Reset the data store
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
    public void reset(){
        RobotDataEx1.resetJunctionData();
        explorerMode = 1;
        pollRun = 0;
    }


    /**
    * Decides the robot's next move when exploreMode is 1. 
    * Also stores new junction when exploring.
    *
    * @param robot the IRobot under control
    */
    private void exploreControl(IRobot robot){

        int direction = 0;
        int beenBefores = pathTypeCheck(robot, IRobot.BEENBEFORE);
        int exits = pathTypeCheck(robot, IRobot.WALL);

        /* Check for and store new junction */
        if ( (beenBefores == 1) && (exits > 2) ){     
            RobotDataEx1.recordJunction(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
        }

        /* Choose a direction based on the number of exits */
        switch (exits){
            case 1: 
                    if (beenBefores == 1){ 
                        explorerMode = 0;       // The next step will be a backtrack
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
    * Is able to search for junctions in the data store and backtrack in the arrived from header direction.
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
                // Search and retrieve junction heading when there is no passage
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
* RobotDataEx1 contains a data store, variables and methods for working with this data store. 
* There should be only one RobotDataEx1 object at any point during runtime. 
*/
class RobotDataEx1 {

    private static int junctionCounter = 0;                                                                         // Number of junctions stored
    private static ArrayList<JunctionRecorderEx1> junctionRecorderArray = new ArrayList<JunctionRecorderEx1>();     // Stores JunctionRecorderEx1 objects


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
    * Stores a junction and its data as a JunctionRecorder object in the junctionRecorderArray.
    *
    * @param robotX   the x-coordinate of the junction to be stored
    * @param robotY   the y-coordinate of the junction to be stored     
    * @param arrived  the heading of the robot when it first arrived at the junction
    */
    public void recordJunction(int robotX, int robotY, int arrived) {

        JunctionRecorderEx1 newJunction = new JunctionRecorderEx1(robotX, robotY, arrived);
        junctionRecorderArray.add(newJunction);
        junctionCounter++;      //Increment for the recording of the next junction
    }


    /**
    * Searches for a junction in the junctionRecorderArray.
    *
    * @param  robotX   the x-coordinate of the junction to be searched for
    * @param  robotY   the y-coordinate of the junction to be searched for   
    * @return heading  the absolute heading from which the robot first arrived at the junction
    */
    public int searchJunction(int robotX, int robotY) {

        int heading = 0;

        /* Search for junction, starting from first junction */
        for(int i = 0; i < junctionRecorderArray.size(); i++){
            JunctionRecorderEx1 currentJunction = junctionRecorderArray.get(i);

            if ( (currentJunction.getJuncX() == robotX) && (currentJunction.getJuncY() == robotY) ){
                heading = currentJunction.getArrived();
                break;
            }
        }
        return heading;
    }


}




/**
* JunctionRecorderEx1 is a junction to be stored.  
* Each junction has its x-coordinate, y-coordinate and arrived from header.
* 
*/
class JunctionRecorderEx1 {

    private int juncX;      // X-coordinates of the junctions
    private int juncY;      // Y-coordinates of the junctions
    private int arrived;    // Heading the robot first arrived from (Absolute)


    /**
    * JunctionRecorderEx1 constructor
    */
    public JunctionRecorderEx1(int juncX, int juncY, int arrived){

        this.juncX = juncX;
        this.juncY = juncY;
        this.arrived = getAbsoluteHeading(arrived);        // Absolute heading as per the specification (reversed heading)

    }


    /**
    * Gets the x-coordinate of the stored junction.
    *
    * @return juncX the x-coordinate of the stored junction
    */
    public int getJuncX(){
        return juncX;
    }


    /**
    * Gets the y-coordinate of the stored junction.
    *
    * @return juncY the x-coordinate of the stored junction
    */
    public int getJuncY(){
        return juncY;
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
import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;

//Function that checks all 4 walls for a specific condition eg passage wall beenbefore
//Check feedback to confirm whether random approach of selecting exits was appropriate
//Better explore control backtrack cintrol interface 
//numeric notation for absoslute heading
//arraylists for max junctions etc
//ARRIVED STRING FOR LOOP EXERCISE 1
//WHAT HAPEN WHEN START ON JUNCTION CENTR EXERCISE 1

//Switching controllers within themselves maybe is not the best solution. get better inerfacing.

public class Ex3 {

    private int pollRun = 0; // Incremented after each pass
    private RobotDataEx3 RobotDataEx3; // Data store for junctions
    private int explorerMode; // 1 = explore, 0 = backtrack

    int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};

    public void controlRobot(IRobot robot) {

        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            RobotDataEx3 = new RobotDataEx3(); //reset the data store
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
        RobotDataEx3.resetJunctionData();
        explorerMode = 1;
    }


    private void exploreControl(IRobot robot) {
        int direction = 0;
        int beenBefores = beenBeforeExits(robot);
        int exits = nonwallExits(robot);

        

        // if ( (beenBefores == 1) && (exits > 2) ){    //at a previously unecountered junction/crossroad 
        // System.out.println("rh: "+ robot.getHeading());
        //     RobotDataEx3.recordJunctionHeader(robot.getHeading());
        //     //JunctionRecorder.printJunction();
        // }
    //  System.out.println(beenBefores);
    //  System.out.println(exits);
        switch (exits){
            case 1: 
                    if (beenBefores == 1){ 
                        explorerMode = 0;
                        backtrackControl(robot);
                    }else{
                        direction = atDeadEnd(robot);
                        robot.face(direction);
                    }      
                    break;

            case 2: direction = atCorridor(robot);
                    robot.face(direction);
                    // if (beenBefores == 2){
                    //     explorerMode = 0;
                    // } always prevents 3 from running
                    break;

            case 3: 
                    // if (beenBefores == 2){
                    //     explorerMode = 0;
                    //     backtrackControl(robot);
                    // }
                     if (beenBefores <= 2){
                         RobotDataEx3.recordJunctionHeader(robot.getHeading());
                        direction = atJunction(robot);
                        robot.face(direction);
                    }

                    else if (beenBefores == 3){
                        System.out.println("ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
                        direction = IRobot.BEHIND;
                        robot.face(direction);
                        explorerMode = 0;
                    }
                    break;

            case 4: 
            default:
                    // if (beenBefores == 4){
                    //     explorerMode = 0;
                    //     backtrackControl(robot);
                    // }//bb 2 as BTing? so no need here? 
                     if (beenBefores == 1){
                         RobotDataEx3.recordJunctionHeader(robot.getHeading());
                        direction = atCrossroad(robot);
                        robot.face(direction);
                    }
                    else if (beenBefores == 3 ){
                        direction = IRobot.BEHIND;
                        robot.face(direction);
                        explorerMode = 0;
                    }
                    else if (beenBefores == 4){
                        direction = IRobot.BEHIND;
                        robot.face(direction);
                        explorerMode = 0;
                    }
                    break;
        }


        // if(explorerMode == 0){
        //     backtrackControl(robot);
        // }else{
        //     robot.face(direction);
        // }
        
    }


//for junctions, if passages are 0 and exploring, just reverse and BT, [normal: bt and exit through first entrance]

//for cr, if passages are less than 2 (0 or 1) and exploring, just reverse and BT, [normal: 0-bt and exit through first entrance. 1-expl exit through passage]

//redesign ex and bt handling

    public void backtrackControl(IRobot robot) {
        int direction = 0;
        int passages = passageExits(robot);
        int exits = nonwallExits(robot);
        int beenBefores = beenBeforeExits(robot);

        if (exits > 2){
            if (passages > 0){
                direction = atJunction(robot);
                robot.face(direction);
                explorerMode = 1;
            }
            else{
                int heading = RobotDataEx3.retrieveJunctionHeading();
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



class RobotDataEx3 {

    private static int junctionCounter = 0; // No. of junctions stored
    private static ArrayList<JunctionHeaderEx3> junctionHeaderArray = new ArrayList<JunctionHeaderEx3>();

    public static int getJunctionCounter(){
        return junctionCounter;
    }


    public void resetJunctionData() {
        junctionCounter = 0;
        junctionHeaderArray.clear();
    }


    public void recordJunctionHeader(int arrived) {
        
        JunctionHeaderEx3 newJunctionHeader = new JunctionHeaderEx3(arrived);
        newJunctionHeader.printJunctionHeader();

        junctionHeaderArray.add(newJunctionHeader);
        System.out.println("Storing: " + junctionHeaderArray.get(junctionCounter) + "  " + junctionCounter + " "+ (junctionHeaderArray.get(junctionCounter)).getArrived());
        junctionCounter++;
    }


    public void deleteJunctionHeader(){
        System.out.println("Deleting: " + junctionHeaderArray.get(junctionCounter-1));
        junctionHeaderArray.remove(junctionCounter-1);

        System.out.println("Counter from: " + junctionCounter + " to " + (junctionCounter-1));
        junctionCounter--;
    }


    public int retrieveJunctionHeading() {
        int header;
        try{
            JunctionHeaderEx3 currentJunction = junctionHeaderArray.get(junctionCounter-1);
            header = currentJunction.getArrived();
            System.out.println("Retrieving "+ currentJunction + "  "+(junctionCounter-1) + " " + header);
            deleteJunctionHeader();
        } catch(Exception e){
            System.out.println("TargetUnreachableException: No more explorable paths.");
            header = IRobot.NORTH;
        }
        
        return header;
    }


}



class JunctionHeaderEx3{

    private int arrived;

    public JunctionHeaderEx3(int arrived){
        this.arrived = getAbsoluteHeading(arrived);
    }

    public int getArrived(){
        System.out.println(arrived);
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

    public void printJunctionHeader(){
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


        System.out.println("Junction " + (RobotDataEx3.getJunctionCounter()+1) +  " heading "+ arrivedString);
    }

}
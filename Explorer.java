import uk.ac.warwick.dcs.maze.logic.IRobot;

//DEFINE LOOKDIRECTIONS AS GLOBAL? WATCH LECTURE VIDS

public class Explorer {
    public void controlRobot(IRobot robot) {

        int direction = 0;
        int exits = nonwallExits(robot);

        switch (exits){
            case 1: direction = atDeadEnd(robot);       
                    break;
            case 2: direction = atCorridor(robot);
                    break;
            case 3: direction = atJunction(robot);
                    break;
            case 4: 
            default:
                    direction = atCrossroad(robot);
                    break;
        }

        robot.face(direction);
        
    }


    private int nonwallExits (IRobot robot){

        int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
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

        int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
        int passages = 0;

        for(int i = 0; i < 4; i++){
			if( robot.look(lookDirections[i]) == IRobot.PASSAGE ){		//check all 4 directions for passage
				passages++;		//if passage, increase passages count
			}
			else continue;		//if no passage, check next direction
		}

        return passages;

    }


    private int atDeadEnd (IRobot robot){

        int direction;
        int passages = passageExits(robot); //either 0 or 1
    
        switch (passages){
            case 0: direction = IRobot.BEHIND;
                    break;
            case 1:
            default: direction = IRobot.AHEAD;  
                    break;
        }

        return direction;
    }



    private int atCorridor (IRobot robot){

        int direction;
        // Tutor said maybe it does not matter to be random 
        // int passages = passageExits(robot); //either 0, 1 or 2
        // int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};

        // switch (passages){
        //     case 0:
        //     case 1:
        //         if (robot.look(IRobot.LEFT) != IRobot.WALL){
        //             direction = IRobot.LEFT;
        //         }
        //         else if (robot.look(IRobot.RIGHT) != IRobot.WALL){
        //             direction = IRobot.RIGHT;
        //         }
        //         else{
        //             direction = IRobot.AHEAD;
        //         } 
        //         break;
            
        //     case 2:
        //         //random, what about when facing a wall and starting, so not forward or behind

        // } 

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

        int direction = 0;      //intial value will be overwritten
        int passages = passageExits(robot);     //either 0, 1, 2, or 3 

        int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
        int randno;


        switch (passages){
            
            case 0:
                do {
                    randno = (int) (Math.random()*4); //probabilty is reduced but still the same for the 3 options
                    direction = lookDirections[randno];;
                } while (robot.look(direction) == IRobot.WALL);
                break;

            case 1: 
            case 2:
            case 3:
            default:
                do {
                    //probabilty is reduced but still the same for the 1, 2 or 3 options. This method might be slower than [finding out the viable directions and then picking randomly]
                    randno = (int) (Math.random()*4);    
                    direction = lookDirections[randno];
                } while ( robot.look(direction) != IRobot.PASSAGE );

                break;        
        }

        return direction;
    }



    private int atCrossroad (IRobot robot){

        int direction = 0;  //intial value will be overwritten
        int passages = passageExits(robot);     //either 0, 1, 2, 3 or 4

        int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
        int randno;

        switch (passages){

            case 0:
                randno = (int) (Math.random()*4);
                direction = lookDirections[randno];
                break;

            case 1: 
            case 2: 
            case 3: 
            case 4:
            default:
                do {
                    //probabilty is reduced but still the same for the 1, 2 or 3 options. This method might be slower than [finding out the viable directions and then picking randomly]
                    randno = (int) (Math.random()*4);    
                    direction = lookDirections[randno];
                } while ( robot.look(direction) != IRobot.PASSAGE );
                break;  
        }

        return direction;
    }


}
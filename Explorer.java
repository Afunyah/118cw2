import uk.ac.warwick.dcs.maze.logic.IRobot;



public class Explorer {
    public void controlRobot(IRobot robot) {

        int direction;
        int exits = nonwallExits(robot);

        switch (exits){
            case 1: direction = atDeadEnd(robot);       
                    break;
            case 2: direction = atCorridor(robot);
                    break;
            case 3: direction = atJunction(robot);
                    break;
            case 4: direction = atCrossroad(robot);
                    break;
        }

    }


    private int nonwallExits (IRobot robot){

        int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
        int exits = 0;

        for(int i = 0; i < 4; i++){
			if(robot.look(lookDirections[i]) != IRobot.WALL){		//check all 4 directions for exit
				exits++;		//if exitable, increase exits count
			}
			else continue;		//if not exitable, check next direction
		}

        return exits;
    }

    private int passageExits (IRobot robot){

        int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
        int passages = 0;

        for(int i = 0; i < 4; i++){                                 //DO I REALLY NEED FIRST CONDITION SINCE LIKE THE FOLLOW WILL ONLY EVER BE CALLED COS FIRST COND IS CHECKED IN EXIT FUNC
			if( robot.look(lookDirections[i]) == IRobot.PASSAGE ){		//check all 4 directions for passage
				passages++;		//if passage, increase passages count
			}
			else continue;		//if no passage, check next direction
		}

        return passages;

    }


    private int atDeadEnd (IRobot robot){

        int direction;
        int passages = passageExits(robot);     //If atDeadEnd is being called, passagesExits returns either 0 or 1
    
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

        if (robot.look(IRobot.LEFT) != IRobot.WALL){
            direction = IRobot.LEFT;
        }
        else if (robot.look(IRobot.RIGHT) != IRobot.WALL){
            direction = IRobot.RIGHT;
        }
        else{
            direction = IRobot.RIGHT;
        }

        return direction;

    }



    private int atJunction (IRobot robot){

        int direction = 0;      //intial value will be overwritten
        int passages = passageExits(robot);

        int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
        int randno;


        switch (passages){
            
            case 0:
                do {
                    randno = (int) (Math.random()*4); //probabilty is reduced but still the same for the 3 options
                    direction = lookDirections[randno];;
                } while (robot.look(direction) == IRobot.WALL);

                break;

            // case 1: //Can happen when robot starts in the middle of junction, and later returns from a different passage, leaving only one viable exit
            //     for (int i = 0; i < 4; i++){
            //         if ( (robot.look(lookDirections[i]) == IRobot.PASSAGE) ){
            //             direction = lookDirections[i];
            //         }
            //     }
            //     break;

            case 1: 
            case 2:
            case 3:
            default:
                do {
                    //probabilty is reduced but still the same for the 1, 2 or 3 options. This method might be slower than finding out the viable directions and then picking randomly
                    randno = (int) (Math.random()*4);    
                    direction = lookDirections[randno];
                } while ( robot.look(direction) != IRobot.PASSAGE );

                break;        
        }

        return direction;
    }



    private int atCrossroad (IRobot robot){

        int direction = 0;  //just to initialize
        int passages = passageExits(robot);

        int[] lookDirections = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
        int randno;

        switch (passages){

            case 0:
                randno = (int) (Math.random()*4);
                direction = lookDirections[randno];
                break;

            // case 1:
            //     for (int i = 0; i < 4; i++){
            //         if ( robot.look(lookDirections[i]) == IRobot.PASSAGE ){
            //             direction = lookDirections[i];
            //         }
            //     }
            //     break;

            case 1: 
            case 2: 
            case 3: 
            case 4:
            default:
                do {
                    //probabilty is reduced but still the same for the 1, 2 or 3 options. This method might be slower than finding out the viable directions and then picking randomly
                    randno = (int) (Math.random()*4);    
                    direction = lookDirections[randno];
                } while ( robot.look(direction) != IRobot.PASSAGE );

                break;  
        }

        return direction;
    }



}
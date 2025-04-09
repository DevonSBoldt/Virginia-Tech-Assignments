# multiAgents.py
# --------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


from util import manhattanDistance
from game import Directions
import random, util

from game import Agent

class ReflexAgent(Agent):
    """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
    """


    def getAction(self, gameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {NORTH, SOUTH, WEST, EAST, STOP}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices) # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        newFood = successorGameState.getFood()
        newGhostStates = successorGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]


        foodList = newFood.asList()  
        ghostPositions = []

        for ghost in newGhostStates:
            ghostPosition = ghost.getPosition()[0], ghost.getPosition()[1]
            ghostPositions.append(ghostPosition)

        isScared = newScaredTimes[0] > 0

        if not isScared and (newPos in ghostPositions):
            return -1.0

        if newPos in currentGameState.getFood().asList():
            return 1

        sortedFoodDistances = sorted(foodList, key=lambda food: util.manhattanDistance(food, newPos))
        sortedGhostDistances = sorted(ghostPositions, key=lambda ghost: util.manhattanDistance(ghost, newPos))

        foodDistance = lambda food: util.manhattanDistance(food, newPos)
        ghostDistance = lambda ghost: util.manhattanDistance(ghost, newPos)

        return 1 / foodDistance(sortedFoodDistances[0]) - 1 / ghostDistance(sortedGhostDistances[0])

     

def scoreEvaluationFunction(currentGameState):
    """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
    """
    return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
    """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
    """

    def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
        self.index = 0 # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)

class MinimaxAgent(MultiAgentSearchAgent):
    """
    Your minimax agent (question 2)
    """

    def getAction(self, game_state):
        """
        Returns the minimax action from the current gameState using self.depth
        and self.evaluationFunction.

        Here are some method calls that might be useful when implementing minimax.

        gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

        gameState.generateSuccessor(agentIndex, action):
        Returns the successor game state after an agent takes an action

        gameState.getNumAgents():
        Returns the total number of agents in the game

        gameState.isWin():
        Returns whether or not the game state is a winning state

        gameState.isLose():
        Returns whether or not the game state is a losing state
        """

        def max_level(state, depth):
            """Handles the maximizing player's (Pacman's) turn in Minimax."""
            next_depth = depth + 1
            if state.isWin() or state.isLose() or next_depth == self.depth:
                return self.evaluationFunction(state)

            best_score = float('-inf')
            legal_actions = state.getLegalActions(0)

            for action in legal_actions:
                successor_state = state.generateSuccessor(0, action)
                best_score = max(best_score, min_level(successor_state, next_depth, 1))

            return best_score

        def min_level(state, depth, agent_index):
            """Handles the minimizing player's (ghosts') turn in Minimax."""
            if state.isWin() or state.isLose():
                return self.evaluationFunction(state)

            worst_score = float('inf')
            legal_actions = state.getLegalActions(agent_index)

            for action in legal_actions:
                successor_state = state.generateSuccessor(agent_index, action)
                if agent_index == state.getNumAgents() - 1:
                    worst_score = min(worst_score, max_level(successor_state, depth))
                else:
                    worst_score = min(worst_score, min_level(successor_state, depth, agent_index + 1))

            return worst_score

        best_action = None
        highest_score = float('-inf')

        legal_actions = game_state.getLegalActions(0)

        for action in legal_actions:
            successor_state = game_state.generateSuccessor(0, action)
            score = min_level(successor_state, 0, 1)

            if score > highest_score:
                best_action = action
                highest_score = score

        return best_action


class AlphaBetaAgent(MultiAgentSearchAgent):
    """
    Your minimax agent with alpha-beta pruning (question 3)
    """

    def getAction(self, gameState):
        """
        Returns the minimax action using self.depth and self.evaluationFunction
        """
        "*** YOUR CODE HERE ***"
        def maxLevel(gameState,depth,alpha, beta):
            currDepth = depth + 1
            if gameState.isWin() or gameState.isLose() or currDepth==self.depth:
                return self.evaluationFunction(gameState)
            maxvalue = -999999
            actions = gameState.getLegalActions(0)
            alpha1 = alpha
            for action in actions:
                successor= gameState.generateSuccessor(0,action)
                maxvalue = max (maxvalue,minLevel(successor,currDepth,1,alpha1,beta))
                if maxvalue > beta:
                    return maxvalue
                alpha1 = max(alpha1,maxvalue)
            return maxvalue
        
        #For all ghosts.
        def minLevel(gameState,depth,agentIndex,alpha,beta):
            minvalue = 999999
            if gameState.isWin() or gameState.isLose():  
                return self.evaluationFunction(gameState)
            actions = gameState.getLegalActions(agentIndex)
            beta1 = beta
            for action in actions:
                successor= gameState.generateSuccessor(agentIndex,action)
                if agentIndex == (gameState.getNumAgents()-1):
                    minvalue = min (minvalue,maxLevel(successor,depth,alpha,beta1))
                    if minvalue < alpha:
                        return minvalue
                    beta1 = min(beta1,minvalue)
                else:
                    minvalue = min(minvalue,minLevel(successor,depth,agentIndex+1,alpha,beta1))
                    if minvalue < alpha:
                        return minvalue
                    beta1 = min(beta1,minvalue)
            return minvalue

        # Alpha-Beta Pruning
        actions = gameState.getLegalActions(0)
        currentScore = -999999
        returnAction = ''
        alpha = -999999
        beta = 999999
        for action in actions:
            nextState = gameState.generateSuccessor(0,action)
            score = minLevel(nextState,0,1,alpha,beta)
            if score > currentScore:
                returnAction = action
                currentScore = score
            if score > beta:
                return returnAction
            alpha = max(alpha,score)
        return returnAction

class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 4)
    """

    def getAction(self, gameState):
        """
        Returns the expectimax action using self.depth and self.evaluationFunction.
        All ghosts are modeled as choosing uniformly at random from their legal moves.
        """

        def maxLevel(state, depth):
            nextDepth = depth + 1
            if state.isWin() or state.isLose() or nextDepth == self.depth: 
                return self.evaluationFunction(state)

            actions = state.getLegalActions(0)
            if not actions:
                return self.evaluationFunction(state)

            return max(expectLevel(state.generateSuccessor(0, action), nextDepth, 1) for action in actions)

        def expectLevel(state, depth, agentIndex):
            if state.isWin() or state.isLose():  
                return self.evaluationFunction(state)

            actions = state.getLegalActions(agentIndex)
            if not actions:
                return 0

            numAgents = state.getNumAgents()
            nextAgent = agentIndex + 1
            totalValue = sum(
                maxLevel(state.generateSuccessor(agentIndex, action), depth)
                if agentIndex == numAgents - 1 else
                expectLevel(state.generateSuccessor(agentIndex, action), depth, nextAgent)
                for action in actions
            )

            return totalValue / len(actions)

        legalActions = gameState.getLegalActions(0)
        if not legalActions:
            return None

        return max(legalActions, key=lambda action: expectLevel(gameState.generateSuccessor(0, action), 0, 1))



def betterEvaluationFunction(currentGameState):

    pacmanPos = currentGameState.getPacmanPosition()
    foodGrid = currentGameState.getFood()
    ghostStates = currentGameState.getGhostStates()
    scaredTimes = [ghost.scaredTimer for ghost in ghostStates]

    # Compute Manhattan distances to all food positions
    foodDistances = [manhattanDistance(pacmanPos, foodPos) for foodPos in foodGrid.asList()]
    totalFoodDistance = sum(foodDistances) if foodDistances else 1  # Avoid division by zero
    
    # Compute Manhattan distances to all ghost positions
    ghostDistances = [manhattanDistance(pacmanPos, ghost.getPosition()) for ghost in ghostStates]
    totalGhostDistance = sum(ghostDistances) if ghostDistances else 1  # Avoid division by zero

    numPowerPellets = len(currentGameState.getCapsules())
    numRemainingFood = len(foodGrid.asList())

    # Base score starts from game state score
    score = currentGameState.getScore()

    # Encourage food collection
    score += (1.0 / totalFoodDistance) + numRemainingFood

    # Adjust score based on ghost proximity and scared state
    if sum(scaredTimes) > 0:
        score += sum(scaredTimes) - numPowerPellets - totalGhostDistance
    else:
        score += totalGhostDistance + numPowerPellets

    return score

# Abbreviation
better = betterEvaluationFunction

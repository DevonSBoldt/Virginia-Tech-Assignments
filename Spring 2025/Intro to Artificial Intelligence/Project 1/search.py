# search.py
# ---------
# Licensing Information: You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
#
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).

"""
In search.py, you will implement generic search algorithms which are called by
Pacman agents (in searchAgents.py).
"""

import util
from util import Stack, Queue, PriorityQueue
from game import Directions


class SearchProblem:
    """
    This class outlines the structure of a search problem, but doesn't implement
    any of the methods (in object-oriented terminology: an abstract class).

    You do not need to change anything in this class, ever.
    """

    def getStartState(self):
        """Returns the start state for the search problem."""
        util.raiseNotDefined()

    def isGoalState(self, state):
        """
        state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()

    def getSuccessors(self, state):
        """
        state: Search state

        For a given state, this should return a list of triples, (successor,
        action, stepCost), where 'successor' is a successor to the current
        state, 'action' is the action required to get there, and 'stepCost' is
        the incremental cost of expanding to that successor.
        """
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
        actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()


def tinyMazeSearch(problem):
    """
    Returns a sequence of moves that solves tinyMaze. For any other maze, the
    sequence of moves will be incorrect, so only use this for tinyMaze.
    """
    from game import Directions
    s = Directions.SOUTH
    w = Directions.WEST
    return [s, s, w, s, w, w, s, w]


def depthFirstSearch(problem):
    """Search the deepest nodes in the search tree first using a stack."""

    frontier = Stack()
    frontier.push((problem.getStartState(), []))
    explored = set()

    while not frontier.isEmpty():
        state, path = frontier.pop()

        if state in explored:
            continue

        if problem.isGoalState(state):
            return path  # Return the path if goal is found

        explored.add(state)  # Mark state as explored

        for successor, action, _ in problem.getSuccessors(state):
            if successor not in explored:
                frontier.push((successor, path + [action]))

    return []  # Return empty if no solution is found


def breadthFirstSearch(problem):
    """Search the shallowest nodes in the search tree first using a queue."""

    fringe = Queue()
    fringe.push(problem.getStartState())
    visited = []
    tempPath = []
    path = []
    pathToCurrent = Queue()

    currState = fringe.pop()

    while not problem.isGoalState(currState):
        if currState not in visited:
            visited.append(currState)
            successors = problem.getSuccessors(currState)

            for child, direction, cost in successors:
                fringe.push(child)
                tempPath = path + [direction]
                pathToCurrent.push(tempPath)

        currState = fringe.pop()
        path = pathToCurrent.pop()

    return path


def uniformCostSearch(problem):
    """Search the node of least total cost first using a priority queue."""

    frontier = PriorityQueue()
    frontier.push((problem.getStartState(), [], 0), 0)
    explored = {}

    while not frontier.isEmpty():
        state, path, cost = frontier.pop()

        if state in explored and explored[state] <= cost:
            continue

        explored[state] = cost

        if problem.isGoalState(state):
            return path

        for successor, action, step_cost in problem.getSuccessors(state):
            new_cost = cost + step_cost
            frontier.push((successor, path + [action], new_cost), new_cost)

    return []


def nullHeuristic(state, problem=None):
    """
    A heuristic function estimates the cost from the current state to the nearest
    goal in the provided SearchProblem. This heuristic is trivial.
    """
    return 0


def aStarSearch(problem, heuristic=nullHeuristic):
    """Search the node that has the lowest combined cost and heuristic first."""

    fringe = PriorityQueue()
    fringe.push(problem.getStartState(), 0)
    currState = fringe.pop()
    visited = []
    tempPath = []
    path = []
    pathToCurrent = PriorityQueue()

    while not problem.isGoalState(currState):
        if currState not in visited:
            visited.append(currState)
            successors = problem.getSuccessors(currState)

            for child, direction, cost in successors:
                tempPath = path + [direction]
                costToGo = problem.getCostOfActions(tempPath) + heuristic(child, problem)

                if child not in visited:
                    fringe.push(child, costToGo)
                    pathToCurrent.push(tempPath, costToGo)

        currState = fringe.pop()
        path = pathToCurrent.pop()

    return path


# Abbreviations
bfs = breadthFirstSearch
dfs = depthFirstSearch
astar = aStarSearch
ucs = uniformCostSearch

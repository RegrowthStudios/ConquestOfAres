package Game;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.*;

import Generation.MapData;
import Generation.MapGenerator;
import Generation.MapGenerationParams;
import Graphics.Quadrilateral;
import Graphics.SpriteBatchSystem;
import Utils.Device;
import utkseniordesign.conquestofares.GameActivity;

/**
 * Created by brb55_000 on 2/6/2015.
 */

/// Contains the game logic core
public class GameController {
    private GameState m_gameState = null; ///< Handle to game
    private GameSettings m_gameSettings = null; ///< Settings
    private GameEngine m_gameEngine = new GameEngine(); ///< Initializes the game
    private Player m_currentPlayer = null;
    public Boolean stateHasChanged = false;

    /// Initializes a game by setting up game state and map
    public void initGame(GameState gameState, GameSettings gameSettings) {
        // Set handles so we don't have to pass shit around everywhere
        m_gameState = gameState;
        m_gameState.currentState = GameState.State.GAME_START;
        m_gameSettings = gameSettings;
        // Initialize the game
        m_gameEngine.initGame(m_gameState, m_gameSettings, this);
        m_gameState.currentPlayerIndex = 0;
        m_currentPlayer = m_gameState.players.get(m_gameState.currentPlayerIndex);
    }

    public GameState getGameState(){
        return m_gameState;
    }

    public Player getCurrentPlayer() { return m_currentPlayer; }

    /// Call this to transition to the next turn
    public void nextTurn() {
        Debug.logState(m_gameState);
        // Go to next player (starts at -1)
        m_gameState.currentPlayerIndex++;
        Debug.logRound(m_gameState);
        if(m_gameState.currentPlayerIndex / m_gameState.players.size() > 0){
            m_gameState.currentState = GameState.State.PLACING_UNITS;
        }
        m_currentPlayer = m_gameState.players.get(m_gameState.currentPlayerIndex % m_gameState.players.size());
        if(m_gameState.currentState == GameState.State.PLACING_UNITS) {
            m_currentPlayer.placeableUnits = 0;
            //m_currentPlayer.placeableUnits = m_currentPlayer.territories.size() / 2;
        }
        // Check if we should do AI
        if (m_currentPlayer.isAI) {
            // TODO: Do AI stuff
            nextTurn(); // Recursively go to next turn
            return;
        }
        // Current player is human, he is now placing units
        Debug.logState(m_gameState);
    }

    public void stepState(){
        stateHasChanged = true;
        switch(m_gameState.currentState){
            case PLACING_UNITS:
                m_gameState.currentState = GameState.State.ATTACKING;
                break;
            case ATTACKING:
                m_gameState.currentState = GameState.State.FORTIFYING;
                break;
            case INITIAL_UNIT_PLACEMENT:
                nextTurn();
                break;
            default:
                m_gameState.currentState = GameState.State.PLACING_UNITS;
                nextTurn();
                break;
        }
    }

    /// Call this method when the world is clicked on
    public Territory onClick(float x, float y) {
        Territory territory = getTerritoryAtPoint(x, y);

        if (m_gameState.selectedTerritory == territory) {
            territory.unselect();
            m_gameState.selectedTerritory = null;
        } else if (m_gameState.selectedTerritory == null) {
            territory.select();
            m_gameState.selectedTerritory = territory;
        } else {
            m_gameState.selectedTerritory.unselect();
            territory.select();
            m_gameState.selectedTerritory = territory;
        }
        return territory;
    }

    /// Returns the territory at a specific point
    public Territory getTerritoryAtPoint(float x, float y) {
        return MapGenerator.getClosestTerritory(x, y, m_gameState.mapData);
    }

    public boolean attack(Territory attacker, Territory defender, int numAttackers){
        if(numAttackers == attacker.units.size()){
            return false;
        }
        Action action = new Action(m_currentPlayer, Action.Category.attack, attacker, defender);

        while(defender.units.size() > 0 && numAttackers > 0){
            // I figure we can change the chance of winning based on the type of unit it is, like tanks are weak to airplanes, airplanes are weak to soldiers, and soldiers are weak to tanks
            // kind of like a rock-paper-scissors dynamic

            if(m_gameState.random.nextInt() % 2 == 0){
                action.sUnitsLost.add(attacker.units.get(attacker.units.size()-1));
                numAttackers--;
                attacker.selectedUnits.remove(attacker.selectedUnits.size()-1);
                attacker.units.remove(attacker.units.size()-1);
            }
            else{
                action.dUnitsLost.add(defender.units.get(defender.units.size()-1));
                defender.units.remove(defender.units.size()-1);
            }
        }
        if(defender.units.isEmpty() && numAttackers > 0){
            Player p = defender.owner;
            defender.owner.removeTerritory(defender);
            attacker.owner.addTerritory(defender);
            moveUnits(attacker, defender);
            if(p.territories.isEmpty()){
                m_gameState.players.remove(p);
                if(m_gameState.players.size() == 1){
                    Log.d("WINNER", "Player " + m_gameState.players.get(0).name + " has won!");
                }
            }
        }

        return !attacker.units.isEmpty();
    }

    public void moveUnits(Territory source, Territory destination){
        if(source.selectedUnits.size() == source.units.size()){
            return;
        }
        Action action = new Action(m_currentPlayer, Action.Category.moveUnit, source, destination);

        for(Unit unit : source.selectedUnits ) {
            action.sUnitsLost.add(unit);
            action.dUnitsGained.add(unit);
            source.units.remove(unit);

            if(source.neighbors.contains(destination)){
                unit.destination = destination.getUnitPlace();
                //unit.destination = new PointF(destination.x,destination.y);
            }
            else {
                unit.path = new PathFinding().getPath(source, destination);
                unit.destination = new PointF(unit.path.get(unit.path.size()-1).x,unit.path.get(unit.path.size()-1).y);
            }
            unit.frame = 0;
            unit.location = new PointF(source.x, source.y);

            destination.units.add(unit);
        }

        m_gameState.actions.add(action);

        source.selectedUnits.clear();

        //addUnit(destination, destination.x, destination.y, unit.type);

        //source.owner.unitsInFlight.add(unit);
    }
}

package de.metro.robocod;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import robocode.*;
import robocode.util.Utils;

public class RustyTrumpet extends AdvancedRobot {

    Random rand = new Random();
    static final int SCAN = 0, SEEK = 1, SURROUND = 2;
    int count = 0; // Keeps track of how long we've
    int state = SCAN;
    double gunTurnAmt; // How much to turn our gun when searching
    boolean foundTarget = false;
    ArrayList<Pair> dujmanii;

    /**
     * run: Rusty's main run function
     */
    @Override
    public void run() {
        dujmanii = new ArrayList(getOthers());

        setBodyColor(new Color(218, 165, 32));
        setGunColor(Color.YELLOW);
        setRadarColor(Color.ORANGE);
        setScanColor(Color.ORANGE);
        setBulletColor(Color.WHITE);

        setMaxVelocity(6);

        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        while (true) {
            switch (state) {
                case SCAN:
                    if (dujmanii.size() == getOthers()) {
                        setStop(true);
                        state = SEEK;
                    } else {
                        setTurnRadarLeft(360);
                    }

                    break;

                case SEEK:
                    if (!foundTarget) {
                        setTurnRadarRight(360);
                    }
                    break;
            }

            happyFeet();
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        String currentTarget; // Name of the robot we're currently tracking
        Pair p = new Pair(e.getName(), e.getDistance());
        if (!dujmanii.contains(p)) {
            dujmanii.add(p);
        } else {
            dujmanii.get(dujmanii.indexOf(p)).dist = p.dist;
        }
        Collections.sort(dujmanii, new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                return (int) (o1.dist - o2.dist);
            }

        });
        currentTarget = dujmanii.get(0).name;
        switch (state) {
            case SCAN:
                return;

            case SEEK:
                if (e.getName().equals(currentTarget)) {
                    //clearAllEvents();
                    foundTarget = true;
                    double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();

                    setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
                    rootyTootyPointAndShooty(e);
                } else {
                    foundTarget = false;
                }
                return;

        }

        // ...
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        if(!event.getName().equals(dujmanii.get(0))){
            foundTarget = false;
            dujmanii.clear();
            state = SCAN;
        }
    }

    
    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        dujmanii.clear();
        state = SCAN;
        foundTarget = false;
    }

    /**
     * onHitRobot: Set him as our new target
     */
    /*public void onHitRobot(HitRobotEvent e) {
        // Only print if he's not already our target.
        if (trackName != null && !trackName.equals(e.getName())) {
            out.println("Tracking " + e.getName() + " due to collision");
        }
        // Set the target
        trackName = e.getName();
        // Back up a bit.
        // Note:  We won't get scan events while we're doing this!
        // An AdvancedRobot might use setBack(); execute();
        gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
        turnGunRight(gunTurnAmt);
        fire(3);
        back(50);
    }*/
    /**
     * onWin: Do a victory dance
     *
     * @param e
     */
    @Override
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }

    private void happyFeet() {

        setAhead(Double.MAX_VALUE);
        // Tell the game we will want to turn right 90
        boolean turnDir = rand.nextBoolean();

        if (getTurnRemaining() == 0) {
            if (turnDir) {
                setTurnRight(rand.nextInt(80) + 40);
            } else {
                setTurnLeft(rand.nextInt(80) + 40);
            }
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        // Bounce off!
        setTurnRight(90);
    }

    private void rootyTootyPointAndShooty(ScannedRobotEvent e) {
        double bulletPower = Math.min(3.0, getEnergy());
        double myX = getX();
        double myY = getY();
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
        double enemyHeading = e.getHeadingRadians();
        double enemyVelocity = e.getVelocity();

        double deltaTime = 0;
        double battleFieldHeight = getBattleFieldHeight(),
                battleFieldWidth = getBattleFieldWidth();
        double predictedX = enemyX, predictedY = enemyY;
        while ((++deltaTime) * (20.0 - 3.0 * bulletPower)
                < Point2D.Double.distance(myX, myY, predictedX, predictedY)) {
            predictedX += Math.sin(enemyHeading) * enemyVelocity;
            predictedY += Math.cos(enemyHeading) * enemyVelocity;
            if (predictedX < 18.0
                    || predictedY < 18.0
                    || predictedX > battleFieldWidth - 18.0
                    || predictedY > battleFieldHeight - 18.0) {
                predictedX = Math.min(Math.max(18.0, predictedX),
                        battleFieldWidth - 18.0);
                predictedY = Math.min(Math.max(18.0, predictedY),
                        battleFieldHeight - 18.0);
                break;
            }
        }
        double theta = Utils.normalAbsoluteAngle(Math.atan2(
                predictedX - getX(), predictedY - getY()));

        setTurnRadarRightRadians(
                Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
        setFire(bulletPower);
    }

}

class Pair {

    public Pair(String n, double d) {
        name = n;
        dist = d;
    }
    String name;
    double dist;

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            return ((Pair) o).name.equals(name);
        }
        return false;
    }
}

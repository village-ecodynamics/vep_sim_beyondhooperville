package com.mesaverde.specialization.tasks;

import java.util.HashMap;

import com.mesaverde.hunting.*;
import com.mesaverde.specialization.Constraints;
import com.mesaverde.specialization.Season;
import com.mesaverde.specialization.SpecializedAgent;
import com.mesaverde.specialization.resources.Meat;
import com.mesaverde.specialization.resources.Resource;
import com.mesaverde.village.Agent;
import com.mesaverde.village.Cell;
import com.mesaverde.village.Village;

public class Hunter extends Task {

    public static final double IGNORE = Double.POSITIVE_INFINITY;
    private HuntingStrategy strategy;
    private double hh_protein_need;
    private int hunt_radius;
    private Season season;
    private double C_hunt;
    private Constraints cons; // to keep track of our constraints
    private boolean onlyNeeds = true; // are we going to stop when our personal
    // needs are met
    private double targetProduction = IGNORE; // just for hunting, we find a way
    // to limit production. This way
    // agent doesn't overcompensate
    // when weights increase
    public static int totalKilledDeer;

    public Hunter(SpecializedAgent agt) {
        super(agt);
        strategy = new Strategy(agt);
    }

    /** Currently only using the AlternateHuntingStrategy */
    @Override
    public int performTask(Season season, Constraints cons, boolean onlyNeeds) {
        reset();
        int hunt_cal = 0;

        // get the Meat resource from the agent
        Resource meat = agent.getResource(Meat.class);

        if (meat == null) { // create some if none is there already
            meat = new Meat(0, 0);
            // and add it to the agent
            agent.addResource(meat);
        }

        if (season == Season.SUMMER) {
            int i;

            // This alternative hunting routine is designed to allow the cost of
            // hunting to be tracked
            // and forced on the agents. To do this, hunting has been broken up
            // into
            // several different
            // phases. First the agent looks for a cell to hunt in. This is done
            // in
            // a outwardly
            // radiating pattern from the cells location. Next the agent
            // attempts to
            // find animals in the cell.
            // If animals are found, the agent will then hunt deer in the cell.
            // Once
            // the known deer are depleted,
            // the agent will continue to look in new cells for additional deer.
            // If
            // the agent moves beyond 3 km (a
            // variable number from its location) it will go back over hunted
            // cells
            // and start hunting the hares and
            // rabbits in them.
            Cell c;
            int dx, dy, dw;
            int hunts = 0;
            int tot_deer;
            int known_deer;
            int killed_deer;
            int tot_hares;
            int known_hares;
            int killed_hares;
            int tot_rabbits;
            int known_rabbits;
            int killed_rabbits;
            int numhunts;
            int cont_hunting = 1;
            int hunters = 0;
            double max_hours = 0.0;
            double hunting_time;
            double deer_fix = 0.0;
            C_hunt = 0;

            // cal test
            double DsearchCal;
            double DhuntCal;
            double DcarryCal;
            double HsearchCal;
            double RsearchCal;
            double deer_time = 0;
            double hare_time = 0;
            double rabbit_time = 0;

            DsearchCal = 0;
            DhuntCal = 0;
            DcarryCal = 0;
            HsearchCal = 0;
            RsearchCal = 0;
            agent.deer_hunted = 0; // tracks total animals hunted by agent
            agent.hare_hunted = 0;
            agent.rabbit_hunted = 0;

            hunters = agent.getWorkerSize();

            // now determine how many hours can be spent hunting
            // the max that any agent can work in a year is set at
            // 14 hours per day per individual in household over 7 years of age

            max_hours = hunters * 14 * 365;

            // now subtract last years non hunting hours from this amount
            // to determine time that can be spent hunting
            // hunting_time = max_hours - agent.nonhunt_hrs;
            // DC: Just set the hunting time to that allowed under the
            // constraints
            if (onlyNeeds) {
                hunting_time = max_hours - agent.nonhunt_hrs; // if we're going
                // // time
            } else {
                hunting_time = cons.getAvailableTime();
                hh_protein_need = targetProduction;
            }

            do {
                dx = 0;
                dy = 0;
                dw = 0;

                int[] res = strategy.searchNeighborhoodHuntDX(dx, dy, dw, 0,
                        hunts);
                dx = res[0];
                dy = res[1];
                dw = res[2];
                hunts = res[3];

                if (agent.Hsearchradius < 51) {
                    // accuire cell found
                    c = agent.getCellAt(dx, dy);
                    // if dw = 0 then deer found, 1 = lagomorphs found
                    if (dw == 0) {
                        lock(c.getDeerCell().getLock());
                        
                        // Determine number of deer able to be hunted
                        tot_deer = (int) c.getAnimalTracker().getAmount(Deer.class);

                        double dpercent_found = Village.uniformDblRand(0.3,
                                0.75);
                        known_deer = (int) (tot_deer * dpercent_found);
                        deer_fix = tot_deer * dpercent_found;

                        // DC: BUG: For some reason, the "deer_fix" was
                        // reducing the number of deer the agent found in
                        // some cases
                        if (deer_fix > .50 && known_deer < 1) {
                            known_deer = 1;
                        }

                        double travelCal = agent.calcTravelCal(
                                agent.Hsearchradius, 0, 1.0);
                        hunt_cal += travelCal;
                        C_hunt += travelCal / Village.WORK_CAL_MAN;

                        // JAC 3/06
                        deer_time += travelCal / Village.WORK_CAL_MAN;
                        DsearchCal += travelCal;

                        if (C_hunt >= hunting_time) {
                            known_deer = 0;
                            cont_hunting = 0;
                            unlock(c.getDeerCell().getLock());                           
                            break;
                        }

                        killed_deer = 0;
                        numhunts = 0;

                        // Hunt
                        if (known_deer != 0) {
                            do {
                                // dpercent_kill is the difficulty number
                                // needed
                                // to successfully kill a deer
                                // dpercent_chance is the number compared to
                                // difficulty
                                double dpercent_kill = Village.uniformDblRand(0.3, 0.75);
                                double dpercent_chance = Village.uniformDblRand(0.0, 1.0);

                                numhunts++;

                                if (dpercent_chance > dpercent_kill) {
                                    // deer killed
                                    killed_deer++;
                                    agent.deer_hunted++;
                                    known_deer--;

                                    // protein per deer = 10800 based on
                                    // usda
                                    // database protein on 36kg of cooked
                                    // deer
                                    // meat
                                    // 36kg is meat weight of 60 kg deer
                                    // Hobbs
                                    // and Swift 1985, Simms 1987,
                                    // Christenson
                                    // 1981
                                    agent.setCurrentProteinStorage(agent.getCurrentProteinStorage() + 10800);
                                    unitCount += 10800;

                                    // DC: only pay attention to needs if
                                    // that's our prime objective
                                    if (/*
                                             * onlyNeeds &&
                                             */meat.getAmount() >= hh_protein_need) {
                                        known_deer = 0;
                                    }

                                    // DC: OK, we're going to move stuff
                                    // related to this in here
                                    // for example, the cost should be added
                                    // right away
                                    // add costs for retrieval
                                    double trips = 36 / Village.CARRY_CAPACITY;
                                    travelCal = agent.calcTravelCal(
                                            agent.Hsearchradius, 0, trips);
                                    hunt_cal += travelCal;
                                    C_hunt += travelCal
                                            / Village.WORK_CAL_MAN;

                                    deer_time += travelCal
                                            / Village.WORK_CAL_MAN;
                                    DcarryCal += travelCal;
                                }

                                // accrue costs of hunting
                                hunt_cal += 5 * Village.WORK_CAL_MAN;
                                C_hunt += 5;

                                deer_time += 5;
                                DhuntCal += 5 * Village.WORK_CAL_MAN;

                                // DC: We do the + 5 because we know another
                                // try will take more than that. We don't
                                // want to go over.
                                if (C_hunt >= hunting_time
                                        || (!onlyNeeds && C_hunt + 5 >= hunting_time)) {
                                    cont_hunting = 0;
                                    known_deer = 0;
                                    break;
                                }

                                if (known_deer == 0) {
                                    // set new deer pops in deercell and
                                    // cell
                                    if (c == null) {
                                        System.out.printf("the deer cell is null!");
                                    } else {
                                        c.setHuntedDeer(killed_deer);
                                    }

                                    totalKilledDeer += killed_deer;
                                }
                            } while (known_deer != 0);
                        }

                        try {
                            unlock(c.getDeerCell().getLock());
                        } catch (Exception e) {
                        }
                    } else {
                        // Hunting lagomorphs is different than deer
                        // hunting.
                        // Instead of aquiring one animal
                        // at a time a single hunt will bring in
                        // Village.HLAG_HUNT (= 2 hare) or Village.RLAG_HUNT
                        // (=5
                        // rabbit, Village.h)
                        // animals. This means that a cell
                        // will not be hunted if there are less than
                        // Village.LAG_HUNT rabbits or hares in the cell.
                        // Additionally the cost of acquiring these animals
                        // is
                        // higher. Hunting Lagomophs
                        // will require Village.LAG_HUNTERS (3) members of
                        // the
                        // family to be over the age of 7, and costs are
                        // accrued for Village.LAG_HUNTERS people in the
                        // hunt.

                        if (hunters >= Village.HUNTERS) {
                            lock(c.getHuntLock());

                            // Determine number of hares able to be hunted
                            tot_hares = c.getAnimalTracker().getIntAmount(Hare.class);
                            double hpercent_found = Village.uniformDblRand(
                                    0.3, 0.75);
                            known_hares = (int) (tot_hares * hpercent_found);

                            if (known_hares < Village.HLAG_HUNT / 2) {
                                known_hares = 0;
                            } /*else if (known_hares < Village.HLAG_HUNT + 1) {
                            known_hares = Village.HLAG_HUNT + 1;
                            } */

                            double travelCal = agent.calcTravelCal(
                                    agent.Hsearchradius, 0, 1.0);
                            hunt_cal += travelCal;
                            C_hunt += travelCal / Village.WORK_CAL_MAN;
                            hare_time += travelCal / Village.WORK_CAL_MAN;
                            HsearchCal += travelCal;

                            if (C_hunt >= hunting_time) {
                                known_hares = 0;
                                cont_hunting = 0;
                                unlock(c.getHuntLock());                                
                                continue;
                            }

                            killed_hares = 0;
                            numhunts = 0;

                            if (known_hares > Village.HLAG_HUNT) {
                                do {
                                    // dpercent_kill is the difficulty
                                    // number
                                    // needed to successfully hunt
                                    // dpercent_chance is the number
                                    // compared to
                                    // difficulty
                                    double dpercent_kill = Village.uniformDblRand(0.3, 0.75);
                                    double dpercent_chance = Village.uniformDblRand(0.0, 1.0);

                                    numhunts++;

                                    if (dpercent_chance > dpercent_kill) {
                                        // hares killed
                                        killed_hares += Village.HLAG_HUNT;
                                        agent.hare_hunted += Village.HLAG_HUNT;
                                        known_hares -= Village.HLAG_HUNT;

                                        // protein per hare = 455.4 g based
                                        // on
                                        // usda database protein on 1.38 kg
                                        // of
                                        // cooked deer meat
                                        // 1.38 kg is meat weight of 2.3 kg
                                        // hare
                                        // Haskell and Reynolds 1947, Simms
                                        // 1987, Christenson 1981

                                        double amountOfProtein = (455.4 * Village.HLAG_HUNT);
                                        meat.increaseAmount(
                                                amountOfProtein, 0);

                                        unitCount += amountOfProtein;

                                        if (/* onlyNeeds && */meat.getAmount() >= hh_protein_need) {
                                            known_hares = 0;
                                        }
                                    }

                                    // accrue costs of hunting
                                    hunt_cal += 0.5 * Village.WORK_CAL_MAN;
                                    C_hunt += 0.5;

                                    hare_time += 0.5;

                                    if (Village.HUNTERS > 1) {
                                        hunt_cal += 0.5 * Village.WORK_CAL_WOM;
                                        C_hunt += 0.5;
                                        hare_time += 0.5;

                                        for (i = 0; i < Village.HUNTERS - 2; i++) {
                                            hunt_cal += 0.5 * Village.WORK_CAL_KID;
                                            C_hunt += 0.5;
                                            hare_time += 0.5;
                                        }
                                    }

                                    if (C_hunt
                                            + calcRetrievalCostsForHares(killed_hares) >= hunting_time) {
                                        known_hares = 0;
                                        cont_hunting = 0;
                                    }

                                    if (known_hares < Village.HLAG_HUNT) {
                                        // set new hare pops cell
                                        c.getAnimalTracker().huntAnimals(Hare.class, killed_hares);

                                        // add costs for retrieval
                                        float trips = killed_hares
                                                * Village.HARE_WEIGHT
                                                / Village.CARRY_CAPACITY;
                                        travelCal = agent.calcTravelCal(
                                                agent.Hsearchradius, 0,
                                                trips);
                                        hunt_cal += travelCal;
                                        C_hunt += travelCal
                                                / Village.WORK_CAL_MAN;

                                        hare_time += travelCal
                                                / Village.WORK_CAL_MAN;
                                    }
                                } while (known_hares > Village.HLAG_HUNT);
                            }

                            if (((/* onlyNeeds && */meat.getAmount() < hh_protein_need) || !onlyNeeds)
                                    && C_hunt < hunting_time) {
                                // Determine number of rabbits able to be
                                // hunted
                                tot_rabbits = c.getAnimalTracker().getIntAmount(Rabbit.class);
                                double rpercent_found = Village.uniformDblRand(0.3, 0.75);
                                known_rabbits = (int) (tot_rabbits * rpercent_found);

                                if (known_rabbits < Village.RLAG_HUNT / 2) {
                                    known_rabbits = 0;
                                } /*else if (known_rabbits < Village.RLAG_HUNT + 1) {
                                known_rabbits = Village.RLAG_HUNT + 1;
                                } */

                                /*
                                travelCal = agent.calcTravelCal(
                                        agent.Hsearchradius, 0, 2.0);
                                hunt_cal += travelCal;
                                C_hunt += travelCal / Village.WORK_CAL_MAN;

                                // JAC 3/06
                                rabbit_time += travelCal
                                        / Village.WORK_CAL_MAN;
                                RsearchCal += travelCal;

                                if (C_hunt >= hunting_time) {
                                    known_rabbits = 0;
                                    cont_hunting = 0;
                                    unlock(c.huntLock);
                                    continue;
                                } */

                                killed_rabbits = 0;
                                numhunts = 0;

                                if (known_rabbits > Village.RLAG_HUNT) {
                                    do {
                                        // rpercent_kill is the difficulty
                                        // number needed to successfully
                                        // hunt
                                        // rpercent_chance is the number
                                        // compared to difficulty
                                        double rpercent_kill = Village.uniformDblRand(0.3, 0.75);
                                        double rpercent_chance = Village.uniformDblRand(0.0, 1.0);

                                        numhunts++;

                                        if (rpercent_chance > rpercent_kill) {
                                            // rabbits killed
                                            killed_rabbits += Village.RLAG_HUNT;
                                            agent.rabbit_hunted += Village.RLAG_HUNT;
                                            known_rabbits -= Village.RLAG_HUNT;

                                            // protein per hare = 177.2 g
                                            // based
                                            // on usda database protein on
                                            // 1.38kg of cooked deer meat
                                            // 1.38kg is meat weight of 2.3
                                            // kg
                                            // hare Haskell and Reynolds
                                            // 1947,
                                            // Simms 1987, Christenson 1981
                                            double amountOfProtein = (177.2 * Village.RLAG_HUNT);
                                            meat.increaseAmount(
                                                    amountOfProtein, 0);
                                            unitCount += amountOfProtein;

                                            if (/* onlyNeeds && */meat.getAmount() >= hh_protein_need) {
                                                known_rabbits = 0;
                                            }
                                        }

                                        // accumulate costs of hunting
                                        hunt_cal += 0.5 * Village.WORK_CAL_MAN;
                                        C_hunt += 0.5;

                                        rabbit_time += 0.5;

                                        if (Village.HUNTERS > 1) {
                                            hunt_cal += 0.5 * Village.WORK_CAL_WOM;
                                            C_hunt += 0.5;

                                            rabbit_time += 0.5;

                                            for (i = 0; i < Village.HUNTERS - 2; i++) {
                                                hunt_cal += 0.5
                                                        * numhunts
                                                        * Village.WORK_CAL_KID;
                                                C_hunt += 0.5;

                                                rabbit_time += 0.5;
                                            }
                                        }

                                        // add costs for retrieval
                                        double trips = killed_rabbits
                                                * Village.RABBIT_WEIGHT
                                                / Village.CARRY_CAPACITY;
                                        travelCal = agent.calcTravelCal(
                                                agent.Hsearchradius, 0,
                                                trips);
                                        hunt_cal += travelCal;
                                        C_hunt += travelCal
                                                / Village.WORK_CAL_MAN;

                                        rabbit_time += travelCal
                                                / Village.WORK_CAL_MAN;

                                        if (C_hunt >= hunting_time) {
                                            known_rabbits = 0;
                                            cont_hunting = 0;
                                        }

                                        if (known_rabbits < Village.RLAG_HUNT) {
                                            // set new rabbit pops cell
                                            c.getAnimalTracker().huntAnimals(Rabbit.class, killed_rabbits);
                                        }

                                    } while (known_rabbits > Village.RLAG_HUNT);
                                }
                            }
                            unlock(c.getHuntLock());
                        }
                    }
                } else {
                    cont_hunting = 0;
                }

                if (/* onlyNeeds && */meat.getAmount() > hh_protein_need) {
                    cont_hunting = 0;
                    continue;
                }

                if (C_hunt >= hunting_time) {
                    cont_hunting = 0;
                    continue;
                }

                if (agent.Hsearchradius >= hunt_radius) {
                    // if no meat found within hunt radius km then stop
                    // hunting
                    // can cause birth penalties
                    cont_hunting = 0;
                }
            } while (cont_hunting != 0);

            // This code calculates the return rates for hunting. If no hunting
            // occurred in a given year
            // a value of -999 is given for the return rate, otherwise a value
            // of 0
            // is given if hunting was
            // unsuccessful.
            if (deer_time > 0) {
                agent.deer_return_rate = 0;
            }

            if (agent.deer_hunted != 0) {
                agent.deer_return_rate = agent.deer_hunted * 56880 / deer_time;
            }

            if (hare_time > 0) {
                agent.hare_return_rate = 0;
            }

            if (agent.hare_hunted != 0) {
                agent.hare_return_rate = agent.hare_hunted * 2387.4 / hare_time;
            }

            if (rabbit_time > 0) {
                agent.rabbit_return_rate = 0;
            }

            if (agent.rabbit_hunted != 0) {
                agent.rabbit_return_rate = agent.rabbit_hunted * 929.01
                        / rabbit_time;
            }

            if (deer_time + hare_time + rabbit_time > 0) {
                agent.hunting_return_rate = 0;
            }

            if (agent.deer_hunted + agent.hare_hunted + agent.rabbit_hunted > 0) {
                agent.hunting_return_rate = (agent.deer_hunted * 56880
                        + agent.hare_hunted * 2387.4 + agent.rabbit_hunted * 929.01)
                        / C_hunt;
            }

            cons.setAvailableTime(cons.getAvailableTime() - C_hunt);
            cons.increaseCalories(-hunt_cal);

            totalCost = hunt_cal;            
        }

        meat.increaseCost(totalCost);      
        return hunt_cal;
    }

    /**
     * Corrects the weight of this task to reflect the amount of calories
     * actually used
     */
    public void correctWeight(Constraints cons, HashMap<Task, Double> weights) {
        double calsLeft = cons.getAvailableCalories();
        if (calsLeft > 0) { // then we decrease our weight to reflect the true
            // weight cost used
            Double myWeight = weights.get(this);

            myWeight = myWeight * (1 - calsLeft / (calsLeft + totalCost));
            weights.put(this, myWeight);
        }
    }

    private double calcRetrievalCostsForHares(int killedHares) {
        // costs for retrieval
        double trips = killedHares * Village.HARE_WEIGHT
                / Village.CARRY_CAPACITY;
        double travelCal = agent.calcTravelCal(agent.Hsearchradius, 0, trips);

        return travelCal / Village.WORK_CAL_MAN;
    }

    private class Strategy extends AlternateHuntingStrategy {
        public Strategy(Agent agt) {
            super(agt);
        }

        @Override
        public int getC_hunt() {
            return C_hunt;
        }

        @Override
        public int execute(long p_need, int h_radius) {
            hh_protein_need = p_need;
            hunt_radius = h_radius;
            return performTask(season, cons, onlyNeeds);
        }
    }

    public HuntingStrategy getStrategy() {
        return strategy;
    }

    public void setParams(Season season, Constraints cons, boolean onlyNeeds) {
        this.season = season;
        this.cons = cons;
        this.onlyNeeds = onlyNeeds;

    }

    @Override
    public Class<? extends Resource> getResourceType() {
        return Meat.class;
    }

    /**
     * Sets the desired level of meat that we want the agent to have (not to
     * hunt, but to have collected) in the next hunting season. This is a
     * safeguard so that the agent will not continue hunting after surpassing
     * it.
     */
    public void setTargetProduction(double target) {
        targetProduction = target;
    }

    public double getTargetProduction() {
        return targetProduction;
    }
}

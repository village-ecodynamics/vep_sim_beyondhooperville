package com.mesaverde.specialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.mesaverde.specialization.allocation.*;
import com.mesaverde.specialization.resources.*;
import com.mesaverde.specialization.tasks.*;
import com.mesaverde.village.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpecializedAgent extends com.mesaverde.village.Agent {
    public static final boolean ENFORCING_MAXIMUMS = true;
    public static final boolean ENFORCE_SEASON_CONSTRAINTS = false;

    // These parameters will be modified through the SpecializedAgentModelSwarm
    public static boolean ENABLE_SPECIALIZATION = true;
    public static double REAL_MAX_FACTOR = 5; // so 10 years storage is the max we allow
    public static boolean USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION = true;
    public static boolean APPLY_DEMAND_PRESSURE = false;

    // turn off turkey domestication
    private Hunter hunter;
    private Farmer farmer;
    private Woodsman woodsMan;
    private WaterCarrier waterCarrier;
    private ArrayList<Resource> myResources = new ArrayList<Resource>();
    private ResourceManager resourceManager = new ResourceManager(this);
    private static double averageWorkHoursPerDay = 6;
    /** how many hours per day an agent is willing to work, including weekends */
    // keep track of where storage was in the year before
    private HashMap<Resource, Integer> oldProductionAmount = new HashMap<Resource, Integer>();
    private AllocationStrategy myAllocationStrategy = AllocationFactory.getAllocationStrategy(this);
    private boolean set_weights = true;
    private boolean onlyMeetNeeds = true;  // whether all agents will only try to meet their needs, or produce based on an allocation strategy
    private double calsForYear;  // calculated in step_procure_init, how many calories we have to spend this year
    private boolean notYetAllocating = true;  // marks when we start.  Used in step_procure_init to initialize allocation weights
    private int householdAge = 0;  // how many years has this household existed
    protected static int totalDeathsFromProtein;
    protected static int totalDeathsFromFamine;
    public static int totalDeathsFromDrought;
    private final ConcurrentHashMap<Class<? extends Task>, Double> socialPressure = new ConcurrentHashMap<Class<? extends Task>, Double>();

    public static void printDeathStats() {
        System.out.printf("Deaths from - Famine: %d   Protein: %d   Drought: %d\n"
                + "Deer killed: %d\n", totalDeathsFromFamine, totalDeathsFromProtein, totalDeathsFromDrought, Hunter.totalKilledDeer);
    }

    /** This will also take care of the hunting */
    public SpecializedAgent() {
        // replace current hunting strategy
        hunter = new Hunter(this);
        farmer = new Farmer(this);
        woodsMan = new Woodsman(this);
        waterCarrier = new WaterCarrier(this);
        huntingStrategy = hunter.getStrategy();

        // we no longer do donations, as that distorts our own productivity
        donationNetwork = null;

        // try to trade on BRN/GRN if we can't get by buying
        resourceManager.addShortageListener(new ShortageListener() {

            public void onShortage(Resource r) {
                if (r instanceof Maize && state != -1) {
                    tradeMaizeOldNetworks(r.getAmount()); // getAmount() gives the amount the agent needs to get
                } else if (r instanceof Meat && state != -1) {
                    tradeProtein(r.getAmount());
                } else if (r instanceof Water && state != -1) {
                    // Uncomment this section and comment the other if you wish to disable dying from drought

                    Resource w = getResource(Water.class);

                    if (r.getAmount() > 0 && w.getAmount() < 0) {
                        w.increaseAmount(0 - w.getAmount(), 0 - w.getCost());
                    }

                    /*
                    if (r.getAmount() > 0 && getResource(Water.class).getAmount() < 0) {
                    death();
                    } */
                }

                /** For wood we just return the total to 0, so we assume they just used all they have
                 * and are OK TODO: check with Kohler
                 */
                if (r.getClass() == Wood.class && state != -1) {
                    Resource w = getResource(Wood.class);

                    if (r.getAmount() > 0 && w.getAmount() < 0) {
                        w.increaseAmount(0 - w.getAmount(), 0 - w.getCost());
                    }
                }
            }
        });

        // tell the allocation strategy what tasks we have available, this can be done here as for now
        // they wont be changing
        ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(hunter);
        tasks.add(waterCarrier);
        tasks.add(woodsMan);
        tasks.add(farmer);
        myAllocationStrategy.setTasks(tasks);
    }

    /** Let neighbours inherit the possessions when agents die */
    @Override
    public void death() {
        super.death();

        recordDeath();
        // so that resources do not go to waste
        inheritance();
    }

    protected void recordDeath() {
        if (getMaizeStorage() < 0) {
            totalDeathsFromFamine++;
        } else if (getMeat().getAmount() <= 0 && years_deficient > Village.DEATH_YEAR) {
            totalDeathsFromProtein++;
        } else if (getResource(Water.class).getAmount() < 0) {
            totalDeathsFromDrought++;
        }
    }

    /** At death, the agents possessions (resources) are passed on to their children.
     * If they have no children, things are passed to their neighbours within a certain range (MAX_COOP_RADIUS_BRN for now).
     */
    protected void inheritance() {
        ArrayList<SpecializedAgent> benefactors = findChildren(Village.MAX_COOP_RADIUS_BRN); // first find children

        if (benefactors == null) // if no children, then check for neighbours
        {
            benefactors = findAgentsInRange();
        }

        int amountOfNeighbours = benefactors.size();

        if (amountOfNeighbours > 0) {
            for (Resource r : myResources) {
                if (r.getAmount() > 0) {
                    int ration = r.getAmount() / amountOfNeighbours;
                    for (SpecializedAgent sa : benefactors) {
                        transferResource(sa, r, ration);
                    }
                }
            }
        }
    }

    protected void transferResource(SpecializedAgent agentReceiving, Resource resourceToTransfer, int amount) {
        resourceToTransfer.decreaseAmount(amount);

        agentReceiving.addResource(resourceToTransfer.makeInstance(amount, 0));
    }

    protected ArrayList<SpecializedAgent> findChildren(int distance) {
        ArrayList<SpecializedAgent> results = new ArrayList<SpecializedAgent>();

        for (int i : ChildHHTag) {
            Agent a = searchAgentList(i);

            if (a != null && Utilities.distance(this, a) <= distance) {
                results.add((SpecializedAgent) a);
            }
        }

        return results;
    }

    /** We ignore this situation, as agents are currently allowed to assume there are children to monitor fields.  This runs into a problem when there's farming
     * specialization, when the amount of hours spent monitoring is too much.
     */
    @Override
    protected void printTooMuchWorkError() {
    }

    @Override
    public void createEnd() {
        super.createEnd();

        Resource maize = getResource(Maize.class);

        if (maize == null) {
            maize = new Maize(0, 0);
            myResources.add(maize);
        }
        maize.increaseCost(1000 * maize.getAmount() - maize.getCost()); // make it 1000 calories/kg

        //NOTE: also doing the same thing for protein, even though this will all be used by then
        Resource meat = getMeat();
        meat.increaseCost(2 * meat.getAmount() - meat.getCost()); // make it 2 calories/g

        // we do it differently to ensure we use Meat.DECAY_RATE instead of hardcoding
        setProteinMaxStore((int) (p_need * getFamilySize() * 365 / (1 - Meat.DECAY_RATE)));

        // give each agent some water and some wood initially (2 years worth)
        Resource water = new Water(W_need * getFamilySize() * 2, W_need * getFamilySize() * 4);
        addResource(water);
        Resource wood = new Wood(FW_need * getFamilySize() * 2, FW_need * getFamilySize() * 4);
        addResource(wood);
    }

    public void addResource(int amount, Resource resource, double costForIt) {
        // find it if we already have it
        Resource item = null;

        for (Resource r : myResources) {
            if (r.getClass() == resource.getClass()) { // same resource
                item = r;
                break;
            }
        }

        if (item != null) {
            item.increaseAmount(amount, costForIt);

        } else {
            // add the resource and set the maximum storage for it
            Resource r = resource.makeInstance(amount, costForIt);
            myResources.add(r);
        }

    }

    public void addResource(Resource r) {
        addResource(r.getAmount(), r, r.getCost());
    }

    @Override
    public int burnWood() {
        Woodsman wm = woodsMan;
        int res = 0;

        if (onlyMeetNeeds) {
            res = wm.performTask(Season.SUMMER, new Constraints(), true);
        } else {
            res = wm.performTask(Season.SUMMER, myAllocationStrategy.getConstraintsFor(woodsMan), false);
        }
        int need = FW_need * getFamilySize();

        useResource(new Wood(need, Resource.EMPTY));

        // track usage
        woodsMan.increaseAmountNeeded(need);

        return res;
    }

    /** Calculates how many calories this family can spend per year.
     * Based on how many hours agents are willing to work per day, assuming 365 days/year as well.
     * Only kids 7 or older work.
     * @return number of calories available
     */
    protected double calculateTotalCaloriesPerYear() {
        // count the kids, only those over 7 can work
        double total = familyUnit.getKidCount() * Village.WORK_CAL_KID * 365;

        if (familyUnit.hasWife()) {
            total += Village.BASE_CAL_WOM;
        }
        if (familyUnit.hasHusband()) {
            total += Village.BASE_CAL_MAN;
        }
        total *= averageWorkHoursPerDay;

        return total;
    }

    @Override
    protected void decreaseCurrentProteinStorage(int donateProtein) {
        Resource meat = getResource(Meat.class);

        // if this is null I want to know, because it shouldn't be if this method is called
        meat.decreaseAmount(donateProtein);
    }

    @Override
    public int drinkWater() {
        // we're only setting it to meet our needs right now
        WaterCarrier wc = waterCarrier;
        int res = 0;

        if (onlyMeetNeeds) {
            res = wc.performTask(Season.SUMMER, new Constraints(), true);
        } else {
            res = wc.performTask(Season.SUMMER, myAllocationStrategy.getConstraintsFor(waterCarrier), false);
        }

        // now we use what we needed for ourselves
        int need = W_need * getFamilySize();
        waterCarrier.increaseAmountNeeded(need);
        useResource(new Water(need, Resource.EMPTY));

        return res;
    }

    // **** household utilities ****
    @Override
    protected void eatMaize(final int cals_spent) {
        maize_coop_count = 0;

        // store the ration amount the family ate
        // maize storage is in kg
        last_ration = (int) (Village.MAIZE_PER * cals_spent / (double) Village.MAIZE_KG_CAL);
        farmer.increaseAmountNeeded(last_ration); // track usage

        // DC: we're also going to recall debts when we need it
        if (brnNetwork != null && last_ration > getMaizeStorage()) {
            brnNetwork.update();
            brnNetwork.callInDebts();
        }
        useResource(new Maize(last_ration, Resource.EMPTY));

        // update state for coop
        update_state();

        if (getMaizeStorage() < 5) {
            if (Village.DEBUG) {
                System.out.printf("agent %d , maize storage = %d\n", getTag(),
                        getMaizeStorage());
            }
        }
    }

    /* DC: we keep this method, we just don't have it actually enforce the maximums.
     * Instead it only records when we go over
     */
    @Override
    public void enforceResourceMaximums() {
        if (ENFORCING_MAXIMUMS) {
            // go through each resource, dropping amount to that allowed by the maximum
            for (Resource r : myResources) {
                int amount = r.getAmount();

                // if we're over, then we're just going to lose this amount
                // any donations should have taken place already
                if (amount > REAL_MAX_FACTOR * r.getMaxStorage()) {
                    r.decreaseAmount((int) Math.ceil(amount - REAL_MAX_FACTOR * r.getMaxStorage()));
                }
            }
        }
    }

    public void fallWork() {
        super.step_procure_fall();
    }

    @Override
    public int getCurrentProteinStorage() {
        return getMeat().getAmount();
    }

    @Override
    public int getMaizeMaxStore() {
        return (int) getResource(Maize.class).getMaxStorage();
    }

    @Override
    public int getMaizeStorage() {
        Resource maize = getResource(Maize.class);

        if (maize == null) {
            maize = new Maize(super.getMaizeStorage(), 0);
            addResource(maize);
        }

        return maize.getAmount();
    }

    private Resource getMeat() {
        Resource meat = getResource(Meat.class);
        if (meat == null) {
            meat = new Meat(0, 0);
            addResource(meat);  // make sure we have something for meat
        }

        return meat;
    }

    private int getOwnProtein() {
        long hh_protein_need = 0; // will store annual protein need in g per
        // household using input from .scm or
        // Village.h
        protein_coop_count = 0;
        int hunt_cal = 0;

        int Protein_Need = getSwarm().getProteinNeed(); // In grams of protein
        int hunt_radius = getSwarm().getHuntingRadius();

        // calculate protein need for hh per year to survive
        hh_protein_need = Protein_Need * getFamilySize() * 365; // 2 adults 12g + 4
        // children avg 6g /
        // 6 = 8g
        Resource meat = getMeat();
        meat.decreaseAmount((int) (meat.getAmount() * Meat.DECAY_RATE)); // faster degrade in storage for protein than for	 maize

        protein_deficient = 0; // will switch to one if not enough protein is
        // consumed

        hunt_cal = huntingStrategy.execute(hh_protein_need, hunt_radius);
        //super.rabbit_return_rate = hunter.rabb

        return hunt_cal;
    }

    @Override
    public int getProteinMaxStore() {
        return (int) getMeat().getMaxStorage();
    }

    /** gets this type of resource from the agent, returns null if not here */
    public Resource getResource(Class<? extends Resource> class1) {
        Resource res = null;

        if (myResources == null) {
            myResources = new ArrayList<Resource>();
        }

        for (Resource r : myResources) {
            if (class1 == r.getClass()) {
                res = r;
            }
        }

        return res;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public ArrayList<Resource> getResources() {
        return myResources;
    }

    /** sets the weights of our allocation strategy based on usage during first year, these are normalized */
    private void setAllocationWeights() {
        HashMap<Task, Double> weights = new HashMap<Task, Double>();

        double farmCost = Ag_Cost;
        double huntCost = hunter.getTotalCost();
        double woodCost = woodsMan.getTotalCost();
        double waterCost = waterCarrier.getTotalCost();

        Double total = farmCost + huntCost + woodCost + waterCost;

        weights.put(farmer, farmCost / total);
        weights.put(hunter, huntCost / total);
        hunter.setTargetProduction(getMeat().getMaxStorage() * 2);  // just to limit what we do
        weights.put(woodsMan, woodCost / total);
        weights.put(waterCarrier, waterCost / total);
        myAllocationStrategy.setWeights(weights);
    }

    /** @deprecated - It is preferred not to use this method to increase the amount of meat stored,
     * as it does not adjust the cost for the meat.  If you use it, you must remember to also call increaseCost.
     */
    @Deprecated
    @Override
    public void setCurrentProteinStorage(int amount) {
        Resource meat = getMeat();
        meat.increaseAmount((amount - meat.getAmount()), 0);
    }

    @Override
    /** Does the same thing as Agent's version, but sets the maximum for our maize resource */
    protected void setMaizeMaxStore(int maxStore) {
        Resource maize = getResource(Maize.class);

        if (maize == null) {
            maize = new Maize(0, 0);
            myResources.add(maize);
        }

        maize.setMaxStorage(maxStore);
        super.setMaizeMaxStore(maxStore);
    }

    /** We also set the max storage for water and wood, we're going to set this at 2 years worth */
    @Override
    protected void evalState() {
        super.evalState();

        Resource water = getResource(Water.class);
        if (water != null) {
            water.setMaxStorage(this.W_need * getFamilySize() * 2);  // set max storage to 2 years worth
        }
        Resource wood = getResource(Wood.class);
        if (wood != null) {
            wood.setMaxStorage(this.FW_need * getFamilySize() * 2); // set max storage to 2 years worth
        }
    }

    /** @deprecated - It is preferred not to use this method to increase the amount of maize stored,
     * as it does not adjust the cost for the meat.  If you use it, you must remember to also call increaseCost.
     */
    @Deprecated
    @Override
    public void setMaizeStorage(int amount) {
        Resource maize = getResource(Maize.class);

        if (maize == null) {
            maize = new Maize(amount, Resource.EMPTY);
            myResources.add(maize);
        }

        maize.increaseAmount((amount - maize.getAmount()), 0);
        super.setMaizeStorage(amount);
    }

    @Override
    /** Does the same thing as Agent's version, but sets the maximum for our maize resource */
    public void setProteinMaxStore(int maxStore) {
        Resource meat = getMeat();
        meat.setMaxStorage(maxStore);
        super.setProteinMaxStore(maxStore);
    }

    /** Only difference here is that we need to make sure the cost of farming is accounted for */
    @Override
    public void step_procure_fall() {
        if (isDead()) {
            return;
        }

        if (onlyMeetNeeds) {
            farmer.performTask(Season.FALL, new Constraints(), true);
        } else {
            myAllocationStrategy.updateSeason(calsForYear);
            farmer.performTask(Season.FALL, myAllocationStrategy.getConstraintsFor(farmer), false);
        }


        Resource maize = getResource(Maize.class);
        if (maize != null) {
            maize.increaseCost(Ag_Cost);
        }

    }

    @Override
    /** This just ensures that the allocation strategy is updated for the year */
    public void step_procure_init() {
        if (isDead()) {
            return;
        }

        // we switch over to agents allocating their own time after the fifth year
        if (!notYetAllocating && set_weights) {
            setAllocationWeights();
            onlyMeetNeeds = false;
            set_weights = false;
        }

        // keep track of household age
        householdAge++;

        if (householdAge == 1 && ENABLE_SPECIALIZATION) {
            notYetAllocating = false;
        }

        if (!onlyMeetNeeds) {
            calsForYear = this.calculateTotalCaloriesPerYear();

            if (calsForYear == 0) {
                death();
                return;
            }

            myAllocationStrategy.update(calsForYear);
            myAllocationStrategy.updateSeason(calsForYear);

            // get rid of previous year's social influence
            if (SpecializedAgent.USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION) {
                socialPressure.clear();
            }
        }

        super.step_procure_init();

        updateProductionRecord();
    }

    /** Record storage levels from the previous year */
    private void updateProductionRecord() {
        for (Task t : myAllocationStrategy.getTasks()) {
            Resource r = getResource(t.getResourceType());

            if (r != null) {
                oldProductionAmount.put(r, t.getUnitCount());
            }
        }
        /*
        for (Resource r : myResources) {
        lastYearsStorageAmount.put(r, r.getAmount());
        }	*/
    }

    boolean isDead() {
        return (state == -1);
    }

    @Override
    // This will also take care of the hunting
    public int step_procure_protein() {
        if (onlyMeetNeeds) {
            hunter.setParams(Season.SUMMER, new Constraints(), true);
        } else {
            hunter.setParams(Season.SUMMER, myAllocationStrategy.getConstraintsFor(hunter), false);
        }
        int res = getOwnProtein();

        int need = getSwarm().getProteinNeed() * getFamilySize() * 365;
        hunter.increaseAmountNeeded(need); // track usage
        useResource(new Meat(need, Resource.EMPTY));

        if (getCurrentProteinStorage() > 0) {
            years_deficient = 0;
        }

        return res;
    }

    // march april may
    @Override
    public void step_procure_spring() {
        // DC: we shouldn't need these, agents should be removed right away when they die
        if (state == -1) {
            return;
        }

        int i, eat_cal = 0, work_cal = 0;
        // set caller=0 so that -searchNeighborhood won't try to look for water
        // baseline expenditure of cals

        eat_cal = getNumKids() * Village.BASE_CAL_KID; // each of these based on
        // 91.25 days, or 1 season
        if (familyUnit.hasWife()) {
            eat_cal += Village.BASE_CAL_WOM;
        }
        if (familyUnit.hasHusband()) {
            eat_cal += Village.BASE_CAL_MAN;
        }
        actual_cal = eat_cal;
        Food_Cost = eat_cal;

        // this code is used for determining the average number of workers
        // a family has over the course of one year
        season_fam = familyUnit.getCountAtOrAboveAge(8); // spring

        // plot_need should be equal to what needs to be planted/shed this
        // period
        // as determined in -evalState
        // but here we check to see if there is enough labor
        // only >7 yr olds contribute meaningful labor
        if (onlyMeetNeeds) {
            work_cal = farmer.performTask(Season.SPRING, new Constraints(), true);
        } else {
            Constraints con = myAllocationStrategy.getConstraintsFor(farmer);

            work_cal = farmer.performTask(Season.SPRING, con, false);
        }

        // Increases actual_cal to match other net caloric needs for agents.
        // Village.MAIZE_PER gets reapplied in eatMaize and at the end of each
        // year in evalState.
        int real_cal = 0;
        real_cal = (int) (work_cal * 1 / Village.MAIZE_PER);
        actual_cal += real_cal;

        // new plots have 11 additional days of clearing/hoeing costs charged in
        // -plantPl
        // food usage during season
        eatMaize(actual_cal);
        // -eatMaize decrements storage by consumption, updates agent state and
        // GRN & BRN networks, may request maize if needed
        // Agent can die in -eatMaize if maize runs out
        total_cal += actual_cal;
        if (Village.DEBUG && getTag() == Village.TAG) {
            System.out.printf(
                    "Village.DEBUG -springs over: agent %d with %d people ate %d kg maize\n",
                    getTag(), getFamilySize(),
                    (int) (actual_cal * Village.MAIZE_PER)
                    / Village.MAIZE_KG_CAL);
        }

        return;
    }

    @Override
    public void step_procure_summer() {
        if (isDead()) {
            return;
        }

        if (onlyMeetNeeds) {
            farmer.performTask(Season.SUMMER, new Constraints(), true);
        } else {
            myAllocationStrategy.updateSeason(calsForYear);
            Constraints con = myAllocationStrategy.getConstraintsFor(farmer);
            farmer.performTask(Season.SUMMER, con, false);
        }
    }

    /** just need a way to get to the step_procure_summer in Agent, not the one in here */
    public void summerWork() {
        super.step_procure_summer();
    }

    private void tradeMaizeOldNetworks(int amountShort) {
        int need = amountShort; // using to see how much we got using the old networks
        int trade = getSwarm().getEconomy();

        update_state();


        // Request food
        if (trade == 1 || trade == 3 || trade >= 4) {
            // call in kins
            if ((Village.TRIGGER_REQUEST_MAIZE() & getCoopState()) != 0) {
                if (trade >= 3 && maize_coop_count < Village.COOP_ATTEMPTS) {
                    int received = 0;

                    received = grnNetwork.requestMaize(amountShort); // cooprequest returns local variable maize_retrieved
                    setMaizeStorage(getMaizeStorage() + received);
                    maize_imported += received;
                    amountShort -= received;
                    update_state();
                }
                if (trade >= 4 && maize_coop_count < Village.COOP_ATTEMPTS) {
                    int received = 0;
                    //init_maize_storage = getMaizeStorage();

                    received = brnNetwork.requestMaize(amountShort); // RequestTrade returns local variable maize_retrieved
                    setMaizeStorage(getMaizeStorage() + received);
                    amountShort -= received;
                    maize_imported += received;
                    update_state();
                }
            }
        }

        // if the agent isn't able to get enough food, then they die
        int storage = getMaizeStorage();
        if (storage < 0) {
            // setMaizeStorage(-1);  // DC: This was irrelevant and un-informative
            update_state();
            death();

            if (Village.DEBUG) {
                System.out.printf(
                        "Village.DEBUG -eatMaize: agent %d starved, spent %d, last harv %d kg (%d Cal), state %d\n",
                        getTag(), amountShort, act_yield, act_yield
                        * Village.MAIZE_KG_CAL, state);
            }
        }
    }

    /*
     * Currently, water and wood are not traded at all in the other networks.
     * Other network trading code for hunting is needed, even though I don't think they are needed, as this system works pretty well.
     * Maize is still traded on the other networks (and not at all in this way).
     */
    private void useResource(Resource resource) {
        Resource match = getResource(resource.getClass());	// get our resource that we say match directly this type
        double diff = 0;

        // find if we have it for ourselves first
        for (Resource r : myResources) {
            if (resource.getClass().isAssignableFrom(r.getClass())
                    && r.getAmount() > 0) { // found it or something like it
				/* So if we are looking for Meat resource, and Turkey resource is registered as a Meat resource,
                 * then we've found a match
                 */

                // see if the amount is ok
                int availableAmount = Math.min(resource.getAmount(), r.getAmount());
                availableAmount = Math.max(0, availableAmount);


                r.decreaseAmount(availableAmount);
                resource.decreaseAmount(availableAmount);

                // did we satisfy ourselves?
				/* We count a surplus if we have more than the storage threshold left (the current storage "maximum")
                 */
                if (resource.getAmount() == 0) {
                    if (r.getAmount() > r.getTradeThresholdAmount()) {
                        resourceManager.registerSurplus(r);
                    }
                    break;
                }
            }
        }

        // see if we have enough or we need to get more
        diff = match.getAmount() - match.getMaxStorage();

        // pre-subtract the remainder.  The agent should recover it from trading
        match.decreaseAmount(resource.getAmount());

        if (diff < 0) {
            resource.increaseAmount(-diff, 0);  // make sure we get enough more to get to a decent level
        }
        if (resource.getAmount() > 0) {
            resourceManager.registerShortage(resource);
        }
    }

    public boolean isWillingToTrade(Resource r) {
        Resource mine = getResource(r.getClass());

        if (mine == null) {
            return false;
        }


        return (mine.getAmount() > mine.getTradeThresholdAmount());
    }

    // Used for getting data for our graphs
    public double getFarmWeight() {
        HashMap<Task, Double> weights = myAllocationStrategy.getWeights();
        Double res = weights.get(farmer);

        if (res == null) {
            res = 0.0d;
        }

        return res * 100;
    }

    public double getWoodGatheringWeight() {
        HashMap<Task, Double> weights = myAllocationStrategy.getWeights();
        Double res = weights.get(woodsMan);

        if (res == null) {
            res = 0.0d;
        }

        return res * 100;
    }

    public double getHuntingWeight() {
        HashMap<Task, Double> weights = myAllocationStrategy.getWeights();
        Double res = weights.get(hunter);

        if (res == null) {
            res = 0.0d;
        }

        return res * 100;
    }

    public double getWaterCollectingWeight() {
        HashMap<Task, Double> weights = myAllocationStrategy.getWeights();
        Double res = weights.get(waterCarrier);

        if (res == null) {
            res = 0.0d;
        }

        return res * 100;
    }

    /** Returns the amount of protein stored, in G and in terms of years' supply */
    public double getNumYearsMeatStorage() {
        Resource meat = getMeat();
        double amount = 0;
        double oneYearsSupply = 1;

        if (meat != null) {
            amount = meat.getAmount();
            oneYearsSupply = meat.getMaxStorage() * 0.5;
        }

        return amount / oneYearsSupply;
    }

    /**  get number of years' supply of wood */
    public double getNumYearsWoodStorage() {
        Resource wood = getResource(Wood.class);
        double amount = 0;
        double oneYearsSupply = 1;

        if (wood != null) {
            amount = wood.getAmount();
            oneYearsSupply = wood.getMaxStorage() * 0.5;
        }

        return amount / oneYearsSupply;
    }

    /**  get number of years' supply of water */
    public double getNumYearsWaterStorage() {
        Resource water = getResource(Water.class);
        double amount = 0;
        double oneYearsSupply = 1;

        if (water != null) {
            amount = water.getAmount();
            oneYearsSupply = water.getMaxStorage() * 0.5;
        }

        return amount / oneYearsSupply;
    }

    /** get number of years' supply of maize.
     * Some of the graph methods need doubles to invoke dynamically. */
    public double getNumYearsMaizeStorage() {
        Resource maize = getResource(Maize.class);

        if (maize == null) {
            maize = new Maize(super.getMaizeStorage(), 0);
            addResource(maize);
        }

        double oneYearsSupply = maize.getMaxStorage() * 0.5;

        return maize.getAmount() / oneYearsSupply;
    }

    public ArrayList<SpecializedAgent> findAgentsInRange() {
        return findAgentsInRange(TradeManager.tradeRange);
    }

    public ArrayList<SpecializedAgent> findAgentsInRange(double distance) {
        ArrayList<SpecializedAgent> inRange = new ArrayList<SpecializedAgent>();

        for (Agent sa : getSwarm().getAgentList()) {
            SpecializedAgent sAgent = (SpecializedAgent) sa;

            if (sAgent != this && Utilities.distance(sAgent, this) <= distance) {
                inRange.add(sAgent);
            }
        }

        return inRange;
    }

    public AllocationStrategy getAllocationStrategy() {
        return myAllocationStrategy;
    }

    /** Just saying that we do the first year after a move by needs, then allocate */
    @Override
    public void moveHouse() {
        super.moveHouse();

        householdAge = 0;
        notYetAllocating = true;
        onlyMeetNeeds = true;
        set_weights = true;
    }

    public double getWaterMaxStorage() {
        Resource r = getResource(Water.class);
        double res = 0;

        if (r != null) {
            res = r.getMaxStorage();
        }

        return res;
    }

    public boolean isAllocating() {
        return !notYetAllocating && !set_weights;
    }

    public HashMap<Resource, Integer> getProductionRecord() {
        return oldProductionAmount;
    }

    public int getHouseholdAge() {
        return householdAge;
    }

    @Override
    /** We redefine max storage for each of our resources to be 2 years supply, determined by the
     * maximum of what's needed for our family, and what we used in the previous year.
     */
    public void step_procure_conclude() {
        super.step_procure_conclude();

        for (Task t : getAllocationStrategy().getTasks()) {
            Resource r = getResource(t.getResourceType());
            double newMax = Math.max(t.getAmountNeeded() * 2, r.getMaxStorage());
            r.setMaxStorage(newMax);
        }
    }

    /** Influences the allocation decisions of other agents on the basis of demand and competition.
     * This only occurs when the USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION flag is set.
     */    
    public void applySocialPressure() {
        if (SpecializedAgent.USE_SOCIAL_INFLUENCE_FOR_SPECIALIZATION) {
            ArrayList<SpecializedAgent> neighbours = findAgentsInRange();

            for (Task t : myAllocationStrategy.getTasks()) {
                Resource r = getResource(t.getResourceType());

                if (r == null) {
                    continue;
                }
                double amountExtra = r.getAmount() - r.getMaxStorage();
                double ourCost = 0;

                if (t.getUnitCount() != 0) {
                    ourCost = t.getTotalCost() / t.getUnitCount();
                }

                // apply demand pressure to more efficient neighbours (and ourselves too)
                if (APPLY_DEMAND_PRESSURE) {
                    if (amountExtra < 0) {
                        ArrayList<SpecializedAgent> better = filterCheaperOrSamePriceIncludingMaxTransport(neighbours, r.getClass(), ourCost);
                        better.add(this);

                        // increase pressure for all concerned
                        for (SpecializedAgent sa : better) {
                            synchronized (sa.socialPressure) {
                                Double current = sa.getSocialPressureFor(t);
                                sa.socialPressure.put(t.getClass(), current - amountExtra / better.size());
                            }
                        }
                    }
                }
                if (amountExtra > 0 && ourCost > 0) { // we've got enough to give
                    ArrayList<SpecializedAgent> worse = filterMoreExpensiveIncludingMaxTransport(neighbours, r.getClass(), ourCost);
                    // include ourselves
                    worse.add(this);

                    // make sure we maintain this level of production for it
                    socialPressure.put(t.getClass(), getSocialPressureFor(t) + amountExtra);

                    // increase pressure for all concerned
                    for (SpecializedAgent sa : worse) {
                        synchronized (sa.socialPressure) {
                            Double current = sa.getSocialPressureFor(t);
                            sa.socialPressure.put(t.getClass(), current - amountExtra / worse.size());
                        }
                    }
                }
            }
        }
    }

    private ArrayList<SpecializedAgent> filterCheaperOrSamePriceIncludingMaxTransport(
            ArrayList<SpecializedAgent> agentsInRange, Class<? extends Resource> resourceType, double ourCost) {
        ArrayList<SpecializedAgent> res = new ArrayList<SpecializedAgent>(agentsInRange.size());

        for (SpecializedAgent sa : agentsInRange) {
            Resource r = sa.getResource(resourceType);

            if (r != null && r.getAmount() > 0 && r.getUnitCost() <= ourCost) {
                double transportUnitCost = r.calculateTransportCost(r.getAmount(), TradeManager.tradeRange) / r.getAmount();

                if (r.getUnitCost() + transportUnitCost <= ourCost) {
                    res.add(sa);
                }
            }
        }

        return res;
    }

    private ArrayList<SpecializedAgent> filterMoreExpensiveIncludingMaxTransport(
            ArrayList<SpecializedAgent> agentsInRange, Class<? extends Resource> resourceType, double ourCost) {
        ArrayList<SpecializedAgent> res = new ArrayList<SpecializedAgent>(agentsInRange.size());

        for (SpecializedAgent sa : agentsInRange) {
            Resource r = sa.getResource(resourceType);

            double transportUnitCost = r.calculateTransportCost(r.getAmount(), TradeManager.tradeRange) / r.getAmount();

            if (r != null && r.getAmount() > 0 && (r.getUnitCost() + transportUnitCost) > ourCost) {
                res.add(sa);
            }
        }

        return res;
    }

    public Double getSocialPressureFor(Task c) {
        Double val = socialPressure.get(c.getClass());

        if (val == null) {
            val = 0d;
        }

        return val;
    }

    void updateSocialPressure(Task task, double change) {
        Double current = getSocialPressureFor(task);
        current += change;

        synchronized (socialPressure) {
            socialPressure.put(task.getClass(), current);
        }
    }

    /** Determine how much of the resource we need to get to our desired storage */
    int amountDesired(Resource r) {
        return (int) Math.max(0, r.getMaxStorage() - r.getAmount());
    }
    
    /** Totals the amount of labour in storage */
    public double getStoredProductivity() {
    	double sum = 0;
    	
    	for (Resource r : myResources) {
    		sum += r.getCost();
    	}
    	
    	return sum;
    }
}

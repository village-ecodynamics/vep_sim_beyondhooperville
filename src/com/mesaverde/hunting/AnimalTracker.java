package com.mesaverde.hunting;

import java.util.HashMap;

public class AnimalTracker {

    private HashMap<Class<? extends Animal>, Animal> animals = new HashMap<Class<? extends Animal>, Animal>();

    public void resetAllHunted() {
        for (Animal a : animals.values()) {
            a.setAmountHunted(0);
        }
    }

    public void huntAnimals(Class<? extends Animal> animalType, int killed) {
        Animal ani = animals.get(animalType);

        ani.huntAnimals(killed);
    }

    public double getAmountHunted(Class<? extends Animal> animalType) {
        return animals.get(animalType).getAmountHunted();
    }

    public void setAmountHunted(Class<? extends Animal> animalType, int amount_killed) {
        animals.get(animalType).setAmountHunted(amount_killed);
    }

    public void setAmount(Class<? extends Animal> animalType, double i) {
        Animal ani = animals.get(animalType);

        if (ani == null) {
            try {
                ani = animalType.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            animals.put(animalType, ani);
        }

        ani.setAmount(i);
    }

    public double getAmount(Class<? extends Animal> animalType) {
        Animal ani = animals.get(animalType);

        if (ani == null) {
            return 0;
        }

        return ani.getAmount();
    }

    /** Utility method for when we don't need fractions */
    public int getIntAmount(Class<? extends Animal> animalType) {
        return (int) getAmount(animalType);
    }
}

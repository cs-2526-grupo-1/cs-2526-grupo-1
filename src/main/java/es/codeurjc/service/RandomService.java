package es.codeurjc.service;

import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service for generating random numbers.
 * This service encapsulates Random functionality to make it easier to mock in tests.
 */
@Service
public class RandomService {

    private final Random random;

    public RandomService() {
        this.random = new Random();
    }

    /**
     * Returns a pseudorandom int value between 0 (inclusive) and the specified bound (exclusive).
     *
     * @param bound the upper bound (exclusive). Must be positive.
     * @return a pseudorandom int value between 0 (inclusive) and bound (exclusive)
     */
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}

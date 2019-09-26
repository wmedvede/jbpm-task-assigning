package org.jbpm.services.task.assigning.model.solver;

import java.util.Random;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PriorityHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void isHighLevelTest() {
        Stream.of(0, 1, 2).forEach(level -> assertTrue(PriorityHelper.isHighLevel(level)));
    }

    @Test
    public void isMediumLevelTest() {
        Stream.of(3, 4, 5, 6).forEach(level -> assertTrue(PriorityHelper.isMediumLevel(level)));
    }

    @Test
    public void isLowLevelTest() {
        Stream.of(7, 8, 9, 10).forEach(level -> assertTrue(PriorityHelper.isLowLevel(level)));
    }

    @Test
    public void calculateWeightedPenaltySuccessfulTest() {
        int priority = new Random().nextInt(11);
        int endTime = 1234;
        int expectedValue = -(11 - priority) * endTime;
        assertEquals(expectedValue, PriorityHelper.calculateWeightedPenalty(priority, endTime));
    }

    @Test
    public void calculateWeightedPenaltyFailureTest() {
        String expectedMessage = "Task priority %s is out of range. " +
                "A valid priority value must be between 0 (inclusive) " +
                " and 10 (inclusive)";
        Stream.of(-2, -1, 11, 12).forEach(priority -> {
            expectedException.expectMessage(String.format(expectedMessage, priority));
            PriorityHelper.calculateWeightedPenalty(priority, 1234);
        });
    }
}

package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    Epic epic;

    @BeforeEach
    void beforeEach() {
        epic = new Epic("EpicName", "EpicDescription");
        ArrayList<Integer> ids = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        for (int id : ids) {
            epic.addSubTaskId(id);
        }
    }

    @Test
    void shouldBeWithoutDeletedSubTaskId() {
        epic.deleteSubTaskId(3);

        ArrayList<Integer> expectedList = new ArrayList<>(Arrays.asList(1, 2, 4, 5));
        ArrayList<Integer> actualList = epic.getSubTaskIdList();

        assertEquals(actualList, expectedList);
    }

    @Test
    void shouldBeEmptyAfterDeletingAllSubTaskIds() {
        epic.deleteAllSubTaskId();

        ArrayList<Integer> expectedList = new ArrayList<>();
        ArrayList<Integer> actualList = epic.getSubTaskIdList();

        assertEquals(actualList, expectedList);
    }
}
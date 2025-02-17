package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest {

    private File file;
    private TaskManager taskManager;
    private BufferedReader bufferedReader;

    @BeforeEach
    void setUp() {
        try {
            file = File.createTempFile("test", ".csv");
            taskManager = Manager.getFileBacked(file);
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (IOException exception) {
            System.out.println("Ошибка при создании файла для теста");
        }
    }

    @AfterEach
    void tearDown() {
        try {
            bufferedReader.close();
        } catch (IOException exception) {
            System.out.println("Ошибка закрытия потока после теста");
        }
    }

    @Test
    public void firstLineShouldHaveTitle() throws IOException {
        String title = "id,type,name,status,description,epic";

        Task task = new Task("Name", "Description", Status.NEW);
        taskManager.createTask(task);

        String firstLine = bufferedReader.readLine();

        assertEquals(title, firstLine);
    }
}
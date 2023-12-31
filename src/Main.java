import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    // Блокирующи очереди, максимальный размер которых ограничим 100 строками.
    public static int queueSize = 100;
    public static int totalTexts = 10_000;
    public static int textLength = 100_000;

    // Создайте в статических полях три потокобезопасные блокирующие очереди.
    public static BlockingQueue<String> aCounterQueue = new ArrayBlockingQueue<>(queueSize);
    public static BlockingQueue<String> bCounterQueue = new ArrayBlockingQueue<>(queueSize);
    public static BlockingQueue<String> cCounterQueue = new ArrayBlockingQueue<>(queueSize);

    public static void main(String[] args) throws InterruptedException {

        // Для этого строки будут генерироваться в отдельном потоке и заполнять блокирующие
        // очереди, максимальный размер которых ограничим 100 строками.
        Thread generatorThread = new Thread(() -> {
            // Создаёт из символов "abc" 10 000 текстов
            for (int textCounter = 0; textCounter < totalTexts; textCounter++) {
                // Длиной 100 000 каждый
                String currentText = generateText("abc", textLength);
                // Очереди нужно будет сделать по одной для каждого из трёх анализирующих потоков,
                // т. к. строка должна быть обработана каждым таким потоком.
                try {
                    aCounterQueue.put(currentText);
                    bCounterQueue.put(currentText);
                    cCounterQueue.put(currentText);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        // Создайте по потоку для каждого из трёх символов
        // 'a', 'b' и 'c', которые разбирали бы свою очередь и выполняли подсчёты.
        // Поток для подстчёта символов 'a'
        Thread aThread = new Thread(() -> {
            countCharacters(aCounterQueue, 'a');
        });

        // Поток для подстчёта символов 'b'
        Thread bThread = new Thread(() -> {
            countCharacters(bCounterQueue, 'b');
        });

        // Поток для подстчёта символов 'c'
        Thread cThread = new Thread(() -> {
            countCharacters(cCounterQueue, 'c');
        });

        generatorThread.start();
        aThread.start();
        bThread.start();
        cThread.start();
    }

    private static void countCharacters(BlockingQueue<String> queue, char characterToFind) {
        int maxSize = 0;
        String text = "";
        while (true) {
            try {
                text = queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < text.length(); i++) {
                for (int j = 0; j < text.length(); j++) {
                    if (i >= j) {
                        continue;
                    }
                    boolean charFound = false;
                    for (int k = i; k < j; k++) {
                        if (text.charAt(k) == characterToFind) {
                            charFound = true;
                            break;
                        }
                    }
                    if (!charFound && maxSize < j - i) {
                        maxSize = j - i;
                    }
                }
            }
            System.out.println(text.substring(0, 100) + " -> " + maxSize + " " + characterToFind);
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}

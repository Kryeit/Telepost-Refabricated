package com.kryeit.telepost.autonaming;

import com.kryeit.telepost.Telepost;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.TimerTask;

import static com.kryeit.telepost.Telepost.ID;

public class MonthlyCheckRunnable extends TimerTask {
    private static final String FILE_PATH = "mods/" + ID + "/lastCheckedMonth.txt";
    private int lastCheckedMonth;

    public MonthlyCheckRunnable() {
        lastCheckedMonth = readLastCheckedMonthFromFile();
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);

        if (currentMonth != lastCheckedMonth) {

            // Month changed, run automatic naming and run /setworldspawn command
            AutonamingUtils.autonamePost();

            // Also reset /randompost cooldowns
            try {
                Telepost.randomPostCooldown.resetFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            lastCheckedMonth = currentMonth;
            writeLastCheckedMonthToFile(currentMonth);

        }
    }

    private void writeLastCheckedMonthToFile(int month) {
        // Adjust month to be 1-indexed before writing to file
        month += 1;
        try {
            Files.write(Paths.get(FILE_PATH), String.valueOf(month).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int readLastCheckedMonthFromFile() {
        Path path = Paths.get(FILE_PATH);
        try {
            if (!Files.exists(path)) {
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; // Adjust for 1-indexed
                writeLastCheckedMonthToFile(currentMonth);
                return currentMonth;
            } else {
                String content = new String(Files.readAllBytes(path)).trim();
                // Adjust read month back to 0-indexed for internal use
                return Integer.parseInt(content) - 1;
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            writeLastCheckedMonthToFile(currentMonth);
            return currentMonth;
        }
    }
}

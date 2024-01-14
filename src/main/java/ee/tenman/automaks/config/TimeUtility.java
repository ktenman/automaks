package ee.tenman.automaks.config;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TimeUtility {

    public static CustomDuration durationInSeconds(long startTime) {
        return new CustomDuration(startTime);
    }

    private static String formatDuration(double duration) {
        return String.format("%.3f", duration);
    }

    public static class CustomDuration {
        private final double durationInSeconds;

        public CustomDuration(long startTime) {
            this.durationInSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        }

        public String asString() {
            return TimeUtility.formatDuration(durationInSeconds);
        }

        @Override
        public String toString() {
            return asString();
        }
    }
}

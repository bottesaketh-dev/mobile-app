package java.lang.management;

public class ManagementFactory {
    private static final MemoryMXBean memoryMXBean = new MemoryMXBean() {
        private final MemoryUsage memoryUsage = new MemoryUsage(0, 0, 0, Runtime.getRuntime().maxMemory());

        @Override
        public MemoryUsage getHeapMemoryUsage() {
            return memoryUsage;
        }

        @Override
        public MemoryUsage getNonHeapMemoryUsage() {
            return memoryUsage;
        }
    };

    public static MemoryMXBean getMemoryMXBean() {
        return memoryMXBean;
    }
}

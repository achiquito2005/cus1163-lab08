import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory = new ArrayList<>();
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    /**
     * TODO 1, 2: Process memory requests from file
     * <p>
     * This method reads the input file and processes each REQUEST and RELEASE.
     * <p>
     * TODO 1: Read and parse the file
     *   - Open the file using BufferedReader
     *   - Read the first line to get total memory size
     *   - Initialize the memory list with one large free block
     *   - Read each subsequent line and parse it
     *   - Call appropriate method based on REQUEST or RELEASE
     * <p>
     * TODO 2: Implement allocation and deallocation
     *   - For REQUEST: implement First-Fit algorithm
     *     * Search memory list for first free block >= requested size
     *     * If found: split the block if necessary and mark as allocated
     *     * If not found: increment failedAllocations
     *   - For RELEASE: find the process's block and mark it as free
     *   - Optionally: merge adjacent free blocks (bonus)
     */
    public static void processRequests(String filename) {
       memory.clear(); 

       try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
       
           String line = reader.readLine();
           if (line == null) {
              System.out.println("Input file is empty.");
              return;
           }

           int totalMemory = Integer.parseInt(line.trim());

           memory.add(new MemoryBlock(0, totalMemory, null));

           System.out.println("Reading from: " + filename);
           System.out.println("Total Memory: " + totalMemory + " KB");
           System.out.println("----------------------------------------");
           System.out.println();
           System.out.println("Processing requests...");
           System.out.println();

           while ((line = reader.readLine()) != null) {
               line = line.trim();
               if (line.isEmpty()) continue;

               String[] parts = line.split("\\s+");
               String command = parts[0];

               if (command.equalsIgnoreCase("REQUEST") && parts.length >= 3) {
                   String processName = parts[1];
                   int size = Integer.parseInt(parts[2]);
                   allocate(processName, size);
               } else if (command.equalsIgnoreCase("RELEASE") && parts.length >= 2) {
                   String processName = parts[1];
                   deallocate(processName);
               } else {
                   System.out.println("Invalid command: " + line);
               }
           }
       } catch (IOException e) {
        System.out.println("Error reading file: " + e.getMessage());
       }
    }
                                                              

    /**
     * TODO 2A: Allocate memory using First-Fit
     */
    private static void allocate(String processName, int size) {
        // Search through memory list
        // Find first free block where size >= requested size
        // If found:
        //   - Mark block as allocated (set processName)
        //   - If block is larger than needed, split it:
        //     * Create new free block for remaining space
        //     * Add it to memory list after current block
        //   - Increment successfulAllocations
        //   - Print success message
        // If not found:
        //   - Increment failedAllocations
        //   - Print failure message
        for (int i = 0; i < memory.size(); i++) {
            MemoryBlock block = memory.get(i);

            if (block.isFree() && block.size >= size) {
            // Allocate this block
                int remaining = block.size - size;

                block.processName = processName; // mark as allocated
                block.size = size;               // shrink to requested size

            // If there's leftover space, create a new free block after it
                if (remaining > 0) {
                   MemoryBlock freeBlock = new MemoryBlock(
                           block.start + size,
                           remaining,
                           null
                   );
                   memory.add(i + 1, freeBlock);
                }

                successfulAllocations++;
                System.out.println("REQUEST " + processName + " " + size + " KB → SUCCESS");
                return;
            }
        }

        failedAllocations++;
        System.out.println("REQUEST " + processName + " " + size + " KB → FAILED (insufficient memory)");

    }

    private static void deallocate(String processName) {
        boolean found = false;

        for (MemoryBlock block : memory) {
            if (!block.isFree() && processName.equals(block.processName)) {
               block.processName = null;  
               System.out.println("RELEASE " + processName + " → SUCCESS");
               found = true;


                break;
            }
        } 

        if (!found) {
            System.out.println("RELEASE " + processName + " → FAILED (process not found)");
        }
    }

    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        for (MemoryBlock block : memory) {
            String status = block.isFree() ? "FREE" : block.processName;
            String allocated = block.isFree() ? "" : " - ALLOCATED";
            System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n",
                    blockNum++,
                    block.start,
                    block.getEnd(),
                    " ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())),
                    status,
                    block.size,
                    allocated);
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");

        int allocatedMem = 0;
        int freeMem = 0;
        int numProcesses = 0;
        int numFreeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock block : memory) {
            if (block.isFree()) {
                freeMem += block.size;
                numFreeBlocks++;
                largestFree = Math.max(largestFree, block.size);
            } else {
                allocatedMem += block.size;
                numProcesses++;
            }
        }

        double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
        double freePercent = (freeMem * 100.0) / totalMemory;
        double fragmentation = freeMem > 0 ?
                ((freeMem - largestFree) * 100.0) / freeMem : 0;

        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
        System.out.printf("Number of Processes:    %d\n", numProcesses);
        System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    /**
     * Main method (FULLY PROVIDED)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MemoryAllocationLab <input_file>");
            System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
            return;
        }

        System.out.println("========================================");
        System.out.println("Memory Allocation Simulator (First-Fit)");
        System.out.println("========================================\n");
        System.out.println("Reading from: " + args[0]);

        processRequests(args[0]);
        displayStatistics();
    }
}

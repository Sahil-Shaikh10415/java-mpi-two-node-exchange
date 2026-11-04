import mpi.MPI;

import java.util.Arrays;

public final class TwoNodeExchange {
    private static final int REQUEST_TAG = 100;
    private static final int RESPONSE_TAG = 101;

    private TwoNodeExchange() {
    }

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        try {
            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();

            if (size != 2) {
                if (rank == 0) {
                    System.err.println("This program requires exactly 2 MPI processes; received " + size + ".");
                }
                return;
            }

            if (rank == 0) {
                runCoordinator();
            } else {
                runWorker();
            }
        } finally {
            MPI.Finalize();
        }
    }

    private static void runCoordinator() {
        int[] input = {5, 10, 15, 20};
        int[] output = new int[input.length];

        System.out.println("Rank 0: sending " + Arrays.toString(input) + " to rank 1");
        MPI.COMM_WORLD.Send(input, 0, input.length, MPI.INT, 1, REQUEST_TAG);
        MPI.COMM_WORLD.Recv(output, 0, output.length, MPI.INT, 1, RESPONSE_TAG);

        int[] expected = doubleValues(input);
        if (!Arrays.equals(output, expected)) {
            throw new IllegalStateException(
                    "Unexpected response: expected " + Arrays.toString(expected)
                            + " but received " + Arrays.toString(output));
        }

        System.out.println("Rank 0: received " + Arrays.toString(output) + " from rank 1");
        System.out.println("PASS: two-rank MPI exchange completed successfully.");
    }

    private static void runWorker() {
        int[] input = new int[4];
        MPI.COMM_WORLD.Recv(input, 0, input.length, MPI.INT, 0, REQUEST_TAG);

        int[] output = doubleValues(input);
        System.out.println("Rank 1: processed " + Arrays.toString(input)
                + " into " + Arrays.toString(output));
        MPI.COMM_WORLD.Send(output, 0, output.length, MPI.INT, 0, RESPONSE_TAG);
    }

    private static int[] doubleValues(int[] values) {
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i] * 2;
        }
        return result;
    }
}

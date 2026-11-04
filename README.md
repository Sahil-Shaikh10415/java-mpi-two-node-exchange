# Two-System Java MPI Data Exchange

This example uses the `mpi.*` API provided by MPJ Express. MPI rank 0 sends
four integers to rank 1; rank 1 doubles each value and sends the result back.
Rank 0 checks the response and prints a `PASS` message only when it matches.

The program requires exactly two MPI processes. To run it across two computers,
both machines must be able to reach each other over the network and have the
same Java and MPJ Express installations available to the process launcher.

## 1. Prerequisites

On both Windows computers, install:

1. A JDK supported by the MPJ Express version you choose. The MPJ Express
   0.44 Windows quick-start guide lists Java 1.6 or higher; this project was
   tested with Java 22.0.2.
2. The same MPJ Express release on both computers. The MPJ Express 0.44
   archive is available from
   [SourceForge](https://sourceforge.net/projects/mpjexpress/files/mpj-v0_44.tar.gz/download);
   extract it and use its top-level folder as `MPJ_HOME`.

Add these environment variables on both computers, adapting the paths:

```text
JAVA_HOME=C:\Path\To\Your\JDK
MPJ_HOME=C:\Path\To\mpj
```

Add `%JAVA_HOME%\bin` and `%MPJ_HOME%\bin` to `PATH`. Open a new Command Prompt
and check:

```bat
java -version
where java
where mpjrun.bat
```

The MPJ Express installation should contain
`%MPJ_HOME%\lib\mpj.jar` and `%MPJ_HOME%\bin\mpjrun.bat`. If the archive you
downloaded uses different paths or launcher options, use the documentation
distributed with that release.

## 2. Prepare both computers

1. Find each computer's hostname or private-network IP address. From the
   computer that will launch the job, confirm both hosts are reachable:

   ```bat
   ping 192.168.1.10
   ping 192.168.1.11
   ```

2. Configure Windows Firewall and the remote-launch service on both computers
   to allow the MPJ Express/Java processes to communicate on the private
   network. Follow your MPJ Express release's network-device instructions;
   this project does not assume a fixed communication port.
3. Copy this project to the same directory on both computers. Ensure the
   `src`, `machines.txt`, and `out` paths are accessible to the launcher on
   both hosts.
4. On the launching computer, copy `machines.example.txt` to `machines.txt`
   and replace its example IP addresses with the actual hosts, one per line.
   The file should contain exactly two hosts. MPJ Express 0.44's Windows
   quick-start uses a machines file named `machines`; this project passes
   `machines.txt` explicitly to the launcher.

Example `machines.txt`:

```text
192.168.1.10
192.168.1.11
```

Use the machines-file syntax and remote process setup prescribed by your
installed MPJ Express release if it differs from this example.

## 3. Start the MPJ Express daemons

For MPJ Express 0.44's Windows `niodev` cluster setup, open a Command Prompt
on each computer and run:

```bat
mpjdaemon.bat -boot
```

Leave the daemon running on both computers while you compile and launch the
job. The release's Windows guide requires manually booting a daemon on every
host; OpenSSH is not used by this setup.

## 4. Compile

From the project directory, open Command Prompt and run:

```bat
if not exist out mkdir out
javac -cp "%MPJ_HOME%\lib\mpj.jar" -d out src\TwoNodeExchange.java
```

Compilation should create `out\TwoNodeExchange.class`. If `javac` cannot find
`mpi.MPI`, confirm that `%MPJ_HOME%\lib\mpj.jar` exists and that `MPJ_HOME`
points to the correct installation.

## 5. Test two MPI ranks on one computer

Before using the network, test the program locally with MPJ Express's
multicore device:

```bat
mpjrun.bat -np 2 -dev multicore -cp out TwoNodeExchange
```

This confirms the source, MPI calls, and two-rank data exchange. It does not
test communication between separate computers.

## 6. Run on two computers

From the project directory on the launching computer, run:

```bat
mpjrun.bat -np 2 -dev niodev -machinesfile machines.txt -cp out TwoNodeExchange
```

The `niodev` device is for network communication. The launcher must be able to
start one rank on each configured host. If your MPJ Express release uses
different options, follow that release's cluster instructions while retaining
two ranks and the `niodev` network device.

## 7. Verify the result

The order of rank output can vary, but a successful run should contain all of
these messages:

```text
Rank 0: sending [5, 10, 15, 20] to rank 1
Rank 1: processed [5, 10, 15, 20] into [10, 20, 30, 40]
Rank 0: received [10, 20, 30, 40] from rank 1
PASS: two-rank MPI exchange completed successfully.
```

The rank 0 and rank 1 lines may appear in a different order because both
processes run concurrently. The final `PASS` line means the received values
were checked against the expected result. A missing rank, connection error,
or mismatched response should be treated as a failed test.

## 8. Stop the daemons

After the run has finished, stop the MPJ Express daemon on each computer:

```bat
mpjdaemon.bat -halt
```

## Validation performed

The source was compiled and run on Windows with MPJ Express 0.44 and Java
22.0.2 using the `multicore` device. Both MPI ranks exchanged and verified the
values successfully. This confirms the program locally; execution over two
separate computers depends on the network and remote-launch configuration and
has not been performed as part of this package.

## Project files

- `src\TwoNodeExchange.java` — MPI program source.
- `machines.example.txt` — sample two-host configuration to copy and edit.
- `machines.txt` — your local host list; intentionally not included.
- `out\` — compiled class output; generated during setup.
- `screenshots\implementation.png` — screenshot of the MPI exchange implementation.
- `screenshots\local-mpi-execution.png` — screenshot of the successful local
  two-rank run; it is not evidence of a separate-computer network run.
- `screenshots\local-mpi-run.txt` — captured output from that local test.

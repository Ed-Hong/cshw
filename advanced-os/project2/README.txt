Both versions of the project, the dc version and the local version, are included in their respective directories.


The local version spawns N new threads on the current machine and runs through the Mutex algorithm with the N newly spawned processes. 
The dc version is a modified version of the local version, and will run the Mutex algorithm on the dcXX.utdallas.edu hosts.


The primary differences within the two directories are contained within the scripts. The dc version's startall script will ssh into each of the dcXX hosts and runs
the program. The dc version's killall script also ssh's into each of the dcXX hosts and kills all java processes for the current user.


The program writes two output files: output.out and stats.out.
output.out is used to test for Mutual Exclusion, while stats.out is used to report statistics such as message complexity, response time, etc.
The current version of the program however only tracks two stats: message complexity (counted in number of messages sent) and response time (milliseconds).


The statavg script can be used to compute the average response time and message complexity.
The test script can be used to test for Mutual Exclusion.

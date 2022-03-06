# Real Time NEAT (RT-NEAT)

### The algorithm
The RT-NEAT algorithm is a derived from the NEAT algorithm. Both were developed by Kenneth O. Stanley.

The original NEAT algorithm is step-based. All the networks in the population are evaluated at once, given a fitness 
value, and then reproduced at the same time. This reduces the flexibility of the algorithm, as it forces us to stop all 
processing to decide who gets to reproduce and who doesn't.

The RT-NEAT algorithm, on the other hand, is based around the idea of continuous evolution. 
Periodically, random networks can be evaluated, and it can be then decided whether those networks get to reproduce
or get removed from the population. This way we get a seamless and continuous evolution of a population.

### Features
#### Currently implemented
* Adding nodes
* Adding connections
* Removing (disabling) connections
* Network configuration through a config file
* Innovation tracking
* Genetic distance measurement
* Genome/Network crossover
* Network serialization (json)
* Network deserialization (json)

#### Work in progress 
* Population handling on a different thread
* Species tracking

#### Planned features
* Graph display
* Default configuration file creation
* Full javadoc